@echo off
chcp 65001 >nul
echo Installing doc-reader globally...

:: Create directory in user's bin folder
if not exist "%USERPROFILE%\bin" mkdir "%USERPROFILE%\bin"

:: Copy the main script
copy /Y "%~dp0bin\doc-reader.js" "%USERPROFILE%\bin\doc-reader.js" >nul

:: Create batch file
echo @echo off > "%USERPROFILE%\bin\doc-reader.bat"
echo node "%%USERPROFILE%%\bin\doc-reader.js" %%* >> "%USERPROFILE%\bin\doc-reader.bat"

:: Add to PATH if not already there
echo Checking PATH...
set "BIN_PATH=%USERPROFILE%\bin"
echo %PATH% | find /i "%BIN_PATH%" >nul
if errorlevel 1 (
    echo Adding %BIN_PATH% to PATH...
    setx PATH "%PATH%;%BIN_PATH%" >nul 2>&1
    echo Please restart your terminal to use the command.
) else (
    echo PATH already configured.
)

echo.
echo Installation complete!
echo You can now use: doc-reader ^<file.doc^> [options]
echo.
echo Try: doc-reader --help
pause
