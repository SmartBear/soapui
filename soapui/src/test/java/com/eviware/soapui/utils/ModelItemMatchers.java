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

package com.eviware.soapui.utils;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Hamcrest matchers for different SoapUI model items.
 */
public class ModelItemMatchers {

    public static Matcher<ModelItem> belongsTo(final Project project) {
        return new TypeSafeMatcher<ModelItem>() {
            @Override
            public boolean matchesSafely(ModelItem modelItem) {
                ModelItem parent;
                while ((parent = modelItem.getParent()) != null) {
                    if (parent.equals(project)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an item in the project " + project);
            }
        };
    }

    public static Matcher<WsdlTestSuite> hasATestCaseNamed(final String testCaseName) {
        return new TypeSafeMatcher<WsdlTestSuite>() {
            @Override
            public boolean matchesSafely(WsdlTestSuite wsdlTestSuite) {
                return wsdlTestSuite.getTestCases().containsKey(testCaseName);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a TestSuite with a test case named " + testCaseName);
            }
        };
    }

    public static RestRequestWithParamsMatcher hasARestParameterNamed(String name) {
        return new RestRequestWithParamsMatcher(name);
    }

    public static RestRequestParamsMatcher hasParameter(final String parameterName) {
        return new RestRequestParamsMatcher(parameterName);
    }
}
