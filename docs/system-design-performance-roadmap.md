# System Design and Performance Roadmap

This note explains how to make Money Manager faster, cheaper to run, and easier to scale. It also lists features that can separate free users from pro users.

## Main Goals

- Make dashboard, money plan, family, and friends pages load faster.
- Reduce repeated database queries.
- Reduce expensive AI calls.
- Keep the app usable for free users.
- Add premium features that real users would pay for.
- Prepare the project for production-level traffic later.

## 1. Redis: Where To Use It

Redis is best for data that is read many times, changes often enough that database queries become costly, but does not need permanent storage in Redis.

### Best Redis Use Cases In This App

| Area | What To Cache | Why It Helps |
| --- | --- | --- |
| Dashboard | Total income, total expense, balance, recent transactions | Dashboard is opened often and currently needs multiple database reads |
| Money Plan | Monthly summary, budget progress, category breakdown, cashflow forecast | These calculations can be expensive when transactions grow |
| AI Advisor | Last AI advice and insights per user/month | Avoid calling OpenAI repeatedly for same data |
| Family Dashboard | Family total spending, member-wise spending, allowances | Family pages aggregate many records |
| Friends Dashboard | Net balances, who owes whom, group summary | Split calculations become expensive with many shared expenses |
| Categories | User categories by type | Categories are read often and change rarely |
| Auth | Optional JWT denylist for logout/revoked tokens | Useful when adding real logout/session control |
| Rate Limiting | Request counter per user/IP | Protect free tier and AI endpoints from abuse |

### Redis Cache Keys

Use simple predictable keys:

```text
dashboard:{profileId}:{month}:{year}
money-plan:{profileId}:{month}:{year}
ai-insights:{profileId}:{month}:{year}
family-dashboard:{profileId}:{familyId}:{month}:{year}
friends-dashboard:{profileId}:{month}:{year}
categories:{profileId}:{type}
rate-limit:{profileId}:{endpoint}
```

### Cache Expiry Suggestions

| Data | Expiry |
| --- | --- |
| Dashboard summary | 5 to 15 minutes |
| Money plan summary | 5 to 15 minutes |
| AI insights | 12 to 24 hours |
| Categories | 30 to 60 minutes |
| Family dashboard | 5 to 15 minutes |
| Friends balances | 5 to 15 minutes |
| Rate limits | 1 minute, 1 hour, or 1 day depending on rule |

### Cache Invalidation Rules

Clear related Redis cache when data changes:

| Action | Clear Cache |
| --- | --- |
| Add/edit/delete income | Dashboard, analytics, money plan |
| Add/edit/delete expense | Dashboard, analytics, money plan, budget alerts |
| Add/edit/delete category | Category cache and related dashboards |
| Add savings contribution | Money plan and savings summary |
| Add recurring transaction | Money plan and forecast |
| Process due recurring transaction | Dashboard, money plan, analytics |
| Add family transfer | Family dashboard |
| Add shared expense | Friends dashboard, group summary, balances |
| Settle up | Friends dashboard, settlements, balances |

### Redis Implementation Order

1. Cache dashboard summary.
2. Cache money plan summary.
3. Cache friends dashboard balances.
4. Cache AI insights.
5. Add rate limiting for AI and auth endpoints.

## 2. Database Indexing

Indexes make reads faster when tables become large. Add indexes based on the filters and joins used by the app.

### Profile/User Tables

```sql
CREATE UNIQUE INDEX idx_profiles_email ON profiles(email);
```

Why:

- Login searches by email.
- Register checks if email already exists.

### Income Table

```sql
CREATE INDEX idx_income_profile_date ON incomes(profile_id, date);
CREATE INDEX idx_income_profile_category ON incomes(profile_id, category_id);
CREATE INDEX idx_income_profile_payment_method ON incomes(profile_id, payment_method);
```

Why:

- Dashboard filters income by user and month.
- Filters page searches by category/date.
- Analytics may group by payment method later.

### Expense Table

```sql
CREATE INDEX idx_expense_profile_date ON expenses(profile_id, date);
CREATE INDEX idx_expense_profile_category ON expenses(profile_id, category_id);
CREATE INDEX idx_expense_profile_payment_method ON expenses(profile_id, payment_method);
CREATE INDEX idx_expense_profile_family_member ON expenses(profile_id, family_member_id);
```

Why:

- Dashboard and analytics read expenses by month.
- Budget progress needs category-wise expense totals.
- Family dashboard needs member-wise spending.

### Category Table

```sql
CREATE INDEX idx_category_profile_type ON categories(profile_id, type);
CREATE UNIQUE INDEX idx_category_profile_name_type ON categories(profile_id, name, type);
```

Why:

- Category dropdowns load by income/expense type.
- Prevent duplicate category names for same user and type.

### Budget Table

```sql
CREATE INDEX idx_budget_profile_month_year ON budgets(profile_id, month, year);
CREATE UNIQUE INDEX idx_budget_profile_category_month_year ON budgets(profile_id, category_id, month, year);
```

