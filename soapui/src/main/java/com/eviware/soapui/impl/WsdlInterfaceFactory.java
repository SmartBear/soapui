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

package com.eviware.soapui.impl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.config.WsdlInterfaceConfig;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.SoapUIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WsdlInterfaceFactory implements InterfaceFactory<WsdlInterface> {
    public final static String WSDL_TYPE = "wsdl";
    private final static Logger log = LogManager.getLogger(WsdlInterfaceFactory.class);

    public WsdlInterface build(WsdlProject project, InterfaceConfig config) {
        return new WsdlInterface(project, (WsdlInterfaceConfig) config.changeType(WsdlInterfaceConfig.type));
    }

    public WsdlInterface createNew(WsdlProject project, String name) {
        WsdlInterface iface = new WsdlInterface(project, (WsdlInterfaceConfig) project.getConfig().addNewInterface()
                .changeType(WsdlInterfaceConfig.type));
        iface.setName(name);

        return iface;
    }

    public static WsdlInterface[] importWsdl(WsdlProject project, String url, boolean createRequests)
            throws SoapUIException {
        return importWsdl(project, url, createRequests, null, null);
    }

    public static WsdlInterface[] importWsdl(WsdlProject project, String url, boolean createRequests,
                                             WsdlLoader wsdlLoader) throws SoapUIException {
        return importWsdl(project, url, createRequests, null, wsdlLoader);
    }

    @Nullable
    public static WsdlInterface[] importWsdl(WsdlProject project, String url, boolean createRequests,
                                             QName bindingName, WsdlLoader wsdlLoader) throws SoapUIException {
        WsdlInterface[] result;

        PropertyExpansionContext context = new DefaultPropertyExpansionContext(project.getModelItem());
        url = PropertyExpander.expandProperties(context, url);
        try {
            result = WsdlImporter.importWsdl(project, url, bindingName, wsdlLoader);
        } catch (InvalidDefinitionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error importing wsdl: " + e);
            SoapUI.logError(e);
            throw new SoapUIException("Error importing wsdl", e);
        }

        try {
            if (createRequests && result != null) {
                for (WsdlInterface iface : result) {
                    for (int c = 0; c < iface.getOperationCount(); c++) {
                        WsdlOperation operation = iface.getOperationAt(c);
                        WsdlRequest request = operation.addNewRequest("Request 1");
                        try {
                            String requestContent = operation.createRequest(project.getSettings().getBoolean(
                                    WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS));
                            request.setRequestContent(requestContent);
                        } catch (Exception e) {
                            SoapUI.logError(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error creating requests: " + e.getMessage());
            throw new SoapUIException("Error creating requests", e);
        }

        Analytics.trackAction(SoapUIActions.IMPORT_WSDL);

        return result;
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        java.awt.Desktop.getDesktop().browse(new URI("http://www.sunet.se"));
    }
}
