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

/**
 * Request interface
 *
 * @author Ole.Matzura
 */

public interface Request extends ModelItem {
    public final String REQUEST_PROPERTY = "request";
    public final String ENDPOINT_PROPERTY = "endpoint";
    public final String ENCODING_PROPERTY = "encoding";
    public final String MEDIA_TYPE = "mediaType";

    public String getRequestContent();

    public void setEndpoint(String string);

    public String getEndpoint();

    public String getEncoding();

    public String getTimeout();

    public void setEncoding(String string);

    public Operation getOperation();

    public void addSubmitListener(SubmitListener listener);

    public void removeSubmitListener(SubmitListener listener);

    public Submit submit(SubmitContext submitContext, boolean async) throws SubmitException;

    public Attachment[] getAttachments();

    public MessagePart[] getRequestParts();

    public MessagePart[] getResponseParts();

    public String getUsername();

    public String getPassword();

    public String getAuthType();

    public boolean dependsOn(ModelItem modelItem);

    @SuppressWarnings("serial")
    public static class SubmitException extends Exception {
        public SubmitException(String msg) {
            super(msg);
        }

        public SubmitException(String message, Throwable cause) {
            super(message, cause);
        }

        public SubmitException(Throwable cause) {
            super(cause);
        }
    }
}
