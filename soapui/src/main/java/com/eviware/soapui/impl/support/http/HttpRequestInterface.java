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

package com.eviware.soapui.impl.support.http;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.MutableAttachmentContainer;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;

public interface HttpRequestInterface<T extends AbstractRequestConfig> extends AbstractHttpRequestInterface<T>,
        Request, MutableTestPropertyHolder, PropertyExpansionContainer, MutableAttachmentContainer, MediaType {

    public void setMethod(RestRequestInterface.HttpMethod method);

    public boolean hasRequestBody();

    public RestParamsPropertyHolder getParams();

    public boolean isPostQueryString();

    public void setPostQueryString(boolean b);

    public String getResponseContentAsXml();

    public void updateConfig(T request);

    public String getPath();

    public String getMultiValueDelimiter();

}
