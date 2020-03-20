@echo off
SETLOCAL
set MAIN_CLASS=com.eviware.soapui.tools.SoapUIMockAsWarGenerator
set SOAPUI_RUNNER_NAME=soapui war generator
call %~dp0baserunner.bat %*
