# Money Manager Project Case Study

## 1. What This Project Does

Money Manager is a full-stack finance management application built for personal users, families, and friend groups.

It helps users manage:

- Income
- Expenses
- Categories
- Monthly budgets
- Savings goals
- Recurring transactions
- Bill reminders
- Family money transfers
- Friend expense splitting
- Settlements
- AI-based financial insights

The application started as a simple CRUD project, but it was expanded into a more realistic money-management platform with authentication, dashboards, planning tools, caching, database optimization, deployment, and Pro-user features.

## 2. The Problem It Solves

Most people manage money in scattered places.

Example:

- Salary and payments are visible in banking or UPI apps.
- Budgets are written in notes or spreadsheets.
- Family allowances are discussed manually.
- Friend expenses are calculated separately.
- Bills and subscriptions are easy to forget.
- Savings goals are not connected to monthly spending.

Because of this, users face these problems:

1. They do not know their real monthly balance clearly.
2. They cannot easily understand where their money is going.
3. They forget recurring expenses and bills.
4. Families cannot track who received money and who spent it.
5. Friends need manual calculation for shared expenses.
6. Repeated dashboard and analytics API calls make the app slower and more costly.
7. As data grows, database queries become slower without indexes and caching.

## 3. How Money Manager Solves The Problem

Money Manager gives one organized place for personal, family, and shared expenses.

### Personal Finance Tracking

Users can add income and expenses with:

- Amount
- Date
- Category
- Payment method
- Notes
- Tags
- Family member

The dashboard shows income, expense, balance, and recent activity.

### Budget Planning

The Money Plan page helps users control spending by category.

Example:

```text
Food budget: Rs 10,000
Food spent: Rs 8,400
Remaining: Rs 1,600
Progress: 84%
```

This helps users know whether they are safe or overspending.

### Savings Goals

Users can create savings targets and track contributions.

Example:

```text
Goal: New Laptop
Target: Rs 90,000
Saved: Rs 23,000
Progress: 25.6%
```

This makes savings visible and measurable.

### Recurring Transactions And Bills

Recurring salary, rent, subscriptions, EMI, and bills can be stored once and reused automatically.

This reduces manual entry and helps users plan future cashflow.

### Family Money Management

The app supports family members and family transfers.

Example:

```text
Father gives Rs 5,000 to Son.
Son spends Rs 1,200 on education.
Daughter spends Rs 900 on shopping.
The family dashboard shows member-wise spending.
```

This makes the app useful not only for one person but also for a household.

### Friend Expense Sharing

The Friends module works like a Splitwise-style feature.

It supports:

- Add friends
- Invite friends
- Accept or reject requests
- Create groups
- Add shared expenses
- Split equally
- Split by exact amount
- Split by percentage
- Split by shares
- Choose who paid
- Calculate who owes whom
- Settle payments
- Add reminders
- Add comments
- Show activity timeline

Example:

```text
Ujwal paid Rs 1,200 for dinner.
Rahul, Ankit, and Ujwal split equally.
Each share is Rs 400.
Rahul owes Ujwal Rs 400.
Ankit owes Ujwal Rs 400.
```

### AI Insights

The AI Advisor gives suggestions based on the user's income, expenses, budgets, and spending behavior.

It can help answer questions like:

- Where did my money go?
- Which category is overspending?
- How can I reduce expenses?
- What settlement should I do first?

## 4. Main Features

### Authentication

- Register
- Login
- JWT token authentication
- Protected APIs
- Profile-based data isolation

### Dashboard

- Total income
- Total expense
- Balance
- Recent transactions
- Category breakdown
- AI insights

### Category Management

- Income categories
- Expense categories
- Category icons
- Category-wise filters

### Income And Expense

- Add income
- Add expense
- Payment method tracking
- Notes and tags
- Date tracking
- Family member linking

### Money Plan

- Monthly budgets
- Savings goals
- Contribution history
- Recurring income and expenses
- Bill reminders
- Cashflow forecast
- Monthly comparison

### Family

