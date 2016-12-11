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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PathUtils {
    public static String getAbsoluteFolder(String path) {
        File folder = new File(path);

        if (!folder.exists()) {
            return null;
        }

        if (folder.isDirectory()) {
            return folder.getAbsolutePath();
        }

        File parentFile = folder.getParentFile();
        return parentFile == null ? null : parentFile.getAbsolutePath();
    }

    public static String expandPath(String path, AbstractWsdlModelItem<?> modelItem) {
        return expandPath(path, modelItem, null);
    }

    public static String expandPath(String path, AbstractWsdlModelItem<?> modelItem, PropertyExpansionContext context) {
        // if ( path != null ) {
        path = stripQuotes(path);
        if (isHttpPath(path)) {
            path = path.replaceAll(" ", "%20");
        }
        // }
        path = context == null ? PropertyExpander.expandProperties(modelItem, path) : PropertyExpander
                .expandProperties(context, path);

        if (!isRelativePath(path)) {
            return path;
        }

        String root = getExpandedResourceRoot(modelItem, context);
        if (StringUtils.isNullOrEmpty(root) || StringUtils.isNullOrEmpty(root)) {
            return path;
        }

        if (isHttpPath(root)) {
            root += "/";
        } else {
            root += File.separatorChar;
        }

        return Tools.joinRelativeUrl(root, path);
    }

    private static String stripQuotes(String path) {
        if (path != null) {
            if (path.startsWith("\"") && path.endsWith("\"")) {
                path = path.substring(1, path.length() - 1);
            }
        }
        return path;
    }

    public static String adjustRelativePath(String str, String root, ModelItem contextModelItem) {
        if (StringUtils.isNullOrEmpty(root) || StringUtils.isNullOrEmpty(str)) {
            return str;
        }

        if (!isRelativePath(str)) {
            return str;
        }

        root = PropertyExpander.expandProperties(contextModelItem, root);

        if (isHttpPath(root)) {
            root += "/";
        } else {
            root += File.separatorChar;
        }

        return Tools.joinRelativeUrl(root, str);

        // if( isHttpPath( str ))
        // return root + '/' + str;
        // else
        // return root + File.separatorChar + str;
    }

    public static boolean isHttpPath(String str) {
        if (StringUtils.isNullOrEmpty(str)) {
            return false;
        }

        str = str.toLowerCase();

        return str.startsWith("http:/") || str.startsWith("https:/");
    }

    public static boolean isRelativePath(String str) {
        if (StringUtils.isNullOrEmpty(str)) {
            return false;
        }

        str = str.toLowerCase();

        return !str.startsWith("/") && !str.startsWith("\\") && !str.startsWith("http:/")
                && !str.startsWith("https:/") && str.indexOf(":\\") != 1 && !str.startsWith("file:")
                && str.indexOf(":/") != 1;
    }

    public static String createRelativePath(String path, String root, ModelItem contextModelItem) {
        if (StringUtils.isNullOrEmpty(root)) {
            return path;
        }

        root = PropertyExpander.expandProperties(contextModelItem, root);

        return relativize(path, root);
    }

    public static String relativizeResourcePath(String path, ModelItem modelItem) {
        if (modelItem == null || StringUtils.isNullOrEmpty(path) || isRelativePath(path) || isHttpPath(path)) {
            return path;
        }

        Project project = ModelSupport.getModelItemProject(modelItem);
        if (project == null) {
            return path;
        }

        if (StringUtils.isNullOrEmpty(project.getPath()) && project.getResourceRoot().indexOf("${projectDir}") >= 0) {
            if (UISupport.confirm("Save project before setting path?", "Project has not been saved")) {
                try {
                    project.save();
                } catch (IOException e) {
                    SoapUI.logError(e);
                    UISupport.showErrorMessage(e);
                    return path;
                }
            }
        }

        String projectPath = PropertyExpander.expandProperties(project, project.getResourceRoot());
        if (StringUtils.isNullOrEmpty(projectPath)) {
            return path;
        }

        return PathUtils.relativize(path, projectPath);
    }

    public static String resolveResourcePath(String path, ModelItem modelItem) {
        if (path == null || modelItem == null) {
            return path;
        }

        path = PathUtils.denormalizePath(path);
        path = PropertyExpander.expandProperties(new DefaultPropertyExpansionContext(modelItem), path);

        String prefix = "";

        if (path.startsWith("file:")) {
            prefix = path.substring(0, 5);
            path = path.substring(5);
        }

        if (PathUtils.isAbsolutePath(path)) {
            return prefix + path;
        }

        WsdlProject project = (WsdlProject) ModelSupport.getModelItemProject(modelItem);
        if (project == null) {
            return prefix + path;
        }

        String resourceRoot = getExpandedResourceRoot(modelItem);

        if (StringUtils.hasContent(resourceRoot) && !resourceRoot.endsWith(File.separator)) {
            resourceRoot += File.separator;
        }

        String result = Tools.joinRelativeUrl(resourceRoot, path);
        return prefix + result;
    }

    public static String relativize(String path, String rootPath) {
        if (StringUtils.isNullOrEmpty(path) || StringUtils.isNullOrEmpty(rootPath)) {
            return path;
        }

        if (path.toLowerCase().startsWith("http:/") || path.toLowerCase().startsWith("https:/")) {
            String prefix = "";

            while (rootPath != null) {
                if (path.startsWith(rootPath)) {
                    path = path.substring(rootPath.length());
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }

                    break;
                } else {
                    int ix = rootPath.lastIndexOf('/');
                    rootPath = ix == -1 ? null : rootPath.substring(0, ix);
                    prefix += "../";
                }
            }

            return prefix + path;
        } else {
            String prefix = "";

            // file url?
            if (path.toLowerCase().startsWith("file:")) {
                try {
                    path = new File(new URL(path).toURI()).getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (rootPath.startsWith("file:")) {
                try {
                    rootPath = new File(new URL(rootPath).toURI()).getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // different drives on windows? (can't relativize)
            if (rootPath.toUpperCase().charAt(0) != path.toUpperCase().charAt(0)
                    && ((rootPath.indexOf(":\\") == 1 || rootPath.indexOf(":/") == 1) && (path.indexOf(":\\") == 1 || path
                    .indexOf(":/") == 1))) {
                return path;
            }

            while (rootPath != null) {
                if (path.startsWith(rootPath)) {
                    path = path.substring(rootPath.length());
                    if (path.startsWith(File.separator)) {
                        path = path.substring(1);
                    }

                    break;
                } else {
                    File file = new File(rootPath);
                    rootPath = file.getParent();
                    prefix += ".." + File.separatorChar;
                }
            }

            return prefix + path;
        }
    }

    public static boolean isAbsolutePath(String path) {
        return !isRelativePath(path);
    }

    public static boolean isFilePath(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return false;
        }

        return !isHttpPath(path);
    }

    public static String normalizePath(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return path;
        }

        return File.separatorChar == '/' ? path : path.replace(File.separatorChar, '/');
    }

    public static String denormalizePath(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return path;
        }

        if (isHttpPath(path)) {
            return path;
        }

        return File.separatorChar == '/' ? path.replace('\\', File.separatorChar) : path.replace('/',
                File.separatorChar);
    }

    public static String getExpandedResourceRoot(ModelItem modelItem) {
        return getExpandedResourceRoot(modelItem, null);
    }

    public static String getExpandedResourceRoot(ModelItem modelItem, PropertyExpansionContext context) {
        if (!(modelItem instanceof AbstractWsdlModelItem<?>)) {
            return null;
        }

        WsdlProject project = (WsdlProject) ModelSupport.getModelItemProject(modelItem);
        if (project == null) {
            return null;
        }

        String docroot = project.getResourceRoot();
        if (!StringUtils.hasContent(docroot)) {
            return new File("").getAbsolutePath();
        }

        docroot = context == null ? PropertyExpander.expandProperties(modelItem, docroot) : PropertyExpander
                .expandProperties(context, docroot);

        return docroot;
    }

    public static String ensureFilePathIsUrl(String url) {
        if (isFilePath(url) && !url.startsWith("file:")) {
            try {
                return new File(url).toURI().toURL().toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return url;
    }

    public static String fixForwardSlashesInPath(String path) {
        String prefix = null;
        String query = null;

        int ix = path.indexOf('?');
        if (ix >= 0) {
            query = path.substring(ix);
            path = path.substring(0, ix);
        }

        if (path.contains("://")) {
            prefix = path.substring(0, path.indexOf("://") + 3);
            path = path.substring(prefix.length());
        }

        // remove double-slashes in path
        path = path.replaceAll("/{2,}", "/");

        if (prefix != null) {
            path = prefix + path;
        }

        if (query != null) {
            path = path + query;
        }

        return path;
    }
}
