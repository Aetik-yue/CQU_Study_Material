@echo off
rem ============================================================
rem  rbac-cli run script
rem  1) make sure the jar is packaged (auto build if missing)
rem  2) make sure MySQL 8 is listening on port 3306
rem     (auto-start the portable MySQL when available)
rem  3) launch the CLI with UTF-8 console settings
rem
rem  NOTE: set MYSQL_HOME below to match your MySQL installation
rem        (only used when MySQL is not already running).
rem ============================================================
setlocal
chcp 65001 >nul
cd /d %~dp0
title RBAC Permission Management System
rem Prefer a 120-column window so wide tables (e.g. audit log) fit on one screen.
rem Harmless when unsupported (e.g. Windows Terminal keeps its own size).
mode con cols=120 lines=32 >nul 2>nul

set "MYSQL_HOME=C:\Users\yanha\.tools\mysql-8.4.11-winx64"

rem ---------- 1) jar ----------
if not exist "target\rbac-cli.jar" (
    echo [INFO] target\rbac-cli.jar not found, building with Maven ...
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo [ERROR] Maven not found. Please run build.bat first.
        pause
        exit /b 1
    )
    call mvn -B -q -DskipTests package
    if errorlevel 1 (
        echo [ERROR] Build failed. Please check the messages above.
        pause
        exit /b 1
    )
)

rem ---------- 2) MySQL ----------
netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul
if not errorlevel 1 goto mysql_ready
if not exist "%MYSQL_HOME%\bin\mysqld.exe" goto mysql_missing

echo [INFO] MySQL is not running, starting portable MySQL ...
start "MySQL" /min "%MYSQL_HOME%\bin\mysqld.exe" --defaults-file="%MYSQL_HOME%\my.ini"
set /a WAIT=0
:wait_loop
ping -n 2 127.0.0.1 >nul
netstat -ano | findstr /r /c:":3306 .*LISTENING" >nul
if not errorlevel 1 goto mysql_ready
set /a WAIT+=1
if %WAIT% lss 30 goto wait_loop
echo [WARN] MySQL did not start within 30s. Check %MYSQL_HOME%\data\mysqld.err
pause
exit /b 1

:mysql_missing
echo [WARN] MySQL is not listening on 3306 and no portable install found at:
echo        %MYSQL_HOME%
echo        Please start MySQL 8 manually, then run this script again.
pause
exit /b 1

rem ---------- 3) run ----------
:mysql_ready
java -Dstdin.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar target\rbac-cli.jar
echo.
pause
endlocal
