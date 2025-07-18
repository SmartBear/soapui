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

package com.eviware.soapui.Util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class SoapUITools {
    private final static Logger log = LoggerFactory.getLogger(SoapUITools.class);

    public static File createTemporaryDirectory() throws IOException {
        String libDirectoryName = UUID.randomUUID().toString();
        final File libDirectory = new File(System.getProperty("java.io.tmpdir"), libDirectoryName);
        if (!libDirectory.mkdir()) {
            throw new IOException("Could not create directory for unpacked JAR libraries at " + libDirectory);
        }
        deleteDirectoryOnExit(libDirectory);
        return libDirectory;
    }

    public static void deleteDirectoryOnExit(final File directory) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // Try to delete the directory with retry logic for Windows file locks
                int maxRetries = 5;
                int retryDelay = 100; // milliseconds

                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                    try {
                        // First, try to close any potential file handles by running garbage collection
                        if (attempt > 1) {
                            System.gc();
                            System.runFinalization();
                            Thread.sleep(retryDelay * attempt);
                        }

                        FileUtils.deleteDirectory(directory);
                        // If successful, log at debug level only
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "Successfully deleted temporary directory " + directory + " on attempt " + attempt);
                        }
                        return; // Success, exit the method

                    } catch (IOException e) {
                        if (attempt == maxRetries) {
                            // Only warn on the final attempt to reduce log noise
                            log.warn("Could not delete temporary directory " + directory + " after " + maxRetries
                                    + " attempts: " + e.getMessage());
                        } else {
                            // Log at debug level for intermediate attempts
                            if (log.isDebugEnabled()) {
                                log.debug("Attempt " + attempt + " to delete temporary directory " + directory
                                        + " failed: " + e.getMessage());
                            }
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Could not delete temporary directory " + directory);
                        return;
                    }
                }
            }
        }));
    }

    public static Path soapuiHomeDir() {
        String homePath = System.getProperty("soapui.home");
        if (homePath == null) {
            File homeFile = new File(".");
            log.warn("System property 'soapui.home' is not set! Using this directory instead: {}", homeFile);
            return homeFile.toPath();
        }
        return ensureExists(Paths.get(homePath));
    }

    public static Path ensureExists(Path path) {
        File file = path.toFile();
        boolean ok = true;
        if (!file.exists()) {
            ok = file.mkdirs();
        }
        if (!ok) {
            throw new RuntimeException("Cannot create local storage at: " + file);
        }
        return path;
    }

    public static String absolutePath(Path path) {
        return path.toFile().getAbsolutePath();
    }
}
