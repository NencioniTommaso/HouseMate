# HouseMate Monorepo Architectural Contract & Agent Guidelines

## Git Promotion Flow
- `feat/*` or `fix/*` -> `dev` -> `staging` -> `main`
- Never commit directly to `staging` or `main`.
- Never commit build artifacts.

## Module Map
- `shared/`: Java DTOs (request/response), enums, `ValidationPatterns`, and DTO validation unit tests (`*ValidationTest`).
- `backend/`: Spring Boot 3 REST API, JPA entities (`model/`), repositories + dynamic JPA Specification classes (`QuerySpecification`, `ChoreAssignmentSpecification`), services (using `ExpenseSplitStrategy` for splits), controllers, and structured log masking (`LoggingContextFilter`, `MaskingPatternConverter`).
- `mobile/`: Primary frontend (Flutter/Dart compiled to PWA). Must stay in contract sync with `shared/` DTOs.
- `client/`: Legacy/secondary JavaFX desktop client (`HttpRestClient`, `*ClientService`, `.fxml` views).

## Strict Quota & Execution Rules
- Never run unfiltered `./mvnw test` or `flutter test` directly in the main conversation thread.
- Always delegate test execution to the `silent-test-runner` subagent, or run single targeted test classes in quiet mode:
  - Maven: `./mvnw test -q -pl <module> -Dtest=<ClassName>`
  - Flutter: `flutter test --reporter compact <path_to_test>`
- When modifying `shared/`, always compile it into the reactor (`-pl shared,<target> -am`) before running downstream tests.
