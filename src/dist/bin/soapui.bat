@echo off

set SOAPUI_HOME=%~dp0

set JAVA=%JAVA_HOME%\bin\java

if not "%JAVA_HOME%" == "" goto SET_CLASSPATH

set JAVA=java

echo JAVA_HOME is not set, unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.

:SET_CLASSPATH

rem init classpath

@SOAPUICLASSPATHCOMPACT@

rem JVM parameters, modify as appropriate

set JAVA_OPTS=-Xms128m -Xmx384m -Dsoapui.properties=soapui.properties "-Dsoapui.home=%SOAPUI_HOME%\"

if "%SOAPUI_HOME%" == "" goto START
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.libraries="%SOAPUI_HOME%ext"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.listeners="%SOAPUI_HOME%listeners"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.actions="%SOAPUI_HOME%actions"
	set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path="%SOAPUI_HOME%\"
	set JAVA_OPTS=%JAVA_OPTS% -Dwsi.dir="%SOAPUI_HOME%..\wsi-test-tools"
    set JAVA_OPTS=%JAVA_OPTS% -DGRE_HOME="%SOAPUI_HOME%..\xulrunner-windows"
rem uncomment to disable browser component
rem    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.jxbrowser.disable="true"
:START

rem ********* run soapui ***********

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" com.eviware.soapui.SoapUI %*

