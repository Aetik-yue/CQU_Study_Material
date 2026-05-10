@echo off
setlocal

gcc -O2 -Wall -Wextra -S softfloat_ieee754.c -o softfloat_ieee754.s
if errorlevel 1 exit /b 1

gcc -O2 -Wall -Wextra softfloat_ieee754.s -o softfloat_ieee754.exe
if errorlevel 1 exit /b 1

gcc -O2 -Wall -Wextra -S softfloat_gui.c -o softfloat_gui.s -mwindows
if errorlevel 1 exit /b 1

gcc -O2 -Wall -Wextra softfloat_gui.s -o softfloat_gui.exe -mwindows -luser32 -lgdi32
if errorlevel 1 exit /b 1

echo Build OK: softfloat_ieee754.exe softfloat_gui.exe
