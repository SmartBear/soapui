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

package com.eviware.soapui.impl.rest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlString;

import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestResourceRepresentationConfig;
import com.eviware.soapui.impl.rest.RestRepresentation.Type;
import com.eviware.soapui.impl.rest.support.MediaTypeHandler;
import com.eviware.soapui.impl.rest.support.MediaTypeHandlerRegistry;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.RequestTransportRegistry;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.MessagePart.ContentPart;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;

/**
 * Request implementation holding a SOAP request
 * 
 * @author Ole.Matzura
 */

public class RestRequest extends AbstractHttpRequest<RestMethodConfig> implements MutableTestPropertyHolder,
		PropertyChangeListener
{
	public final static Logger log = Logger.getLogger( RestRequest.class );
	public static final String DEFAULT_MEDIATYPE = "application/xml";
	private List<RestRepresentation> representations = new ArrayList<RestRepresentation>();

	private XmlBeansRestParamsTestPropertyHolder params;
	public static final String REST_XML_RESPONSE = "restXmlResponse";
	public static final String REST_XML_REQUEST = "restXmlRequest";
	private PropertyChangeListener representationPropertyChangeListener = new RepresentationPropertyChangeListener();

	public RestRequest( RestResource resource, RestMethodConfig requestConfig, boolean forLoadTest )
	{
		super( requestConfig, resource, "/rest_request.gif", false );

		if( requestConfig.getParameters() == null )
			requestConfig.addNewParameters();

		if( !requestConfig.isSetMethod() )
			setMethod( RequestMethod.GET );

		if( requestConfig.getParameters() == null )
			requestConfig.addNewParameters();

		for( RestResourceRepresentationConfig config : requestConfig.getRepresentationList() )
		{
			RestRepresentation representation = new RestRepresentation( this, config );
			representation.addPropertyChangeListener( representationPropertyChangeListener );
			representations.add( representation );
		}

		params = new XmlBeansRestParamsTestPropertyHolder( this, requestConfig.getParameters() );

		if( resource != null )
			resource.addPropertyChangeListener( this );
	}

	protected RequestIconAnimator<?> initIconAnimator()
	{
		return new RequestIconAnimator<AbstractHttpRequest<?>>( this, "/rest_request.gif", "/exec_rest_request", 4, "gif" );
	}

	public MessagePart[] getRequestParts()
	{
		List<MessagePart> result = new ArrayList<MessagePart>();

		for( int c = 0; c < getPropertyCount(); c++ )
		{
			result.add( new ParameterMessagePart( getPropertyAt( c ) ) );
		}

		if( getMethod() == RequestMethod.POST || getMethod() == RequestMethod.PUT )
		{
			result.add( new RestContentPart() );
		}

		return result.toArray( new MessagePart[result.size()] );
	}

	public RestRepresentation[] getRepresentations()
	{
		return getRepresentations( null, null );
	}

	public RestRepresentation[] getRepresentations( RestRepresentation.Type type )
	{
		return getRepresentations( type, null );
	}

	public RestRepresentation[] getRepresentations( RestRepresentation.Type type, String mediaType )
	{
		List<RestRepresentation> result = new ArrayList<RestRepresentation>();
		Set<String> addedTypes = new HashSet<String>();

		for( RestRepresentation representation : representations )
		{
			if( ( type == null || type == representation.getType() )
					&& ( mediaType == null || mediaType.equals( representation.getMediaType() ) ) )
			{
				result.add( representation );
				addedTypes.add( representation.getMediaType() );
			}
		}

		if( type == RestRepresentation.Type.REQUEST )
		{
			for( Attachment attachment : getAttachments() )
			{
				if( ( mediaType == null || mediaType.equals( attachment.getContentType() ) )
						&& !addedTypes.contains( attachment.getContentType() ) )
				{
					RestRepresentation representation = new RestRepresentation( this,
							RestResourceRepresentationConfig.Factory.newInstance() );
					representation.setType( RestRepresentation.Type.REQUEST );
					representation.setMediaType( attachment.getContentType() );
					result.add( representation );
				}
			}
		}

		return result.toArray( new RestRepresentation[result.size()] );
	}

	public MessagePart[] getResponseParts()
	{
		return new MessagePart[0];
	}

	public void setMethod( RequestMethod method )
	{
		RequestMethod old = getMethod();
		getConfig().setMethod( method.toString() );
		notifyPropertyChanged( "method", old, method );
	}

	public RequestMethod getMethod()
	{
		String method = getConfig().getMethod();
		return method == null ? null : RequestMethod.valueOf( method );
	}

	public String getAccept()
	{
		String accept = getConfig().getAccept();
		return accept == null ? "" : accept;
	}

	public void setAccept( String acceptEncoding )
	{
		String old = getAccept();
		getConfig().setAccept( acceptEncoding );
		notifyPropertyChanged( "accept", old, acceptEncoding );
	}

	public void setMediaType( String mediaType )
	{
		String old = getMediaType();
		getConfig().setMediaType( mediaType );
		notifyPropertyChanged( "mediaType", old, mediaType );
	}

	public String getMediaType()
	{
		String mediaType = getConfig().getMediaType();
		return mediaType;
	}

	public WsdlSubmit<RestRequest> submit( SubmitContext submitContext, boolean async ) throws SubmitException
	{
		String endpoint = PropertyExpansionUtils.expandProperties( submitContext, getEndpoint() );

		if( StringUtils.isNullOrEmpty( endpoint ) )
		{
			try
			{
				endpoint = new URL( getPath() ).toString();
			}
			catch( MalformedURLException e )
			{
			}
		}

		if( StringUtils.isNullOrEmpty( endpoint ) )
		{
			UISupport.showErrorMessage( "Missing endpoint for request [" + getName() + "]" );
			return null;
		}

		try
		{
			WsdlSubmit<RestRequest> submitter = new WsdlSubmit<RestRequest>( this, getSubmitListeners(),
					RequestTransportRegistry.getTransport( endpoint, submitContext ) );
			submitter.submitRequest( submitContext, async );
			return submitter;
		}
		catch( Exception e )
		{
			throw new SubmitException( e.toString() );
		}
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( this, this );
		result.addAll( super.getPropertyExpansions() );
		result.addAll( params.getPropertyExpansions() );

		return result.toArray();
	}

	public RestParamProperty addProperty( String name )
	{
		return params.addProperty( name );
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		params.moveProperty( propertyName, targetIndex );
	}

	public RestParamProperty removeProperty( String propertyName )
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

	@Override
	public RestResource getOperation()
	{
		return ( RestResource )super.getOperation();
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

	public List<TestProperty> getPropertyList()
	{
		return params.getPropertyList();
	}

	public boolean hasProperty( String name )
	{
		return params.hasProperty( name );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		params.removeTestPropertyListener( listener );
	}

	public void setPropertyValue( String name, String value )
	{
		params.setPropertyValue( name, value );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "path" ) )
		{
			notifyPropertyChanged( "path", null, getPath() );
		}
	}

	public String[] getResponseMediaTypes()
	{
		StringList result = new StringList();

		for( RestRepresentation representation : getRepresentations( Type.RESPONSE, null ) )
		{
			if( !result.contains( representation.getMediaType() ) )
				result.add( representation.getMediaType() );
		}

		return result.toStringArray();
	}

	public boolean isPostQueryString()
	{
		return getConfig().getPostQueryString();
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

	public final static class ParameterMessagePart extends MessagePart.ParameterPart
	{
		private String name;

		public ParameterMessagePart( TestProperty propertyAt )
		{
			this.name = propertyAt.getName();
		}

		@Override
		public SchemaType getSchemaType()
		{
			return XmlString.type;
		}

		@Override
		public SchemaGlobalElement getPartElement()
		{
			return null;
		}

		@Override
		public QName getPartElementName()
		{
			return new QName( getName() );
		}

		public String getDescription()
		{
			return null;
		}

		public String getName()
		{
			return name;
		}
	}

	public String getPropertiesLabel()
	{
		return "Request Params";
	}

	public XmlBeansRestParamsTestPropertyHolder getParams()
	{
		return params;
	}

	public HttpAttachmentPart getAttachmentPart( String partName )
	{
		return null;
	}

	public HttpAttachmentPart[] getDefinedAttachmentParts()
	{
		return new HttpAttachmentPart[0];
	}

	public class RestContentPart extends ContentPart implements MessagePart
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
			return "application/xml";
		}
	}

	public boolean hasRequestBody()
	{
		RequestMethod method = getMethod();
		return method == RequestMethod.POST || method == RequestMethod.PUT;
	}

	public RestResource getResource()
	{
		return getOperation();
	}

	public String getPath()
	{
		if( getConfig().isSetFullPath() || getResource() == null )
			return getConfig().getFullPath();
		else
			return getResource().getFullPath();
	}

	public void setPath( String fullPath )
	{
		String old = getPath();

		if( getResource() != null && getResource().getFullPath().equals( fullPath ) && getConfig().isSetFullPath() )
			getConfig().unsetFullPath();
		else
			getConfig().setFullPath( fullPath );

		notifyPropertyChanged( "path", old, fullPath );
	}

	public String getResponseContentAsXml()
	{
		HttpResponse response = getResponse();
		if( response == null )
			return null;

		return response.getProperty( REST_XML_RESPONSE );
	}

	public void setResponse( HttpResponse response, SubmitContext context )
	{
		if( response != null )
			response.setProperty( REST_XML_RESPONSE, createXmlResponse( response ) );

		super.setResponse( response, context );
	}

	private String createXmlResponse( HttpResponse response )
	{
		if( response == null )
			return "<xml/>";

		MediaTypeHandler typeHandler = MediaTypeHandlerRegistry.getTypeHandler( response.getContentType() );
		if( typeHandler != null )
			return typeHandler.createXmlRepresentation( response );
		else
			return "<xml/>";
	}

	@Override
	public void release()
	{
		super.release();
		params.release();

		if( getResource() != null )
			getResource().removePropertyChangeListener( this );

		for( RestRepresentation representation : representations )
		{
			representation.release();
		}
	}

	public void updateConfig( RestMethodConfig request )
	{
		setConfig( request );

		params.resetPropertiesConfig( request.getParameters() );

		for( int c = 0; c < request.sizeOfRepresentationArray(); c++ )
		{
			representations.get( c ).setConfig( request.getRepresentationArray( c ) );
		}

		List<AttachmentConfig> attachmentConfigs = getConfig().getAttachmentList();
		for( int i = 0; i < attachmentConfigs.size(); i++ )
		{
			AttachmentConfig config = attachmentConfigs.get( i );
			getAttachmentsList().get( i ).updateConfig( config );
		}
	}

	public RestParamProperty addProperty( RestParamProperty prop )
	{
		return params.addProperty( prop );
	}

	public RestRepresentation addNewRepresentation( Type type )
	{
		RestRepresentation representation = new RestRepresentation( this, getConfig().addNewRepresentation() );
		representation.setType( type );

		representation.addPropertyChangeListener( representationPropertyChangeListener );

		representations.add( representation );

		notifyPropertyChanged( "representations", null, representation );

		return representation;
	}

	public void removeRepresentation( RestRepresentation representation )
	{
		int ix = representations.indexOf( representation );

		representations.remove( ix );
		representation.removePropertyChangeListener( representationPropertyChangeListener );

		notifyPropertyChanged( "representations", representation, null );
		getConfig().removeRepresentation( ix );
		representation.release();
	}

	public boolean hasEndpoint()
	{
		return super.hasEndpoint() || PathUtils.isHttpPath( getPath() );
	}

	private class RepresentationPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( "mediaType" )
					&& ( ( RestRepresentation )evt.getSource() ).getType() == Type.RESPONSE )
			{
				RestRequest.this.notifyPropertyChanged( "responseMediaTypes", null, getResponseMediaTypes() );
			}
		}
	}
}
