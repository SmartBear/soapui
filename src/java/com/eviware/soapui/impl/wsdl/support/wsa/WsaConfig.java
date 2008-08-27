/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.support.wsa;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaConfigConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.support.PropertyChangeNotifier;

public class WsaConfig implements PropertyChangeNotifier 
{

	private final WsaConfigConfig wsaConfig;
	
	private PropertyChangeSupport propertyChangeSupport;

	private final WsaContainer container;

	public WsaConfig(WsaConfigConfig wsaConfig, WsaContainer container)
	{
		this.wsaConfig = wsaConfig;
		this.container = container;
		propertyChangeSupport = new PropertyChangeSupport(this);
		// TODO Auto-generated constructor stub
		if (!wsaConfig.isSetMustUnderstand())
		{
			wsaConfig.setMustUnderstand(MustUnderstandTypeConfig.NONE);
		}
		if (!wsaConfig.isSetVersion())
		{
			wsaConfig.setVersion(WsaVersionTypeConfig.X_200508);
		}
	}

	public String getAction()
	{
		return wsaConfig.getAction();
	}

	public String getFaultTo()
	{
		return wsaConfig.getFaultTo();
	}

	public String getFrom()
	{
		return wsaConfig.getFrom();
	}

	public String getTo()
	{
		return wsaConfig.getTo();
	}
	public String getRelationshipType()
	{
		return wsaConfig.getRelationshipType();
	}

	public String getRelatesTo()
	{
		return wsaConfig.getRelatesTo();
	}

	public String getMessageID()
	{
		return wsaConfig.getMessageID();
	}
	public String getReplyTo()
	{
		return wsaConfig.getReplyTo();
	}

	public String getVersion()
	{
		return wsaConfig.getVersion().toString();
	}
	
	public boolean isWsaEnabled ()
	{
		return container.isWsaEnabled();
	}


	public String getMustUnderstand()
	{
		return wsaConfig.getMustUnderstand().toString();
	}

	public void setAction(String arg0)
	{
		String oldValue = getAction();
		wsaConfig.setAction(arg0);
		propertyChangeSupport.firePropertyChange("action", oldValue, arg0);
	}

	public void setFaultTo(String arg0)
	{
		String oldValue = getFaultTo();
		wsaConfig.setFaultTo(arg0);
		propertyChangeSupport.firePropertyChange("faultTo", oldValue, arg0);
		
	}

	public void setFrom(String arg0)
	{
		String oldValue = getFrom();
		wsaConfig.setFrom(arg0);
		propertyChangeSupport.firePropertyChange("from", oldValue, arg0);
	}

	public void setTo(String arg0)
	{
		String oldValue = getTo();
		wsaConfig.setTo(arg0);
		propertyChangeSupport.firePropertyChange("to", oldValue, arg0);
	}

	public void setRelationshipType(String arg0)
	{
		String oldValue = getRelationshipType();
		wsaConfig.setRelationshipType(arg0);
		propertyChangeSupport.firePropertyChange("relationshipType", oldValue, arg0);
	}
	public void setRelatesTo(String arg0)
	{
		String oldValue = getRelatesTo();
		wsaConfig.setRelatesTo(arg0);
		propertyChangeSupport.firePropertyChange("relatesTo", oldValue, arg0);
	}
	public void setMessageID(String arg0)
	{
		String oldValue = getMessageID();
		wsaConfig.setMessageID(arg0);
		propertyChangeSupport.firePropertyChange("messageID", oldValue, arg0);
	}

	public void setReplyTo(String arg0)
	{
		String oldValue = getReplyTo();
		wsaConfig.setReplyTo(arg0);
		propertyChangeSupport.firePropertyChange("replyTo", oldValue, arg0);
	}

	public void setMustUnderstand(String arg0)
	{
		String oldValue = getMustUnderstand();
		wsaConfig.setMustUnderstand(MustUnderstandTypeConfig.Enum.forString(arg0));
		propertyChangeSupport.firePropertyChange("mustUnderstand", oldValue, arg0);
	}

	public void setVersion(String arg0)
	{
		String oldValue = getVersion();
		wsaConfig.setVersion(WsaVersionTypeConfig.Enum.forString(arg0));
		propertyChangeSupport.firePropertyChange("version", oldValue, arg0);
	}

	public void setWsaEnabled(boolean arg0)
	{
		boolean oldValue = isWsaEnabled();
		container.setWsaEnabled(arg0);
		propertyChangeSupport.firePropertyChange("wsaEnabled", oldValue, arg0);
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
