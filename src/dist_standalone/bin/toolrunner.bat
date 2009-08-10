@echo off

set SOAPUI_HOME=%~dp0

set JAVA=%SOAPUI_HOME%..\jre\bin\java

rem init classpath

@SOAPUICLASSPATHCOMPACT@

rem JVM parameters, modify as appropriate
set JAVA_OPTS=-Xms128m -Xmx384m -Dsoapui.properties=soapui.properties "-Dsoapui.home=%SOAPUI_HOME%\"

if "%SOAPUI_HOME%\" == "" goto START
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.libraries="%SOAPUI_HOME%ext"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.listeners="%SOAPUI_HOME%listeners"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.actions="%SOAPUI_HOME%actions"

:START

rem ********* run soapui toolrunner ***********

"%JAVA%" %JAVA_OPTS% com.eviware.soapui.tools.SoapUIToolRunner %*