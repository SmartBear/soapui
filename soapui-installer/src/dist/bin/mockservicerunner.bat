@echo off
SETLOCAL
set MAIN_CLASS=com.eviware.soapui.tools.SoapUIMockServiceRunner
set SOAPUI_RUNNER_NAME=soapui mock service runner
call %~dp0baserunner.bat %*
