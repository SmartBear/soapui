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

package com.eviware.soapui.model.iface;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.ResultContainer;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

/**
 * An exchange of a request and response message
 *
 * @author ole.matzura
 */

public interface MessageExchange extends ResultContainer {
    public Operation getOperation();

    public ModelItem getModelItem();

    public long getTimestamp();

    public long getTimeTaken();

    public String getEndpoint();

    public StringToStringMap getProperties();

    public String getRequestContent();

    public String getResponseContent();

    public String getRequestContentAsXml();

    public String getResponseContentAsXml();

    public StringToStringsMap getRequestHeaders();

    public StringToStringsMap getResponseHeaders();

    public Attachment[] getRequestAttachments();

    public Attachment[] getResponseAttachments();

    public String[] getMessages();

    public boolean isDiscarded();

    public boolean hasRawData();

    public byte[] getRawRequestData();

    public byte[] getRawResponseData();

    public Attachment[] getRequestAttachmentsForPart(String partName);

    public Attachment[] getResponseAttachmentsForPart(String partName);

    public boolean hasRequest(boolean ignoreEmpty);

    public boolean hasResponse();

    public Response getResponse();

    public String getProperty(String name);
}
