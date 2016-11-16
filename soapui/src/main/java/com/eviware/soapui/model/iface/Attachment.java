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

import java.io.InputStream;

/**
 * Attachment for Requests and their responses
 *
 * @author Ole.Matzura
 */

public interface Attachment {
    public String getName();

    public String getContentType();

    public void setContentType(String contentType);

    public long getSize();

    public String getPart();

    public void setPart(String part);

    public InputStream getInputStream() throws Exception;

    public String getUrl();

    public boolean isCached();

    public AttachmentType getAttachmentType();

    public enum AttachmentType {
        MIME, XOP, CONTENT, SWAREF, UNKNOWN
    }

    public String getContentID();

    public enum AttachmentEncoding {
        BASE64, HEX, NONE
    }

    public AttachmentEncoding getEncoding();

    public String getContentEncoding();

    public String getId();
}
