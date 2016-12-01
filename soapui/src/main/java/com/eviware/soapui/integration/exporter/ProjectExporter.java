/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.integration.exporter;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.support.SoapUIException;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Project exporting means that copy of existing project with copies of all
 * external dependencies will be put in one directory and packed together.
 * <p/>
 * Project copy will resolve all its dependency to dependency copies. Paths have
 * to be relative to the project file.
 *
 * @author robert
 */
public class ProjectExporter {

    private static final int BUFFER = 1024;
    private WsdlProject project;
    private WsdlProject projectCopy;
    private final int TEMP_DIR_ATTEMPTS = 10000;
    private File tmpDir;

    public ProjectExporter(WsdlProject project) {
        this.project = project;
    }

    /**
     * Creates packed project on given path
     *
     * @param exportPath
     * @return
     * @throws SoapUIException
     * @throws XmlException
     * @throws IOException
     */
    public boolean exportProject(String exportPath) throws IOException, XmlException, SoapUIException {

        boolean result = false;
        if ((tmpDir = createTemporaryDirectory()) != null) {
            if (createProjectCopy()) {
                if (copyDependencies()) {
                    projectCopy.setResourceRoot("${projectDir}");
                    projectCopy.save();
                    if (packageAll(exportPath)) {
                        result = true;
                    }
                }
            }
            deleteDir(tmpDir);
        }
        return result;
    }

    /**
     * Compress temporary directory and save it on given path.
     *
     * @param exportPath
     * @return
     * @throws IOException
     */
    private boolean packageAll(String exportPath) {
        if (!exportPath.endsWith(".zip")) {
            exportPath = exportPath + ".zip";
        }

        BufferedInputStream origin = null;
        ZipOutputStream out;
        boolean result = true;
        try {
            FileOutputStream dest = new FileOutputStream(exportPath);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            // get a list of files from current directory

            String files[] = tmpDir.list();

            for (int i = 0; i < files.length; i++) {
                //				System.out.println( "Adding: " + files[i] );
                FileInputStream fi = new FileInputStream(new File(tmpDir, files[i]));
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files[i]);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (IOException e) {
            // TODO: handle exception
            result = false;
            SoapUI.logError(e, "Error packaging export");
        }
        return result;
    }

    public static void unpackageAll(String archive, String path) {
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(archive);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                // write the files to the disk
                FileOutputStream fos = new FileOutputStream(path + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public static List<String> getZipContents(String archive) {
        List<String> contents = new ArrayList<String>();

        try {
            ZipFile zipFile = new ZipFile(archive);
            for (Enumeration<? extends ZipEntry> em1 = zipFile.entries(); em1.hasMoreElements(); ) {
                contents.add(em1.nextElement().toString());
            }
        } catch (ZipException ze) {
            SoapUI.logError(ze);
        } catch (IOException e) {
            SoapUI.logError(e);
        }

        return contents;
    }

    /**
     * Do actual dependency copying and update project's copy dependency path.
     *
     * @throws IOException
     */
    private boolean copyDependencies() throws IOException {
        boolean result = true;
        projectCopy.setResourceRoot("${projectDir}");
        List<ExternalDependency> dependencies = projectCopy.getExternalDependencies();
        for (ExternalDependency dependency : dependencies) {
            switch (dependency.getType()) {
                case FILE:
                    File originalDependency = new File(dependency.getPath());
                    if (originalDependency.exists()) {
                        File targetDependency = new File(tmpDir, originalDependency.getName());
                        FileUtils.copyFile(originalDependency, targetDependency);
                        dependency.updatePath(targetDependency.getPath());
                    } else {
                        SoapUI.log.warn("Do not exists on local file system [" + originalDependency.getPath() + "]");
                    }
                    break;
                case FOLDER:
                    originalDependency = new File(dependency.getPath());
                    File targetDependency = new File(tmpDir, originalDependency.getName());
                    targetDependency.mkdir();
                    FileUtils.copyDirectory(originalDependency, targetDependency, false);
                    dependency.updatePath(targetDependency.getPath());
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    /**
     * Creates project copy and save it in temporary directory. Set copy's
     * project path and resource root to ${projectDir}
     *
     * @return
     * @throws IOException
     * @throws SoapUIException
     * @throws XmlException
     */
    private boolean createProjectCopy() throws IOException, XmlException, SoapUIException {
        project.saveIn(new File(tmpDir, project.getName() + "-soapui-project.xml"));

        projectCopy = (WsdlProject) ProjectFactoryRegistry.getProjectFactory("wsdl").createNew(
                new File(tmpDir, project.getName() + "-soapui-project.xml").getAbsolutePath());//new WsdlProject( new File( tmpDir, project.getName() + ".xml" ).getAbsolutePath() );

        return projectCopy != null;
    }

    /**
     * Creates temporary directory where package will be created
     *
     * @return if operation is successuful
     */
    private File createTemporaryDirectory() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        return null;
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
