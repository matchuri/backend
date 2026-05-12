# Backend Agent Context

백엔드 작업에서 반복 참조하는 최소 기준입니다. 상세 설명은 루트 `AGENTS.md`와 `docs/BACKEND.md`를 봅니다.

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
- `global`: 공통 응답, 예외 처리, 보안/설정 공통.
- `infra`: 외부 연동과 기술 세부 구현.

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

- 기본: `./gradlew test`
- 커버리지 필요 시: `./gradlew test jacocoTestReport`
- API 계약 변경 시 OpenAPI metadata, Swagger 산출물, 관련 `docs/api/` 문서를 함께 확인합니다.
- 데이터 모델 변경 시 관련 `docs/data/`와 `docs/generated/db-schema.md`를 함께 확인합니다.
