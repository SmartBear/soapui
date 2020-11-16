package com.smartbear.soapui.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class Logging {
    public static final String GLOBAL_GROOVY_LOG = "GLOBAL_GROOVY_LOG";

    public static Logger ensureGroovyLog() {
        return LogManager.getLogger("groovy.log");
    }

    public static void addAppender(String loggerName, Appender appender) {
        addAppender(loggerName, appender, null);
    }

    public static void addAppender(String loggerName, Appender appender, Level level) {
        LoggerContext context = ((LoggerContext) LogManager.getContext(false));
        Configuration config = context.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);

        //add logger to the configuration to avoid adding appender to root logger in case there is not such logger
        if (!loggerName.equals(loggerConfig.getName())) {
            loggerConfig = LoggerConfig.createLogger(loggerConfig.isAdditive(),
                    loggerConfig.getLevel(),
                    loggerName,
                    "true",
                    new AppenderRef[]{AppenderRef.createAppenderRef(appender.getName(), level, null)},
                    null,
                    config,
                    null);
            config.addLogger(loggerName, loggerConfig);
        }
        appender.start();
        loggerConfig.addAppender(appender, level, null);
        context.updateLoggers();
    }

    public static void removeAppender(String loggerName, Appender appender) {
        LoggerContext context = ((LoggerContext) LogManager.getContext(false));
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
        loggerConfig.removeAppender(appender.getName());
        context.updateLoggers();
    }

    public static Appender getAppender(String name) {
        LoggerContext context = ((LoggerContext) LogManager.getContext(false));
        return context.getConfiguration().getAppender(name);
    }
}
