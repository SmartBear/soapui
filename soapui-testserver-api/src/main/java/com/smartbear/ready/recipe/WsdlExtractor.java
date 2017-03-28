package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.SoapUIException;

import java.util.List;

/**
 * A utility class to extract objects after adding WSDL to a project.
 */
public class WsdlExtractor {
    static WsdlInterface getWsdlInterface(WsdlTestCase testCase, String wsdlUrl, String binding) throws ParseException {
        WsdlProject project = testCase.getTestSuite().getProject();
        List<AbstractInterface<?>> wsdlInterfaces = project.getInterfaces(WsdlInterfaceFactory.WSDL_TYPE);
        WsdlInterface[] wsdlInterfacesArray = wsdlInterfaces.toArray(new WsdlInterface[wsdlInterfaces.size()]);

        WsdlInterface wsdlInterface = findNamedInterface(wsdlInterfacesArray, wsdlUrl, binding);

        if (wsdlInterface == null) {
            try {
                WsdlInterface[] importedInterfaces = WsdlInterfaceFactory.importWsdl(project, wsdlUrl, false);
                wsdlInterface = findNamedInterface(importedInterfaces, wsdlUrl, binding);
            } catch (SoapUIException e) {
                throw new ParseException("Failed to import WSDL from [" + wsdlUrl + "]", e);
            }
        }

        return wsdlInterface;
    }

    private static WsdlInterface findNamedInterface(WsdlInterface[] interfaces, String wsdlUrl, String binding) {
        for (WsdlInterface iface : interfaces) {
            if (interfaceMatchesConfig(wsdlUrl, binding, iface)) {
                return iface;
            }
        }

        return null;
    }

    private static boolean interfaceMatchesConfig(String wsdlUrl, String binding, WsdlInterface iface) {
        return (binding == null &&
                iface.getWsdlContext().getUrl().equalsIgnoreCase(wsdlUrl)) ||
                iface.getName().equalsIgnoreCase(binding);
    }
}