- Family workspace
- Family members
- Family transfers
- Member-wise spending
- Family dashboard

### Friends

- Friend profiles
- Invite code or link
- Friend request lifecycle
- Groups
- Shared expenses
- Multiple split types
- Balances
- Settlements
- Reminders
- Comments
- Activity timeline
- WebSocket live updates

### Pro Features

Pro users can receive:

- Higher limits
- Advanced split options
- Receipt uploads
- Recurring shared expenses
- Group reports
- AI settlement suggestions
- Advanced friend and group features

## 5. Technology Stack

| Layer | Technology |
| --- | --- |
| Frontend | React, Vite |
| Styling | Tailwind CSS |
| Routing | React Router |
| API calls | Axios |
| Frontend caching | TanStack Query |
| Charts | Recharts |
| Icons | Lucide React |
| Backend | Spring Boot |
| Security | Spring Security, JWT |
| Database access | Spring Data JPA, Hibernate |
| Database | PostgreSQL / Neon |
| Backend cache | Redis |
| Live updates | WebSocket, STOMP |
| AI | Spring AI / OpenAI-compatible API |
| Deployment | Vercel, Render |
| Local infrastructure | Docker, Docker Compose |

## 6. Architecture

```text
React Frontend
    |
    | Axios HTTP requests
    | JWT token in Authorization header
    v
Spring Boot Backend
    |
    +-- Spring Security validates JWT
    |
    +-- Services apply business logic
    |
    +-- Repositories query PostgreSQL
    |
    +-- Redis stores cached summaries
    |
    +-- AI provider gives financial suggestions
```

## 7. Request Flow

Example: user opens dashboard.

```text
1. React dashboard page loads.
2. TanStack Query checks if dashboard data is already fresh.
3. If data is fresh, frontend uses cache.
4. If data is stale, frontend calls backend API.
5. Axios sends JWT token.
6. Spring Security validates token.
7. DashboardController calls DashboardService.
8. DashboardService checks Redis cache.
9. If cached data exists, backend returns it.
10. If not cached, service queries PostgreSQL.
11. Response is saved in Redis and returned to frontend.
12. TanStack Query stores it on frontend.
```

This reduces both API calls and database load.

## 8. Database Design Idea

The database is organized around the authenticated profile.

Important entities include:

- Profile
- Category
- Income
- Expense
- Budget
- SavingsGoal
- SavingsContribution
- RecurringTransaction
- BillReminder
- Family
- FamilyMember
- FamilyTransfer
- Friend
- FriendGroup
- SharedExpense
- SharedExpenseSplit
- FriendSettlement
- FriendReminder
- FriendActivity

Most records belong to a profile, which keeps user data separate.

## 9. Why JWT Authentication Was Used

JWT was used because the frontend and backend are deployed separately.

Flow:

```text
User logs in
    |
Backend validates credentials
    |
Backend returns JWT
    |
Frontend stores token
    |
Future requests send Authorization: Bearer token
    |
Backend validates token before allowing access
```

Benefits:

- Works well with React and Spring Boot separation.
- Backend does not need server-side sessions.
- Protected APIs can identify the logged-in user.
- Each user's financial data stays isolated.

## 10. Why TanStack Query Was Used

TanStack Query was used to reduce unnecessary frontend API calls.

Without it:

```text
User opens dashboard -> API call
User opens category -> API call
User returns dashboard -> API call again
```

With TanStack Query:

```text
User opens dashboard -> API call
User returns dashboard within 5 minutes -> cached data is used
```

It helps with:

- Frontend caching
- Loading states
- Error states
- Background refetch
- Cache invalidation after create/update/delete

Example:

```text
Add expense
    -> invalidate expenses
    -> invalidate dashboard
    -> invalidate money plan
```

This keeps data fresh without calling every API again unnecessarily.

## 11. Why Redis Was Used

Redis was added for backend caching.

Frontend caching reduces browser-to-backend calls. Redis reduces backend-to-database work.

Best Redis use cases in this project:

