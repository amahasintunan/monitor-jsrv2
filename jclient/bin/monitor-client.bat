@echo off
REM File. monitor_client2.bat
REM Date. 06/23/2026
REM Description.
REM       Multi-Protocol Client for Linux Performance Monitor (Windows).
REM       Supports UDP, TCP, HTTP, and gRPC transports.
REM       Examples:
REM         monitor_client2.bat --host=localhost --port=2019 --protocol=udp
REM         monitor_client2.bat --host=localhost --port=2019 --protocol=tcp
REM         monitor_client2.bat --host=localhost --port=2019 --protocol=http
REM         monitor_client2.bat --host=localhost --port=2019 --protocol=grpc

setlocal enabledelayedexpansion
cd /d "%~dp0"

SET INI_FILE=monitor_client.ini
for /f "tokens=1,* delims==" %%a in (%INI_FILE%) do (
    if "%%a"=="JAVA_HOME" set JAVA_HOME=%%b
)

if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set. Check %INI_FILE%
    exit /b 1
)
if not exist "%JAVA_HOME%" (
    echo ERROR: "%JAVA_HOME%" directory does not exist!
    exit /b 1
)

set PATH=%JAVA_HOME%\bin;%PATH%
set LOOK_AND_FEEL=-Dswing.defaultlaf=javax.swing.plaf.nimbus.NimbusLookAndFeel

SET JAR_FILE=monitor_client.jar
if not exist "%JAR_FILE%" set JAR_FILE=target\%JAR_FILE%
if not exist "%JAR_FILE%" (
    echo ERROR: Cannot find monitor_client.jar
    echo Run build.bat first to compile the project.
    exit /b 1
)

SET CP=%JAR_FILE%;lib\*
java -Xms128m -Xmx128m %LOOK_AND_FEEL% -cp "%CP%" MonitorClient %*
if %ERRORLEVEL% == 127 (
    echo The specified JAVA path does not exist. Check the setting of JAVA_HOME in the script.
    exit /b 1
)
