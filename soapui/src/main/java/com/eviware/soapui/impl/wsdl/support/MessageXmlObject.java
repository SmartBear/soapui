/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.wsdl.BindingOperation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * XmlObject based wrapper for manipulation, etc..
 *
 * @author Ole.Matzura
 */

public class MessageXmlObject {
    private XmlObject messageObj;
    private WsdlContext wsdlContext;
    private List<MessageXmlPart> messageParts = new ArrayList<MessageXmlPart>();

    private final static Logger log = LogManager.getLogger(MessageXmlObject.class);
    private final String messageContent;
    private WsdlOperation operation;
    private final boolean isRequest;

    public MessageXmlObject(WsdlOperation operation, String messageContent, boolean isRequest) {
        this.messageContent = messageContent;
        this.operation = operation;
        this.isRequest = isRequest;
        wsdlContext = operation.getInterface().getWsdlContext();
    }

    public String getMessageContent() {
        if (messageObj == null) {
            return messageContent;
        } else {
            for (int c = 0; c < messageParts.size(); c++) {
                messageParts.get(c).update();
            }

            return messageObj.xmlText();
        }
    }

    public XmlObject getMessageObj() throws XmlException {
        if (messageObj == null) {
            // messageObj = XmlObject.Factory.parse( getMessageContent() );
            messageObj = XmlUtils.createXmlObject(getMessageContent(), XmlUtils.createDefaultXmlOptions());
        }

        return messageObj;
    }

    public MessageXmlPart[] getMessageParts() throws Exception {
        String operationName = operation.getName();
        BindingOperation bindingOperation = operation.getBindingOperation();
        if (bindingOperation == null) {
            throw new Exception("Missing operation [" + operationName + "] in wsdl definition");
        }

        if (!wsdlContext.hasSchemaTypes()) {
            throw new Exception("Missing schema types for message");
        }

        XmlObject msgXml = getMessageObj();
        Part[] inputParts = isRequest ? WsdlUtils.getInputParts(bindingOperation) : WsdlUtils
                .getOutputParts(bindingOperation);
        messageParts.clear();

        if (WsdlUtils.isRpc(wsdlContext.getDefinition(), bindingOperation)) {
            // get root element
            XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                    + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='"
                    + WsdlUtils.getTargetNamespace(wsdlContext.getDefinition()) + "';" + "$this/env:Envelope/env:Body/ns:"
                    + bindingOperation.getName());

            if (paths.length != 1) {
                throw new Exception("Missing message wrapper element ["
                        + WsdlUtils.getTargetNamespace(wsdlContext.getDefinition()) + "@" + bindingOperation.getName());
            } else {
                XmlObject wrapper = paths[0];

                for (int i = 0; i < inputParts.length; i++) {
                    Part part = inputParts[i];

                    QName partName = part.getElementName();
                    if (partName == null) {
                        partName = new QName(part.getName());
                    }

                    XmlObject[] children = wrapper.selectChildren(partName);

                    // attachment part?
                    if (WsdlUtils.isAttachmentInputPart(part, bindingOperation)) {
                        // not required
                        if (children.length == 1) {
                            QName typeName = part.getTypeName();
                            SchemaType type = typeName == null ? null : wsdlContext.getSchemaTypeLoader().findType(typeName);
                            messageParts.add(new MessageXmlPart(children[0], type, part, bindingOperation, isRequest));
                        }
                    } else if (children.length != 1) {
                        log.error("Missing message part [" + part.getName() + "]");
                    } else {
                        QName typeName = part.getTypeName();
                        if (typeName == null) {
                            typeName = partName;
                            SchemaGlobalElement type = wsdlContext.getSchemaTypeLoader().findElement(typeName);

                            if (type != null) {
                                messageParts.add(new MessageXmlPart(children[0], type.getType(), part, bindingOperation,
                                        isRequest));
                            } else {
                                log.error("Missing element [" + typeName + "] in associated schema for part ["
                                        + part.getName() + "]");
                            }
                        } else {
                            SchemaType type = wsdlContext.getSchemaTypeLoader().findType(typeName);
                            if (type != null) {
                                messageParts.add(new MessageXmlPart(children[0], type, part, bindingOperation, isRequest));
                            } else {
                                log.error("Missing type [" + typeName + "] in associated schema for part [" + part.getName()
                                        + "]");
                            }
                        }
                    }
                }
            }
        } else {
            Part part = inputParts[0];
            QName elementName = part.getElementName();
            if (elementName != null) {
                // just check for correct message element, other elements are
                // avoided (should create an error)
                XmlObject[] paths = msgXml.selectPath("declare namespace env='"
                        + wsdlContext.getSoapVersion().getEnvelopeNamespace() + "';" + "declare namespace ns='"
                        + elementName.getNamespaceURI() + "';" + "$this/env:Envelope/env:Body/ns:"
                        + elementName.getLocalPart());

                if (paths.length == 1) {
                    SchemaGlobalElement elm = wsdlContext.getSchemaTypeLoader().findElement(elementName);
                    if (elm != null) {
                        messageParts.add(new MessageXmlPart(paths[0], elm.getType(), part, bindingOperation, isRequest));
                    } else {
                        throw new Exception("Missing part type in associated schema");
                    }
                } else {
                    throw new Exception("Missing message part with name [" + elementName + "]");
                }
            }
        }

        return messageParts.toArray(new MessageXmlPart[messageParts.size()]);
    }
}
