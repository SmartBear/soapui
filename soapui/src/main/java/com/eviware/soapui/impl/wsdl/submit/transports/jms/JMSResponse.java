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

package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.support.types.StringToStringsMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class JMSResponse implements WsdlResponse {

    private String payload;
    private Message messageReceive;
    private Message messageSend;
    private Attachment[] attachments = new Attachment[0];
    private Request request;
    private long requestStartedTime;
    private String endpoint;

    public JMSResponse(String payload, Message messageSend, Message messageReceive, Request request,
                       long requestStartedTime) {
        this.payload = payload;
        this.messageReceive = messageReceive;
        this.messageSend = messageSend;
        this.request = request;
        this.requestStartedTime = requestStartedTime;
        this.endpoint = request.getEndpoint();
    }

    public Attachment[] getAttachments() {
        return attachments;
    }

    public void setAttachments(Attachment[] attachments) {
        this.attachments = attachments;
    }

    public Attachment[] getAttachmentsForPart(String partName) {
        return attachments;
    }

    public String getContentAsString() {
        return payload;
    }

    public long getContentLength() {
        return payload.length();
    }

    public String getContentType() {
        if (messageReceive != null) {
            try {
                return messageReceive.getJMSType();
            } catch (JMSException e) {
                SoapUI.logError(e);
            }
        }
        return null;
    }

    public String getProperty(String name) {
        if (messageReceive != null) {
            try {
                return messageReceive.getStringProperty(name);
            } catch (JMSException e) {
                SoapUI.logError(e);
            }
        }
        return null;
    }

    public String[] getPropertyNames() {
        List<String> propertyNames = new ArrayList<String>();
        Enumeration<?> temp;
        try {
            if (messageReceive != null) {
                temp = messageReceive.getPropertyNames();
                while (temp.hasMoreElements()) {
                    propertyNames.add((String) temp.nextElement());
                }
                return propertyNames.toArray(new String[propertyNames.size()]);
            } else {
                return new String[0];
            }
        } catch (JMSException e) {
            SoapUI.logError(e);
        }
        return null;
    }

    public byte[] getRawRequestData() {
        if (messageSend != null) {
            return messageSend.toString().getBytes();
        } else {
            return "".getBytes();
        }
    }

    public byte[] getRawResponseData() {
        if (messageReceive != null) {
            return messageReceive.toString().getBytes();
        } else {
            return "".getBytes();
        }
    }

    public String getRequestContent() {
        if (messageSend != null) {
            try {
                if (messageSend instanceof TextMessage) {
                    return ((TextMessage) messageSend).getText();
                }
            } catch (JMSException e) {
                SoapUI.logError(e);
            }
            return messageSend.toString();
        }
        return "";
    }

    public StringToStringsMap getRequestHeaders() {
        if (messageSend != null) {
            return JMSHeader.getMessageHeadersAndProperties(messageSend);
        } else {
            return new StringToStringsMap();
        }

    }

    public StringToStringsMap getResponseHeaders() {
        if (messageReceive != null) {
            return JMSHeader.getMessageHeadersAndProperties(messageReceive);
        } else {
            return new StringToStringsMap();
        }
    }

    public long getTimeTaken() {
        return Calendar.getInstance().getTimeInMillis() - requestStartedTime;
    }

    public long getTimestamp() {
        try {
            if (messageReceive != null) {
                return messageReceive.getJMSTimestamp();
            } else {
                return 0;
            }
        } catch (JMSException e) {
            SoapUI.logError(e);
        }
        return 0;
    }

    public void setProperty(String name, String value) {
        try {
            messageReceive.setStringProperty(name, value);
        } catch (JMSException e) {
            SoapUI.logError(e);
        }

    }

    public String getContentAsXml() {
        if (payload != null && !"".equals(payload)) {
            return payload;
        } else {
            return "<xml/>";
        }
    }

    public String getHttpVersion() {
        return null;
    }

    public String getMethod() {
        return null;
    }

    public SSLInfo getSSLInfo() {
        return null;
    }

    public int getStatusCode() {
        return 0;
    }

    public URL getURL() {
        return null;
    }

    public void setResponseContent(String responseContent) {
        this.payload = responseContent;
    }

    public Vector<?> getWssResult() {
        return null;
    }

    public WsdlRequest getRequest() {
        return (WsdlRequest) request;
    }

    public Message getMessageReceive() {
        return messageReceive;
    }

    public Message getMessageSend() {
        return messageSend;
    }

    public String getEndpoint() {
        return endpoint;
    }

}
