# Matchuri Backend CONTRIBUTING

## Branch

`main`, `develop`를 제외한 작업 브랜치는 아래 형식을 사용한다.

```text
<type>/<description>
```

예시:

```text
feat/social-login
refactor/group-exit
fix/sse-connection
```

`description`은 작업 내용을 식별할 수 있도록 간결하게 작성한다.

### Branch Types

| Type       | 용도                     | Base   | Merge Target  |
| ---------- | ---------------------- | ------ | ------------- |
| `feat`     | 기능 추가                  | `develop`  | `develop`         |
| `fix`      | 일반 버그 수정               | `develop`  | `develop`         |
| `docs`     | 문서 수정                  | `develop`  | `develop`         |
| `refactor` | 기능 변화 없는 구조 개선         | `develop`  | `develop`         |
| `chore`    | 의존성, 디렉터리 구조 등 기타 유지보수 | `develop`  | `develop`         |
| `hotfix`   | 운영 환경 긴급 수정            | `main` | `main`, `develop` |

### Protected Branches

* `main`

  * 운영 배포 브랜치
  * 직접 push하지 않는다.
  * 항상 배포 가능한 상태를 유지한다.

* `develop`

  * 개발 통합 브랜치
  * 일반 작업 브랜치는 `develop`에서 분기하고 `develop`로 병합한다.
  * 개발/스테이징 환경 배포에 사용한다.

---

## Commit

커밋 메시지는 다음 형식을 사용한다.

```text
<type>: <description>
```

`description`은 한글로 작성한다.

예시:

```text
feat: 소셜 로그인 기능 추가
fix: SSE 연결 종료 오류 수정
refactor: 그룹 탈퇴 로직 구조 개선
```

### Commit Types

| Type       | 용도                 |
| ---------- | ------------------ |
| `feat`     | 기능 추가              |
| `fix`      | 버그 수정              |
| `docs`     | 문서 수정              |
| `style`    | 로직 변경 없는 코드 스타일 수정 |
| `refactor` | 기능 변화 없는 코드 구조 개선  |
| `perf`     | 성능 개선              |
| `test`     | 테스트 추가 및 수정        |
| `build`    | 빌드 설정 또는 외부 의존성 변경 |
| `chore`    | 기타 유지보수 작업         |

---

## Pull Request

PR 작성 시 `.github/PULL_REQUEST_TEMPLATE.md`를 따른다.

PR 제목은  `[Task] 작업 내용` 방식으로 작성한다. 아래는 예시이다.
- `[Feat] 그룹 탈퇴 API 추가`
- `[Fix] 개인 메뉴 추천 이력에 필터링 조건 누락 개선`
