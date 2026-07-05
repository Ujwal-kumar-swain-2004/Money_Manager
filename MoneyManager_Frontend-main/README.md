# Money Manager Frontend

React/Vite frontend for the Money Manager application.

## Run Locally

From the repository root:

```powershell
.\run-frontend-local.ps1
```

Or from this folder:

```powershell
npm install
npm run dev
```

Local URL:

```text
http://localhost:5173
```

The frontend expects the backend at:

```text
http://localhost:8080/api/v1.0
```

Set `VITE_API_URL` if you need a different backend URL.

## Main Pages

- Dashboard
- Categories
- Income and expenses
- Money Plan
- Family money manager
- Friends and split expenses
- AI Advisor with persistent per-user chat history

## Build

```powershell
npm run build
```
