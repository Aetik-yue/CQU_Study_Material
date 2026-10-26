@echo off
rem ============================================================
rem  rbac-cli build script
rem  Package the project into a runnable fat jar:
rem    target\rbac-cli.jar
rem  Requirement: JDK 17+ and Maven 3.8+ in PATH.
rem ============================================================
setlocal
chcp 65001 >nul
cd /d %~dp0

where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Maven not found in PATH. Please install Maven 3.8+ first.
    pause
    exit /b 1
)

echo [INFO] Building rbac-cli (mvn clean package) ...
call mvn -B -DskipTests clean package
if errorlevel 1 (
    echo [ERROR] Build failed. Please check the messages above.
    pause
    exit /b 1
)

echo.
echo [OK] Build finished. Jar file: target\rbac-cli.jar
pause
endlocal
