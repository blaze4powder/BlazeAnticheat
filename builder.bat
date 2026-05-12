@echo off
echo Building BlazeAnticheat...
call gradlew.bat build
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Build successful!
pause
