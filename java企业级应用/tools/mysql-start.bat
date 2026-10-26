@echo off
rem ============================================================
rem  Start the portable MySQL 8 server (minimized window).
rem  Change MYSQL_HOME if your MySQL is installed elsewhere.
rem ============================================================
setlocal
set "MYSQL_HOME=C:\Users\yanha\.tools\mysql-8.4.11-winx64"

netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul
if not errorlevel 1 (
    echo [OK] MySQL is already running on port 3306.
    ping -n 4 127.0.0.1 >nul
    exit /b 0
)

echo [INFO] Starting MySQL 8 ...
start "MySQL 8" /min "%MYSQL_HOME%\bin\mysqld.exe" --defaults-file="%MYSQL_HOME%\my.ini"
set /a WAIT=0
:wait_loop
ping -n 2 127.0.0.1 >nul
netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul
if not errorlevel 1 goto ready
set /a WAIT+=1
if %WAIT% lss 30 goto wait_loop
echo [WARN] MySQL did not start within 30s. See %MYSQL_HOME%\data\mysqld.err
exit /b 1

:ready
echo [OK] MySQL is listening on port 3306.
ping -n 4 127.0.0.1 >nul
endlocal
