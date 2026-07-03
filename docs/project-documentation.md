# Money Manager - Complete Project Documentation

## 1. Project Overview

Money Manager is a full-stack financial management application for individuals, families, and groups of friends.

The project began as a basic CRUD application for recording income and expenses. It was expanded into a broader financial platform that helps users:

- Understand where their money comes from and where it goes.
- Create monthly budgets and monitor overspending.
- Build savings goals and record contributions.
- Automate recurring income and expenses.
- Track bills, subscriptions, and payment methods.
- Manage money shared between family members.
- Split expenses with friends and calculate balances.
- Receive AI-assisted financial insights.

The system is built with React, Spring Boot, PostgreSQL, Redis, JWT authentication, and REST APIs.

## 2. Problem Statement

Money management is often spread across multiple places:

- Banking and UPI applications show payments but do not explain spending habits.
- Notes and spreadsheets require manual calculations.
- Budget applications may not support family allowances or transfers.
- Expense-splitting applications focus on friends but not personal budgets.
- Users repeatedly calculate recurring bills, shared balances, and savings progress.

This creates several problems:

1. Users cannot see their complete financial position in one place.
2. They may know how much they spent but not which categories consumed their money.
3. Families cannot clearly track who received money and who made purchases.
4. Friends must manually calculate who owes whom.
5. Repeated API, database, and AI requests can make a financial application slow and expensive.
6. As transaction data grows, unoptimized database queries take longer to execute.

## 3. How The Application Solves The Problem

Money Manager combines personal, family, and shared finance workflows in one application.

### Personal Finance

Users record income and expenses with:

- Amount
- Date
- Category
- Payment method
- Notes and tags
- Family member association
- Receipt or attachment information

The dashboard calculates total income, total expenses, available balance, and recent activity.

### Financial Planning

The Money Plan module provides:

- Monthly category budgets
- Budget progress and alerts
- Savings goals
- Savings contribution history
- Recurring transactions
- Bill and subscription reminders
- Monthly spending comparison
- Category-wise spending breakdown
- Spending calendar
- Cashflow forecast

This changes the app from a transaction recorder into a planning tool.

### Family Finance

A user can create a family workspace, add family members, and record transfers or expenses for each member.

Example:

```text
Father transfers Rs 5,000 to Son.
Son spends Rs 1,200 on education and Rs 800 on travel.
The family dashboard shows the transfer, remaining amount, and spending categories.
```

This gives the household a shared view without mixing every transaction into one unexplained list.

### Friends And Shared Expenses

The Friends module supports:

- Friend invitations
- Accept, reject, activate, and block states
- Friend groups
- Group roles
- Equal, exact, percentage, and share-based splits
- Payer selection
- Automatic balance calculation
- Settlements
- Reminders
- Comments
- Activity history
- Recurring shared expenses
- Group reports
- AI settlement suggestions
- Live updates through WebSocket support

The application calculates balances from the payer and each participant's assigned share.

Example:

```text
Ujwal pays Rs 1,200 for dinner.
Three people share the expense equally.
Each person's share is Rs 400.
The system records that the other two participants owe Ujwal Rs 400 each.
```

## 4. Target Users

### Individual Users

People who want to track spending, income, budgets, savings, and bills.

### Families

Households that want to manage allowances, transfers, shared expenses, and member-wise spending.

### Friends And Groups

Roommates, travelers, office groups, and friends who regularly split expenses.

### Free And Pro Users

The profile contains a `planType` such as `FREE` or `PRO`.

Free users receive the core money-management experience with usage limits. Pro users can receive larger limits, advanced shared-expense options, automation, reports, receipt support, and AI-assisted features.

## 5. Main Features

### Authentication And User Management

- Registration and login
- JWT-based authentication
- Protected REST endpoints
- Profile management
- User-level data isolation
- Free and Pro plan type

### Transactions

- Income management
- Expense management
- Categories
- Payment methods such as cash, UPI, card, and bank
- Notes and tags
- Filtering by type, date, and category
- Excel export and email report support

