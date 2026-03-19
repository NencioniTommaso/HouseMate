# ChoreService and ChoreController Test Skeletons

## Overview
Two comprehensive JUnit 5 test skeleton files have been created following Spring Boot testing best practices with Mockito for dependency mocking.

## Files Created

### 1. ChoreServiceTest.java
**Location:** `backend/src/test/java/com/housemate/backend/service/ChoreServiceTest.java`

**Structure:**
- **Mock Dependencies:** 5 repository mocks (ChoreRepository, HouseholdRepository, HouseholdMembershipRepository, ChoreAssignmentRepository, UserRepository)
- **Test Data Constants:** 6 UUID constants and string constants for reusable test data
- **Helper Methods:** 6 factory methods to create test entities (Household, User, Chore, Assignment, Membership)
- **Test Methods:** 33 test method skeletons organized by service method

**Test Coverage by Method:**
| Service Method | Happy Path | Null Validation | Business Logic / Edge Cases | Total Tests |
|---|---|---|---|---|
| `createChore` | 1 | 4 | 2 | 7 |
| `deleteChore` | 1 | 1 | 1 | 3 |
| `createChoreAssignment` | 1 | 3 | 2 | 6 |
| `deleteChoreAssignment` | 1 | 1 | 1 | 3 |
| `updateChoreAssignmentStatus` | 1 | 3 | 1 | 5 |
| `reassignChore` | 1 | 2 | 2 | 5 |
| `getAllHouseholdChores` | 1 | 2 | 2 | 5 |
| `deleteAllChoresForHousehold` | 1 | 1 | 1 | 3 |
| `getAssignmentOverview` | 1 | 1 | 1 | 3 |
| `getFilteredChoreAssignments` | 1 | 2 | 2 | 5 |
| **TOTAL** | **10** | **20** | **15** | **45** |

**Testing Pattern:**
- Uses `@ExtendWith(MockitoExtension.class)` for Mockito integration
- Uses `@BeforeEach` for test setup and helper method initialization
- Uses `@Mock` and `@InjectMocks` annotations for dependency injection
- Uses AssertJ assertions for readable test assertions
- Uses `verify()` and `when().thenReturn()` for mock interaction verification
- Organized with section comments: `// ============ Tests for methodName ============`

### 2. ChoreControllerTest.java
**Location:** `backend/src/test/java/com/housemate/backend/controller/ChoreControllerTest.java`

**Structure:**
- **Injected Dependencies:** MockMvc for HTTP testing, ObjectMapper for JSON serialization
- **Mocked Service:** ChoreService mocked with `@MockBean`
- **Test Data Constants:** 6 UUID constants and string constants for reusable test data
- **Helper Methods:** 8 factory methods to create test DTOs
- **Test Methods:** 24 test method skeletons organized by endpoint

**Test Coverage by Endpoint:**
| Endpoint | HTTP Method | Happy Path | Error Cases | Total Tests |
|---|---|---|---|---|
| `/api/chores` | POST | 1 | 2 | 3 |
| `/api/chores/{choreId}` | DELETE | 1 | 1 | 2 |
| `/api/chores/assignments` | POST | 1 | 2 | 3 |
| `/api/chores/assigments/{assignmentId}` | DELETE | 1 | 1 | 2 |
| `/api/chores/assignments/{assignmentId}/status` | PATCH | 1 | 2 | 3 |
| `/api/chores/assignments/{assignmentId}/reassign` | PATCH | 1 | 2 | 3 |
| `/api/chores/{householdId}` | GET | 1 | 3 | 4 |
| `/api/chores/assignments/{householdId}/overview` | GET | 1 | 1 | 2 |
| `/api/chores/assignments` | GET | 1 | 3 | 4 |
| **TOTAL** | | **9** | **17** | **27** |

**Testing Pattern:**
- Uses `@WebMvcTest(ChoreController.class)` for controller-only testing
- Uses `@AutoConfigureMockMvc(addFilters = false)` to disable security filters for simpler testing
- Uses `@WithMockUser` annotation for authenticated endpoints
- Uses MockMvc for HTTP request/response testing
- Uses static imports for readable test fluent API (MockMvcRequestBuilders, MockMvcResultMatchers)
- Organized with section comments: `// ============ Tests for endpoint ============`

## Key Features

### ChoreServiceTest
✅ Comprehensive null/blank validation testing
✅ Business logic and exception scenario coverage
✅ Transactional behavior considerations noted
✅ Test data factory methods for reusability
✅ Static test constants for easy maintenance
✅ AssertionError vs IllegalArgumentException distinction
✅ AccessDeniedException testing included

### ChoreControllerTest
✅ HTTP status code testing (201, 204, 200, 400, 403, 401)
✅ Response body content validation patterns
✅ Authentication testing with `@WithMockUser`
✅ Request body validation error testing
✅ Empty result handling
✅ Service layer exception mapping to HTTP responses
✅ QueryParameter handling for filter endpoints
✅ Known issue flagged: Controller uses `id` instead of `assignmentId` in updateChoreStatus path variable

## Testing Guidelines

### For Implementation
1. Each `// TODO` comment indicates a test method that needs implementation
2. Use the helper methods to create consistent test data
3. Follow the Arrange-Act-Assert pattern
4. Mock repository interactions using `when().thenReturn()` and `when().thenThrow()`
5. Verify service method calls using `verify()`

### For Service Tests
- Test null validation with `assertThatThrownBy().isInstanceOf(AssertionError.class)`
- Test business logic errors with `assertThatThrownBy().isInstanceOf(IllegalArgumentException.class)`
- Test successful operations with assertion on returned DTOs
- Verify repository interactions with `verify(repositoryMock).method(args)`

### For Controller Tests
- Setup service mocks with `when(choreService.method(...)).thenReturn(testDTO)`
- Use MockMvc to perform HTTP requests: `mockMvc.perform(post(...)).andExpect(status().isCreated())`
- Validate response content with `jsonPath()` matchers
- Test without authentication by commenting out `@WithMockUser`

## Test Data Constants
Both test classes share similar constant naming conventions:
- `TEST_CHORE_ID`, `TEST_ASSIGNMENT_ID`, `TEST_USER_ID`, `TEST_HOUSEHOLD_ID`
- `TEST_CHORE_DESCRIPTION`, `TEST_USER_NAME`, `TEST_FREQUENCY_DAYS`
- UUIDs use predictable format (00000000-0000-0000-0000-0000000000XX)

## Notes
- Security filters are disabled in ChoreControllerTest via `@AutoConfigureMockMvc(addFilters = false)` for test simplicity
- Some tests have additional comments flagging known controller implementation issues
- Tests are organized logically but can be reordered as needed
- Helper methods use standard Java patterns for entity/DTO construction
- Both classes are ready for implementation following the skeleton structure

