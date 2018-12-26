#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  SoapUI Bootstrap Script                                                 ##
##                                                                          ##
### ====================================================================== ###

### $Id$ ###

EXECUTABLE=`dirname $0`/`basename $0`
LS_LD=`ls -ld "${EXECUTABLE}"`
SYM_LINK_INDICATOR="->"
if [ "$LS_LD" != "${LS_LD%$SYM_LINK_INDICATOR*}" ]
then
  EXECUTABLE=`ls -ld "${EXECUTABLE}" | sed -e 's|.*-> ||' -e 's|.* ${EXECUTABLE}|${EXECUTABLE}|'`
  case "$EXECUTABLE" in
    /*);;
    *)EXECUTABLE=`dirname $0`/$EXECUTABLE
  esac
fi
DIRNAME=`dirname $EXECUTABLE`

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
    Darwin*)
        darwin=true
        ;;
esac

# Setup SOAPUI_HOME
if [ -d $SOAPUI_HOME ]
then
    # get the full path (without any relative bits)
    SOAPUI_HOME=`cd $DIRNAME/..; pwd`
fi

export SOAPUI_HOME

if [ -f "$SOAPUI_HOME/jre/bin/java" ]
then
  JAVA=$SOAPUI_HOME/jre/bin/java
else
    if [ -f "$SOAPUI_HOME/../../PlugIns/jre.bundle/Contents/Home/jre/bin/java" ]
    then
        JAVA=$SOAPUI_HOME/../../PlugIns/jre.bundle/Contents/Home/jre/bin/java
    else
        JAVA=java
    fi
fi

SOAPUI_CLASSPATH=$SOAPUI_HOME/bin/${project.src.artifactId}-${project.version}.jar:$SOAPUI_HOME/lib/*
JFXRTPATH=`$JAVA -cp $SOAPUI_CLASSPATH com.eviware.soapui.tools.JfxrtLocator`
SOAPUI_CLASSPATH=$JFXRTPATH:$SOAPUI_CLASSPATH

export SOAPUI_CLASSPATH

JAVA_OPTS="-Xms128m -Xmx1024m -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40 -Dsoapui.properties=soapui.properties -Dgroovy.source.encoding=iso-8859-1 -Dsoapui.home=$SOAPUI_HOME/bin -splash:SoapUI-Spashscreen.png"

if $darwin
then
    JAVA_OPTS="$JAVA_OPTS -Dswing.crossplatformlaf=apple.laf.AquaLookAndFeel -Dapple.eawt.quitStrategy=CLOSE_ALL_WINDOWS"
fi

if [ $SOAPUI_HOME != "" ]
then
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.libraries=$SOAPUI_HOME/bin/ext"
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.listeners=$SOAPUI_HOME/bin/listeners"
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.actions=$SOAPUI_HOME/bin/actions"
    JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$SOAPUI_HOME/bin"
	JAVA_OPTS="$JAVA_OPTS -Dwsi.dir=$SOAPUI_HOME/wsi-test-tools"
# uncomment to disable browser component
#   JAVA_OPTS="$JAVA_OPTS -Dsoapui.browser.disabled=true"
fi

export JAVA_OPTS

# For Cygwin, switch paths to Windows format before running java
if $cygwin
then
    SOAPUI_HOME=`cygpath --path --dos "$SOAPUI_HOME"`
    SOAPUI_CLASSPATH=`cygpath --path --dos "$SOAPUI_CLASSPATH"`
fi

cd $DIRNAME
echo ================================
echo =
echo = SOAPUI_HOME = $SOAPUI_HOME
echo =
echo ================================

$JAVA $JAVA_OPTS -cp $SOAPUI_CLASSPATH com.eviware.soapui.SoapUI "$@"
