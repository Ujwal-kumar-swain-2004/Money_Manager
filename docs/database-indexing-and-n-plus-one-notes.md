# Database Indexing and N+1 Query Notes

This note explains the optimization work added to the backend.

## What Database Indexes Do

Database indexes make searches faster. Without an index, PostgreSQL may scan many rows to find matching data. With an index, PostgreSQL can jump directly to the matching rows.

Example:

```text
User opens dashboard for June 2026.
Backend asks: give me all expenses for profile 10 between June 1 and June 30.
```

The useful index is:

```text
profile_id + date
```

Because the query filters by user and date.

## Where Indexes Were Added

Indexes were added in the JPA entity `@Table(indexes = ...)` definitions.

### Login and Profile

File:

```text
ProfileEntity.java
```

Indexes:

```text
email
activation_token
```

Why:

- Login searches by email.
- Register checks duplicate email.
- Account activation searches by activation token.

### Income and Expense

Files:

```text
IncomeEntity.java
ExpenseEntity.java
```

Indexes:

```text
profile_id + date
profile_id + category_id
profile_id + payment_method
profile_id + family_member_id
```

Why:

- Dashboard loads monthly income and expenses.
- Filters search by date/category/payment method.
- Family dashboard groups spending by family member.

### Categories

File:

```text
CategoryEntity.java
```

Indexes:

```text
profile_id + type
profile_id + name + type
```

Why:

- Category dropdown loads by user and income/expense type.
- Duplicate category names for same user/type are prevented.

### Money Plan

Files:

```text
BudgetEntity.java
SavingsGoalEntity.java
SavingsContributionEntity.java
RecurringTransactionEntity.java
BillReminderEntity.java
```

Indexes:

```text
budget: profile_id + month + year
budget: profile_id + category_id + month + year
savings goal: profile_id + target_date
savings contribution: goal_id + contribution_date
recurring: profile_id + next_run_date
recurring: active + next_run_date
bills: profile_id + due_date
bills: profile_id + paid
```

Why:

- Money plan loads budgets by month.
- Savings goal page sorts goals by target date.
- Contribution history loads by goal.
- Recurring processor finds due items fast.
- Bill reminders are filtered by due date and paid status.

### Family

Files:

```text
FamilyEntity.java
FamilyMemberEntity.java
FamilyTransferEntity.java
```

Indexes:

```text
owner_profile_id
invite_code
family_id
profile_id
family_id + transfer_date
from_member_id
to_member_id
```

Why:

- Family dashboard loads by owner/family.
- Invite code lookup becomes fast.
- Transfers are loaded by family and date.
- Member-based transfer lookups become faster.

### Friends and Shared Expenses

Files:

```text
FriendEntity.java
FriendGroupEntity.java
FriendGroupMemberEntity.java
SharedExpenseEntity.java
SharedExpenseSplitEntity.java
FriendSettlementEntity.java
FriendReminderEntity.java
FriendActivityEntity.java
FriendCommentEntity.java
```

Indexes:

```text
friends: profile_id + status
friends: profile_id + name
groups: profile_id
group members: group_id
group members: friend_id
group members: group_id + friend_id
shared expenses: profile_id + expense_date
shared expenses: group_id
shared expenses: paid_by_friend_id
splits: expense_id
splits: friend_id
settlements: profile_id + settlement_date
settlements: friend_id
reminders: profile_id + due_date
reminders: friend_id
activities: profile_id + created_at
comments: expense_id + created_at
```

Why:

- Friends dashboard loads friends, groups, expenses, balances, settlements, reminders, and timeline.
- Shared expense splitting depends heavily on expense and friend IDs.
- Comments and activities are sorted by created time.

## What N+1 Query Means

N+1 query means:

```text
1 query loads a list
then N extra queries load child data for each row
```

Example bad case:

```text
Load 20 shared expenses = 1 query
For each expense, load splits = 20 more queries
For each expense, load comments = 20 more queries
Total = 41 queries
```

This becomes slow when records grow.

## N+1 Queries Solved

The clear N+1 problem was in:

```text
FriendsService.java
```

### 1. Groups and Group Members

Before:

```text
Load all groups
For each group, query group members separately
```

After:

```text
Load all groups
Load all members for those group IDs in one query
Group members in Java Map
```

Repository method added:

```text
FriendGroupMemberRepository.findByGroupIdInWithFriend(...)
```

This uses `JOIN FETCH member.friend`, so friend IDs are available without extra lazy queries.

### 2. Shared Expenses, Splits, and Comments

Before:

```text
Load all shared expenses
For each expense, query splits
For each expense, query comments
```

After:

```text
Load all shared expenses
Load all splits for those expense IDs in one query
Load all comments for those expense IDs in one query
Group splits/comments in Java Map
```

Repository methods added:

```text
SharedExpenseSplitRepository.findByExpenseIdInWithFriend(...)
FriendCommentRepository.findByExpenseIdInOrderByCreatedAtDesc(...)
```

### 3. Expense Paid By Friend and Group Name

Before:

```text
Load shared expenses
Access paidByFriend.name or group.name
Hibernate may fire extra queries
```

After:

```text
SharedExpenseRepository.findByProfileIdOrderByExpenseDateDesc(...)
```

Now uses:

```text
LEFT JOIN FETCH paidByFriend
LEFT JOIN FETCH group
```

### 4. Settlements and Reminders Friend Name

Before:

```text
Load settlements/reminders
Access friend.name for each row
Hibernate may fire extra queries
```

After:

```text
FriendSettlementRepository.findByProfileIdOrderBySettlementDateDesc(...)
FriendReminderRepository.findByProfileIdOrderByDueDateAsc(...)
```

Both now use:

```text
JOIN FETCH friend
```

### 5. Friend Balance Calculation

Before:

```text
For every split where paid friend matched, query total splits for that expense
```

After:

```text
Load all splits once
Pre-calculate split total per expense in a Map
Use the Map inside the loop
```

This removes repeated `totalSplitAmount(expenseId)` calls during balance calculation.

## Important Production Note

The indexes were added through JPA annotations. This is fine for local development. For production, move the same index changes into a migration tool:

```text
Flyway
Liquibase
```

That gives safe, repeatable database schema upgrades.

## Simple Explanation

Indexes make PostgreSQL find rows faster.

`JOIN FETCH` makes Hibernate load related data together.

Batch loading with `IN (...)` avoids calling the database once per row.

Together, these changes improve:

- Dashboard speed.
- Money plan speed.
- Family dashboard speed.
- Friends/Splitwise page speed.
- Database cost as the app grows.
