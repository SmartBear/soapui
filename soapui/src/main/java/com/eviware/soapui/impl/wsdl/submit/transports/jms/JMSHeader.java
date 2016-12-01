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
import com.eviware.soapui.config.JMSPropertyConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderConfig;
import com.eviware.soapui.impl.wsdl.support.jms.property.JMSPropertiesConfig;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import hermes.Domain;
import hermes.Hermes;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.NamingException;
import java.util.Enumeration;
import java.util.List;

/**
 * @author nebojsa.tasic
 */
public class JMSHeader {
    public static final String JMSCORRELATIONID = "JMSCorrelationID";
    public static final String JMSREPLYTO = "JMSReplyTo";
    public static final String TIMETOLIVE = "timeToLive";
    public static final String JMSTYPE = "JMSType";
    public static final String JMSPRIORITY = "JMSPriority";
    public static final String JMSDELIVERYMODE = "JMSDeliveryMode";
    public static final String JMSEXPIRATION = "JMSExpiration";
    public static final String JMSMESSAGEID = "JMSMessageID";
    public static final String JMSTIMESTAMP = "JMSTimestamp";
    public static final String JMSREDELIVERED = "JMSRedelivered";
    public static final String JMSDESTINATION = "JMSDestination";
    public static final String DURABLE_SUBSCRIPTION_NAME = "durableSubscriptionName";
    public static final String MESSAGE_SELECTOR = "messageSelector";
    public static final String CLIENT_ID = "clientID";
    public static final String SEND_AS_BYTESMESSAGE = "sendAsBytesMessage";
    public static final String SOAP_ACTION_ADD = "soapActionAdd";
    public static final String SOAP_ACTION = "SOAPAction";
    public static final String SOAPJMS_SOAP_ACTION = "SOAPJMS_soapAction";

    private long timeTolive = Message.DEFAULT_TIME_TO_LIVE;

    public void setMessageHeaders(Message message, Request request, Hermes hermes, SubmitContext submitContext) {
        if (request instanceof AbstractHttpRequest) {
            JMSHeaderConfig jmsConfig = ((AbstractHttpRequest<?>) request).getJMSHeaderConfig();
            try {
                // JMSCORRELATIONID
                if (jmsConfig.getJMSCorrelationID() != null && !jmsConfig.getJMSCorrelationID().equals("")) {
                    message.setJMSCorrelationID(PropertyExpander.expandProperties(submitContext,
                            jmsConfig.getJMSCorrelationID()));
                }

                // JMSREPLYTO
                if (jmsConfig.getJMSReplyTo() != null && !jmsConfig.getJMSReplyTo().equals("")) {
                    message.setJMSReplyTo(hermes.getDestination(
                            PropertyExpander.expandProperties(submitContext, jmsConfig.getJMSReplyTo()), Domain.QUEUE));
                }

                // TIMETOLIVE
                if (jmsConfig.getTimeToLive() != null && !jmsConfig.getTimeToLive().equals("")) {
                    setTimeTolive(Long.parseLong(PropertyExpander.expandProperties(submitContext,
                            jmsConfig.getTimeToLive())));
                } else {
                    setTimeTolive(Message.DEFAULT_TIME_TO_LIVE);
                }

                // JMSTYPE
                if (jmsConfig.getJMSType() != null && !jmsConfig.getJMSType().equals("")) {
                    message.setJMSType(PropertyExpander.expandProperties(submitContext, jmsConfig.getJMSType()));
                }

                // JMSPRIORITY
                if (jmsConfig.getJMSPriority() != null && !jmsConfig.getJMSPriority().equals("")) {
                    message.setJMSPriority(Integer.parseInt(PropertyExpander.expandProperties(submitContext,
                            jmsConfig.getJMSPriority())));
                } else {
                    message.setJMSPriority(Message.DEFAULT_PRIORITY);
                }

                // JMSDELIVERYMODE
                if (jmsConfig.getJMSDeliveryMode() != null && !jmsConfig.getJMSDeliveryMode().equals("")) {
                    int deliveryMode = jmsConfig.getJMSDeliveryMode().equals("PERSISTENT") ? javax.jms.DeliveryMode.PERSISTENT
                            : javax.jms.DeliveryMode.NON_PERSISTENT;
                    message.setJMSDeliveryMode(deliveryMode);
                } else {
                    message.setJMSDeliveryMode(Message.DEFAULT_DELIVERY_MODE);
                }

            } catch (NamingException e) {
                SoapUI.logError(
                        e,
                        "Message header JMSReplyTo = "
                                + PropertyExpander.expandProperties(submitContext, jmsConfig.getJMSReplyTo())
                                + "destination not exists!");
            } catch (Exception e) {
                SoapUI.logError(e, "error while seting message header properties!");
            }
        }
    }

