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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import org.apache.xmlbeans.SchemaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Descriptor for attachments
 *
 * @author Ole.Matzura
 */

public final class HttpAttachmentPart extends MessagePart.AttachmentPart {
    public static final String ANONYMOUS_NAME = "<anonymous>";
    private String name;
    private List<String> contentTypes = new ArrayList<String>();
    private Attachment.AttachmentType type;
    private boolean anonymous;
    private SchemaType schemaType;

    public HttpAttachmentPart() {
        anonymous = true;
        name = ANONYMOUS_NAME;
        type = Attachment.AttachmentType.UNKNOWN;
    }

    public HttpAttachmentPart(String name, List<String> types) {
        super();
        this.name = name;

        if (types != null) {
            contentTypes.addAll(types);
        }
    }

    public HttpAttachmentPart(String name, String type) {
        this.name = name;
        if (type != null) {
            contentTypes.add(type);
        }
    }

    public String[] getContentTypes() {
        return contentTypes.toArray(new String[contentTypes.size()]);
    }

    public String getName() {
        return name;
    }

    public void addContentType(String contentType) {
        contentTypes.add(contentType);
    }

    public Attachment.AttachmentType getAttachmentType() {
        return type;
    }

    public void setType(Attachment.AttachmentType type) {
        this.type = type;
    }

    public String getDescription() {
        return name + " attachment; [" + Arrays.toString(getContentTypes()) + "]";
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public SchemaType getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(SchemaType schemaType) {
        this.schemaType = schemaType;
    }
}