| Cache | Why |
| --- | --- |
| Dashboard | It is opened often and needs totals |
| Money Plan | It has budget and forecast calculations |
| Categories | Categories are used on many forms and change rarely |
| AI insights | AI calls are slow and costly |
| Friends dashboard | Balance calculations can become heavy |

Redis is not used as the main database. PostgreSQL remains the permanent source of truth.

## 12. Why Database Indexes Were Added

Indexes make database reads faster when tables grow.

Example:

```sql
CREATE INDEX idx_expense_profile_date
ON tbl_expenses(profile_id, date);
```

This helps when the dashboard asks:

```sql
SELECT *
FROM tbl_expenses
WHERE profile_id = 1
  AND date BETWEEN '2026-06-01' AND '2026-06-30';
```

Without an index, PostgreSQL may scan many rows. With the index, it can quickly find expenses for one user and month.

## 13. How N+1 Query Problems Were Solved

N+1 happens when one query loads a list, then the app runs one extra query for every item.

Bad example:

```text
Load 20 shared expenses = 1 query
Load splits for each expense = 20 queries
Load comments for each expense = 20 queries
Total = 41 queries
```

Better solution:

```text
Load all shared expenses once.
Load all splits using expense_id IN (...).
Load all comments using expense_id IN (...).
Group them in Java maps.
```

Also used:

- `JOIN FETCH`
- Batch loading
- Aggregate queries
- Avoiding repository calls inside loops

## 14. Problems Faced While Building And How To Solve Them

This is the most important section for interview explanation.

### Problem 1: The App Looked Like A Basic CRUD Website

At first, the pages looked old and empty.

How to solve:

- Create a proper dashboard layout.
- Use consistent spacing, colors, and typography.
- Add cards only where useful.
- Add charts and progress bars.
- Add empty states and demo data.
- Make forms and modals responsive.
- Use real product flows instead of only tables.

What I learned:

Frontend design is not just colors. A real app needs hierarchy, spacing, states, and workflow clarity.

### Problem 2: Backend Returned 500 Errors

Some APIs returned internal server errors.

How to solve:

1. Check browser console only to know which API failed.
2. Check backend logs to find the real exception.
3. Check request body, DTO, database constraints, and service logic.
4. Add proper validation.
5. Return meaningful error messages instead of generic 500.

What I learned:

Frontend shows symptoms. Backend logs show the cause.

### Problem 3: AI API Was Slow Or Timing Out

AI calls can take longer than normal APIs.

How to solve:

- Increase frontend timeout.
- Send summarized data instead of all transactions.
- Cache AI insights.
- Add fallback advice if AI fails.
- Do not block the whole app if AI is unavailable.

What I learned:

External services should be optional. Core app features must still work.

### Problem 4: Deployment Failed Because Environment Variables Were Missing

Render deployment failed when database, JWT, or API keys were missing.

How to solve:

- Move secrets to environment variables.
- Keep local and production configuration separate.
- Do not commit secrets.
- Add clear documentation for required variables.
- Use health endpoints to verify the app is running.

What I learned:

Production problems are often configuration problems, not code problems.

### Problem 5: Neon PostgreSQL Password Error

The backend could not connect to Neon because of wrong or mismatched database credentials.

How to solve:

1. Copy the exact Neon connection details.
2. Convert URL to JDBC format.
3. Use the correct database name.
4. Use the correct role and password.
5. Save the variables in Render.
6. Redeploy after saving.

JDBC format:

```text
jdbc:postgresql://host/database?sslmode=require
```

What I learned:

Host, database, username, password, and branch must belong to the same Neon connection.

### Problem 6: Vercel Frontend Could Not Connect To Render Backend

The frontend was deployed but API calls failed.

How to solve:

- Set the correct backend URL in Vercel environment variables.
- Rebuild the frontend after changing Vite environment variables.
- Add backend CORS support for the Vercel domain.
- Add React Router rewrite so page refresh does not give 404.

What I learned:

Frontend deployment success does not mean backend integration is correct.

### Problem 7: Dashboard And Pages Called APIs Too Often

Every page navigation caused new API calls.