Why:

- Money plan loads budgets by month/year.
- One budget per category per month should be unique.

### Savings Goal Tables

```sql
CREATE INDEX idx_savings_goal_profile_status ON savings_goals(profile_id, status);
CREATE INDEX idx_savings_contribution_goal_date ON savings_contributions(goal_id, contribution_date);
```

Why:

- Savings page loads active goals.
- Contribution history is shown by goal.

### Recurring Transactions

```sql
CREATE INDEX idx_recurring_profile_next_due ON recurring_transactions(profile_id, next_due_date);
CREATE INDEX idx_recurring_active_due ON recurring_transactions(active, next_due_date);
```

Why:

- Auto-create due transactions should quickly find due recurring items.

### Bill Reminders

```sql
CREATE INDEX idx_bill_profile_due_date ON bill_reminders(profile_id, due_date);
CREATE INDEX idx_bill_profile_status ON bill_reminders(profile_id, status);
```

Why:

- Bill reminder screen and due alerts depend on due date/status.

### Family Tables

```sql
CREATE INDEX idx_family_profile ON families(profile_id);
CREATE INDEX idx_family_member_family ON family_members(family_id);
CREATE INDEX idx_family_transfer_family_date ON family_transfers(family_id, transfer_date);
```

Why:

- Family dashboard loads members and transfers by family.

### Friends/Split Tables

```sql
CREATE INDEX idx_friend_profile_status ON friends(profile_id, status);
CREATE INDEX idx_friend_group_profile ON friend_groups(profile_id);
CREATE INDEX idx_shared_expense_profile_date ON shared_expenses(profile_id, expense_date);
CREATE INDEX idx_shared_expense_group ON shared_expenses(group_id);
CREATE INDEX idx_shared_split_friend ON shared_expense_splits(friend_id);
CREATE INDEX idx_settlement_profile_date ON friend_settlements(profile_id, settlement_date);
CREATE INDEX idx_reminder_profile_due ON friend_reminders(profile_id, due_date);
```

Why:

- Friends dashboard calculates balances and recent activity.
- Groups load shared expenses.
- Reminders need due-date search.

## 3. Backend Performance Concepts

### Use DTO Projections For Heavy Pages

Avoid loading full entities when only totals are needed.

Good examples:

- Dashboard summary query should directly return totals.
- Category breakdown should return `categoryName`, `totalAmount`, `percentage`.
- Friend balances should return calculated totals instead of loading all splits into Java.

### Use Pagination

Add pagination to lists that can grow:

- Income list
- Expense list
- Activity timeline
- Shared expenses
- Settlements
- Savings contribution history
- AI advice history

Example API style:

```text
GET /expenses?page=0&size=20&sort=date,desc
```

### Use Background Jobs

Move slow work out of direct user requests:

- Generate monthly AI report.
- Send email reports.
- Process recurring transactions.
- Send bill reminders.
- Send friend repayment reminders.

### Avoid N+1 Queries

Watch service methods that loop over records and call repositories inside the loop.

Better options:

- Use `JOIN FETCH` for required relationships.
- Use custom aggregate queries.
- Load related data in one query by IDs.

### Add API Rate Limits

Useful especially for free users:

- Login/register attempts.
- AI advice endpoint.
- Excel/email export.
- Reminder sending.

Redis is a good fit for rate limiting counters.

## 4. Frontend Performance Concepts

### Code Splitting

Lazy-load large pages:

- Dashboard
- Money Plan
- Family
- Friends
- AI Advisor

Example:

```jsx
const Dashboard = React.lazy(() => import("./components/Dashboard"));
```

### Cache API Data On Frontend

Use a library like TanStack Query later.

Benefits:

- Avoid repeated API calls.
- Automatic loading/error states.
- Background refresh.
- Easy cache invalidation after create/update/delete.

Best pages for this:

- Dashboard
- Category dropdowns
- Money plan
- Family dashboard
- Friends dashboard

### Debounce Search And Filters

For filters/search boxes, wait 300-500ms before calling backend.

Use this for:

- Transaction search
- Category search
- Friend search
- Group search

### Virtualize Long Lists

When the user has thousands of transactions, render only visible rows.

Use for:

- Expense list
- Income list
- Friend activity timeline
- Shared expense history

Possible library:

```text
react-window
```

### Reduce Large Re-renders

Use:

- `React.memo` for repeated cards.
- `useMemo` for calculated chart data.
- `useCallback` for handlers passed into child components.
- Smaller components for big pages.

### Optimize Charts

Charts can become slow when too much data is passed.

Do this:

- Aggregate data on backend.
- Send only chart-ready data.
- Limit calendar view to selected month.
- Avoid rendering huge raw transaction arrays in charts.

### Asset Optimization

