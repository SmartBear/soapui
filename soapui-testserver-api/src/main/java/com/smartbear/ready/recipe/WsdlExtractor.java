package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.SoapUIException;

/**
 * A utility class to extract objects after adding WSDL to a project.
 */
public class WsdlExtractor {
    static WsdlInterface getWsdlInterface(WsdlTestCase testCase, String wsdlUrl, String binding) throws ParseException {
        WsdlProject project = testCase.getTestSuite().getProject();
        WsdlInterface[] projectInterfaces = project.getInterfaces(WsdlInterfaceFactory.WSDL_TYPE).toArray(
                new WsdlInterface[project.getInterfaceCount()]);

        WsdlInterface wsdlInterface = findNamedInterface(projectInterfaces, wsdlUrl, binding);

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
