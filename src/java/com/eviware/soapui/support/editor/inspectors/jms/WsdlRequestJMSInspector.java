/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.inspectors.jms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eviware.soapui.config.JMSDeliveryModeTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.xml.XmlInspector;

public class WsdlRequestJMSInspector extends AbstractJMSInspector implements XmlInspector, PropertyChangeListener
{

	private SimpleBindingForm simpleform;
	
	public WsdlRequestJMSInspector(WsdlRequest request)
	{
		super(request);
	}

	public void propertyChange(PropertyChangeEvent arg0)
	{

	}

	public void buildContent(SimpleBindingForm form)
	{
		this.simpleform=form;
		simpleform.addSpace(5);
		simpleform.appendTextField("JMSCorrelationID", "JMSCorrelationID", "JMSCorrelationID header property of JMS message");
		simpleform.appendTextField("JMSReplyTo", "JMSReplyTo", "JMSReplyTo header property of JMS message");
		simpleform.appendTextField("JMSType", "JMSType", "JMSType header property of JMS message");
		simpleform.appendTextField("JMSPriority", "JMSPriority", "JMSPriority header property of JMS message");
		simpleform.appendComboBox("JMSDeliveryMode", "JMSDeliveryMode", new String[] {
				JMSDeliveryModeTypeConfig.PERSISTENT.toString(),JMSDeliveryModeTypeConfig.NON_PERSISTENT.toString()},
				"Choose between NON PERSISTENT and PERSISTENT (default) message");
		simpleform.appendTextField("timeToLive", "TimeToLive",
				"specify 'time to live' of JMS message , zero means never expire which is default");
		simpleform.addSpace(5);
	}


}