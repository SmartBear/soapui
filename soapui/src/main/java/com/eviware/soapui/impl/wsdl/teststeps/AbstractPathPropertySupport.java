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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ResolveContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractPathPropertySupport {
    private final String propertyName;
    private final AbstractWsdlModelItem<?> modelItem;

    public AbstractPathPropertySupport(AbstractWsdlModelItem<?> modelItem, String propertyName) {
        this.modelItem = modelItem;
        this.propertyName = propertyName;
    }

    public String set(String value, boolean notify) {
        String old = get();
        value = PathUtils.relativizeResourcePath(value, modelItem);
        try {
            setPropertyValue(PathUtils.normalizePath(value));
            if (notify) {
                notifyUpdate(value, old);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return old;
    }

    public String get() {
        try {
            return getPropertyValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public AbstractWsdlModelItem<?> getModelItem() {
        return modelItem;
    }

    public abstract void setPropertyValue(String value) throws Exception;

    protected void notifyUpdate(String value, String old) {
        modelItem.notifyPropertyChanged(modelItem.getClass().getName() + "@" + propertyName, old, value);
    }

    public String expand(TestCaseRunContext context) {
        try {
            return PathUtils.expandPath(getPropertyValue(), modelItem, context);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String expand() {
        try {
            return PathUtils.resolveResourcePath(getPropertyValue(), modelItem);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String expandUrl() {
        String result = expand();
        if (StringUtils.hasContent(result)) {
            try {
                if (PathUtils.isFilePath(result) && !result.startsWith("file:")) {
                    result = new File(result).toURI().toURL().toString();
                } else {
                    result = new URL(result).toString();
                }
            } catch (MalformedURLException e) {
                SoapUI.logError(e);
            }
        }

        return result;
    }

    public abstract String getPropertyValue() throws Exception;

    public void resolveFile(ResolveContext<?> context, String errorDescription) {
        resolveFile(context, errorDescription, null, null, true);
    }

    public boolean containsPropertyExpansion() {
        try {
            return PropertyExpansionUtils.containsPropertyExpansion(getPropertyValue());
        } catch (Exception e1) {
            SoapUI.logError(e1);
            return false;
        }
    }

    public void resolveFile(ResolveContext<?> context, String errorDescription, String extension, String fileType,
                            final boolean notify) {
        if (containsPropertyExpansion()) {
            return;
        }

        String source = expand();
        if (StringUtils.hasContent(source)) {
            try {
                new URL(source);
            } catch (Exception e) {
                File file = new File(source);
                if (!file.exists()) {
                    if (context.hasThisModelItem(modelItem, errorDescription, source)) {
                        return;
                    }
                    context.addPathToResolve(modelItem, errorDescription, source, new ResolveContext.FileResolver(
                            "Select File", extension, fileType, file.getParent()) {

                        @Override
                        public boolean apply(File newFile) {
                            set(newFile.getAbsolutePath(), notify);
                            return true;
                        }
                    });
                } else {
                    if (context.hasThisModelItem(modelItem, errorDescription, source)) {
                        context.getPath(modelItem, errorDescription, source).setSolved(true);
                    }
                }
            }
        }
    }

    public void resolveFolder(ResolveContext<?> context, String errorDescription, final boolean notify) {
        if (containsPropertyExpansion()) {
            return;
        }

        String source = expand();
        if (StringUtils.hasContent(source)) {
            try {
                new URL(source);
            } catch (Exception e) {
                File file = new File(source);
                if (!file.exists() || !file.isDirectory()) {
                    if (context.hasThisModelItem(modelItem, errorDescription, source)) {
                        return;
                    }
                    context.addPathToResolve(modelItem, errorDescription, source, new ResolveContext.DirectoryResolver(
                            "Select Directory", source) {
                        @Override
                        public boolean apply(File newFile) {
                            set(newFile.getAbsolutePath(), notify);
                            return true;
                        }
                    });
                } else {
                    if (context.hasThisModelItem(modelItem, errorDescription, source)) {
                        context.getPath(modelItem, errorDescription, source).setSolved(true);
                    }
                }
            }
        }
    }
}
