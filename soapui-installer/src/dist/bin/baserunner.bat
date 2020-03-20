@echo off

set SOAPUI_HOME=%~dp0

set JAVA=%JAVA_HOME%\bin\java

if exist "%SOAPUI_HOME%..\jre\bin" goto SET_BUNDLED_JAVA

if exist "%JAVA_HOME%" goto END_SETTING_JAVA

set JAVA=java

echo JAVA_HOME is not set, unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.

:SET_BUNDLED_JAVA
set JAVA=%SOAPUI_HOME%..\jre\bin\java
goto END_SETTING_JAVA

:END_SETTING_JAVA

rem init classpath

set CLASSPATH=%SOAPUI_HOME%${project.src.artifactId}-${project.version}.jar;%SOAPUI_HOME%..\lib\*
"%JAVA%" -cp "%CLASSPATH%" com.eviware.soapui.tools.JfxrtLocator > %TEMP%\jfxrtpath
set /P JFXRTPATH= < %TEMP%\jfxrtpath
del %TEMP%\jfxrtpath
set CLASSPATH=%CLASSPATH%;%JFXRTPATH%

rem JVM parameters, modify as appropriate
set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx1024m -Dsoapui.properties=soapui.properties "-Dsoapui.home=%SOAPUI_HOME%\"

if "%SOAPUI_HOME%\" == "" goto START
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.libraries="%SOAPUI_HOME%ext"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.listeners="%SOAPUI_HOME%listeners"
    set JAVA_OPTS=%JAVA_OPTS% -Dsoapui.ext.actions="%SOAPUI_HOME%actions"

:START

rem ********* run %SOAPUI_RUNNER_NAME% ***********

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" %MAIN_CLASS% %*
