/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

package com.eviware.soapui.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jakartaee.EESpecProfiles;
import org.apache.tomcat.jakartaee.Migration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Wraps the Apache Tomcat Jakarta EE migration tool (https://tomcat.apache.org/download-migration.cgi)
 * that converts a javax.* WAR into a jakarta.* WAR.
 */
public class WarConverter {
    private static final String MIGRATION_LOGGER_NAME = "org.apache.tomcat.jakartaee";
    private static final Logger log = LogManager.getLogger(WarConverter.class);

    private WarConverter() {
    }

    public static void convert(File source, File destination) throws IOException {
        Migration migration = new Migration();
        migration.setEESpecProfile(EESpecProfiles.TOMCAT);
        migration.setSource(source);
        migration.setDestination(destination);

        // The migration tool logs through java.util.logging, which would otherwise end up on stderr only.
        // JUL keeps loggers weakly referenced, so hold on to it while the conversion runs.
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(MIGRATION_LOGGER_NAME);
        boolean useParentHandlers = julLogger.getUseParentHandlers();
        Handler handler = new Log4jHandler();
        julLogger.addHandler(handler);
        julLogger.setUseParentHandlers(false);
        try {
            migration.execute();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            julLogger.removeHandler(handler);
            julLogger.setUseParentHandlers(useParentHandlers);
        }
    }

    /**
     * Forwards java.util.logging records to log4j so that they show up in the SoapUI log window and log file.
     */
    private static class Log4jHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            if (record == null) {
                return;
            }
            log.log(toLog4jLevel(record.getLevel()), record.getMessage() == null ? "" : getFormattedMessage(record),
                    record.getThrown());
        }

        private String getFormattedMessage(LogRecord record) {
            try {
                return new java.util.logging.SimpleFormatter().formatMessage(record);
            } catch (RuntimeException e) {
                return record.getMessage();
            }
        }

        private static org.apache.logging.log4j.Level toLog4jLevel(Level level) {
            int value = level.intValue();
            if (value >= Level.SEVERE.intValue()) {
                return org.apache.logging.log4j.Level.ERROR;
            } else if (value >= Level.WARNING.intValue()) {
                return org.apache.logging.log4j.Level.WARN;
            } else if (value >= Level.INFO.intValue()) {
                return org.apache.logging.log4j.Level.INFO;
            } else if (value >= Level.FINE.intValue()) {
                return org.apache.logging.log4j.Level.DEBUG;
            }
            return org.apache.logging.log4j.Level.TRACE;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
