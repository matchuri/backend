# Backend Agent Router

백엔드 작업 전에는 루트 `AGENTS.md`를 확인한 뒤 아래 문서만 필요한 만큼 엽니다.

- 백엔드 위키: `../docs/backend/index.md`
- 구현 규칙: `../docs/backend/guide.md`
- 아키텍처 기준: `../docs/backend/architecture.md`
- API 위치 지도: `../docs/api/API_CONTRACTS.md`
- 데이터 위치 지도: `../docs/data/DATA_SCHEMA.md`

## Source Of Truth

- API 계약은 `src/main/java/matchuri/backend/api/**/*Api.java`, DTO, `/docs/openapi`를 우선합니다.
- 데이터 구조는 `src/main/java/matchuri/backend/domain/**/entity/*.java`, repository, `init/sql/01-schema.sql`를 우선합니다.

## Layer Invariant

- 의존성 흐름: `entity/domain model -> repository -> service/usecase -> api/controller`.
- 하위 레이어는 상위 레이어를 import하지 않습니다.
- 공통 값이 필요하면 상위 레이어에서 아래로 끌어오지 말고 낮은 domain/global 위치에 둡니다.

기본 검증은 `./gradlew test`입니다.
