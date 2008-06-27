#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  SoapUI LoadTestRunner Bootstrap Script                                  ##
##                                                                          ##
### ====================================================================== ###

### $Id$ ###

DIRNAME=`dirname $0`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# Setup SOAPUI_HOME
if [ "x$SOAPUI_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    SOAPUI_HOME=`cd $DIRNAME/..; pwd`
fi
export SOAPUI_HOME

@SOAPUISHCLASSPATH@

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    SOAPUI_HOME=`cygpath --path --dos "$SOAPUI_HOME"`
    SOAPUI_CLASSPATH=`cygpath --path --dos "$SOAPUI_CLASSPATH"`
fi

echo ================================
echo =
echo = SOAPUI_HOME = $SOAPUI_HOME
echo =
echo ================================

java -Dsoapui.properties=soapui.properties -cp $SOAPUI_CLASSPATH com.eviware.soapui.tools.SoapUILoadTestRunner $*
