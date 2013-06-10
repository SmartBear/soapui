@ECHO off
SETLOCAL ENABLEDELAYEDEXPANSION
TITLE Script %~nx0 running from %~dp0
ECHO Args passed to %~nx0 :
FOR %%I IN (%*) DO ECHO %%I
ECHO.
:::::::::::::::::::::::::::::::::
:: testrunner.bat
:: for SoapUI
:: pre-configuration and execution script for SoapUITestCaseRunner
:::::::::::::::::::::::::::::::::

SET ARTIFACT=${project.src.artifactId}
SET VERSION=${project.version}
:: SET JARFILE=soapui-4.5.2.jar
SET JARFILE=%ARTIFACT%-%VERSION%.jar

:: Set BIN_HOME to current directory
SET "BIN_HOME=%~dp0"
IF "%BIN_HOME:~-1%"=="\" SET "BIN_HOME=%BIN_HOME:~0,-1%"
FOR /F "delims=" %%I IN ("%BIN_HOME%") DO (
  SET THISFOLDER=%%~nI
)
ECHO Current directory is: %BIN_HOME%
ECHO Current folder name is: %THISFOLDER%

:: check if configured jar file exists
IF NOT EXIST "%BIN_HOME%\%JARFILE%" (
  ECHO The JARFILE variable configured in this script is not pointing to an existing jar file:
  ECHO     %BIN_HOME%\%JARFILE%
  GOTO :ERROR
)

:: Make sure script runs from bin directory.
IF "bin"=="%THISFOLDER%" (
  ECHO Satisfied folder check.
) ELSE (
  ECHO Function arg ^"%~1^" must match actual parent folder name.
  ECHO This script may not be running from the expected folder.
  GOTO :ERROR
)

:: Set SOAPUI_HOME var based on parent folder
IF DEFINED SOAPUI_HOME (
  ECHO The SOAPUI_HOME variable was explicitly defined:
  ECHO Defined SOAPUI_HOME: %SOAPUI_HOME%
)
SET SOAPUI_HOME=!BIN_HOME:\%THISFOLDER%=!
ECHO Determined SOAPUI_HOME: %SOAPUI_HOME%

IF NOT DEFINED JAVA_HOME (
  IF EXIST "%SOAPUI_HOME%\jre\bin" (
    ECHO Using embedded version of Java at ^"%SOAPUI_HOME%\jre\bin\java.exe^".
    SET "JAVA=%SOAPUI_HOME%\jre\bin\java.exe"
    SET JAVA=java.exe
  ) ELSE (
    ECHO JAVA_HOME is not set, unexpected results may occur with %~nx0 .
    ECHO Set JAVA_HOME to the directory of your local JDK to avoid this message.
    GOTO :ERROR
  )
) ELSE (
  ECHO Using Java defined by JAVA_HOME.
  ECHO JAVA_HOME=%JAVA_HOME%
  SET "JAVA=%JAVA_HOME%\bin\java.exe"
)

IF "%ARTIFACT%"=="soapui-pro" (
  SET CLASSNAME=com.eviware.soapui.SoapUIProTestCaseRunner
) ELSE (
  SET CLASSNAME=com.eviware.soapui.tools.SoapUITestCaseRunner
)

:::::::::::::::::::::::::::::::::
:: Ability to prepend CLASSPATH with libraries if you define PRE_CLASSPATH before
:: calling this script. Otherwise place libraries in the 'soapui.ext.libraries' directory.
:::::::::::::::::::::::::::::::::
IF NOT DEFINED CLASSPATH SET CLASSPATH=.
IF DEFINED PRE_CLASSPATH SET "CLASSPATH=.;%PRE_CLASSPATH%;%CLASSPATH%"
SET "CLASSPATH=%CLASSPATH%;%SOAPUI_HOME%\bin\%JARFILE%;%SOAPUI_HOME%\lib\*"

:::::::::::::::::::::::::::::::::
:: JVM parameters. Modify as desired.
:::::::::::::::::::::::::::::::::
SET "JAVA_OPTS=-Xms128m -Xmx1024m -Dsoapui.properties=%BIN_HOME%\soapui.properties"
SET "JAVA_OPTS=%JAVA_OPTS% -Dgroovy.source.encoding=iso-8859-1 -Dsoapui.home=%BIN_HOME%"
SET "JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.libraries=%SOAPUI_HOME%\bin\ext"
SET "JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.listeners=%SOAPUI_HOME%\bin\listeners"
SET "JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.actions=%SOAPUI_HOME%\bin\actions"

:::::::::::::::::::::::::::::::::
:: Start SoapUI.
:::::::::::::::::::::::::::::::::
ECHO ----------------------------------------
ECHO -- Running soapui testcase runner...
ECHO -- Implicit classpath: %CLASSPATH%
ECHO -- Java opts: %JAVA_OPTS%
ECHO ----------------------------------------
ECHO.

"%JAVA%" %JAVA_OPTS% %CLASSNAME% %*

GOTO :END

:ERROR
ECHO There was an error in the %~nx0 script.
PING.exe -n 10 -w 1000 127.0.0.1>NUL

:END
ECHO The script %~nx0 is finished.
ECHO.