- Use local icons from Lucide instead of external image URLs where possible.
- Compress large images.
- Lazy-load avatars/receipts.
- Use placeholders for missing avatars.

## 5. Cost Reduction Ideas

### Reduce AI Cost

- Cache AI advice per user/month in Redis or database.
- Do not call AI when financial data has not changed.
- Use smaller prompts.
- Send summarized totals, not full transaction lists.
- Limit free users to a small number of AI calls per month.
- Run AI reports in background instead of every page load.
- Use fallback local advice when OpenAI fails.

### Reduce Database Cost

- Add indexes.
- Cache heavy summaries.
- Use pagination.
- Avoid loading all user data at once.
- Archive very old transactions later.
- Use aggregate SQL queries for totals.

### Reduce Server Cost

- Make frontend static and host cheaply.
- Keep backend stateless so it can scale horizontally.
- Use scheduled jobs carefully.
- Use connection pooling.
- Add request limits.

### Reduce Email Cost

- Batch reminders.
- Let users choose reminder frequency.
- Limit email reports for free users.
- Use in-app notifications for free users.

## 6. Free vs Pro User Plan

### Free User Features

Free users should get enough value to use the app daily.

- Personal dashboard.
- Limited categories.
- Income and expense tracking.
- Monthly budget tracking.
- Basic savings goals.
- Basic recurring transactions.
- Limited friend expense splits.
- Limited family members.
- Basic charts.
- Manual export.
- Limited AI insights per month.

Suggested limits:

| Feature | Free Limit |
| --- | --- |
| Categories | 10 to 15 |
| Monthly transactions | 100 to 200 |
| Savings goals | 2 |
| Budgets | 5 per month |
| Family members | 3 |
| Friends | 5 |
| Groups | 2 |
| Shared expenses | 20 per month |
| AI insights | 3 per month |
| Exports | Manual only |

### Pro User Features

Pro users should get automation, deeper analytics, and fewer limits.

- Unlimited transactions.
- Unlimited categories.
- Unlimited budgets.
- Unlimited savings goals.
- Unlimited friends and groups.
- Advanced split options.
- Bill/subscription reminders.
- Auto-created recurring transactions.
- Full family money workspace.
- Advanced cashflow forecast.
- Monthly AI report.
- Smart budget alerts.
- Receipt attachments.
- Export to Excel/PDF.
- Email reports.
- Multi-device sync.
- Priority support.

### Good Pro Feature Ideas

| Feature | Why Users Pay |
| --- | --- |
| AI monthly report | Gives personalized financial advice |
| Unlimited friend splits | Useful for roommates, trips, groups |
| Family dashboard | Helps households manage shared money |
| Receipt attachments | Useful for records and proof |
| Advanced analytics | Shows deeper spending patterns |
| Cashflow forecast | Helps avoid running out of money |
| Auto reminders | Saves manual follow-up |
| Export reports | Useful for tax, records, and planning |

## 7. Production Architecture Later

Recommended future architecture:

```text
React Frontend
    |
CDN / Static Hosting
    |
Spring Boot API
    |
+--------------------+
| PostgreSQL         |
| Redis              |
| Object Storage     |
| Email Provider     |
| OpenAI / AI API    |
+--------------------+
```

### Production Components

- CDN for frontend static files.
- Load balancer before backend.
- PostgreSQL for permanent data.
- Redis for cache, rate limiting, and temporary session data.
- Object storage for receipts and attachments.
- Background worker for AI reports, emails, reminders, and recurring jobs.
- Monitoring with logs, metrics, and alerts.

## 8. Best Implementation Order

Follow this order so improvements are useful and not over-engineered:

1. Add database indexes for existing tables.
2. Add pagination for income, expenses, shared expenses, and activity timeline.
3. Cache dashboard summary in Redis.
4. Cache money plan summary in Redis.
5. Cache friends balances in Redis.
6. Cache AI insights and add AI rate limits.
7. Add frontend code splitting.
8. Add frontend API caching with TanStack Query.
9. Add background jobs for reminders and recurring transactions.
10. Add free/pro feature limits.
11. Add pro billing later.

## 9. What To Avoid For Now

- Do not add microservices yet. The app is still better as one Spring Boot backend.
- Do not cache everything. Cache only expensive and repeated reads.
- Do not add Redis before adding basic indexes.
- Do not call AI on every page refresh.
- Do not load all transactions on dashboard.
- Do not make every feature pro-only. Free users need a useful product.

## 10. Simple Next Tasks

These are practical next coding tasks:

- Add indexes using JPA `@Table(indexes = ...)` or database migration.
- Add `Pageable` support to income and expense APIs.
- Add Redis dependency and cache dashboard summary.
- Add cache invalidation after transaction create/update/delete.
- Add AI request limit for free users.
- Add lazy route loading in React.
- Add TanStack Query for API state.
- Add a `planType` field to user profile: `FREE` or `PRO`.
- Add usage counters for AI, exports, friends, groups, and transactions.
