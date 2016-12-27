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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.wsdl.BindingOperation;
import javax.wsdl.Part;

/**
 * Wrapper for WSDL parts
 *
 * @author ole.matzura
 */

public class MessageXmlPart {
    private XmlObject partXmlObject;
    private final XmlObject sourceXmlObject;
    private final Part part;
    private final BindingOperation bindingOperation;
    private final boolean isRequest;
    private final SchemaType type;

    public MessageXmlPart(XmlObject sourceXmlObject, SchemaType type, Part part, BindingOperation bindingOperation,
                          boolean isRequest) {
        this.sourceXmlObject = sourceXmlObject;
        this.type = type;
        this.part = part;
        this.bindingOperation = bindingOperation;
        this.isRequest = isRequest;
        partXmlObject = type == null ? sourceXmlObject.copy() : sourceXmlObject.copy().changeType(type);
    }

    public void update() {
        sourceXmlObject.set(partXmlObject);
    }

    public XmlCursor newCursor() {
        return partXmlObject.newCursor();
    }

    public boolean isAttachmentPart() {
        return isRequest ? WsdlUtils.isAttachmentInputPart(part, bindingOperation) : WsdlUtils.isAttachmentOutputPart(
                part, bindingOperation);
    }

    public Part getPart() {
        return part;
    }

    public SchemaType getSchemaType() {
        return type;
    }
}
