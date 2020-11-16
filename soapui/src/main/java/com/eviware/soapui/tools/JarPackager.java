/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarPackager {
    public static int BUFFER_SIZE = 10240;
    static Logger log = LogManager.getLogger(JarPackager.class);

    public static File copyFileToDir(File fromFile, File toDir) {
        File toFile = new File(toDir, fromFile.getName());
        try {
            copyFile(fromFile, toFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return toFile;
    }

    public static void copyAllFromTo(File fromDir, File toDir, FileFilter filter) {
        if (fromDir.isDirectory() && toDir.isDirectory()) {
            log.info("Coping files from " + fromDir.getAbsolutePath() + " to " + toDir.getAbsolutePath());
            File[] fromFiles = filter == null ? fromDir.listFiles() : fromDir.listFiles(filter);
            for (File file : fromFiles) {
                File toFile = new File(toDir, file.getName());
                if (file.isDirectory()) {
                    if (toFile.exists() || toFile.mkdir()) {
                        copyAllFromTo(file, toFile, filter);
                    } else {
                        log.error("Could not create directory " + toFile.getAbsolutePath());
                    }
                } else {
                    try {
                        copyFile(file, toFile);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            log.error(fromDir.getAbsolutePath() + " or " + toDir.getAbsolutePath() + " is not directory!");
        }
    }

    private static void copyFile(File fromFile, File toFile) throws IOException {
        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    throw e;
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }

    }

    public static void createJarArchive(File archiveFile, File root, File... tobeJared) {
        try {
            byte buffer[] = new byte[BUFFER_SIZE];
            // Open archive file
            log.info("Creating archive [" + archiveFile.getAbsolutePath() + "]");
            FileOutputStream stream = new FileOutputStream(archiveFile);
            JarOutputStream out = new JarOutputStream(stream, new Manifest());

            for (int i = 0; i < tobeJared.length; i++) {
                if (tobeJared[i] == null || !tobeJared[i].exists()) {
                    continue; // Just in case...
                }

                // Add archive entry
                String jarName = tobeJared[i].isDirectory() ? tobeJared[i].getAbsolutePath() + "/" : tobeJared[i]
                        .getAbsolutePath();
                jarName = jarName.replace(root.getAbsolutePath(), "").substring(1);
                jarName = jarName.replace(File.separatorChar, '/');
                JarEntry jarAdd = new JarEntry(jarName);
                log.info("Adding " + jarName);
                jarAdd.setTime(tobeJared[i].lastModified());
                out.putNextEntry(jarAdd);

                if (jarAdd.isDirectory()) {
                    continue;
                }

                // Write file to archive
                FileInputStream in = new FileInputStream(tobeJared[i]);
                while (true) {
                    int nRead = in.read(buffer, 0, buffer.length);
                    if (nRead <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nRead);
                }
                in.close();
            }

            out.close();
            stream.close();
            log.info("Adding completed OK");
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
