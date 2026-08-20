# Implementation Plan: Pull-to-Refresh for "No Household" State

Add "drag down to refresh" functionality to the `HouseholdScreen` when the user is not in a household. Update `HouseholdProvider.refreshAll()` to be more comprehensive by including chore and member fetching.

## Proposed Changes

### State Management (Provider)

#### [MODIFY] [HouseholdProvider](file:///C:/Users/Admin/StudioProjects/HouseMate/mobile/lib/state/household_provider.dart)
- Add `ChoreService` dependency.
- Add `List<ChoreResponseDTO> _householdChores` and `List<HouseholdMemberResponseDTO> _members` state.
- Add `loadHouseholdChores()` and `loadMembers()` methods.
- Update `refreshAll()` to use `Future.wait` for:
    - `loadHouseholdData()` (Household details)
    - `loadShoppingLists()` (Shopping lists)
    - `loadHouseholdChores()` (Global chore definitions)
    - `loadMembers()` (Standalone member list fetching)

### UI Components (Screens)

#### [MODIFY] [HouseholdScreen](file:///C:/Users/Admin/StudioProjects/HouseMate/mobile/lib/ui/screens/tabs/household_screen.dart)
- Reorganize the `build` method to ensure the `RefreshIndicator` wraps the UI in both states (Orphaned and Active).
- For the "No Household" state:
    - Wrap the existing `Center` content in a `SingleChildScrollView` (or `ListView`) to enable scrolling.
    - Set `physics: const AlwaysScrollableScrollPhysics()` to ensure the `RefreshIndicator` triggers even when there is no content to scroll.

## Verification Plan

### Manual Verification
- **No Household State**: Drag down on the screen and verify the loading spinner appears and `refreshAll()` is called.
- **Data Sync**: Verify that if a user is added to a household on the backend, dragging down on the "No Household" screen successfully transitions them to the active household dashboard.
- **Provider Consistency**: Confirm that `refreshAll()` correctly populates chores and members in the provider state.
