# Expense Split Strategy Pattern

This package implements the **Strategy Pattern** for calculating expense shares across different splitting methods.

## Overview

The Strategy Pattern allows the selection of different expense splitting algorithms at runtime based on the `ExpenseSplitType` enum. Each strategy encapsulates a different algorithm for calculating how an expense should be divided among participants.

## Components

### 1. `ExpenseSplitStrategy` (Interface)
The core interface that all concrete strategies implement.

```java
public interface ExpenseSplitStrategy {
    List<ExpenseShare> calculateShares(Expense expense, List<User> involvedUsers, List<BigDecimal> splitParameters);
}
```

### 2. Concrete Strategies

#### `EqualSplitStrategy`
- **Purpose**: Divides an expense equally among all involved users
- **Use Case**: When all participants pay the same amount
- **Parameters**: Not used (empty list)
- **Rounding**: Handles rounding remainder by adding it to the payer's share

**Example**: $30 split equally among 3 people = $10 each

#### `SharesSplitStrategy`
- **Purpose**: Divides an expense based on weighted shares
- **Use Case**: When participants contribute unequal "shares" (e.g., half vs. quarter vs. quarter)
- **Parameters**: List of share values in the same order as `involvedUsers`
- **Rounding**: Distributes rounding remainder to the payer's share

**Example**: $30 split by shares [2, 1, 1]
- User 1: (2/4) × $30 = $15
- User 2: (1/4) × $30 = $7.50
- User 3: (1/4) × $30 = $7.50

#### `ExactAmountStrategy`
- **Purpose**: Assigns exact amounts to each participant
- **Use Case**: When each person's share is explicitly defined
- **Parameters**: List of exact amounts in the same order as `involvedUsers`
- **Validation**: Sum of amounts must equal the total expense
- **Rounding**: Not applicable; exact amounts are used as-is

**Example**: $30 split as [$10, $12, $8]

#### `AdjustmentStrategy`
- **Purpose**: Applies relative adjustments to an equal baseline split
- **Use Case**: When some participants should pay more or less than an equal split
- **Parameters**: List of adjustment deltas (positive = pays more, negative = pays less)
- **Calculation**: 
  1. Baseline = (Total - Sum of Adjustments) / Number of Users
  2. Each user's share = Baseline + Their Adjustment
- **Example**: $100 with adjustments [+10, -5, 0]
  - Baseline: (100 - 5) / 3 = $31.67
  - User A: 31.67 + 10 = $41.67
  - User B: 31.67 - 5 = $26.67
  - User C: 31.67 + 0 = $31.67
  - Total: $100.00

### 3. `ExpenseSplitStrategyFactory`
A factory class that manages the mapping between `ExpenseSplitType` enum values and their corresponding strategy implementations.

**Encapsulates**: The logic for selecting the appropriate strategy

```java
ExpenseSplitStrategy strategy = strategyFactory.getStrategy(ExpenseSplitType.EQUAL_SPLIT);
```

## Usage Example

```java
@Service
public class ExpenseService {
    private final ExpenseSplitStrategyFactory strategyFactory;
    
    public Expense createExpense(String description, BigDecimal amount, User payer, 
                                 ExpenseSplitType splitType, List<User> involvedUsers, 
                                 List<BigDecimal> splitParameters) {
        // Create expense
        Expense expense = new Expense(description, amount, payer, splitType);
        expense = expenseRepository.save(expense);

        // Get strategy and calculate shares
        var strategy = strategyFactory.getStrategy(splitType);
        List<ExpenseShare> shares = strategy.calculateShares(expense, involvedUsers, splitParameters);
        
        // Save shares and update debts
        expenseShareRepository.saveAll(shares);
        return expense;
    }
}
```

## Adding a New Strategy

To add a new expense split type:

1. **Add a new enum value** to `ExpenseSplitType` in the shared module:
   ```java
   public enum ExpenseSplitType {
       EQUAL_SPLIT,
       SHARES,
       EXACT_AMOUNT,
       ADJUSTMENT,
       YOUR_NEW_TYPE  // Add here
   }
   ```

2. **Create a new strategy class** implementing `ExpenseSplitStrategy`:
   ```java
   @Component
   public class YourNewStrategy implements ExpenseSplitStrategy {
       @Override
       public List<ExpenseShare> calculateShares(Expense expense, List<User> involvedUsers, 
                                                  List<BigDecimal> splitParameters) {
           // Implement your algorithm
       }
   }
   ```

3. **Update the factory** to handle the new type:
   ```java
   public ExpenseSplitStrategy getStrategy(ExpenseSplitType splitType) {
       return switch (splitType) {
           case EQUAL_SPLIT -> equalSplitStrategy;
           case SHARES -> sharesSplitStrategy;
           case EXACT_AMOUNT -> exactAmountStrategy;
           case ADJUSTMENT -> adjustmentStrategy;
           case YOUR_NEW_TYPE -> yourNewStrategy;  // Add here
       };
   }
   ```

## Design Principles

- **Single Responsibility**: Each strategy handles one specific splitting algorithm
- **Open/Closed**: Open for extension (new strategies), closed for modification
- **Dependency Injection**: Strategies are injected via Spring's `@Component` and factory autowiring
- **Immutability**: Strategies are stateless and thread-safe

## File Structure

```
service/expense/strategy/
├── ExpenseSplitStrategy.java          (Interface)
├── EqualSplitStrategy.java            (Concrete Strategy)
├── SharesSplitStrategy.java           (Concrete Strategy)
├── ExactAmountStrategy.java           (Concrete Strategy)
├── AdjustmentStrategy.java            (Concrete Strategy)
└── ExpenseSplitStrategyFactory.java   (Factory)
```

## Validation Rules

- All strategies validate that `involvedUsers` is not empty
- `SharesSplitStrategy` requires non-zero share values
- `ExactAmountStrategy` validates that the sum equals the total expense
- `AdjustmentStrategy` allows negative values but sum must equal total
- Rounding is handled gracefully with remainder distribution to the payer
