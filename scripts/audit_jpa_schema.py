#!/usr/bin/env python3
"""Audit stable JPA mapping conventions without generating schema documentation."""

from __future__ import annotations

import argparse
import re
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path


ENTITY_RE = re.compile(r"(?m)^\s*@Entity(?:\([^\r\n]*\))?\s*$")
CLASS_RE = re.compile(r"\bclass\s+(?P<name>[A-Za-z_][A-Za-z0-9_]*)\b")
TABLE_RE = re.compile(r"@Table\s*\((?P<body>.*?)\)", re.DOTALL)
NAME_RE = re.compile(r'\bname\s*=\s*"(?P<name>[^"]+)"')
ENUMERATED_RE = re.compile(r"@Enumerated(?:\s*\((?P<body>[^)]*)\))?")
ASSOCIATION_RE = re.compile(
    r"@(?P<kind>ManyToOne|OneToOne)"
    r"(?P<arguments>\s*\([^)]*\))?"
    r"(?P<annotations>(?:\s*@[A-Za-z_][A-Za-z0-9_]*(?:\s*\([^)]*\))?)*)"
    r"\s+(?:private|protected|public)\s+",
    re.DOTALL,
)
JOIN_COLUMN_RE = re.compile(r"@JoinColumn\s*\((?P<body>[^)]*)\)", re.DOTALL)


@dataclass(frozen=True)
class EntityMapping:
    class_name: str
    table_name: str | None
    source: Path
    line: int


@dataclass(frozen=True)
class Finding:
    source: Path
    line: int
    message: str


def line_number(text: str, offset: int) -> int:
    return text.count("\n", 0, offset) + 1


def relative(path: Path, root: Path) -> Path:
    try:
        return path.relative_to(root)
    except ValueError:
        return path


def inspect_java_file(path: Path, root: Path) -> tuple[list[EntityMapping], list[Finding]]:
    text = path.read_text(encoding="utf-8")
    entity_matches = list(ENTITY_RE.finditer(text))
    if not entity_matches:
        return [], []

    mappings: list[EntityMapping] = []
    findings: list[Finding] = []
    display_path = relative(path, root)

    for entity_match in entity_matches:
        class_match = CLASS_RE.search(text, entity_match.end())
        if class_match is None:
            findings.append(
                Finding(display_path, line_number(text, entity_match.start()), "@Entity 뒤에서 class 선언을 찾지 못했습니다.")
            )
            continue

        declaration = text[entity_match.end() : class_match.start()]
        table_match = TABLE_RE.search(declaration)
        table_name = None
        if table_match is not None:
            name_match = NAME_RE.search(table_match.group("body"))
            if name_match is not None:
                table_name = name_match.group("name")

        entity_line = line_number(text, entity_match.start())
        mappings.append(
            EntityMapping(
                class_name=class_match.group("name"),
                table_name=table_name,
                source=display_path,
                line=entity_line,
            )
        )
        if table_name is None:
            findings.append(
                Finding(display_path, entity_line, f"{class_match.group('name')}에 명시적인 @Table(name = ...)이 없습니다.")
            )

    for enum_match in ENUMERATED_RE.finditer(text):
        body = enum_match.group("body") or ""
        if "EnumType.STRING" not in body:
            findings.append(
                Finding(
                    display_path,
                    line_number(text, enum_match.start()),
                    "enum 영속화는 @Enumerated(EnumType.STRING)을 사용해야 합니다.",
                )
            )

    for association_match in ASSOCIATION_RE.finditer(text):
        arguments = association_match.group("arguments") or ""
        if association_match.group("kind") == "OneToOne" and "mappedBy" in arguments:
            continue

        join_match = JOIN_COLUMN_RE.search(association_match.group("annotations"))
        if join_match is None or NAME_RE.search(join_match.group("body")) is None:
            findings.append(
                Finding(
                    display_path,
                    line_number(text, association_match.start()),
                    f"소유 측 @{association_match.group('kind')}에 명시적인 @JoinColumn(name = ...)이 없습니다.",
                )
            )

    return mappings, findings


def audit(root: Path) -> tuple[list[EntityMapping], list[Finding]]:
    source_root = root / "src" / "main" / "java"
    if not source_root.is_dir():
        raise FileNotFoundError(f"JPA source root를 찾을 수 없습니다: {source_root}")

    mappings: list[EntityMapping] = []
    findings: list[Finding] = []
    for path in sorted(source_root.rglob("*.java")):
        file_mappings, file_findings = inspect_java_file(path, root)
        mappings.extend(file_mappings)
        findings.extend(file_findings)

    if not mappings:
        raise FileNotFoundError(f"검사할 JPA entity가 없습니다: {root}")

    tables: dict[str, list[EntityMapping]] = defaultdict(list)
    for mapping in mappings:
        if mapping.table_name is not None:
            tables[mapping.table_name].append(mapping)

    for table_name, owners in sorted(tables.items()):
        if len(owners) < 2:
            continue
        owner_names = ", ".join(owner.class_name for owner in owners)
        for owner in owners:
            findings.append(
                Finding(owner.source, owner.line, f"테이블 이름 '{table_name}'이 중복되었습니다: {owner_names}")
            )

    findings.sort(key=lambda finding: (str(finding.source), finding.line, finding.message))
    return mappings, findings


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path.cwd(), help="backend repository root")
    parser.add_argument("--strict", action="store_true", help="finding이 있으면 실패")
    args = parser.parse_args()

    root = args.root.resolve()
    try:
        mappings, findings = audit(root)
    except (FileNotFoundError, OSError, UnicodeError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2

    explicit_tables = sum(mapping.table_name is not None for mapping in mappings)
    print(f"JPA entities: {len(mappings)}")
    print(f"Explicit tables: {explicit_tables}")
    print(f"Findings: {len(findings)}")
    for finding in findings:
        print(f"- {finding.source}:{finding.line}: {finding.message}")

    if args.strict and findings:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
