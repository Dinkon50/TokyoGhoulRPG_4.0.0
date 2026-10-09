@echo off
setlocal EnableExtensions
cd /d "%~dp0"

rem Prefer Java 17 for Forge 1.20.1. If Temurin/Oracle 17 is installed elsewhere, set JAVA_HOME manually.
if exist "C:\Program Files\Java\jdk-17.0.3\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-17.0.3"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ================================================
echo Tokyo Ghoul RPG - Forge 1.20.1 build
echo ================================================
echo.

where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java was not found in PATH.
  echo Install Java 17 and reopen this window.
  pause
  exit /b 1
)

java -version
if errorlevel 1 goto :java_error

echo.
echo Checking Gradle...
if exist "%~dp0.gradle-dist\gradle-8.8\bin\gradle.bat" goto :build

if not exist "%~dp0.gradle-dist" mkdir "%~dp0.gradle-dist"
if exist "%~dp0.gradle-dist\gradle-8.8-bin.zip" del /q "%~dp0.gradle-dist\gradle-8.8-bin.zip"

echo Downloading Gradle 8.8. This may take a few minutes...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-8.8-bin.zip' -OutFile '%~dp0.gradle-dist\gradle-8.8-bin.zip'"
if errorlevel 1 goto :download_error

echo Extracting Gradle...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%~dp0.gradle-dist\gradle-8.8-bin.zip' -DestinationPath '%~dp0.gradle-dist' -Force"
if errorlevel 1 goto :extract_error

:build
set "GRADLE_HOME=%~dp0.gradle-dist\gradle-8.8"
call "%GRADLE_HOME%\bin\gradle.bat" --no-daemon build
if errorlevel 1 goto :build_error

if not exist "%~dp0build\libs\tokyoghoulrpg-4.0.0.jar" goto :jar_error

echo.
echo ================================================
echo BUILD SUCCESSFUL
echo JAR:
echo %~dp0build\libs\tokyoghoulrpg-4.0.0.jar
echo ================================================
pause
exit /b 0

:java_error
echo ERROR: Java could not be started.
pause
exit /b 1
:download_error
echo ERROR: Could not download Gradle. Check internet access.
pause
exit /b 1
:extract_error
echo ERROR: Could not extract Gradle.
pause
exit /b 1
:build_error
echo.
echo BUILD FAILED. The full Gradle error is above this message.
pause
exit /b 1
:jar_error
echo ERROR: Gradle reported success but the expected JAR was not created.
pause
exit /b 1
