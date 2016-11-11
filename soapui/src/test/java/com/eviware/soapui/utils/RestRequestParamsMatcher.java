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

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * @author manne
 */
public class RestRequestParamsMatcher extends TypeSafeMatcher<RestParamsPropertyHolder> {

    private String parameterName;
    private String parameterValue;

    RestRequestParamsMatcher(String parameterName) {
        this.parameterName = parameterName;
    }

    public RestRequestParamsMatcher withValue(String value) {
        RestRequestParamsMatcher matcherToReturn = new RestRequestParamsMatcher(parameterName);
        matcherToReturn.parameterValue = value;
        return matcherToReturn;
    }

    @Override
    public boolean matchesSafely(RestParamsPropertyHolder restParameters) {
        RestParamProperty property = restParameters.getProperty(parameterName);
        return property != null && (parameterValue == null || parameterValue.equals(parameterValue));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a REST requests having a parameter named '" + parameterName + "'");
        if (parameterValue != null) {
            description.appendText(" with the value '" + parameterValue + "'");
        }
    }
}

