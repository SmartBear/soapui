@echo off

set JAVA=%JAVA_HOME%\bin\java

if not "%JAVA_HOME%" == "" goto SET_CLASSPATH

set JAVA=java

echo JAVA_HOME is not set, unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.

:SET_CLASSPATH

rem init classpath

@SOAPUICLASSPATH@

rem JVM parameters, modify as appropriate
set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx256m -Dsoapui.properties=soapui.properties

rem ********* run soapui loadtest runner ***********

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" com.eviware.soapui.tools.SoapUILoadTestRunner %*