/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.inspectors.jms.header;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eviware.soapui.config.JMSDeliveryModeTypeConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSEndpoint;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.xml.XmlInspector;

public class RequestJMSHeaderInspector extends AbstractJMSHeaderInspector implements XmlInspector,
		PropertyChangeListener
{

	private SimpleBindingForm simpleform;
	AbstractHttpRequest<?> request;

	public RequestJMSHeaderInspector( AbstractHttpRequest<?> request )
	{
		super( request );
		this.request = request;
		request.addPropertyChangeListener( this );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( request.getEndpoint() != null && evt.getPropertyName().equals( AbstractHttpRequest.ENDPOINT_PROPERTY ) )
		{
			this.setEnabled( request.getEndpoint().startsWith( JMSEndpoint.JMS_ENDPIONT_PREFIX ) );
		}
	}

	@Override
	public void release()
	{
		super.release();
		request.removePropertyChangeListener( this );
	}

	public void buildContent( SimpleBindingForm form )
	{
		this.simpleform = form;
		simpleform.addSpace( 5 );
		simpleform.appendTextField( "JMSCorrelationID", "JMSCorrelationID",
				"JMSCorrelationID header property of JMS message" );
		simpleform.appendTextField( "JMSReplyTo", "JMSReplyTo", "JMSReplyTo header property of JMS message" );
		simpleform.appendTextField( "JMSType", "JMSType", "JMSType header property of JMS message" );
		simpleform.appendTextField( "JMSPriority", "JMSPriority", "JMSPriority header property of JMS message" );
		simpleform.appendComboBox( "JMSDeliveryMode", "JMSDeliveryMode", new String[] {
				JMSDeliveryModeTypeConfig.PERSISTENT.toString(), JMSDeliveryModeTypeConfig.NON_PERSISTENT.toString() },
				"Choose between NON PERSISTENT and PERSISTENT (default) message" );
		simpleform.appendTextField( "timeToLive", "TimeToLive",
				"specify 'time to live' of JMS message , zero means never expire which is default" );

		simpleform.appendCheckBox( "sendAsBytesMessage", "Send As Bytes Message", "" ).setToolTipText(
				"if selected message will be sent as BytesMessage" );

		if( request.getOperation() != null )
		{
			simpleform.appendCheckBox( "soapActionAdd", "Add SoapAction as property", "" ).setToolTipText(
					"Add properties SOAPJMS_soapAction=" + request.getOperation().getName() + "\n and " + "SoapAction="
							+ request.getOperation().getName() + " to outgoing message" );
		}

		simpleform
				.appendTextField(
						"durableSubscriptionName",
						"Durable Subscription Name",
						"specify 'Durable Subscription Name' for subscribing to topic , if not specified automatic name is 'durableSubscription' + 'topic name'" );
		simpleform.appendTextField( "clientID", "ClientID", "specify optional 'ClientID' for of JMS connection" );
		simpleform.appendTextField( "messageSelector", "Message Selector",
				"specify message selector string to determine which messages you want to receive" );
		simpleform.addSpace( 5 );
	}

}