### Dashboard And Analytics

- Total income
- Total expense
- Current balance
- Recent transactions
- Category breakdown
- Monthly comparison
- Chart-ready analytics data

### Money Plan

- Monthly budgets
- Budget alerts
- Savings goals
- Contribution history
- Recurring income and expenses
- Automatic processing of due recurring items
- Bill reminders
- Cashflow forecast
- Spending calendar

### Family Management

- Family workspace
- Family members
- Family roles
- Transfers between members
- Member-wise spending
- Family dashboard

### Friends Management

- Friend profiles
- Invitation workflow
- Groups and roles
- Shared expenses
- Multiple split methods
- Balances
- Settlement history
- Repayment reminders
- Comments and timeline
- Live WebSocket events

### AI Advisor

- Financial advice
- Dashboard insight summaries
- Settlement suggestions
- Fallback behavior when the external AI provider is unavailable

## 6. Technology Stack

| Layer | Technology | Responsibility |
| --- | --- | --- |
| Frontend | React and Vite | User interface and client-side navigation |
| Styling | Tailwind CSS | Responsive application design |
| API state | TanStack Query | Frontend caching, loading states, and invalidation |
| HTTP client | Axios | REST API communication |
| Charts | Recharts | Financial visualizations |
| Backend | Spring Boot | APIs and business logic |
| Security | Spring Security and JWT | Authentication and authorization |
| Persistence | Spring Data JPA and Hibernate | Database access |
| Database | PostgreSQL / Neon | Permanent application data |
| Server cache | Redis | Cached summaries and category data |
| Live updates | Spring WebSocket and STOMP | Friend activity updates |
| AI | Spring AI and OpenAI-compatible API | Financial insights |
| Deployment | Render and Vercel | Backend and frontend hosting |
| Containers | Docker and Docker Compose | Local PostgreSQL and Redis services |

## 7. High-Level Architecture

```text
User
  |
  v
React Frontend
  |
  | HTTPS REST requests with JWT
  v
Spring Boot API
  |
  +---- Redis Cache
  |
  +---- PostgreSQL / Neon
  |
  +---- OpenAI API
  |
  +---- Email Provider
```

### Request Flow

1. The user performs an action in React.
2. React sends a request through Axios.
3. Axios adds the JWT token to the request.
4. Spring Security validates the token.
5. The controller forwards the request to a service.
6. The service applies business rules.
7. The repository reads or writes PostgreSQL data.
8. Cached read operations may return data from Redis.
9. The backend sends a DTO response.
10. TanStack Query stores the response temporarily in the browser.

## 8. Backend Design

The backend follows a layered architecture:

```text
Controller -> Service -> Repository -> Database
```

### Controllers

Controllers define REST endpoints and receive HTTP requests.

Examples:

- `DashboardController`
- `IncomeController`
- `ExpenseController`
- `MoneyPlanController`
- `FamilyController`
- `FriendsController`

### Services

Services contain calculations and business rules.

Examples:

- Calculating dashboard totals
- Validating split amounts
- Calculating friend balances
- Enforcing plan limits
- Processing recurring transactions
- Invalidating related cache entries

### Repositories

Repositories use Spring Data JPA to query PostgreSQL.

They include:

- Standard CRUD queries
- Date and profile filters
- Aggregate queries
- Batch loading
- `JOIN FETCH` queries to prevent N+1 problems

### DTOs

DTOs control the API request and response shape. They prevent database entities from becoming the public API contract and avoid returning unnecessary fields.

## 9. Authentication Flow

1. A user registers with their profile information.
2. The password is stored as a secure hash.
3. The user submits email and password during login.
4. Spring Security validates the credentials.
5. The backend creates a signed JWT.
6. The frontend stores the token and sends it in later requests:

```http
Authorization: Bearer <token>
```

7. `JwtRequestFilter` validates the token before protected controllers run.
8. The authenticated profile is placed in the Spring Security context.
9. Services use the authenticated profile ID to isolate the user's data.

The JWT secret must be stored in an environment variable and must never be committed to Git.

