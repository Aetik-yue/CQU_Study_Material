@echo off
rem ============================================================
rem  Stop the portable MySQL 8 server (mysqladmin shutdown).
rem  Change MYSQL_HOME if your MySQL is installed elsewhere.
rem ============================================================
setlocal
set "MYSQL_HOME=C:\Users\yanha\.tools\mysql-8.4.11-winx64"

"%MYSQL_HOME%\bin\mysqladmin.exe" -u root -proot --port=3306 shutdown
if errorlevel 1 (
    echo [WARN] Shutdown command failed. Is MySQL running?
) else (
    echo [OK] MySQL has been stopped.
)
ping -n 4 127.0.0.1 >nul
endlocal
