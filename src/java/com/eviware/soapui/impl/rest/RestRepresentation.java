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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestResourceRepresentationConfig;
import com.eviware.soapui.config.RestResourceRepresentationTypeConfig;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class RestRepresentation
{
	private final RestRequest restRequest;
	private RestResourceRepresentationConfig config;
	private XmlBeansRestParamsTestPropertyHolder params;
	private PropertyChangeSupport propertyChangeSupport;
   private SchemaType schemaType;

   public enum Type { REQUEST, RESPONSE, FAULT };
	
	public RestRepresentation(RestRequest restResource, RestResourceRepresentationConfig config)
	{
		this.restRequest = restResource;
		this.config = config;
		
		if( config.getParams() == null )
			config.addNewParams();
		
		params = new XmlBeansRestParamsTestPropertyHolder( restResource, config.getParams() );
		propertyChangeSupport = new PropertyChangeSupport( this );
	}

	public RestRequest getRestRequest()
	{
		return restRequest;
	}

	public RestResourceRepresentationConfig getConfig()
	{
		return config;
	}
	
	public XmlBeansRestParamsTestPropertyHolder getParams()
	{
		return params;
	}

	public void setConfig(RestResourceRepresentationConfig config)
	{
		this.config = config;
	}

	public String getId()
	{
		return config.getId();
	}
	
	public Type getType()
	{
		return Type.valueOf(config.getType().toString());
	}

	public String getMediaType()
	{
		return config.getMediaType();
	}

	public void setId(String arg0)
	{
		config.setId(arg0);
	}

	public void setType(Type type)
	{
		config.setType( RestResourceRepresentationTypeConfig.Enum.forString(type.toString()));
	}

	public void setMediaType(String arg0)
	{
		config.setMediaType(arg0);
	}

   public void setElement( QName name )
   {
      config.setElement( name );
      schemaType = null;
   }

   public List getStatus()
	{
		return config.getStatus() == null ? new ArrayList() :  config.getStatus();
	}

	public void setStatus(List arg0)
	{
		config.setStatus(arg0);
	}

   public SchemaType getSchemaType()
   {
      if( schemaType == null )
      {
         try
         {
            if( getElement() != null )
            {
               WadlDefinitionContext context = getRestRequest().getResource().getService().getWadlContext();
               if( context.hasSchemaTypes() )
               {
                  SchemaGlobalElement element = context.getSchemaTypeSystem().findElement( getElement() );
                  if( element != null )
                     schemaType = element.getType();
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
	}

   public void setDescription( String description )
   {
      config.setDescription( description );
   }

   public String getDescription()
   {
      return config.getDescription();
   }

   public QName getElement()
   {
      return config.getElement();
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
