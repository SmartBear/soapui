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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.AbstractMockResponse;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestMockService;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.SecurityTest;

public class ModelItemPropertyResolver implements PropertyResolver {
    public String resolveProperty(PropertyExpansionContext context, String pe, boolean globalOverride) {
        if (pe.charAt(0) == PropertyExpansion.SCOPE_PREFIX) {
            return getScopedProperty(context, pe, globalOverride);
        }

        ModelItem modelItem = context.getModelItem();
        if (modelItem instanceof WsdlLoadTest) {
            modelItem = ((WsdlLoadTest) modelItem).getTestCase();
        } else if (modelItem instanceof TestRequest) {
            modelItem = ((TestRequest) modelItem).getTestStep();
        } else if (modelItem instanceof AbstractMockResponse
                && ((AbstractMockResponse) modelItem).getMockOperation().getMockService() instanceof WsdlTestMockService) {
            modelItem = ((WsdlTestMockService) ((AbstractMockResponse) modelItem).getMockOperation().getMockService())
                    .getMockResponseStep();
        }
        if (modelItem instanceof SecurityTest) {
            modelItem = ((SecurityTest) modelItem).getTestCase();
        }

        if (modelItem instanceof WsdlTestStep || modelItem instanceof WsdlTestCase) {
            WsdlTestStep testStep = (WsdlTestStep) (modelItem instanceof WsdlTestStep ? modelItem : null);
            WsdlTestCase testCase = (WsdlTestCase) (testStep == null ? modelItem : testStep.getTestCase());

            int sepIx = pe.indexOf(PropertyExpansion.PROPERTY_SEPARATOR);
            Object property = null;

            if (sepIx > 0) {
                String step = pe.substring(0, sepIx);
                String name = pe.substring(sepIx + 1);
                String xpath = null;

                sepIx = name.indexOf(PropertyExpansion.PROPERTY_SEPARATOR);
                WsdlTestStep ts = testCase.getTestStepByName(step);

                if (sepIx != -1) {
                    xpath = name.substring(sepIx + 1);
                    name = name.substring(0, sepIx);
                }

                if (step != null) {
                    if (ts != null) {
                        TestProperty p = ts.getProperty(name);
                        if (p != null) {
                            property = p.getValue();
                        }
                    }
                } else {
                    property = context.getProperty(name);
                }

                if (property != null && xpath != null) {
                    property = ResolverUtils.extractXPathPropertyValue(property,
                            PropertyExpander.expandProperties(context, xpath));
                }
            }

            if (property != null) {
                return property.toString();
            }
        }

        return null;
    }

    private String getScopedProperty(PropertyExpansionContext context, String pe, boolean globalOverride) {
        ModelItem modelItem = context.getModelItem();

        TestStep testStep = null;
        TestCase testCase = null;
        TestSuite testSuite = null;
        Project project = null;
        MockService mockService = null;
        AbstractMockResponse mockResponse = null;
        SecurityTest securityTest = null;

        if (modelItem instanceof WsdlTestStep) {
            testStep = (WsdlTestStep) modelItem;
            testCase = testStep.getTestCase();
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        } else if (modelItem instanceof WsdlTestCase) {
            testCase = (WsdlTestCase) modelItem;
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        } else if (modelItem instanceof WsdlLoadTest) {
            testCase = ((WsdlLoadTest) modelItem).getTestCase();
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        } else if (modelItem instanceof WsdlTestSuite) {
            testSuite = (WsdlTestSuite) modelItem;
            project = testSuite.getProject();
        } else if (modelItem instanceof WsdlInterface) {
            project = ((WsdlInterface) modelItem).getProject();
        } else if (modelItem instanceof WsdlProject) {
            project = (WsdlProject) modelItem;
        } else if (modelItem instanceof WsdlMockService) {
            mockService = (WsdlMockService) modelItem;
            project = mockService.getProject();
        } else if (modelItem instanceof TestRequest) {
            testStep = ((TestRequest) modelItem).getTestStep();
            testCase = testStep.getTestCase();
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        } else if (modelItem instanceof AbstractHttpRequestInterface<?>) {
            project = ((AbstractHttpRequest<?>) modelItem).getOperation().getInterface().getProject();
        } else if (modelItem instanceof RestResource) {
            project = modelItem.getProject();
        } else if (modelItem instanceof WsdlMockOperation) {
            mockService = ((WsdlMockOperation) modelItem).getMockService();
            project = mockService.getProject();
        } else if (modelItem instanceof AbstractMockResponse) {
            mockResponse = (AbstractMockResponse) modelItem;
            mockService = mockResponse.getMockOperation().getMockService();
            project = mockService.getProject();
        } else if (modelItem instanceof SecurityTest) {
            securityTest = (SecurityTest) modelItem;
            testCase = ((SecurityTest) modelItem).getTestCase();
            testSuite = testCase.getTestSuite();
            project = testSuite.getProject();
        } else if (modelItem instanceof OAuth2Profile) {
            project = ((WsdlProject) modelItem.getParent());
        }

        // no project -> nothing
        if (project == null) {
            return null;
        }

        // explicit item reference?
        String result = ResolverUtils.checkForExplicitReference(pe, PropertyExpansion.PROJECT_REFERENCE, project,
                context, globalOverride);
        if (result != null) {
            return result;
        }

        result = ResolverUtils.checkForExplicitReference(pe, PropertyExpansion.TESTSUITE_REFERENCE, testSuite, context,
                globalOverride);
        if (result != null) {
            return result;
        }

        result = ResolverUtils.checkForExplicitReference(pe, PropertyExpansion.TESTCASE_REFERENCE, testCase, context,
                globalOverride);
        if (result != null) {
            return result;
        }

        result = ResolverUtils.checkForExplicitReference(pe, PropertyExpansion.MOCKSERVICE_REFERENCE, mockService,
                context, globalOverride);
        if (result != null) {
            return result;
        }

        result = ResolverUtils.checkForExplicitReference(pe, PropertyExpansion.MOCKRESPONSE_REFERENCE, mockResponse,
                context, globalOverride);
        if (result != null) {
            return result;
        }

        result = ResolverUtils.checkForExplicitReference(pe, PropertyExpansion.SECURITYTEST_REFERENCE, securityTest,
                context, globalOverride);
        if (result != null) {
            return result;
        }

        return null;
    }
}