## 10. Frontend Data Caching

TanStack Query reduces repeated API requests.

Suggested cache times used by the project include:

| Data | Fresh Time |
| --- | --- |
| Dashboard | 5 minutes |
| Money Plan | 5 minutes |
| Friends dashboard | 5 minutes |
| Categories | 30 minutes |
| AI insights | Up to 12 hours |

When a user moves between pages, TanStack Query can immediately reuse fresh cached data instead of requesting it again.

When data changes, the related query is invalidated:

```text
Add expense
  -> invalidate expense list
  -> invalidate dashboard
  -> invalidate money plan
  -> fetch fresh data when required
```

This gives the speed of caching without forcing users to wait for the normal cache expiry after a change.

## 11. Redis Caching

Redis reduces repeated backend calculations and database reads.

The implemented caches include:

| Cache | Purpose |
| --- | --- |
| `dashboard` | Dashboard totals and recent transactions |
| `moneyPlan` | Budget progress, alerts, and forecasts |
| `categories` | Frequently reused category lists |

Example cache key:

```text
moneyPlan::12:6:2026:30
```

The key represents a specific profile, month, year, and forecast period.

### Cache Invalidation

When income or expense data changes, the application clears dashboard and money-plan caches. When category data changes, it also clears the category cache.

This is important because cache expiry alone could show old values for several minutes.

## 12. Database Performance

### Database Indexes

Indexes were added for fields commonly used in filters, sorting, joins, and authentication.

Example:

```sql
CREATE INDEX idx_expense_profile_date
ON expenses(profile_id, date);
```

This helps queries such as:

```sql
SELECT *
FROM expenses
WHERE profile_id = 10
  AND date BETWEEN '2026-06-01' AND '2026-06-30';
```

Without the index, PostgreSQL may scan the entire expense table. With the index, it can locate the user's date range more efficiently.

### N+1 Query Prevention

An N+1 problem happens when the application loads a list with one query and then executes another query for every item.

Example:

```text
1 query loads 20 shared expenses.
20 queries load splits.
20 queries load comments.
Total: 41 queries.
```

The project improves this by:

- Loading child records with `IN (...)`.
- Using `JOIN FETCH` for required relationships.
- Grouping batch-loaded records in Java maps.
- Calculating totals once instead of querying inside loops.

## 13. Important Engineering Problems And Solutions

### Problem 1: The Application Looked Like A Basic CRUD Project

The first version mainly contained forms and table-like cards.

Solution:

- Created a consistent application shell and sidebar.
- Added responsive layouts and clearer visual hierarchy.
- Replaced external icon URLs with proper rendered icons.
- Added useful empty, loading, error, and progress states.
- Built dedicated pages for planning, family, friends, and AI workflows.

Lesson:

Good frontend design is not only color. It requires information hierarchy, consistent spacing, predictable actions, responsive behavior, and meaningful states.

### Problem 2: Registration Returned HTTP 500

Possible causes in this type of failure include database constraints, missing environment variables, invalid request data, or uncaught backend exceptions.

Solution approach:

1. Read the complete backend stack trace.
2. Verify the request body against the DTO.
3. Check duplicate-email handling.
4. Check database connectivity.
5. Return a clear validation or conflict response instead of a generic 500.

Lesson:

Frontend console errors only show the symptom. The backend exception identifies the real cause.

### Problem 3: AI Requests Timed Out

External AI APIs can be slow, unavailable, or rate-limited.

Solution:

- Increased the frontend request timeout.
- Reduced the data sent to the AI.
- Used summarized financial totals instead of all transactions.
- Added fallback financial advice.
- Planned long-lived caching for AI insights.
- Disabled mandatory AI startup configuration where production should run without AI.

Lesson:

An optional external service must not prevent the main financial application from starting or working.

### Problem 4: Deployment Failed Because Secrets Were Missing

Spring Boot failed when database, JWT, or OpenAI configuration was unavailable.

Solution:

- Moved secrets to environment variables.
- Created separate local and production configuration.
- Made optional integrations degrade gracefully.
- Added health endpoints.
- Used Render environment variables instead of committing secrets.

