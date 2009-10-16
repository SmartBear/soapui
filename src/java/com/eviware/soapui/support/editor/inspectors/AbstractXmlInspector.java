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

package com.eviware.soapui.support.editor.inspectors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ImageIcon;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.JMSUtil;
import com.eviware.soapui.support.editor.inspectors.jms.header.JMSHeaderInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSHeaderAndPropertyInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSPropertyInspectorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.editor.xml.XmlInspector;

/**
 * Abstract base-class to be extended by XmlInspectors
 * 
 * @author ole.matzura
 */

public abstract class AbstractXmlInspector implements XmlInspector
{
	private final PropertyChangeSupport propertySupport;
	private String title;
	private String description;
	private boolean enabled;
	private XmlEditor editor;
	private final String inspectorId;
	private boolean active;

	protected AbstractXmlInspector( String title, String description, boolean enabled, String inspectorId )
	{
		this.title = title;
		this.description = description;
		this.enabled = enabled;
		this.inspectorId = inspectorId;

		propertySupport = new PropertyChangeSupport( this );
//		this.addPropertyChangeListener(new PropertyChangeListener(){
//
//			public void propertyChange(PropertyChangeEvent evt)
//			{
//			// when selecting endpoint
//				if ((evt.getSource() instanceof WsdlTestRequest) && evt.getPropertyName().equals(AbstractHttpRequest.ENDPOINT_PROPERTY))
//				{
//					boolean jmsEndpoint = ((WsdlTestRequest)evt.getSource()).getEndpoint().startsWith(JMSUtil.JMS_ENDPIONT_PREFIX);
//
//					// for request
//					updateInspector(editor, getInspectorId(), jmsEndpoint);
////					super.repaint();
//				
//				}
//			}
//			private void updateInspector(XmlEditor<XmlDocument> editor, String inspectorId, boolean enable)
//			{
//				AbstractXmlInspector inspector = (AbstractXmlInspector)editor.getInspector(inspectorId);
//				if (inspector != null)
//				{
//						inspector.setEnabled(enable);
//				}
//			}
//		});
	}

	public final String getInspectorId()
	{
		return inspectorId;
	}

	public void deactivate()
	{
		active = false;
	}

	public ImageIcon getIcon()
	{
		return null;
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertySupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertySupport.removePropertyChangeListener( listener );
	}

	public String getDescription()
	{
		return description;
	}

	public String getTitle()
	{
		return title;
	}

	public void setDescription( String description )
	{
		String oldDescription = this.description;
		this.description = description;
		propertySupport.firePropertyChange( DESCRIPTION_PROPERTY, oldDescription, description );
	}

	public void setTitle( String title )
	{
		String oldTitle = this.title;
		this.title = title;
		propertySupport.firePropertyChange( TITLE_PROPERTY, oldTitle, title );
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( boolean enabled )
	{
		boolean oldEnabled = this.enabled;
		this.enabled = enabled;
		propertySupport.firePropertyChange( ENABLED_PROPERTY, oldEnabled, enabled );
	}

	public void init( Editor<XmlDocument> editor )
	{
		this.editor = ( XmlEditor )editor;
	}

	public Editor<XmlDocument> getEditor()
	{
		return ( Editor<XmlDocument> )editor;
	}

	public void release()
	{
	}

	public void activate()
	{
		active = true;
		getComponent().requestFocusInWindow();
	}

	public boolean isActive()
	{
		return active;
	}

	public boolean isContentHandler()
	{
		return false;
	}

	public void locationChanged( EditorLocation<XmlDocument> location )
	{
	}

	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return false;
	}
}
