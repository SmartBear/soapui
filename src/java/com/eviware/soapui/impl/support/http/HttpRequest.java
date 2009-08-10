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
package com.eviware.soapui.impl.support.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestRequest.ParameterMessagePart;
import com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpOperation;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.MessagePart.ContentPart;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

public class HttpRequest extends AbstractHttpRequest<HttpRequestConfig> implements
		HttpRequestInterface<HttpRequestConfig>
{
	private XmlBeansRestParamsTestPropertyHolder params;

	protected HttpRequest( HttpRequestConfig config, boolean forLoadTest )
	{
		super( config, null, "/http_request.gif", forLoadTest );

		if( config.getParameters() == null )
			config.addNewParameters();

		params = new XmlBeansRestParamsTestPropertyHolder( this, config.getParameters() );
	}

	public TestProperty addProperty( String name )
	{
		return params.addProperty( name );
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		params.moveProperty( propertyName, targetIndex );
	}

	public TestProperty removeProperty( String propertyName )
	{
		return params.removeProperty( propertyName );
	}

	public boolean renameProperty( String name, String newName )
	{
		return params.renameProperty( name, newName );
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		params.addTestPropertyListener( listener );
	}

	public ModelItem getModelItem()
	{
		return this;
	}

	public String getMediaType()
	{
		return getConfig().getMediaType() != null ? getConfig().getMediaType() : "application/xml";
	}

	public String getPath()
	{
		return getEndpoint();
	}

	public boolean hasRequestBody()
	{
		RestRequestInterface.RequestMethod method = getMethod();
		return method == RestRequestInterface.RequestMethod.POST || method == RestRequestInterface.RequestMethod.PUT;
	}

	public RestParamsPropertyHolder getParams()
	{
		return params;
	}

	public Map<String, TestProperty> getProperties()
	{
		return params.getProperties();
	}

	public RestParamProperty getProperty( String name )
	{
		return params.getProperty( name );
	}

	public RestParamProperty getPropertyAt( int index )
	{
		return params.getPropertyAt( index );
	}

	public int getPropertyCount()
	{
		return params.getPropertyCount();
	}

	public String[] getPropertyNames()
	{
		return params.getPropertyNames();
	}

	public String getPropertyValue( String name )
	{
		return params.getPropertyValue( name );
	}

	public boolean isPostQueryString()
	{
		return hasRequestBody() && getConfig().getPostQueryString();
	}

	public boolean hasProperty( String name )
	{
		return params.hasProperty( name );
	}

	public void setPropertyValue( String name, String value )
	{
		params.setPropertyValue( name, value );
	}

	public void setMediaType( String mediaType )
	{
		String old = getMediaType();
		getConfig().setMediaType( mediaType );
		notifyPropertyChanged( "mediaType", old, mediaType );
	}

	public void setPostQueryString( boolean b )
	{
		boolean old = isPostQueryString();
		getConfig().setPostQueryString( b );
		notifyPropertyChanged( "postQueryString", old, b );

		if( !"multipart/form-data".equals( getMediaType() ) )
		{
			setMediaType( b ? "application/x-www-form-urlencoded" : "" );
		}
	}

	public void setMethod( RequestMethod method )
	{
		RestRequestInterface.RequestMethod old = getMethod();
		getConfig().setMethod( method.toString() );
		setIcon( UISupport.createImageIcon( "/" + method.toString().toLowerCase() + "_method.gif" ) );
		notifyPropertyChanged( "method", old, method );
	}

	public String getPropertiesLabel()
	{
		return "HTTP Params";
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		params.removeTestPropertyListener( listener );
	}

	public HttpAttachmentPart getAttachmentPart( String partName )
	{
		return null;
	}

	public HttpAttachmentPart[] getDefinedAttachmentParts()
	{
		return new HttpAttachmentPart[0];
	}

	@Override
	public RestRequestInterface.RequestMethod getMethod()
	{
		String method = getConfig().getMethod();
		return method == null ? null : RestRequestInterface.RequestMethod.valueOf( method );
	}

	public MessagePart[] getRequestParts()
	{
		List<MessagePart> result = new ArrayList<MessagePart>();

		for( int c = 0; c < getPropertyCount(); c++ )
		{
			result.add( new ParameterMessagePart( getPropertyAt( c ) ) );
		}

		if( getMethod() == RestRequestInterface.RequestMethod.POST
				|| getMethod() == RestRequestInterface.RequestMethod.PUT )
		{
			result.add( new HttpContentPart() );
		}

		return result.toArray( new MessagePart[result.size()] );
	}

	public MessagePart[] getResponseParts()
	{
		return new MessagePart[0];
	}

	public String getResponseContentAsXml()
	{
		HttpResponse response = getResponse();
		if( response == null )
			return null;

		return response.getContentAsXml();
	}

	public WsdlSubmit<HttpRequest> submit( SubmitContext submitContext, boolean async ) throws SubmitException
	{
		String endpoint = PropertyExpander.expandProperties( submitContext, getEndpoint() );

		if( StringUtils.isNullOrEmpty( endpoint ) )
		{
			UISupport.showErrorMessage( "Missing endpoint for request [" + getName() + "]" );
			return null;
		}

		try
		{
			WsdlSubmit<HttpRequest> submitter = new WsdlSubmit<HttpRequest>( this, getSubmitListeners(),
					RequestTransportRegistry.getTransport( endpoint, submitContext ) );
			submitter.submitRequest( submitContext, async );
			return submitter;
		}
		catch( Exception e )
		{
			throw new SubmitException( e.toString() );
		}
	}

	public void updateConfig( HttpRequestConfig request )
	{
		setConfig( request );
		if( params == null )
			params = new XmlBeansRestParamsTestPropertyHolder( this, request.getParameters() );
		else
			params.resetPropertiesConfig( request.getParameters() );

		List<AttachmentConfig> attachmentConfigs = getConfig().getAttachmentList();
		for( int i = 0; i < attachmentConfigs.size(); i++ )
		{
			AttachmentConfig config = attachmentConfigs.get( i );
			getAttachmentsList().get( i ).updateConfig( config );
		}
	}

	public AbstractHttpOperation getOperation()
	{
		return null;
	}

	public class HttpContentPart extends ContentPart implements MessagePart
	{
		@Override
		public SchemaGlobalElement getPartElement()
		{
			return null;
		}

		@Override
		public QName getPartElementName()
		{
			return null;
		}

		@Override
		public SchemaType getSchemaType()
		{
			return null;
		}

		public String getDescription()
		{
			return null;
		}

		public String getName()
		{
			return null;
		}

		public String getMediaType()
		{
			return getConfig().getMediaType();
		}
	}

	public List<TestProperty> getPropertyList()
	{
		return params.getPropertyList();
	}
}
