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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.BindingImporter;
import com.eviware.soapui.impl.wsdl.support.policy.PolicyUtils;
import com.eviware.soapui.impl.wsdl.support.soap.Soap11HttpBindingImporter;
import com.eviware.soapui.impl.wsdl.support.soap.Soap12HttpBindingImporter;
import com.eviware.soapui.impl.wsdl.support.soap.SoapJMSBindingImporter;
import com.eviware.soapui.impl.wsdl.support.soap.TibcoSoapJMSBindingImporter;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Importer for WsdlInterfaces from WSDL urls / files
 *
 * @author Ole.Matzura
 */

public class WsdlImporter {
    private static List<BindingImporter> bindingImporters = new ArrayList<BindingImporter>();
    @SuppressWarnings("unused")
    private static WsdlImporter instance;

    private final static Logger log = LogManager.getLogger(WsdlImporter.class);

    static {
        try {
            bindingImporters.add(new Soap11HttpBindingImporter());
            bindingImporters.add(new Soap12HttpBindingImporter());
            bindingImporters.add(new SoapJMSBindingImporter());
            bindingImporters.add(new TibcoSoapJMSBindingImporter());
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    public static WsdlInterface[] importWsdl(WsdlProject project, String wsdlUrl) throws Exception {
        return importWsdl(project, wsdlUrl, null);
    }

    public static WsdlInterface[] importWsdl(WsdlProject project, String wsdlUrl, QName bindingName) throws Exception {
        return importWsdl(project, wsdlUrl, bindingName, null);
    }

    @Nullable
    public static WsdlInterface[] importWsdl(WsdlProject project, String wsdlUrl, QName bindingName,
                                             WsdlLoader wsdlLoader) throws Exception {
        wsdlUrl = Tools.normalizeFileSeparators(wsdlUrl);

        WsdlContext wsdlContext = new WsdlContext(wsdlUrl);
        if (!wsdlContext.load(wsdlLoader)) {
            UISupport.showErrorMessage("Failed to import WSDL");
            return null;
        }

        Definition definition = wsdlContext.getDefinition();
        List<WsdlInterface> result = new ArrayList<WsdlInterface>();
        if (bindingName != null) {
            WsdlInterface iface = importBinding(project, wsdlContext,
                    (Binding) definition.getAllBindings().get(bindingName));
            return iface == null ? new WsdlInterface[0] : new WsdlInterface[]{iface};
        }

        Map<Binding, WsdlInterface> importedBindings = new HashMap<Binding, WsdlInterface>();

        Map<?, ?> serviceMap = definition.getAllServices();
        if (serviceMap.isEmpty()) {
            log.info("Missing services in [" + wsdlUrl + "], check for bindings");
        } else {
            Iterator<?> i = serviceMap.values().iterator();
            while (i.hasNext()) {
                Service service = (Service) i.next();
                Map<?, ?> portMap = service.getPorts();
                Iterator<?> i2 = portMap.values().iterator();
                while (i2.hasNext()) {
                    Port port = (Port) i2.next();

                    Binding binding = port.getBinding();
                    if (importedBindings.containsKey(binding)) {
                        // add endpoint since it could differ from already imported
                        // one..
                        String endpoint = WsdlUtils.getSoapEndpoint(port);
                        if (endpoint != null) {
                            importedBindings.get(binding).addEndpoint(endpoint);
                        }

                        continue;
                    }

                    String ifaceName = getInterfaceNameForBinding(binding);
                    WsdlInterface ifc = (WsdlInterface) project.getInterfaceByName(ifaceName);
                    if (ifc != null) {
                        Boolean res = UISupport.confirmOrCancel("Interface [" + ifc.getName()
                                + "] already exists in project, update instead?", "Import WSDL");
                        if (res == null) {
                            return new WsdlInterface[0];
                        }

                        if (res.booleanValue()) {
                            if (ifc.updateDefinition(wsdlUrl, false)) {
                                importedBindings.put(binding, ifc);
                                result.add(ifc);
                            }
                        }

                        continue;
                    }

                    WsdlInterface iface = importBinding(project, wsdlContext, binding);
                    if (iface != null) {
                        String endpoint = WsdlUtils.getSoapEndpoint(port);
                        if (endpoint != null) {
                            iface.addEndpoint(endpoint);
                        }
                        // NOTE: question is what has priority wsaw:usingAddressing or
                        // wsam:Addressing policy
                        // in case addressing is defined both ways in the wsdl and
                        // there is conflict
                        // currently the first one that's set is final
                        // first is checked wsdl binding and policy attachment
                        // and then for port in the same order

                        if (iface.getWsaVersion().equals(WsaVersionTypeConfig.NONE.toString())) {
                            iface.setWsaVersion(WsdlUtils.getUsingAddressing(port));
                        }
                        if (iface.getWsaVersion().equals(WsaVersionTypeConfig.NONE.toString())) {
                            iface.processPolicy(PolicyUtils.getAttachedPolicy(port, wsdlContext.getDefinition()));
                        }

                        result.add(iface);
                        importedBindings.put(binding, iface);
                    }
                }
            }
        }

        Map<?, ?> bindingMap = definition.getAllBindings();
        if (!bindingMap.isEmpty()) {
            Iterator<?> i = bindingMap.values().iterator();
            while (i.hasNext()) {
                Binding binding = (Binding) i.next();
                if (importedBindings.containsKey(binding)) {
                    continue;
                }

                PortType portType = binding.getPortType();
                if (portType == null) {
                    log.warn("Missing portType for binding [" + binding.getQName().toString() + "]");
                } else {
                    String ifaceName = getInterfaceNameForBinding(binding);
                    WsdlInterface ifc = (WsdlInterface) project.getInterfaceByName(ifaceName);
                    if (ifc != null && result.indexOf(ifc) == -1) {
                        Boolean res = UISupport.confirmOrCancel("Interface [" + ifc.getName()
                                + "] already exists in project, update instead?", "Import WSDL");
                        if (res == null) {
                            return new WsdlInterface[0];
                        }

                        if (res.booleanValue()) {
                            if (ifc.updateDefinition(wsdlUrl, false)) {
                                importedBindings.put(binding, ifc);
                                result.add(ifc);
                            }
                        }

                        continue;
                    }

                    WsdlInterface iface = importBinding(project, wsdlContext, binding);
                    if (iface != null) {
                        result.add(iface);
                        importedBindings.put(binding, ifc);
                    }
                }
            }
        }

        if (importedBindings.isEmpty() && serviceMap.isEmpty() && bindingMap.isEmpty()) {
            UISupport.showErrorMessage("Found nothing to import in [" + wsdlUrl + "]");
        }

        // only the last gets the context
        if (result.size() > 0) {
            result.get(result.size() - 1).setWsdlContext(wsdlContext);
        }

        return result.toArray(new WsdlInterface[result.size()]);
    }

    public final static String getInterfaceNameForBinding(Binding binding) {
        if (SoapUI.getSettings().getBoolean(WsdlSettings.NAME_WITH_BINDING)) {
            return binding.getQName().getLocalPart();
        } else {
            return binding.getPortType().getQName().getLocalPart();
        }
    }

    private static WsdlInterface importBinding(WsdlProject project, WsdlContext wsdlContext, Binding binding)
            throws Exception {
        log.info("Finding importer for " + binding.getQName());
        for (int c = 0; c < bindingImporters.size(); c++) {
            BindingImporter importer = bindingImporters.get(c);
            if (importer.canImport(binding)) {
                log.info("Importing binding " + binding.getQName());
                WsdlInterface iface = importer.importBinding(project, wsdlContext, binding);

                String url = wsdlContext.getUrl();
                iface.setDefinition(url);

                return iface;
            }
        }
        log.info("Missing importer for " + binding.getQName());

        return null;
    }
}
