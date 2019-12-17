package com.eviware.soapui.impl.wsdl.monitor;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class JProxyServletWsdlMonitorMessageExchangeTest {

    @Test
    public void testConstructor() throws XmlException, IOException, SoapUIException {
        WsdlProject project = ModelItemFactory.makeWsdlProject();
        JProxyServletWsdlMonitorMessageExchange sut = new JProxyServletWsdlMonitorMessageExchange(project);
    }
}