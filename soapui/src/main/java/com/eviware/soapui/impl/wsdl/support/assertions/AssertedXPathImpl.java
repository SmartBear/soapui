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

package com.eviware.soapui.impl.wsdl.support.assertions;

import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.TestAssertion;
import org.apache.xmlbeans.XmlObject;

public class AssertedXPathImpl implements AssertedXPath {
    private final TestAssertion assertion;
    private final String path;
    private XmlObject assertedContent;

    public AssertedXPathImpl(TestAssertion assertion, String path, XmlObject assertedContent) {
        this.assertion = assertion;
        this.path = path;
        this.assertedContent = assertedContent;
    }

    public TestAssertion getAssertion() {
        return assertion;
    }

    public String getLabel() {
        return assertion.getName();
    }

    public String getPath() {
        return path;
    }

    public XmlObject getAssertedContent() {
        return assertedContent;
    }

    public void setAssertedContent(XmlObject assertedContent) {
        this.assertedContent = assertedContent;
    }

}
