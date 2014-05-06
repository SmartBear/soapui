/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.model.iface.Attachment;

/**
 * WSDL-specific Attachment behaviour
 *
 * @author ole.matzura
 */

public interface WsdlAttachment extends Attachment {
    public void updateConfig(AttachmentConfig config);

    public XmlObject getConfig();

    public void setContentID(String contentID);

    public void reload(File file, boolean cache) throws IOException;

    public void setName(String value);

    public void setUrl(String string);
}
