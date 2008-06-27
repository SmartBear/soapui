@echo off

set JAVA=..\jre\bin\java

rem init classpath

@SOAPUICLASSPATH@

rem JVM parameters, modify as appropriate
set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx256m -Dsoapui.properties=soapui.properties

rem ********* run soapui loadtest runner ***********

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" com.eviware.soapui.tools.SoapUILoadTestRunner %*