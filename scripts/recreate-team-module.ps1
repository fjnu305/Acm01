# 开发环境：重建组队模块表（未上线可直接执行）
$ErrorActionPreference = 'Stop'
$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$dbDir = Join-Path (Split-Path $PSScriptRoot -Parent) 'src\main\resources\db'
$password = '123456'

if (-not (Test-Path $mysql)) {
    Write-Error "MySQL not found: $mysql"
    exit 1
}

Write-Host 'Recreating team module tables...'
$sql = @"
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS match_record;
DROP TABLE IF EXISTS team_member_log;
DROP TABLE IF EXISTS team_member;
DROP TABLE IF EXISTS team_post;
SET FOREIGN_KEY_CHECKS = 1;
"@
$sql | & $mysql -uroot "-p$password" acm
Get-Content -Raw -Encoding UTF8 (Join-Path $dbDir 'init-team.sql') | & $mysql -uroot "-p$password" --default-character-set=utf8mb4 acm
Write-Host 'Team module recreated.'
