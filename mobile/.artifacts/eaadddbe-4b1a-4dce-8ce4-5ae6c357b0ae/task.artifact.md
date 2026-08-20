# Task: Pull-to-Refresh for "No Household" Scenario

- [x] Update `HouseholdProvider`
    - [x] Add `ChoreService` dependency
    - [x] Add `_householdChores` state
    - [x] Update `refreshAll()` to fetch chores and members
- [x] Update `HouseholdScreen`
    - [x] Move `RefreshIndicator` to wrap the entire `build` content
    - [x] Ensure "No Household" view is scrollable with `AlwaysScrollableScrollPhysics`
- [x] Verification
    - [ ] Run static analysis
    - [ ] Confirm state update on drag-down
