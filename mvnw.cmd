@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven start up batch script
@REM ----------------------------------------------------------------------------

@REM Begin all REM://scriptlet
@echo off

@REM Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%~dp0\.mvn\wrapper\maven-wrapper.properties"
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@REM Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
@REM This allows using the maven wrapper in projects that prohibit checking in binary data.
if exist %WRAPPER_JAR% (
    if "%MVNW_VERBOSE%" == "true" (
        echo Found %WRAPPER_JAR%
    )
) else (
    if not "%MVNW_REPOURL%" == "" (
        SET WRAPPER_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    )
    if "%MVNW_VERBOSE%" == "true" (
        echo Couldn't find %WRAPPER_JAR%, downloading it ...
        echo Downloading from: %WRAPPER_URL%
    )

    powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"^
        "}"
    if "%MVNW_VERBOSE%" == "true" (
        echo Finished downloading %WRAPPER_JAR%
    )
)

@REM Provide a "standardized" way to retrieve the CLI args that will
@REM work with both Windows and non-Windows executions.
set MAVEN_CMD_LINE_ARGS=%*

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.

set MAVEN_PROJECTBASEDIR=%~dp0
@REM Remove trailing backslash
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set MAVEN_OPTS="-Xmx512m"

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="distributionUrl" SET MVNW_DIST_URL=%%B
)

@REM Check if JAVA_HOME is set
if not "%JAVA_HOME%" == "" goto foundJavaHome

@REM Try to find java.exe in PATH
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
if not "%JAVACMD%" == "" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
goto error

:foundJavaHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"
if exist "%JAVACMD%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
goto error

:init
@REM Download Maven distribution if wrapper jar doesn't exist
if not exist %WRAPPER_JAR% (
    echo Maven Wrapper jar not found. Downloading Maven directly...
    
    set "MAVEN_HOME=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-dist"
    
    if not exist "%MAVEN_HOME%" (
        mkdir "%MAVEN_HOME%"
    )
    
    @REM Download and extract Maven
    powershell -Command "&{"^
        "$ProgressPreference = 'SilentlyContinue';"^
        "$url = '%MVNW_DIST_URL%';"^
        "$zipFile = '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven.zip';"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;"^
        "(New-Object System.Net.WebClient).DownloadFile($url, $zipFile);"^
        "Expand-Archive -Path $zipFile -DestinationPath '%MAVEN_HOME%' -Force;"^
        "Remove-Item $zipFile -Force"^
        "}"
    
    @REM Find the extracted maven directory
    for /D %%d in ("%MAVEN_HOME%\apache-maven-*") do set "MAVEN_DIST=%%d"
    
    @REM Run Maven directly
    "%MAVEN_DIST%\bin\mvn.cmd" %MAVEN_CMD_LINE_ARGS%
    if ERRORLEVEL 1 goto error
    goto end
)

"%JAVACMD%" ^
  %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

cmd /C exit /B %ERROR_CODE%
