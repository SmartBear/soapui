@echo off
SETLOCAL
set MAIN_CLASS=com.eviware.soapui.tools.SoapUILoadTestRunner
set SOAPUI_RUNNER_NAME=soapui loadtest runner
call %~dp0baserunner.bat %*
