$env:SPRING_PROFILES_ACTIVE = "local"
$env:LOCAL_DB_URL = "jdbc:postgresql://localhost:5439/moneymanager"
$env:LOCAL_DB_USERNAME = "postgres"
$env:LOCAL_DB_PASSWORD = "postgres"
$env:SPRING_DATASOURCE_URL = $env:LOCAL_DB_URL
$env:SPRING_DATASOURCE_USERNAME = $env:LOCAL_DB_USERNAME
$env:SPRING_DATASOURCE_PASSWORD = $env:LOCAL_DB_PASSWORD
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
mvn spring-boot:run *> backend-run.local.log
