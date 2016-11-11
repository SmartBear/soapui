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
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicSubscriber;

public class HermesJmsRequestSubscribeTransport extends HermesJmsRequestTransport {

    public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception {
        Session topicSession = null;
        TopicSubscriber topicDurableSubsriber = null;
        JMSConnectionHolder jmsConnectionHolder = null;
        try {
            init(submitContext, request);
            String clientIDString = StringUtils.hasContent(clientID) ? clientID : jmsEndpoint.getSessionName() + "-"
                    + jmsEndpoint.getReceive();
            jmsConnectionHolder = new JMSConnectionHolder(jmsEndpoint, hermes, true, clientIDString, username, password);

            // session
            topicSession = jmsConnectionHolder.getSession();
            // destination
            topicDurableSubsriber = createDurableSubscription(submitContext, topicSession, jmsConnectionHolder);

            return makeResponse(submitContext, request, timeStarted, null, topicDurableSubsriber);
        } catch (JMSException jmse) {
            return errorResponse(submitContext, request, timeStarted, jmse);
        } catch (Throwable t) {
            SoapUI.logError(t);
        } finally {
            if (topicDurableSubsriber != null) {
                topicDurableSubsriber.close();
            }
            if (jmsConnectionHolder != null) {
                jmsConnectionHolder.closeAll();
            }
            closeSessionAndConnection(jmsConnectionHolder != null ? jmsConnectionHolder.getConnection() : null,
                    topicSession);
        }
        return null;
    }
}
