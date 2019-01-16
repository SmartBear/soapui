@echo off
SETLOCAL
set MAIN_CLASS=com.eviware.soapui.tools.SoapUITestCaseRunner
set SOAPUI_RUNNER_NAME=soapui testcase runner
call %~dp0baserunner.bat %*
