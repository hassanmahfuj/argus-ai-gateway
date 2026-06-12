# AI Gateway

A Spring Boot reverse proxy gateway that forwards AI API requests (OpenAI and Anthropic) to ZAI upstream endpoints, with an Angular + Tailwind CSS management frontend served from the same application.

---

## How It Works

The gateway accepts OpenAI- and Anthropic-compatible API requests and transparently proxies them to ZAI upstream servers, injecting authentication and handling streaming responses.

### Routing

| Client Request                     | Upstream Target                                  |
| ---------------------------------- | ------------------------------------------------ |
| `POST /v1/anthropic/**`            | `https://api.z.ai/api/anthropic/**`              |
| `POST /v1/**` (all other methods)  | `https://api.z.ai/api/coding/paas/v4/**`         |

The `/v1` prefix is stripped before forwarding, so clients using OpenAI-compatible SDKs can point their `base_url` directly at this gateway.

### Features

- **Bearer token injection** — API key is added server-side via the `ZAI_API_KEY` environment variable; clients never need to handle credentials.
- **Full SSE streaming support** — streaming responses (`text/event-stream`) are piped through in real time.
- **Request/response logging** — logs method, URI, upstream target, model, stream flag, message count, and response latency.
- **120 s upstream timeout** — configurable via `gateway.proxy.timeout`.
- **Structured error responses** — returns JSON (`{ "error": "...", "message": "..." }`) on upstream timeouts (504), connection failures (502), and general errors (502).
- **Hop-by-hop header filtering** — strips `host`, `connection`, `transfer-encoding`, and `content-length` headers.

---

## Tech Stack

| Layer       | Technology                                          |
| ----------- | --------------------------------------------------- |
| Backend     | Java 21, Spring Boot 4.0, Spring Web, Spring Data JPA |
| Build       | Gradle (with node-gradle plugin for frontend build) |
| Frontend    | Angular 21, Tailwind CSS 4, TypeScript 5.9          |
| Database    | PostgreSQL (via Spring Data JPA / Hibernate)        |
| API Docs    | SpringDoc OpenAPI (Swagger UI)                      |

---

## Project Structure

```
src/
├── main/
│   ├── java/uk/mahfuj/aigateway/
│   │   ├── config/
│   │   │   ├── AiGatewayApplication.java   # @SpringBootApplication entry point
│   │   │   ├── GatewayProperties.java      # gateway.proxy.* configuration binding
│   │   │   ├── JacksonConfig.java          # ObjectMapper customisation
│   │   │   ├── AngularLocalConfig.java     # CORS for local development
│   │   │   └── DomainConfig.java           # JPA domain scanning
│   │   ├── controller/
│   │   │   ├── GatewayProxyController.java # /v1/** proxy endpoints
│   │   │   └── AngularForwardController.java # SPA route forwarding
│   │   └── service/
│   │       └── GatewayProxyService.java    # HTTP forwarding & streaming logic
│   ├── resources/
│   │   └── application.yml                 # Spring configuration
│   └── webapp/                             # Angular frontend
│       ├── app/
│       │   ├── home/                       # Home page component
│       │   ├── error/                      # Error page component
│       │   ├── common/header/              # Shared header component
│       │   ├── app.routes.ts               # Route definitions
│       │   └── app.config.ts               # Angular app configuration
│       ├── environments/                   # Dev & prod environment configs
│       ├── styles.css                      # Global styles (Tailwind)
│       └── index.html                      # SPA entry point
└── test/                                   # Java & Angular tests
```

---

## Getting Started

### Prerequisites

- **Java 21** — for the Spring Boot backend
- **Node.js 24** — for the Angular frontend (managed by Gradle via node-gradle plugin)
- **PostgreSQL** — database (defaults to `localhost:5432/aigateway`)

### Configuration

#### API Key

Set the ZAI API key via environment variable:

```bash
export ZAI_API_KEY=your-key-here
```

Or override in `application-local.yml`:

```yaml
gateway:
  proxy:
    api-key: your-key-here
```

#### Database

Default connection settings (overridable via environment variables):

| Variable                  | Default                              |
| ------------------------- | ------------------------------------ |
| `JDBC_DATABASE_URL`       | `jdbc:postgresql://localhost:5432/aigateway` |
| `JDBC_DATABASE_USERNAME`  | `aigateway`                          |
| `JDBC_DATABASE_PASSWORD`  | `aigateway123`                       |

---

## Development

Use the `local` Spring profile during development:

```bash
./gradlew bootRun
```

This activates the `local` profile by default (configured in `build.gradle`).

For frontend development with hot-reload, start the Angular dev server in a separate terminal:

```bash
npm install
ng serve
```

The frontend is then accessible at `http://localhost:4200` with API calls proxied to the Spring Boot backend.

Generate new Angular components using schematics:

```bash
ng generate component my-component
```

Run Angular unit tests:

```bash
ng test
```

> **IDE note:** Lombok is used in the project. Install the Lombok plugin and enable annotation processing in your IDE.

---

## Build

Build the complete application (backend + frontend):

```bash
./gradlew clean build
```

This automatically runs `npm install` and `ng build` via the node-gradle plugin, copying the compiled frontend into the JAR's static resources.

Run the packaged application:

```bash
java -Dspring.profiles.active=production -jar ./build/libs/aigateway-0.0.1-SNAPSHOT.jar
```

Build a Docker image:

```bash
./gradlew bootBuildImage --imageName=uk.mahfuz/aigateway
```

---

## API Usage Examples

### OpenAI-compatible request

```bash
curl http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4",
    "messages": [{"role": "user", "content": "Hello"}],
    "stream": true
  }'
```

### Anthropic-compatible request

```bash
curl http://localhost:8080/v1/anthropic/v1/messages \
  -H "Content-Type: application/json" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-sonnet-4-6",
    "messages": [{"role": "user", "content": "Hello"}],
    "max_tokens": 1024
  }'
```

No `Authorization` header is needed — the gateway injects it automatically.

---

## Further Reading

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)
- [Gradle User Manual](https://docs.gradle.org/)
- [Angular Documentation](https://angular.dev/)
- [Tailwind CSS](https://tailwindcss.com/)
