@echo off
SETLOCAL
set MAIN_CLASS=com.eviware.soapui.tools.SoapUIToolRunner
set SOAPUI_RUNNER_NAME=soapui toolrunner
call %~dp0baserunner.bat %*
