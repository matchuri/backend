# Backend Agent Context

백엔드 작업에서 반복 참조하는 최소 기준입니다. 상세 설명은 루트 `../../AGENTS.md`와 `../../docs/backend/index.md`를 봅니다.

## Stack

- Spring Boot 4, Java 21, Gradle Kotlin DSL.
- Spring Web MVC, Spring Security, OAuth2 Client, Validation, Spring Data JPA, Actuator, Mail, Thymeleaf.
- MySQL 런타임, H2 테스트 런타임.
- OpenAPI: springdoc 3.x.

## Package Shape

```text
src/main/java/matchuri/backend
├─ api
├─ domain
├─ global
└─ infra
```

- `api/<domain>`: Controller, DTO, Mapper, Swagger/OpenAPI metadata.
- `domain/<domain>`: service, command, result, support, exception, entity, repository.
- `global`: 공통 응답, 예외 처리, 보안/설정과 트랜잭션 지원.
- `infra`: 외부 연동과 기술 세부 구현, 초기 데이터 구성(`infra/seed`).

## DTO Rules

- 실제 request: `api/<domain>/dto/request`
- 실제 response payload: `api/<domain>/dto/response`
- Swagger 문서 전용 wrapper/example: `api/<domain>/dto/docs`
- 공통 응답 구조는 `success/data/error` 형태를 유지합니다.
- `dto/docs`를 런타임 payload DTO로 사용하지 않습니다.

## Domain Rules

- `service`는 유스케이스 진입점과 트랜잭션 경계입니다.
- `command`/`result`는 API DTO와 분리한 서비스 입출력 모델입니다.
- `support`는 여러 유스케이스에서 반복되는 조회, 검증, 계산, 정책 판단을 둡니다.
- 도메인 전용 에러 코드는 해당 도메인의 `exception`에 둡니다.
- `support -> service`, `entity -> repository`, `api dto -> entity` 직접 노출은 피합니다.

## Product Rules

- 추천은 메뉴 중심입니다. 장소/place는 보조 레이어입니다.
- 그룹 추천은 `group room`과 다른 실행 단위입니다.
- MVP 그룹 추천은 후보 3개 안팎, 투표, 최종 메뉴 확정을 우선합니다.
- `attribute category`, `restriction ingredient`, `group recommendation` 용어를 일관되게 사용합니다.

## Verification

- 개발 루프: 변경 영향에 가장 가까운 테스트를 `./gradlew fastTest --tests "패키지.테스트클래스" --quiet`로 우선 실행합니다.
- Controller·Security·JPA 경계를 바꿨다면 관련 `*IntegrationTest` 또는 `*RepositoryTest`를 `./gradlew test --tests "패키지.테스트클래스" --quiet`로 개발 중에도 실행합니다.
- 통합 테스트는 OWNER·인증/인가, HTTP request/response 계약, JPA unique/lock, 대표 정상·실패 흐름에만 둡니다.
- 정책 분기와 상태 계산은 Spring context 없이 service/support/entity 테스트로 검증하고 같은 분기를 통합 테스트에 반복하지 않습니다.
- Spring context·WebMvc 테스트는 `*IntegrationTest`, JPA slice는 `*RepositoryTest`로 이름을 끝내 `fastTest` 제외 규칙을 유지합니다.
- 전체 suite: 마지막 동작 변경 후 `./gradlew test --quiet`를 1회 이상 성공시킵니다. 실패를 고친 뒤에는 다시 실행합니다.
- 패키징: `./gradlew bootJar`로 `build/libs/backend-<version>.jar` 하나를 생성합니다. 로컬 실행은 `./gradlew bootRun --args='...'`을 사용합니다.
- 실패 원인 분석에 상세 로그가 필요할 때만 해당 테스트를 `--quiet` 없이 다시 실행합니다.
- API registry drift: `python scripts/audit_api_contract.py --root . --strict`
- JPA mapping drift: `python scripts/audit_jpa_schema.py --root . --strict`
- API 계약 변경 시 OpenAPI metadata, Swagger 산출물, 관련 `../../docs/api/` 문서를 함께 확인합니다.
- API 계약 변경은 `../../.agents/skills/matchuri-backend-api-change/SKILL.md`를 우선 사용하고, 프론트엔드 소비 코드까지 움직이면 `../../.agents/skills/matchuri-api-contract-sync/SKILL.md`도 사용합니다.
- 품질 리뷰는 `../../.agents/skills/matchuri-backend-quality-review/SKILL.md`를 사용합니다.
- 인증/인가/시크릿 변경은 `../../.agents/skills/matchuri-backend-security-review/SKILL.md`를 사용합니다.
- 배포/로그/복구/운영 신뢰성 변경은 `../../.agents/skills/matchuri-backend-reliability-review/SKILL.md`를 사용합니다.
- Swagger/OpenAPI 산출물 전용 테스트는 작성하지 않습니다. API 변경은 service/domain 테스트나 필요한 controller 통합 테스트로 검증합니다.
- 데이터 모델 변경 시 엔티티와 테스트를 기준으로 검증하고, 구조가 아닌 정책이 바뀐 경우에만 `../../docs/data/policies.md`를 수정합니다.
