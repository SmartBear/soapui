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

import com.eviware.soapui.impl.rest.RestRequest;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * A matcher for REST requests with parameters.
 */
public class RestRequestWithParamsMatcher extends TypeSafeMatcher<RestRequest> {

    private RestRequestParamsMatcher parametersMatcher;
    private String parameterName;


    RestRequestWithParamsMatcher(String parameterName) {
        this.parameterName = parameterName;
        this.parametersMatcher = new RestRequestParamsMatcher(parameterName);
    }

    public RestRequestWithParamsMatcher withValue(String value) {
        RestRequestWithParamsMatcher matcherToReturn = new RestRequestWithParamsMatcher(parameterName);
        matcherToReturn.parametersMatcher = new RestRequestParamsMatcher(parameterName).withValue(value);
        return matcherToReturn;
    }

    @Override
    public boolean matchesSafely(RestRequest restRequest) {
        return parametersMatcher.matchesSafely(restRequest.getParams());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a REST request with ");
        parametersMatcher.describeTo(description);
    }
}
