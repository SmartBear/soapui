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
package com.eviware.soapui.impl.wsdl.support.jms;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eviware.soapui.config.JMSDeliveryModeTypeConfig;
import com.eviware.soapui.config.JmsConfConfig;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSHeader;
import com.eviware.soapui.support.PropertyChangeNotifier;

public class JMSConfig implements PropertyChangeNotifier
{

	private JmsConfConfig jmsConfConfig;

	private PropertyChangeSupport propertyChangeSupport;

	private final JMSContainer container;

	public JMSConfig(JmsConfConfig jmsConfConfig, JMSContainer container)
	{
		this.jmsConfConfig = jmsConfConfig;
		this.container = container;
		propertyChangeSupport = new PropertyChangeSupport(this);
		if (!jmsConfConfig.isSetJMSDeliveryMode())
		{
			jmsConfConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.PERSISTENT);
		}
	}

	public JMSContainer getContainer()
	{
		return container;
	}

	public void setJmsConfConfig(JmsConfConfig jmsConfConfig)
	{
		this.jmsConfConfig = jmsConfConfig;
	}

	public String getJMSCorrelationID()
	{
		return jmsConfConfig.getJMSCorrelationID();
	}

	public String getJMSReplyTo()
	{
		return jmsConfConfig.getJMSReplyTo();
	}

	public String getJMSDeliveryMode()
	{
		return jmsConfConfig.getJMSDeliveryMode().toString();
	}

	public String getJMSPriority()
	{
		return jmsConfConfig.getJMSPriority();
	}

	public String getJMSType()
	{
		return jmsConfConfig.getJMSType();
	}

	public String getTimeToLive()
	{
		return jmsConfConfig.getTimeToLive();
	}

	public void setJMSCorrelationID(String newValue)
	{
		String oldValue = getJMSCorrelationID();
		jmsConfConfig.setJMSCorrelationID(newValue);
		propertyChangeSupport.firePropertyChange(JMSHeader.JMSCORRELATIONID, oldValue, newValue);
	}

	public void setJMSDeliveryMode(String newValue)
	{
		String oldValue = getJMSDeliveryMode();
		jmsConfConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.Enum.forString(newValue));
		propertyChangeSupport.firePropertyChange(JMSHeader.JMSDELIVERYMODE, oldValue, newValue);
	}

	public void setJMSPriority(String newValue)
	{
		String oldValue = getJMSPriority();
		jmsConfConfig.setJMSPriority(newValue);
		propertyChangeSupport.firePropertyChange(JMSHeader.JMSPRIORITY, oldValue, newValue);
	}

	public void setJMSReplyTo(String newValue)
	{
		String oldValue = getJMSReplyTo();
		jmsConfConfig.setJMSReplyTo(newValue);
		propertyChangeSupport.firePropertyChange(JMSHeader.JMSREPLYTO, oldValue, newValue);
	}

	public void setJMSType(String newValue)
	{
		String oldValue = getJMSType();
		jmsConfConfig.setJMSType(newValue);
		propertyChangeSupport.firePropertyChange(JMSHeader.JMSTYPE, oldValue, newValue);
	}

	public void setTimeToLive(String newValue)
	{
		String oldValue = getTimeToLive();
		jmsConfConfig.setTimeToLive(newValue);
		propertyChangeSupport.firePropertyChange(JMSHeader.TIMETOLIVE, oldValue, newValue);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

}
