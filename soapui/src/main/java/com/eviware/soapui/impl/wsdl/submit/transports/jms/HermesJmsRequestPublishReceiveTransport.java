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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

public class HermesJmsRequestPublishReceiveTransport extends HermesJmsRequestTransport {

    public Response execute(SubmitContext submitContext, Request request, long timeStarted) throws Exception {
        Session topicSession = null;
        Session queueSession = null;

        JMSConnectionHolder jmsConnectionHolderTopic = null;
        JMSConnectionHolder jmsConnectionHolderQueue = null;
        try {
            init(submitContext, request);
            jmsConnectionHolderTopic = new JMSConnectionHolder(jmsEndpoint, hermes, true, clientID, username, password);
            jmsConnectionHolderQueue = new JMSConnectionHolder(jmsEndpoint, hermes, false, null, username, password);

            // session
            topicSession = jmsConnectionHolderTopic.getSession();
            queueSession = jmsConnectionHolderQueue.getSession();

            // destination
            Topic topicPublish = jmsConnectionHolderTopic.getTopic(jmsConnectionHolderTopic.getJmsEndpoint().getSend());
            Queue queueReceive = jmsConnectionHolderQueue
                    .getQueue(jmsConnectionHolderQueue.getJmsEndpoint().getReceive());

            Message messagePublish = messagePublish(submitContext, request, topicSession,
                    jmsConnectionHolderTopic.getHermes(), topicPublish, queueReceive);

            MessageConsumer messageConsumer = queueSession.createConsumer(queueReceive,
                    submitContext.expand(messageSelector));

            return makeResponse(submitContext, request, timeStarted, messagePublish, messageConsumer);
        } catch (JMSException jmse) {
            return errorResponse(submitContext, request, timeStarted, jmse);
        } catch (Throwable t) {
            SoapUI.logError(t);
        } finally {
            if (jmsConnectionHolderQueue != null) {
                jmsConnectionHolderQueue.closeAll();
            }

            closeSessionAndConnection(jmsConnectionHolderQueue != null ? jmsConnectionHolderQueue.getConnection() : null,
                    queueSession);
            closeSessionAndConnection(jmsConnectionHolderTopic != null ? jmsConnectionHolderTopic.getConnection() : null,
                    topicSession);

        }
        return null;

    }
}
