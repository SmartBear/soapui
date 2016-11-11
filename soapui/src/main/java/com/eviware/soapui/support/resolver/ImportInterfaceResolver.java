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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStepInterface;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.ProjectDirProvider;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

import javax.swing.JOptionPane;
import java.io.File;

public abstract class ImportInterfaceResolver implements Resolver {
    private boolean resolved = false;
    private WsdlTestStep item;

    public ImportInterfaceResolver(WsdlTestStep item) {
        this.item = item;
    }

    public String getResolvedPath() {
        return "";
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean resolve() {
        String[] options = {"File(Wsdl)", "Url(Wsdl)", "File(Wadl)", "Url(Wadl)", "Cancel"};
        int choosed = JOptionPane
                .showOptionDialog(UISupport.getMainFrame(), "Choose source for new interface from ...",
                        "New interface source", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        options, null);
        switch (choosed) {
            case 0:
                loadWsdlFromFile();
                resolved = update();
                break;
            case 1:
                loadWsdlFromUrl();
                resolved = update();
                break;
            case 2:
                loadWadlFromFile();
                resolved = update();
                break;
            case 3:
                loadWadlFromUrl();
                resolved = update();
                break;
            default:
                resolved = false;
                break;
        }

        return resolved;
    }

    private void loadWadlFromUrl() {
        WsdlProject project = item.getTestCase().getTestSuite().getProject();
        String url = UISupport.prompt("Enter WADL URL", "Add WADL from URL", "");
        if (url == null) {
            return;
        }

        importWadl(project, url);

    }

    private void loadWadlFromFile() {
        WsdlProject project = item.getTestCase().getTestSuite().getProject();
        File file = UISupport.getFileDialogs().open(this, "Select WADL file", ".wadl", "WADL Files (*.wadl)",
                ProjectDirProvider.getProjectFolder(project));
        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();
        if (path == null) {
            return;
        }

        importWadl(project, "file:/" + path);
    }

    private void importWadl(WsdlProject project, String path) {
        RestService restService = (RestService) project.addNewInterface(((RestTestRequestStepInterface) item)
                .getRequestStepConfig().getService(), RestServiceFactory.REST_TYPE);
        try {
            new WadlImporter(restService).initFromWadl(path);
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }

    protected abstract boolean update();

    private void loadWsdlFromUrl() {
        WsdlProject project = item.getTestCase().getTestSuite().getProject();
        String url = UISupport.prompt("Enter WSDL URL", "Add WSDL from URL", "");
        if (url == null) {
            return;
        }

        importWsdl(project, url);
    }

    private void loadWsdlFromFile() {

        WsdlProject project = item.getTestCase().getTestSuite().getProject();
        File file = UISupport.getFileDialogs().open(this, "Select WSDL file", ".wsdl", "WSDL Files (*.wsdl)",
                ProjectDirProvider.getProjectFolder(project));
        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();
        if (path == null) {
            return;
        }

        importWsdl(project, file.getAbsolutePath());
    }

    private void importWsdl(WsdlProject project, String file) {
        try {
            Boolean createRequests = UISupport.confirmOrCancel("Create default requests for all operations",
                    "Import WSDL");
            if (createRequests == null) {
                return;
            }

            Interface[] ifaces = WsdlInterfaceFactory.importWsdl(project, file, createRequests);
            if (ifaces.length > 0) {
                UISupport.select(ifaces[0]);
            }
        } catch (InvalidDefinitionException ex) {
            UISupport.showExtendedInfo("Error loading WSDL",
                    "There was something wrong with the WSDL you are trying to import", ex.getDetailedMessage(), null);
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex.getMessage() + ":" + ex.getCause());
        }
    }

    public String getDescription() {
        return "Resolve: Import interface";
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
