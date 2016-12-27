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

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import javax.wsdl.Part;
import javax.xml.namespace.QName;

/**
 * A message part in a Request
 *
 * @author ole.matzura
 */

public interface MessagePart {
    public String getName();

    public String getDescription();

    public PartType getPartType();

    public enum PartType {
        HEADER, CONTENT, ATTACHMENT, FAULT, PARAMETER
    }

    ;

    public abstract static class ContentPart implements MessagePart {
        public abstract SchemaType getSchemaType();

        public abstract QName getPartElementName();

        public abstract SchemaGlobalElement getPartElement();

        public PartType getPartType() {
            return PartType.CONTENT;
        }
    }

    public abstract static class AttachmentPart implements MessagePart {
        public abstract String[] getContentTypes();

        public abstract boolean isAnonymous();

        public PartType getPartType() {
            return PartType.ATTACHMENT;
        }
    }

    public abstract static class HeaderPart extends ContentPart {
        public PartType getPartType() {
            return PartType.HEADER;
        }
    }

    public abstract static class ParameterPart extends ContentPart {
        public PartType getPartType() {
            return PartType.PARAMETER;
        }
    }

    public abstract static class FaultPart extends ContentPart {
        public PartType getPartType() {
            return PartType.FAULT;
        }

        public abstract Part[] getWsdlParts();
    }
}
