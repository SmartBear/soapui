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
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

import java.util.Arrays;

public class JMSEndpoint {
    public static final String JMS_OLD_ENDPOINT_SEPARATOR = "/";
    public static final String JMS_ENDPOINT_SEPARATOR = "::";
    public static final String QUEUE_ENDPOINT_PREFIX = "queue_";
    public static final String TOPIC_ENDPOINT_PREFIX = "topic_";
    public static final String JMS_EMPTY_DESTIONATION = "-";
    public static final String JMS_ENDPOINT_PREFIX = "jms://";
    Request request;
    SubmitContext submitContext;
    String[] parameters;
    String sessionName;
    String send;
    String receive;

    public JMSEndpoint(Request request, SubmitContext submitContext) {
        this.request = request;
        this.submitContext = submitContext;
        parameters = extractEndpointParameters(request, submitContext);
        sessionName = getEndpointParameter(0);
        send = getEndpointParameter(1);
        receive = getEndpointParameter(2);
    }

    public JMSEndpoint(String sessionName, String send, String receive) {
        this.sessionName = sessionName;
        this.send = send;
        this.receive = receive;
    }

    public JMSEndpoint(String jmsEndpointString) {
        parameters = jmsEndpointString.replaceFirst(JMS_ENDPOINT_PREFIX, "").split(JMS_ENDPOINT_SEPARATOR);
        sessionName = getEndpointParameter(0);
        send = getEndpointParameter(1);
        receive = getEndpointParameter(2);
    }

    public static String[] extractEndpointParameters(Request request, SubmitContext context) {
        resolveOldEndpointPattern(request);

        String endpoint = PropertyExpander.expandProperties(context, request.getEndpoint());
        String[] parameters = endpoint.replaceFirst(JMS_ENDPOINT_PREFIX, "").split(JMS_ENDPOINT_SEPARATOR);
        return parameters;
    }

    private static void resolveOldEndpointPattern(Request request) {
        String oldEndpoint = request.getEndpoint();
        if (oldEndpoint.contains("/queue_") || oldEndpoint.contains("/topic_")) {
            String newEndpoint = request.getEndpoint()
                    .replaceAll(JMS_OLD_ENDPOINT_SEPARATOR + "queue_", JMS_ENDPOINT_SEPARATOR + "queue_")
                    .replaceAll(JMS_OLD_ENDPOINT_SEPARATOR + "topic_", JMS_ENDPOINT_SEPARATOR + "topic_")
                    .replaceAll(JMS_OLD_ENDPOINT_SEPARATOR + "-", JMS_ENDPOINT_SEPARATOR + "-");

            request.setEndpoint(newEndpoint);

            refreshEndpointList(request, oldEndpoint, newEndpoint);

            SoapUI.log("JMS endpoint resolver changed endpoint pattern from " + oldEndpoint + "to " + newEndpoint);
        }
    }

    private static void refreshEndpointList(Request request, String oldEndpoint, String newEndpoint) {
        Interface iface = request.getOperation().getInterface();
        for (String endpoint : iface.getEndpoints()) {
            if (endpoint.equals(oldEndpoint)) {
                iface.changeEndpoint(endpoint, newEndpoint);
            }
        }
    }

    private boolean checkParameterIndex(int parameterIndex, String[] parameters) throws IllegalArgumentException {
        if (parameterIndex < 0 || parameterIndex > 2) {
            throw new IllegalArgumentException(
                    "\n" +
                            "Illegal JMS endpoint parameter index: \" + parameterIndex \n" +
                            "For JMS please use this endpoint pattern: \n" +
                            "for sending 'jms://sessionName::queue_myqueuename' \n" +
                            "for receive 'jms://sessionName::-::queue_myqueuename' \n" +
                            "for send-receive 'jms://sessionName::queue_myqueuename1::queue_myqueuename2'"
            );
        }

        if (parameterIndex > parameters.length - 1) {
            SoapUI.log("JMS Endpoint String does not contain a parameter at index " +
                            parameterIndex + ", parameters: " + Arrays.toString(parameters)
            );
            return false;
        }

        return true;
    }

    private String getEndpointParameter(int i) {
        if (!checkParameterIndex(i, parameters)) {
            return "";
        }

        String stripParameter = PropertyExpander.expandProperties(submitContext, parameters[i])
                .replaceFirst(QUEUE_ENDPOINT_PREFIX, "").replaceFirst(TOPIC_ENDPOINT_PREFIX, "");
        return stripParameter;
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getSend() {
        return send;
    }

    public String getReceive() {
        return receive;
    }
}
