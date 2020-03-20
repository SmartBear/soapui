@echo off
SETLOCAL
set MAIN_CLASS=com.eviware.soapui.tools.SoapUISecurityTestRunner
set SOAPUI_RUNNER_NAME=soapui security test runner
call %~dp0baserunner.bat %*
