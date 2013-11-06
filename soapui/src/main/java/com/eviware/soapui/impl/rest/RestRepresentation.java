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

package com.eviware.soapui.impl.rest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestResourceRepresentationConfig;
import com.eviware.soapui.config.RestResourceRepresentationTypeConfig;
import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaManager;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import com.eviware.soapui.impl.wsdl.support.xsd.SampleXmlUtil;
import com.eviware.soapui.support.PropertyChangeNotifier;

public class RestRepresentation implements PropertyChangeNotifier, PropertyChangeListener
{
	// private final RestRequest restRequest;
	private final RestMethod restMethod;
	private RestResourceRepresentationConfig config;
	private RestParamsPropertyHolder params;
	private PropertyChangeSupport propertyChangeSupport;
	private SchemaType schemaType;

	public enum Type
	{
		REQUEST, RESPONSE, FAULT
	}

	/*
	 * //TODO: Remove this? public RestRepresentation( RestRequest restResource,
	 * RestResourceRepresentationConfig config ) { this.restMethod = null;
	 * this.restRequest = restResource; this.config = config;
	 * 
	 * if( config.getParams() == null ) config.addNewParams();
	 * 
	 * params = new XmlBeansRestParamsTestPropertyHolder( restResource,
	 * config.getParams() ); propertyChangeSupport = new PropertyChangeSupport(
	 * this );
	 * 
	 * if(this.restRequest.getResource() != null &&
	 * this.restRequest.getResource().getService() != null)
	 * InferredSchemaManager.
	 * addPropertyChangeListener(this.restRequest.getResource().getService(),
	 * this); }
	 */

	public RestRepresentation( RestMethod restMethod, RestResourceRepresentationConfig config )
	{
		this.restMethod = restMethod;
		this.config = config;

		if( config.getParams() == null )
			config.addNewParams();

		params = new XmlBeansRestParamsTestPropertyHolder( restMethod, config.getParams() );
		propertyChangeSupport = new PropertyChangeSupport( this );

		InferredSchemaManager.addPropertyChangeListener( this.restMethod.getResource().getService(), this );
	}

	public RestMethod getRestMethod()
	{
		return restMethod;
	}

	public RestResourceRepresentationConfig getConfig()
	{
		return config;
	}

	public RestParamsPropertyHolder getParams()
	{
		return params;
	}

	public void setConfig( RestResourceRepresentationConfig config )
	{
		this.config = config;
	}

	public String getId()
	{
		return config.getId();
	}

	public Type getType()
	{
		if( !config.isSetType() )
			return null;

		return Type.valueOf( config.getType().toString() );
	}

	public String getMediaType()
	{
		return config.getMediaType();
	}

	public void setId( String arg0 )
	{
		String old = getId();
		config.setId( arg0 );
		propertyChangeSupport.firePropertyChange( "id", old, arg0 );
	}

	public void setType( Type type )
	{
		Type old = getType();
		config.setType( RestResourceRepresentationTypeConfig.Enum.forString( type.toString() ) );
		propertyChangeSupport.firePropertyChange( "type", old, type );
	}

	public void setMediaType( String arg0 )
	{
		String old = getMediaType();
		config.setMediaType( arg0 );
		propertyChangeSupport.firePropertyChange( "mediaType", old, arg0 );
	}

	public void setElement( QName name )
	{
		QName old = getElement();
		config.setElement( name );
		schemaType = null;
		propertyChangeSupport.firePropertyChange( "element", old, name );
	}

	public List<?> getStatus()
	{
		return config.getStatus() == null ? new ArrayList<Object>() : config.getStatus();
	}

	public void setStatus( List<?> arg0 )
	{
		List<?> old = getStatus();
		config.setStatus( arg0 );
		propertyChangeSupport.firePropertyChange( "status", old, arg0 );
	}

	public SchemaType getSchemaType()
	{
		if( schemaType == null )
		{
			try
			{
				if( getElement() != null )
				{
					WadlDefinitionContext context = getRestMethod().getResource().getService().getWadlContext();
					if( context.hasSchemaTypes() )
					{
						schemaType = context.getSchemaTypeSystem().findDocumentType( getElement() );
						if( schemaType == null )
						{
							SchemaGlobalElement element = context.getSchemaTypeSystem().findElement( getElement() );
							if( element != null )
							{
								schemaType = element.getType();
							}
						}
					}
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		return schemaType;
	}

	public void release()
	{
		InferredSchemaManager.removePropertyChangeListener( getRestMethod().getResource().getService(), this );
	}

	public void setDescription( String description )
	{
		String old = getDescription();
		config.setDescription( description );
		propertyChangeSupport.firePropertyChange( "description", old, description );
	}

	public String getDescription()
	{
		return config.getDescription();
	}

	public QName getElement()
	{
		return config.getElement();
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

	public String getDefaultContent()
	{
		if( getSchemaType() != null )
		{
			// Document document = XmlUtils.createDocument( getElement() );
			SampleXmlUtil generator = new SampleXmlUtil( false );
			generator.setIgnoreOptional( false );
			return generator.createSample( getSchemaType() );
			// return XmlUtils.serialize( document );
		}
		else
		{
			return "";
		}
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		schemaType = null;
	}
}
