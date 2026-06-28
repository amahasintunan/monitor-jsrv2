@echo off
REM File.   build.bat
REM Date.   06/23/2026
REM Description.
REM         Build the multi-protocol Linux Performance Monitor Client (Windows)

setlocal enabledelayedexpansion

if exist "..\monitor_client.ini" (
    for /f "tokens=1,* delims==" %%a in (..\monitor_client.ini) do (
        set %%a=%%b
    )
)

if exist "monitor_client.ini" (
    for /f "tokens=1,* delims==" %%a in (monitor_client.ini) do (
        set %%a=%%b
    )
)

if "%JAVA_HOME%"=="" (
    echo JAVA_HOME is not set. Please check monitor_client.ini
    exit /b 1
)

set PATH=%JAVA_HOME%\bin;%PATH%

echo Building pmon-client2-mp...
call mvn clean package

echo.
echo Build complete. JAR: target\monitor_client.jar
echo Run: monitor_client2.bat --help
