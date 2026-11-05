#!/usr/bin/env python3
"""Check Controller mappings against the OpenAPI API ID registry."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path
import re


HTTP_MAPPING = {
    "GetMapping": "GET",
    "PostMapping": "POST",
    "PutMapping": "PUT",
    "PatchMapping": "PATCH",
    "DeleteMapping": "DELETE",
}


@dataclass(frozen=True)
class Endpoint:
    method: str
    path: str
    source: Path
    line: int


@dataclass(frozen=True)
class ApiMetadata:
    api_id: str
    tag: str
    method: str
    path: str
    source: Path
    line: int


def normalize_path(path: str) -> str:
    path = re.sub(r"/+", "/", path.strip())
    if not path.startswith("/"):
        path = "/" + path
    return path.rstrip("/") if len(path) > 1 else path


def annotation_value(annotation: str) -> str:
    match = re.search(r'\(\s*(?:path\s*=\s*|value\s*=\s*)?"([^"]*)"', annotation)
    return match.group(1) if match else ""


def line_number(text: str, offset: int) -> int:
    return text.count("\n", 0, offset) + 1


def openapi_config_path(root: Path) -> Path:
    return root / "src" / "main" / "java" / "matchuri" / "backend" / "global" / "config" / "OpenApiConfig.java"


def parse_backend_endpoints(root: Path) -> list[Endpoint]:
    api_root = root / "src" / "main" / "java" / "matchuri" / "backend" / "api"
    if not api_root.is_dir():
        raise FileNotFoundError(f"API source root를 찾을 수 없습니다: {api_root}")
    endpoints: list[Endpoint] = []

    for source in sorted(api_root.rglob("*Controller*.java")):
        text = source.read_text(encoding="utf-8")
        class_match = re.search(r"\bclass\s+\w+", text)
        class_prefix = text[: class_match.start()] if class_match else text
        request_mappings = list(re.finditer(r"@RequestMapping\s*\((.*?)\)", class_prefix, re.S))
        base = normalize_path(annotation_value(request_mappings[-1].group(0))) if request_mappings else ""

        pattern = re.compile(r"@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)\s*(\((.*?)\))?", re.S)
        for match in pattern.finditer(text):
            method_path = normalize_path(annotation_value(match.group(0)))
            full_path = normalize_path(base + ("" if method_path == "/" else method_path))
            if not re.match(r"^/api/v\d+(?:/|$)", full_path):
                continue
            endpoints.append(
                Endpoint(
                    method=HTTP_MAPPING[match.group(1)],
                    path=full_path,
                    source=source.relative_to(root),
                    line=line_number(text, match.start()),
                )
            )

    config = openapi_config_path(root)
    text = config.read_text(encoding="utf-8")
    synthetic_pattern = re.compile(
        r'\.path\s*\(\s*"([^"]+)"\s*,\s*new\s+PathItem\s*\(\)\s*\.\s*(get|post|put|patch|delete)\s*\(',
        re.S,
    )
    for match in synthetic_pattern.finditer(text):
        endpoints.append(
            Endpoint(
                method=match.group(2).upper(),
                path=normalize_path(match.group(1)),
                source=config.relative_to(root),
                line=line_number(text, match.start()),
            )
        )
    return endpoints


def parse_openapi_metadata(root: Path) -> list[ApiMetadata]:
    source = openapi_config_path(root)
    text = source.read_text(encoding="utf-8")
    pattern = re.compile(
        r'metadata\.put\(\s*key\(\s*"([^"]+)"\s*,\s*PathItem\.HttpMethod\.'
        r'(GET|POST|PUT|PATCH|DELETE)\s*\)\s*,\s*meta\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\)\s*\)',
        re.S,
    )
    return [
        ApiMetadata(
            api_id=match.group(3),
            tag=match.group(4),
            method=match.group(2),
            path=normalize_path(match.group(1)),
            source=source.relative_to(root),
            line=line_number(text, match.start()),
        )
        for match in pattern.finditer(text)
    ]


def duplicates(items: list[Endpoint] | list[ApiMetadata], key) -> dict[object, list[object]]:
    grouped: dict[object, list[object]] = {}
    for item in items:
        grouped.setdefault(key(item), []).append(item)
    return {item_key: values for item_key, values in grouped.items() if len(values) > 1}


def report(root: Path) -> tuple[str, bool]:
    endpoints = parse_backend_endpoints(root)
    metadata = parse_openapi_metadata(root)
    if not endpoints or not metadata:
        raise ValueError("검사할 API endpoint 또는 OpenAPI metadata가 없습니다.")
    endpoint_keys = {(item.method, item.path) for item in endpoints}
    metadata_keys = {(item.method, item.path) for item in metadata}

    duplicate_endpoints = duplicates(endpoints, lambda item: (item.method, item.path))
    duplicate_metadata = duplicates(metadata, lambda item: (item.method, item.path))
    duplicate_api_ids = duplicates(metadata, lambda item: item.api_id)
    missing_metadata = [item for item in endpoints if (item.method, item.path) not in metadata_keys]
    stale_metadata = [item for item in metadata if (item.method, item.path) not in endpoint_keys]

    lines = [
        "# Backend API Contract Audit",
        "",
        f"- Backend versioned API endpoints: {len(endpoints)}",
        f"- OpenAPI operation metadata entries: {len(metadata)}",
        f"- Duplicate backend endpoint keys: {len(duplicate_endpoints)}",
        f"- Duplicate OpenAPI metadata keys: {len(duplicate_metadata)}",
        f"- Duplicate API IDs: {len(duplicate_api_ids)}",
        f"- Backend endpoints missing OpenAPI metadata: {len(missing_metadata)}",
        f"- OpenAPI metadata not found in backend mappings: {len(stale_metadata)}",
        "",
    ]

    if missing_metadata:
        lines.extend(["## Missing OpenAPI Metadata", "", "| Method | Path | Source |", "| --- | --- | --- |"])
        for item in missing_metadata:
            lines.append(f"| {item.method} | `{item.path}` | `{item.source.as_posix()}:{item.line}` |")
        lines.append("")

    if stale_metadata:
        lines.extend(["## Stale OpenAPI Metadata", "", "| Method | Path | API ID | Source |", "| --- | --- | --- | --- |"])
        for item in stale_metadata:
            lines.append(f"| {item.method} | `{item.path}` | `{item.api_id}` | `{item.source.as_posix()}:{item.line}` |")
        lines.append("")

    if duplicate_api_ids:
        lines.extend(["## Duplicate API IDs", "", "| API ID | Sources |", "| --- | --- |"])
        for api_id, items in sorted(duplicate_api_ids.items()):
            sources = ", ".join(f"`{item.source.as_posix()}:{item.line}`" for item in items)
            lines.append(f"| `{api_id}` | {sources} |")
        lines.append("")

    has_findings = bool(
        duplicate_endpoints
        or duplicate_metadata
        or duplicate_api_ids
        or missing_metadata
        or stale_metadata
    )
    return "\n".join(lines), has_findings


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".", help="Backend repository root")
    parser.add_argument("--strict", action="store_true", help="Exit non-zero when drift is found")
    args = parser.parse_args()

    output, has_findings = report(Path(args.root).resolve())
    print(output)
    return 1 if args.strict and has_findings else 0


if __name__ == "__main__":
    raise SystemExit(main())
