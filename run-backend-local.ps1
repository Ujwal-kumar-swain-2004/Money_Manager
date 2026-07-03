$env:SPRING_PROFILES_ACTIVE = "local"
$env:DB_URL = "jdbc:postgresql://localhost:5439/moneymanager"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
$env:JWT_SECRET = "supersecretjwtkeythatislongenoughformyapptobesecure12345"
$env:PORT = "8080"
$env:BREVO_SMTP_LOGIN = "placeholder_login"
$env:BREVO_SMTP_KEY = "placeholder_key"
$env:BREVO_FROM_EMAIL = "noreply@moneymanager.com"
$env:FRONTEND_URL = "http://localhost:5173"
$env:BACKEND_URL = "http://localhost:8080"
if (-not $env:OPENAI_API_KEY) {
    $env:OPENAI_API_KEY = [Environment]::GetEnvironmentVariable("OPENAI_API_KEY", "User")
}

Set-Location "$PSScriptRoot\Money_Manager-main\moneymanager"
mvn spring-boot:run *> backend-run.log