    public static void setMessageProperties(Message message, Request request, Hermes hermes, SubmitContext submitContext) {
        if (request instanceof AbstractHttpRequest) {
            JMSPropertiesConfig jmsPropertyConfig = ((AbstractHttpRequest<?>) request).getJMSPropertiesConfig();
            try {
                List<JMSPropertyConfig> propertyList = jmsPropertyConfig.getJMSProperties();
                StringToStringMap stringToStringMap = new StringToStringMap(propertyList.size());
                for (JMSPropertyConfig jmsProperty : propertyList) {
                    stringToStringMap.put(jmsProperty.getName(), jmsProperty.getValue());
                }

                // CUSTOM PROPERTIES
                String keys[] = stringToStringMap.getKeys();
                for (String key : keys) {
                    if (!key.equals(JMSCORRELATIONID) && !key.equals(JMSREPLYTO) && !key.equals(TIMETOLIVE)
                            && !key.equals(JMSTYPE) && !key.equals(JMSPRIORITY) && !key.equals(JMSDELIVERYMODE)) {
                        message.setStringProperty(key,
                                PropertyExpander.expandProperties(submitContext, stringToStringMap.get(key)));
                    }
                }
            } catch (Exception e) {
                SoapUI.logError(e, "error while seting jms message properties!");
            }
        }
    }

    public long getTimeTolive() {
        return timeTolive;
    }

    public void setTimeTolive(long timeTolive) {
        this.timeTolive = timeTolive;
    }

    public static StringToStringsMap getMessageHeadersAndProperties(Message message) {
        StringToStringsMap headermap = new StringToStringsMap();
        try {
            headermap.put(JMSDELIVERYMODE, String.valueOf(message.getJMSDeliveryMode()));
            headermap.put(JMSEXPIRATION, String.valueOf(message.getJMSExpiration()));
            headermap.put(JMSPRIORITY, String.valueOf(message.getJMSPriority()));
            headermap.put(JMSTIMESTAMP, String.valueOf(message.getJMSTimestamp()));
            headermap.put(JMSREDELIVERED, String.valueOf(message.getJMSRedelivered()));

            if (message.getJMSDestination() != null) {
                headermap.put(JMSDESTINATION, String.valueOf(message.getJMSDestination()));
            }

            if (message.getJMSMessageID() != null) {
                headermap.put(JMSMESSAGEID, message.getJMSMessageID());
            }

            if (message.getJMSCorrelationID() != null) {
                headermap.put(JMSCORRELATIONID, message.getJMSCorrelationID());
            }

            if (message.getJMSReplyTo() != null) {
                headermap.put(JMSREPLYTO, String.valueOf(message.getJMSReplyTo()));
            }

            if (message.getJMSType() != null) {
                headermap.put(JMSTYPE, message.getJMSType());
            }

            Enumeration<?> properties = message.getPropertyNames();
            while (properties.hasMoreElements()) {
                String key = (String) properties.nextElement();
                headermap.put(key, message.getStringProperty(key));
            }

        } catch (JMSException e) {
            SoapUI.logError(e);
        }
        return headermap;
    }
}
