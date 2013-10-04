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

package com.eviware.soapui.impl.wsdl.support.jms.property;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import com.eviware.soapui.config.JMSPropertiesConfConfig;
import com.eviware.soapui.config.JMSPropertyConfig;
import com.eviware.soapui.support.PropertyChangeNotifier;

public class JMSPropertiesConfig implements PropertyChangeNotifier
{

	private JMSPropertiesConfConfig jmsPropertiesConfConfig;

	private PropertyChangeSupport propertyChangeSupport;

	private final JMSPropertyContainer container;

	public JMSPropertiesConfig( JMSPropertiesConfConfig jmsPropertiesConfConfig, JMSPropertyContainer container )
	{
		this.jmsPropertiesConfConfig = jmsPropertiesConfConfig;
		this.container = container;
		// propertyChangeSupport = new PropertyChangeSupport(this);
		// if (!jmsPropertyConfConfig.isSetJMSDeliveryMode())
		// {
		// jmsPropertyConfConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.PERSISTENT);
		// }
	}

	public JMSPropertiesConfConfig getJmsPropertyConfConfig()
	{
		return jmsPropertiesConfConfig;
	}

	public void setJmsPropertyConfConfig( JMSPropertiesConfConfig jmsPropertiesConfConfig )
	{
		this.jmsPropertiesConfConfig = jmsPropertiesConfConfig;
	}

	public JMSPropertyContainer getContainer()
	{
		return container;
	}

	public List<JMSPropertyConfig> getJMSProperties()
	{
		return jmsPropertiesConfConfig.getJmsPropertiesList();
	}

	public void setJMSProperties( List<JMSPropertyConfig> map )
	{

		// List<JMSPropertyConfig> propertyList =
		// jmsPropertiesConfConfig.getJmsPropertiesList();
		// StringToStringMap stringToStringMap = new
		// StringToStringMap(propertyList.size());
		// for (JMSPropertyConfig jmsProperty:propertyList){
		// stringToStringMap
		// }
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
	}

}
