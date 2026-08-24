# Initialize acm database (MySQL80 must already be running).
$ErrorActionPreference = 'Stop'

$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$dbDir = Join-Path (Split-Path $PSScriptRoot -Parent) 'src\main\resources\db'
$password = '123456'

if (-not (Test-Path $mysql)) {
    Write-Error "MySQL not found: $mysql"
    exit 1
}

$service = Get-Service MySQL80 -ErrorAction SilentlyContinue
if ($service -and $service.Status -ne 'Running') {
    Write-Error 'MySQL80 is not running. Run start-mysql-admin.ps1 as Administrator.'
    exit 1
}

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
    'init-oj-sync.sql',
    'init-friend-inbox.sql'
)

foreach ($name in $scripts) {
    $path = Join-Path $dbDir $name
    if (-not (Test-Path $path)) {
        Write-Warning "Skip missing: $name"
        continue
    }
    Write-Host "Running $name ..."
    Get-Content -Raw -Encoding UTF8 $path | & $mysql -uroot "-p$password" --default-character-set=utf8mb4 acm
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed: $name"
        exit $LASTEXITCODE
    }
}

Write-Host 'Database init complete.'