How to solve:

- Add TanStack Query.
- Set cache times.
- Use query keys.
- Invalidate related queries only after data changes.

What I learned:

Good caching reduces cost and improves speed, but it needs correct invalidation.

### Problem 8: Redis Cache Could Show Old Data

If dashboard data is cached, newly added expenses might not appear immediately.

How to solve:

- Clear dashboard cache after adding income or expense.
- Clear money-plan cache after budget, savings, or recurring changes.
- Clear category cache after category changes.

What I learned:

Cache expiry is not enough. Important writes must invalidate related reads.

### Problem 9: Family Dashboard Gave 500 Errors

Family data can have missing members, old demo data, or relationship mismatches.

How to solve:

- Verify family belongs to the logged-in profile.
- Handle empty family data safely.
- Avoid null pointer errors.
- Map entities to DTOs.
- Fetch required relationships properly.

What I learned:

Real data is messy. Service code must handle empty and partial states.

### Problem 10: Income And Expense Modals Did Not Fit Screen

Large forms were hard to use on smaller screens.

How to solve:

- Give modal a maximum height.
- Make modal body scrollable.
- Keep close button and title visible.
- Test on desktop and smaller viewport.

What I learned:

Forms must be designed for real screens, not only full desktop height.

### Problem 11: Friend Expense Calculations Became Complex

Shared expenses need payer, participants, split type, balances, and settlement history.

How to solve:

- Separate shared expense and split records.
- Store payer clearly.
- Store split amount per friend.
- Calculate balances from paid amount and owed amount.
- Store settlement records separately.

What I learned:

Complex features become easier when the database model is clear.

### Problem 12: Pro And Free User Rules Needed Separation

Some features should be limited for free users and expanded for Pro users.

How to solve:

- Add `planType` to profile.
- Create a service for plan limits.
- Return limit information in dashboard responses.
- Let frontend show locked or limited states.
- Also validate limits in backend, not only frontend.

What I learned:

Frontend restrictions are for UX. Backend restrictions are for real security.

## 15. How I Would Improve It Further

Next improvements:

1. Add Flyway or Liquibase migrations.
2. Add pagination to large lists.
3. Add unit and integration tests.
4. Add Redis rate limiting.
5. Add object storage for receipts.
6. Add notification jobs.
7. Add better monitoring and logs.
8. Add subscription billing for Pro users.
9. Add audit logs for shared expenses.
10. Add full mobile polish.

## 16. Security Considerations

Important rules:

- Never commit secrets.
- Store JWT secret in environment variables.
- Hash passwords.
- Validate every request.
- Check profile ownership before returning data.
- Restrict CORS in production.
- Use HTTPS.
- Add rate limits for login and AI endpoints.
- Validate uploaded receipt files.
- Rotate exposed API keys immediately.

## 17. Interview Explanation

If an interviewer asks what this project is, you can answer:

```text
Money Manager is a full-stack financial management platform that I built using React, Spring Boot, PostgreSQL, Redis, and JWT authentication. It started as a CRUD app for income and expenses, but I expanded it into a real money-management system with budgets, savings goals, recurring transactions, bill reminders, family transfers, friend expense splitting, settlement tracking, and AI insights.

The main problem it solves is that users usually manage personal spending, family money, and shared expenses in separate tools. My app brings those workflows into one place.

On the engineering side, I used TanStack Query to reduce repeated frontend API calls, Redis to cache expensive backend summaries, database indexes to speed up monthly and category queries, and JOIN FETCH or batch loading to solve N+1 query problems. I deployed the frontend on Vercel, backend on Render, and database on Neon.
```

## 18. Short Project Summary

Money Manager is not only a CRUD app. It is a practical finance system that includes:

- Personal money tracking
- Planning and forecasting
- Family money management
- Friend expense splitting
- AI insights
- Frontend caching
- Backend Redis caching
- Database indexing
- JWT security
- Deployment-ready architecture

The main learning from this project is how to convert a simple CRUD application into a real-world system by improving domain modeling, user experience, performance, security, and deployment quality.
