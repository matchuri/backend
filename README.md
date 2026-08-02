# Matchuri Backend

## 1. 기술스택

- Java 21
- Spring Boot 4.0.3
- Gradle Kotlin DSL, Gradle Wrapper 9.3.1
- Spring Web MVC
- Spring Security, OAuth2 Client
- Spring Data JPA
- Bean Validation
- MySQL 8.0
- H2 Database
- Spring Actuator
- Spring Mail, Thymeleaf
- springdoc-openapi 3.x
- AWS SDK for Java v2, S3 compatible storage
- Lombok
- JUnit 5, Spring REST Docs, JaCoCo
- Docker Compose

## 2. 프로젝트 구조

```text
backend
├─ .github
│  ├─ ISSUE_TEMPLATE
│  └─ workflows
├─ gradle
│  └─ wrapper
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ matchuri
│  │  │     └─ backend
│  │  │        ├─ api
│  │  │        ├─ domain
│  │  │        ├─ global
│  │  │        └─ infra
│  │  └─ resources
│  │     ├─ seed
│  │     │  ├─ reference-data.json
│  │     │  └─ local-sample-data.json
│  │     ├─ application.yaml
│  │     └─ application-local.yaml
│  └─ test
│     ├─ java
│     └─ resources
├─ build.gradle.kts
├─ docker-compose.yml
├─ gradlew
├─ gradlew.bat
└─ settings.gradle.kts
```

## 3. 실행환경

- JDK 21
- Docker Desktop 또는 Docker Engine
- MySQL 8.0
- 기본 Spring profile: `local`
- 기본 서버 포트: `8080`
- 로컬 DB 기본 접속 정보
  - host: `localhost`
  - port: `3331`
  - database: `matchuri`
  - username: `matchuri`
  - password: `matchuri`
- 주요 로컬 엔드포인트
  - Swagger UI: `http://localhost:8080/docs/swagger-ui.html`
  - OpenAPI JSON: `http://localhost:8080/docs/openapi`
  - Health API: `http://localhost:8080/api/v1/health`

로컬 Docker Compose 실행에는 `backend/.env`가 필요합니다. 최소 예시는 아래와 같습니다.

```env
MATCHURI_DB_PORT=3331
MATCHURI_DB_ROOT_PW=root
MATCHURI_DB_NAME=matchuri
MATCHURI_DB_USER=matchuri
MATCHURI_DB_PW=matchuri
MATCHURI_SPRING_PROFILE=local
MATCHURI_FRONTEND_ORIGIN=http://localhost:3000
MATCHURI_GOOGLE_EMAIL_APP_PW=dummy
```

애플리케이션 기동 시 JPA가 스키마를 갱신한 뒤 기준 데이터를 멱등하게 생성합니다.
로컬 샘플 데이터는 `local` 프로필에서만 생성하며 메뉴 대표 이미지는 시드하지 않습니다.

## 4. 로컬 실행 방법

```bash
# backend 디렉터리로 이동
cd backend

# 로컬 MySQL 실행
docker compose up -d db

# 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

Windows PowerShell에서는 아래 명령어를 사용할 수 있습니다.

```powershell
cd backend
docker compose up -d db
.\gradlew.bat bootRun
.\gradlew.bat test
```
