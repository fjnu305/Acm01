#Requires -RunAsAdministrator
# Start MySQL80 and initialize acm database for local dev.
$ErrorActionPreference = 'Stop'

$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$dbDir = Join-Path (Split-Path $PSScriptRoot -Parent) 'src\main\resources\db'
$password = '123456'

Write-Host '========================================'
Write-Host ' ACMer: start MySQL80 and init database'
Write-Host '========================================'

if (-not (Test-Path $mysql)) {
    Write-Error "MySQL not found: $mysql"
    exit 1
}

$service = Get-Service MySQL80 -ErrorAction SilentlyContinue
if (-not $service) {
    Write-Error 'MySQL80 service not found.'
    exit 1
}

if ($service.Status -ne 'Running') {
    Write-Host 'Starting MySQL80...'
    Start-Service MySQL80
    Start-Sleep -Seconds 3
}

if ((Get-Service MySQL80).Status -ne 'Running') {
    Write-Error 'MySQL80 failed to start.'
    exit 1
}

Write-Host 'OK: MySQL80 is running.'
Write-Host 'Creating database acm...'
& $mysql -uroot "-p$password" -e 'CREATE DATABASE IF NOT EXISTS acm DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;'

$scripts = @(
    'init-user.sql',
    'init-rbac.sql',
    'init-roles.sql',
    'init-contest.sql',
    'init-contest-crawl.sql',
    'init-subscription.sql',
    'init-social.sql',
    'init-solution.sql',
    'init-team.sql',
    'init-oj-sync.sql'
)

foreach ($name in $scripts) {
    $path = Join-Path $dbDir $name
    if (-not (Test-Path $path)) {
        Write-Warning "Skip missing: $name"
        continue
    }
    Write-Host "Running $name ..."
    Get-Content -Raw -Encoding UTF8 $path | & $mysql -uroot "-p$password" acm
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed: $name"
        exit $LASTEXITCODE
    }
}

Write-Host '========================================'
Write-Host ' Done. Restart backend, refresh browser.'
Write-Host ' Frontend: http://localhost:5173'
Write-Host ' Backend:  http://localhost:8080'
Write-Host '========================================'
