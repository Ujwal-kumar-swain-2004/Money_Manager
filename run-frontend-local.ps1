$env:VITE_API_URL = "http://localhost:8080/api/v1.0"

Set-Location "$PSScriptRoot\MoneyManager_Frontend-main"
npm run dev -- --host 0.0.0.0 *> frontend-run.log
