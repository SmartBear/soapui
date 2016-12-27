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

package com.eviware.soapui.impl.wsdl.submit.filters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveEmptyContentRequestFilterTestCase {

    @Test
    public void testRemoval() throws Exception {
        assertEquals(doRemoval("<test><testing/></test>"), "<test/>");
        assertEquals(doRemoval("<test><testing test=\"\"/></test>"), "<test/>");

        assertEquals(doRemoval("<test><testing>   </testing></test>"), "<test/>");
        assertEquals(doRemoval("<test><testing>  <testar test=\"\"></testar> </testing></test>"),
                "<test><testing>   </testing></test>");

        assertEquals(doRemoval("<test><testing>\n   <testar test=\"\"></testar>\n </testing></test>"),
                "<test><testing>\n   \n </testing></test>");

        assertEquals(doRemoval("<test></test>"), "<test></test>");

        assertEquals(doRemoval("<test><testing/><testing/></test>"), "<test/>");

        assertEquals(
                doRemoval("<dat1:documentType xmlns:dat1=\"test\"><dat1:listName test=\"\" xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/></dat1:documentType>"),
                "<dat1:documentType xmlns:dat1=\"test\"/>");

    }

    private String doRemoval(String request) throws Exception {
        return RemoveEmptyContentRequestFilter.removeEmptyContent(request, null, true);
    }
}
