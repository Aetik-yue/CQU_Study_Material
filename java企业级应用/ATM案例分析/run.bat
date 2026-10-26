@echo off
rem ============================================================
rem  ATM Case Study - run script
rem  Run build.bat first to compile.
rem ============================================================
chcp 65001 >nul
cd /d %~dp0
if not exist out\com\atm\AtmApplication.class (
    echo [ERROR] Compiled classes not found. Please run build.bat first.
    exit /b 1
)
java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp out com.atm.AtmApplication
