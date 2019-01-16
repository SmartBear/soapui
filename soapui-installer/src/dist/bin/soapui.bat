@echo off
SETLOCAL
set MAIN_CLASS=com.eviware.soapui.SoapUI
set SOAPUI_RUNNER_NAME=soapui
set JAVA_OPTS=%JAVA_OPTS% -splash:SoapUI-Spashscreen.png
set OLDDIR=%CD%
set SCRIPTDIR=%~dp0
cd /d %SCRIPTDIR%
call %SCRIPTDIR%baserunner.bat %*
cd /d %OLDDIR%
