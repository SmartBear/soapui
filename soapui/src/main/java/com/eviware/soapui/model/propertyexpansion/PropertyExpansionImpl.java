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

package com.eviware.soapui.model.propertyexpansion;

import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;

public class PropertyExpansionImpl implements PropertyExpansion {
    private String xpath;
    private TestProperty property;
    private String containerInfo;

    public PropertyExpansionImpl(TestProperty property, String xpath) {
        this.property = property;
        this.xpath = xpath;

        containerInfo = property.getName();
        if (property.getModelItem() != null) {
            containerInfo += " in " + property.getModelItem().getName();
        }
    }

    public TestProperty getProperty() {
        return property;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("${");

        ModelItem modelItem = property.getModelItem();

        if (modelItem instanceof Project) {
            result.append(PropertyExpansionImpl.PROJECT_REFERENCE);
        } else if (modelItem instanceof TestSuite) {
            result.append(PropertyExpansionImpl.TESTSUITE_REFERENCE);
        } else if (modelItem instanceof TestCase) {
            result.append(PropertyExpansionImpl.TESTCASE_REFERENCE);
        } else if (modelItem instanceof SecurityTest) {
            result.append(PropertyExpansionImpl.SECURITYTEST_REFERENCE);
        } else if (modelItem instanceof MockService) {
            result.append(PropertyExpansionImpl.MOCKSERVICE_REFERENCE);
        } else if (modelItem instanceof MockResponse) {
            result.append(PropertyExpansionImpl.MOCKRESPONSE_REFERENCE);
        } else if (modelItem instanceof TestStep) {
            result.append(modelItem.getName()).append(PROPERTY_SEPARATOR);
        } else if (modelItem instanceof TestRequest) {
            result.append(((TestRequest) modelItem).getTestStep().getName()).append(PROPERTY_SEPARATOR);
        }

        result.append(property.getName());
        if (StringUtils.hasContent(xpath)) {
            result.append(PROPERTY_SEPARATOR).append(xpath);
        }

        result.append('}');

        return result.toString();
    }

    public String getXPath() {
        return xpath;
    }

    public String getContainerInfo() {
        return containerInfo;
    }

    public void setContainerInfo(String containerInfo) {
        this.containerInfo = containerInfo;
    }

    protected void setProperty(TestProperty property) {
        this.property = property;
    }

    protected void setXPath(String xpath) {
        this.xpath = xpath;
    }
}