Lesson:

Configuration should be externalized, validated, and documented. Secrets exposed in chat, screenshots, or Git should be rotated.

### Problem 5: Neon Database Authentication Failed

The application received PostgreSQL password authentication errors.

Solution approach:

1. Copy the current Neon connection details.
2. Convert the URL to JDBC format.
3. Keep username and password in separate variables.
4. Confirm the pooler host belongs to the same Neon project and branch.
5. Remove accidental spaces or outdated credentials.
6. redeploy after saving environment variables.

Correct JDBC structure:

```text
jdbc:postgresql://<pooler-host>/<database>?sslmode=require
```

Lesson:

Database host, role, password, branch, and project must belong to the same connection configuration.

### Problem 6: Render Showed A 404 At The Root URL

The backend was running, but no controller handled `/`.

Solution:

- Use a real API or health endpoint such as `/api/v1.0/health`.
- Treat a root 404 separately from an application startup failure.

Lesson:

A 404 can mean the server is healthy but the requested route does not exist.

### Problem 7: Vercel Frontend Could Not Reach Render

The production frontend was built with an incorrect or missing backend URL.

Solution:

- Configure the Vite backend environment variable in Vercel.
- Allow the Vercel frontend origin in backend CORS configuration.
- Redeploy the frontend because Vite variables are inserted during build.
- Add a Vercel rewrite so React Router paths do not return 404.

Lesson:

Frontend and backend deployment can succeed independently while integration still fails due to URL, CORS, or route-rewrite configuration.

### Problem 8: Repeated API Calls Increased Cost

Pages requested the same data whenever the user navigated back to them.

Solution:

- Added TanStack Query in React.
- Added suitable `staleTime` values.
- Invalidated only related queries after mutations.
- Added Redis for expensive backend summaries.

Lesson:

Frontend cache reduces network requests. Redis reduces backend calculations and database reads. They solve different layers of the same performance problem.

### Problem 9: Cached Data Could Become Stale

Long cache durations can display old balances after a transaction changes.

Solution:

- Invalidate TanStack Query keys after successful mutations.
- Clear related Redis caches in backend services.
- Keep expiry times as a fallback.

Lesson:

Cache invalidation should follow business relationships, not only individual endpoints.

### Problem 10: Family Dashboard Returned HTTP 500

This can happen when old demo data, missing relationships, lazy-loaded entities, or inconsistent database records conflict with newer code.

Solution approach:

1. Inspect the Render stack trace and request profile.
2. Verify that the family belongs to the authenticated user.
3. Check nullable members and transfers.
4. Fetch required relationships in a transaction or query.
5. Map entities to DTOs before serialization.
6. Add a test for a family with no members, no transfers, and partially populated legacy data.

Lesson:

Production data often contains states that perfect local demo data does not reveal.

### Problem 11: Large Forms Did Not Scroll Properly

Income and expense forms could become taller than the viewport.

Solution:

- Constrained modal height to the viewport.
- Kept the modal header visible.
- Made the form body independently scrollable.
- Tested desktop and mobile viewport sizes.

Lesson:

Every modal must work when its content is taller than the screen.

### Problem 12: Friend Data Caused N+1 Queries

Groups, splits, comments, settlements, and reminders could trigger many small database queries.

Solution:

- Batch-loaded records by IDs.
- Used `JOIN FETCH`.
- Built lookup maps in the service.
- Removed repository calls from loops.

Lesson:

Database performance must be measured by total query count, not only the speed of one query.

## 14. Development And Production Environments

### Local Development

```text
React:     http://localhost:5173
Backend:  http://localhost:8080/api/v1.0
Redis:    redis://localhost:6379
```

Docker can run PostgreSQL and Redis locally.

### Production

```text
React frontend -> Vercel
Spring Boot API -> Render
PostgreSQL      -> Neon
Redis           -> Managed cloud Redis
```

Local and production environments should use the same variable names but different secret values.

## 15. Testing Strategy

