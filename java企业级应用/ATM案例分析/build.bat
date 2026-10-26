@echo off
rem ============================================================
rem  ATM Case Study - build script
rem  Compiles all sources (from the entry class) to the out dir.
rem  NOTE: source files are UTF-8, so -encoding UTF-8 is required.
rem ============================================================
chcp 65001 >nul
cd /d %~dp0
if not exist out mkdir out

javac -encoding UTF-8 -d out -sourcepath src src\com\atm\AtmApplication.java

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please check the messages above.
    exit /b 1
)
echo [OK] Compilation finished. Class files are in the out directory.
