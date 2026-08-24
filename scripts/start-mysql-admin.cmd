@echo off
setlocal
echo ========================================
echo  ACMer: start MySQL80 and init database
echo  Run as Administrator (right-click script)
echo ========================================
echo.

net start MySQL80
if errorlevel 1 (
    echo FAILED to start MySQL80 service.
    echo Run this script as Administrator.
    echo Also check MySQL 8.0 is installed.
    pause
    exit /b 1
)

echo OK: MySQL80 is running.
echo.
echo Initializing acm database...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0init-local-db.ps1"
if errorlevel 1 (
    echo FAILED database init. Check root password is 123456 in application.yml
    pause
    exit /b 1
)

echo.
echo ========================================
echo  Done. Restart backend, then refresh browser.
echo  Frontend: http://localhost:5173
echo  Backend:  http://localhost:8080
echo ========================================
pause
