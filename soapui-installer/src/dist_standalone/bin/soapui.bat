@echo off

set SOAPUI_HOME=%~dp0
if exist "%SOAPUI_HOME%..\jre\bin" goto SET_BUNDLED_JAVA

if exist "%JAVA_HOME%" goto SET_SYSTEM_JAVA

echo JAVA_HOME is not set, unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
goto SET_SYSTEM_JAVA

:SET_BUNDLED_JAVA
set JAVA=%SOAPUI_HOME%..\jre\bin\java
goto END_SETTING_JAVA

:SET_SYSTEM_JAVA
set JAVA=java

:END_SETTING_JAVA


rem init classpath

set CLASSPATH=%SOAPUI_HOME%${project.src.artifactId}-${project.version}.jar;%SOAPUI_HOME%..\lib\*;

rem JVM parameters, modify as appropriate
set JAVA_OPTS=-Xms128m -Xmx1024m -Dsoapui.properties=soapui.properties "-Dsoapui.home=%SOAPUI_HOME%\" -splash:soapui-splash.png

if "%SOAPUI_HOME%" == "" goto START
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.libraries="%SOAPUI_HOME%ext"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.listeners="%SOAPUI_HOME%listeners"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.actions="%SOAPUI_HOME%actions"
	set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path="%SOAPUI_HOME%\"
	set JAVA_OPTS=%JAVA_OPTS% -Dwsi.dir="%SOAPUI_HOME%..\wsi-test-tools"
rem uncomment to disable browser component
rem    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.jxbrowser.disable="true"

:START

set OLDDIR=%CD%
cd /d %SOAPUI_HOME%
rem ********* run soapui ***********

"%JAVA%" %JAVA_OPTS% com.eviware.soapui.SoapUI %*
cd /d %OLDDIR%
