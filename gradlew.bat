@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

SET APP_HOME=%~dp0
SET WRAPPER_DIR=%APP_HOME%gradle\wrapper
SET WRAPPER_JAR=%WRAPPER_DIR%\gradle-wrapper.jar
SET WRAPPER_PROPS=%WRAPPER_DIR%\gradle-wrapper.properties

IF EXIST "%WRAPPER_JAR%" GOTO RUN

IF NOT EXIST "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
FOR /F "tokens=2 delims==-" %%A IN ('findstr /R /C:"distributionUrl=.*gradle-[0-9.]*-bin.zip" "%WRAPPER_PROPS%"') DO SET GRADLE_VERSION=%%A
IF "%GRADLE_VERSION%"=="" (
  ECHO Could not determine Gradle version from %WRAPPER_PROPS%
  EXIT /B 1
)

SET DOWNLOAD_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-wrapper.jar
ECHO Downloading missing Gradle wrapper jar for Gradle %GRADLE_VERSION%...

powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing '%DOWNLOAD_URL%' -OutFile '%WRAPPER_JAR%'"
IF ERRORLEVEL 1 (
  ECHO Failed to download %DOWNLOAD_URL%
  EXIT /B 1
)

:RUN
IF DEFINED JAVA_HOME (
  SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
) ELSE (
  SET JAVA_EXE=java.exe
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
ENDLOCAL
