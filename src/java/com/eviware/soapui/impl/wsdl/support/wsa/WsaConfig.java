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

	public WsaConfig(WsaConfigConfig wsaConfig)
	{
		this.wsaConfig = wsaConfig;
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
		String oldValue = getAction();
		wsaConfig.setFaultTo(arg0);
		propertyChangeSupport.firePropertyChange("faultTo", oldValue, arg0);
		
	}

	public void setFrom(String arg0)
	{
		String oldValue = getAction();
		wsaConfig.setFrom(arg0);
		propertyChangeSupport.firePropertyChange("from", oldValue, arg0);
	}

	public void setMessageID(String arg0)
	{
		String oldValue = getAction();
		wsaConfig.setMessageID(arg0);
		propertyChangeSupport.firePropertyChange("messageID", oldValue, arg0);
	}

	public void setReplyTo(String arg0)
	{
		String oldValue = getAction();
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
		String oldValue = getAction();
		wsaConfig.setVersion(WsaVersionTypeConfig.Enum.forString(arg0));
		propertyChangeSupport.firePropertyChange("version", oldValue, arg0);
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
