@ECHO off
SETLOCAL ENABLEDELAYEDEXPANSION

SET JARFILE=soapui-4.5.2.jar

:::::::::::::::::::::::::::::::::
:: testrunner.bat v.2.0
:: for SoapUI
:: pre-configuration and execution script for SoapUITestCaseRunner
::
:::::::::::::::::::::::::::::::::

TITLE Script %~nx0 running from %~dp0
ECHO Args passed to %~n0%~x0 :
FOR %%I IN (%*) DO ECHO %%I
ECHO.

:: check if configured jar file exists
IF NOT EXIST "%SOAPUI_HOME%\bin\%JARFILE%" (
  ECHO The JARFILE variable configured in this script is not pointing to an existing jar file:
  ECHO     ^"%SOAPUI_HOME%\bin\%JARFILE%^"
  GOTO :ERROR
)

:: Set SOAPUI_HOME and make sure script runs from bin directory.
CALL :getparentfolder bin

IF EXIST "%SOAPUI_HOME%\jre\bin" (
  ECHO Using embedded version of Java at ^"%SOAPUI_HOME%\jre\bin\java.exe^".
  SET "JAVA=%SOAPUI_HOME%\jre\bin\java.exe"
) ELSE (
  IF NOT DEFINED JAVA_HOME (
    ECHO JAVA_HOME is not set, unexpected results may occur with %~n0%~x0 .
    ECHO Set JAVA_HOME to the directory of your local JDK to avoid this message.
    SET JAVA=java.exe
  ) ELSE (
    ECHO Using Java defined by JAVA_HOME.
    SET "JAVA=%JAVA_HOME%\bin\java.exe"
  )
)
ECHO.

:: Initialize classpath var with optional PRE_CLASSPATH var prepended testcaserunner
:: class will auto load this var implicitly without need to pass as arg.
IF NOT DEFINED CLASSPATH SET CLASSPATH=.
IF DEFINED PRE_CLASSPATH SET "CLASSPATH=%PRE_CLASSPATH%;%CLASSPATH%"
SET "CLASSPATH=%CLASSPATH%;%SOAPUI_HOME%\bin\%JARFILE%;%SOAPUI_HOME%\lib\*"

:: JVM parameters, modify as appropriate
SET JAVA_OPTS=-Xms128m -Xmx1024m -D^"soapui.properties=%BIN_HOME%\soapui.properties^" -D^"soapui.home=%BIN_HOME%^"
SET JAVA_OPTS=%JAVA_OPTS% -D^"soapui.ext.libraries=%SOAPUI_HOME%\bin\ext^"
SET JAVA_OPTS=%JAVA_OPTS% -D^"soapui.ext.listeners=%SOAPUI_HOME%\bin\listeners^"
SET JAVA_OPTS=%JAVA_OPTS% -D^"soapui.ext.actions=%SOAPUI_HOME%\bin\actions^"

:START
pause
ECHO ::::::::::::::::::::::::::::::::::::::::
ECHO :: Running soapui testcase runner...
ECHO :: Implicit classpath: %CLASSPATH%
ECHO :: Java opts: %JAVA_OPTS%
ECHO ::::::::::::::::::::::::::::::::::::::::
ECHO.
"%JAVA%" %JAVA_OPTS% com.eviware.soapui.tools.SoapUITestCaseRunner %*

GOTO :END

:: Function to get parent folder name of scripts currrent folder.
:: Function requires current folders name as an arg or it will fail to run.
:getparentfolder dirName
SET "BIN_HOME=%~dp0"
IF "%BIN_HOME:~-1%"=="\" SET "BIN_HOME=%BIN_HOME:~0,-1%"
ECHO Current directory is: %BIN_HOME%
FOR /F "delims=" %%I IN ("%BIN_HOME%") DO (
  SET THISFOLDER=%%~nI
)
ECHO This folder name is ^"%THISFOLDER%^".
IF "%~1"=="%THISFOLDER%" (
  SET SOAPUI_HOME=!BIN_HOME:\%THISFOLDER%=!
  ECHO SOAPUI_HOME: %SOAPUI_HOME%
) ELSE (
  ECHO Function arg ^"%~1^" must match actual folder name.
  ECHO This script may not be running from the expected folder.
  GOTO :ERROR
)
EXIT /B 0

:ERROR
ECHO There was an error in %~nx0
PING.exe -n 10 -w 1 127.0.0.1>nul

:END
ECHO The script %~n0%~x0 is finished.
