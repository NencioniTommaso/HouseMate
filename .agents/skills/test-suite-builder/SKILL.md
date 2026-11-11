---
name: test-suite-builder
description: Playbook and architectural conventions for writing, completing, and debugging unit and integration tests across shared, backend, client, and mobile modules.
---

# Test Suite Builder Guide

Conventions and execution rules for tests across the HouseMate monorepo.

## 1. Test Classification & Annotations

### Shared DTO Validation Tests (`shared/src/test/`)
- **Technology**: Jakarta Validation (`Validator`, `ValidatorFactory`), JUnit 5.
- **Pattern**: Set up `Validator` in `@BeforeAll`. Test constraint boundaries (`@NotBlank`, `@NotNull`, `@Size`, `@DecimalMin`, regexes from `ValidationPatterns`). Assert `violations.isEmpty()` for valid DTOs and exact `getPropertyPath()` for invalid inputs.

### Backend Unit Tests (`backend/src/test/`)
- **Services & Strategies**:
  - `@ExtendWith(MockitoExtension.class)`, `@Mock` for collaborators/repositories, `@InjectMocks` for target service.
  - Use AssertJ (`assertThat`, `assertThatThrownBy`) and Mockito verifications (`verify`, `never()`).
- **Controllers**:
  - `@WebMvcTest(TargetController.class)`, `@ContextConfiguration(classes = ServerApp.class)`.
  - `@WithMockUser(username = "<uuid>")` with helper mocks via `UserDetailsTestUtils`.
  - `@MockitoBean` for injected services, `MockMvc` for HTTP assertions (`status()`, `jsonPath()`).

### Backend Persistence & Specification Tests (`backend/src/test/`)
- **Repositories & JPA Specifications**:
  - `@DataJpaTest`, `@Autowired TestEntityManager entityManager`, `@Autowired TargetRepository`.
  - Runs against in-memory H2 with `backend/src/test/resources/application-test.properties`.
  - Use `entityManager.persistAndFlush()` and `entityManager.clear()` before executing repository/specification queries.

### Backend Service Integration Tests (`backend/src/test/`)
- **Multi-Service & DB Integration**:
  - Option A: `@DataJpaTest` + `@Import({TargetService.class, CollaboratorService.class})` for transactional persistence-service flows with auto-rollback.
  - Option B: `@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional` for end-to-end container integration.
  - Use `UserDetailsTestUtils.userDetailsOf(...)` to simulate authenticated principals.

### Mobile Flutter Tests (`mobile/test/`)
- Currently no test suite exists in `mobile/`; all Flutter tests will be implemented from scratch in a future task.
- **Conventions**: Use `flutter_test`, `test()`, `testWidgets()`, Mocktail/Mockito for client HTTP services.

## 2. Strict Quota & Execution Loop
- **Main Thread Invariant**: Never run unfiltered or full test suites in the main agent thread.
- **Targeted Test Execution**: Always delegate execution to the `silent-test-runner` subagent:
  - Maven targeted test:
    ```bash
    ./mvnw test -q -pl <module> -Dtest=<TestClass>
    ```
  - Reactor dependency: If `shared/` DTOs or models are modified, always include `-am`:
    ```bash
    ./mvnw test -q -pl shared,<target> -am -Dtest=<TestClass>
    ```
  - Flutter test:
    ```bash
    flutter test --reporter compact <path_to_test>
    ```