### Backend Tests

- Authentication success and failure
- User data isolation
- Income and expense CRUD
- Budget calculations
- Recurring transaction processing
- Savings contribution totals
- Family transfer validation
- Split calculation for every split type
- Settlement balance updates
- Cache invalidation after mutations
- Repository query-count tests for N+1-sensitive pages

### Frontend Tests

- Login and protected routing
- Loading, empty, and error states
- Adding and deleting transactions
- Query invalidation after mutations
- Form validation
- Modal scrolling
- Responsive layouts
- Free and Pro feature states

### Integration Tests

- Register, login, and access protected API
- Add expense and verify dashboard update
- Add savings contribution and verify goal progress
- Add shared expense and verify friend balances
- Settle balance and verify settlement history
- Verify cached response is refreshed after a write

## 16. Security Considerations

- Hash passwords with a strong password encoder.
- Keep JWT secrets long and private.
- Never commit database, Redis, SMTP, or AI credentials.
- Rotate any secret exposed in screenshots, chat, or Git history.
- Validate file type and size for receipt uploads.
- Authorize every resource by authenticated profile ID.
- Rate-limit login and AI endpoints.
- Use HTTPS in production.
- Restrict CORS to trusted frontend domains.
- Avoid returning entities directly from controllers.
- Add audit logs for sensitive shared-money changes.

## 17. Current Limitations

- Pro billing and payment subscription processing are not complete.
- Receipt files should eventually use object storage.
- Database schema changes should move from automatic JPA updates to Flyway or Liquibase.
- Large transaction and activity lists need complete pagination.
- More automated tests are needed before production use.
- Monitoring, alerting, and centralized logs should be added.
- WebSocket authentication and reconnect behavior need production hardening.
- AI usage limits and cost monitoring need enforcement.

## 18. Recommended Next Steps

1. Add Flyway database migrations.
2. Add pagination to all growing lists.
3. Add backend and frontend automated tests.
4. Add Redis rate limiting for authentication and AI.
5. Add object storage for receipts.
6. Add monitoring, structured logs, and alerts.
7. Add email and in-app notification jobs.
8. Add Pro subscription billing.
9. Add database backup and recovery procedures.
10. Perform security and load testing.

## 19. What I Learned While Building This Project

This project demonstrates more than CRUD operations.

It shows how to:

- Convert product requirements into database entities and APIs.
- Build authenticated full-stack workflows.
- Model personal, family, and friend financial relationships.
- Calculate budgets, balances, splits, and forecasts.
- Diagnose failures using frontend errors and backend logs.
- Separate local and production configuration.
- Reduce API requests using frontend caching.
- Reduce database work using Redis and indexes.
- Prevent stale data through cache invalidation.
- Solve N+1 query problems with batch queries and fetch joins.
- Deploy a React frontend and Spring Boot backend separately.
- Design optional integrations so failures do not break core features.

## 20. Interview-Ready Project Summary

> I built Money Manager, a full-stack financial management platform using React, Spring Boot, PostgreSQL, Redis, and JWT authentication. It started as an income and expense CRUD application, but I expanded it with monthly budgets, savings goals, recurring transactions, bill reminders, cashflow forecasting, family transfers, and Splitwise-style friend expenses. I used TanStack Query to reduce repeated frontend API calls and Redis to cache expensive backend summaries. I added database indexes and solved N+1 queries with batch loading and `JOIN FETCH`. The frontend is deployed on Vercel, the backend on Render, and PostgreSQL on Neon. The most important engineering challenges were cache invalidation, database authentication, production configuration, external AI timeouts, CORS, and keeping user data isolated.

## 21. Conclusion

Money Manager solves a practical problem: financial information is usually separated across banking apps, spreadsheets, notes, family conversations, and expense-splitting tools.

By combining personal tracking, planning, family management, shared expenses, analytics, and AI assistance, the project provides one organized financial workspace. Its architecture also demonstrates how an initially simple CRUD application can grow through better domain modeling, security, caching, query optimization, deployment practices, and user-focused design.
