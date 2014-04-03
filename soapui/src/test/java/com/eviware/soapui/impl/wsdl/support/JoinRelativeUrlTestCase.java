/*
 * Copyright 2004-2014 SmartBear Software
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

import static org.junit.Assert.assertEquals;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.support.Tools;

public class JoinRelativeUrlTestCase {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(JoinRelativeUrlTestCase.class);
    }

    @Test
    public void testJoin() throws Exception {
        assertEquals("http://test:8080/my/root/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "test.xsd"));
        assertEquals("http://test:8080/my/root/bu/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "bu/test.xsd"));
        assertEquals("http://test:8080/my/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "../test.xsd"));
        assertEquals("http://test:8080/my/root/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "./test.xsd"));
        assertEquals("http://test:8080/bil/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "../../bil/test.xsd"));
        assertEquals("http://test:8080/bil/test.xsd",
                Tools.joinRelativeUrl("http://test:8080/my/root/test.wsdl", "././../../bil/test/.././test.xsd"));
        assertEquals("file:c:" + File.separator + "bil" + File.separator + "xsd" + File.separator + "test.xsd", Tools.joinRelativeUrl("file:c:\\bil\\test.wsdl", "./xsd/test.xsd"));
        assertEquals("file:c:" + File.separator + "bil" + File.separator + "xsd" + File.separator + "test.xsd",
                Tools.joinRelativeUrl("file:c:\\bil\\test\\test\\test.wsdl", "..\\..\\xsd\\test.xsd"));
    }
}
