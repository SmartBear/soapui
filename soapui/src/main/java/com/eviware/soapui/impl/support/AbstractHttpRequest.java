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

package com.eviware.soapui.impl.support;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.IAfterRequestInjection;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.impl.wsdl.support.jms.header.JMSHeaderContainer;
import com.eviware.soapui.impl.wsdl.support.jms.property.JMSPropertyContainer;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep.RequestHeaderHolder;
import com.eviware.soapui.impl.wsdl.teststeps.SettingPathPropertySupport;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.settings.CommonSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;

public abstract class AbstractHttpRequest<T extends AbstractRequestConfig> extends AbstractWsdlModelItem<T> implements
		Request, AbstractHttpRequestInterface<T>, JMSHeaderContainer, JMSPropertyContainer
{

	private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();
	private String requestContent;
	private RequestIconAnimator<?> iconAnimator;
	private HttpResponse response;
	private SettingPathPropertySupport dumpFile;
	private List<FileAttachment<?>> attachments = new ArrayList<FileAttachment<?>>();
	private IAfterRequestInjection afterRequestInjection;

	protected AbstractHttpRequest( T config, AbstractHttpOperation parent, String icon, boolean forLoadTest )
	{
		super( config, parent, icon );

		if( !forLoadTest )
		{
			iconAnimator = initIconAnimator();
			if( SoapUI.usingGraphicalEnvironment() )
			{
				addSubmitListener( iconAnimator );
			}
		}

		initAttachments();

		dumpFile = new SettingPathPropertySupport( this, DUMP_FILE );
	}

	private void initAttachments()
	{
		for( AttachmentConfig ac : getConfig().getAttachmentList() )
		{
			RequestFileAttachment attachment = new RequestFileAttachment( ac, this );
			attachments.add( attachment );
		}
	}

	protected List<FileAttachment<?>> getAttachmentsList()
	{
		return attachments;
	}

	public Attachment attachBinaryData( byte[] data, String contentType )
	{
		RequestFileAttachment fileAttachment;
		try
		{
			File temp = File.createTempFile( "binaryContent", ".tmp" );

			OutputStream out = new FileOutputStream( temp );
			out.write( data );
			out.close();
			fileAttachment = new RequestFileAttachment( temp, false, this );
			fileAttachment.setContentType( contentType );
			attachments.add( fileAttachment );
			notifyPropertyChanged( ATTACHMENTS_PROPERTY, null, fileAttachment );
			return fileAttachment;
		}
		catch( IOException e )
		{
			SoapUI.logError( e );
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.AttachmentContainer#attachFile(java.io.File,
	 * boolean)
	 */

	public Attachment attachFile( File file, boolean cache ) throws IOException
	{
		RequestFileAttachment fileAttachment = new RequestFileAttachment( file, cache, this );
		attachments.add( fileAttachment );
		notifyPropertyChanged( ATTACHMENTS_PROPERTY, null, fileAttachment );
		return fileAttachment;
	}

	public abstract RestRequestInterface.RequestMethod getMethod();

	/**
	 * Override just to get a better return type
	 * 
	 * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentPart(java.lang.String)
	 */

	public abstract HttpAttachmentPart getAttachmentPart( String partName );

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentCount()
	 */
	public int getAttachmentCount()
	{
		return attachments.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentAt(int)
	 */
	public Attachment getAttachmentAt( int index )
	{
		return attachments.get( index );
	}

	@SuppressWarnings( "rawtypes" )
	public void setAttachmentAt( int index, Attachment attachment )
	{
		if( attachments.size() > index )
			attachments.set( index, ( FileAttachment )attachment );
		else
			attachments.add( ( FileAttachment )attachment );
		notifyPropertyChanged( ATTACHMENTS_PROPERTY, null, attachment );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachmentsForPart
	 * (java.lang.String)
	 */
	public Attachment[] getAttachmentsForPart( String partName )
	{
		List<Attachment> result = new ArrayList<Attachment>();

		for( Attachment attachment : attachments )
		{
			if( partName.equals( attachment.getPart() ) )
				result.add( attachment );
		}

		return result.toArray( new Attachment[result.size()] );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.AttachmentContainer#removeAttachment(com.
	 * eviware.soapui.model.iface.Attachment)
	 */
	public void removeAttachment( Attachment attachment )
	{
		int ix = attachments.indexOf( attachment );
		attachments.remove( ix );

		try
		{
			notifyPropertyChanged( ATTACHMENTS_PROPERTY, attachment, null );
		}
		finally
		{
			getConfig().removeAttachment( ix );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.AttachmentContainer#getAttachments()
	 */
	public Attachment[] getAttachments()
	{
		return attachments.toArray( new Attachment[attachments.size()] );
	}

	protected RequestIconAnimator<?> initIconAnimator()
	{
		return new RequestIconAnimator<AbstractHttpRequest<?>>( this, "/request.gif", "/exec_request.gif", 4 );
	}

	public void addSubmitListener( SubmitListener listener )
	{
		submitListeners.add( listener );
	}

	public void removeSubmitListener( SubmitListener listener )
	{
		submitListeners.remove( listener );
	}

	public boolean isMultipartEnabled()
	{
		return !getSettings().getBoolean( DISABLE_MULTIPART_ATTACHMENTS );
	}

	public void setMultipartEnabled( boolean multipartEnabled )
	{
		getSettings().setBoolean( DISABLE_MULTIPART_ATTACHMENTS, !multipartEnabled );
	}

	public boolean isEntitizeProperties()
	{
		return getSettings().getBoolean( CommonSettings.ENTITIZE_PROPERTIES );
	}

	public void setEntitizeProperties( boolean entitizeProperties )
	{
		getSettings().setBoolean( CommonSettings.ENTITIZE_PROPERTIES, entitizeProperties );
	}

	@Override
	public void release()
	{
		submitListeners.clear();

		super.release();
	}

	public SubmitListener[] getSubmitListeners()
	{
		return submitListeners.toArray( new SubmitListener[submitListeners.size()] );
	}

	public AbstractHttpOperation getOperation()
	{
		return ( AbstractHttpOperation )getParent();
	}

	public void copyAttachmentsTo( WsdlRequest newRequest )
	{
		if( getAttachmentCount() > 0 )
		{
			try
			{
				UISupport.setHourglassCursor();
				for( int c = 0; c < getAttachmentCount(); c++ )
				{
					try
					{
						Attachment attachment = getAttachmentAt( c );
						newRequest.importAttachment( attachment );
					}
					catch( Exception e )
					{
						SoapUI.logError( e );
					}
				}
			}
			finally
			{
				UISupport.resetCursor();
			}
		}
	}

	public Attachment importAttachment( Attachment attachment )
	{
		if( attachment instanceof FileAttachment<?> )
		{
			AttachmentConfig oldConfig = ( ( FileAttachment<?> )attachment ).getConfig();
			AttachmentConfig newConfig = ( AttachmentConfig )getConfig().addNewAttachment().set( oldConfig );
			RequestFileAttachment newAttachment = new RequestFileAttachment( newConfig, this );
			attachments.add( newAttachment );
			return newAttachment;
		}
		else
			log.error( "Unkown attachment type: " + attachment );

		return null;
	}

	public void addAttachmentsChangeListener( PropertyChangeListener listener )
	{
		addPropertyChangeListener( ATTACHMENTS_PROPERTY, listener );
	}

	public boolean isReadOnly()
	{
		return false;
	}

	public void removeAttachmentsChangeListener( PropertyChangeListener listener )
	{
		removePropertyChangeListener( ATTACHMENTS_PROPERTY, listener );
	}

	public String getRequestContent()
	{
		if( getConfig().getRequest() == null )
			getConfig().addNewRequest();

		if( requestContent == null )
			requestContent = CompressedStringSupport.getString( getConfig().getRequest() );

		return requestContent;
	}

	public void setRequestContent( String request )
	{
		String old = getRequestContent();

		if( ( StringUtils.isNullOrEmpty( request ) && StringUtils.isNullOrEmpty( old ) )
				|| ( request != null && request.equals( old ) ) )
			return;

		requestContent = request;
		notifyPropertyChanged( REQUEST_PROPERTY, old, request );
	}

	public boolean isPrettyPrint()
	{
		return getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES );
	}

	public void setPrettyPrint( boolean prettyPrint )
	{
		boolean old = getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES );
		getSettings().setBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, prettyPrint );
		notifyPropertyChanged( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES, old, prettyPrint );
	}

	public void setEndpoint( String endpoint )
	{
		if( getOperation() != null )
		{
			getOperation().getInterface().getProject().getEndpointSupport()
					.setEndpoint( ( AbstractHttpRequest<AbstractRequestConfig> )this, endpoint );
		}
		else
		{
			String old = getEndpoint();
			if( old != null && old.equals( endpoint ) )
				return;

			getConfig().setEndpoint( endpoint );
			notifyPropertyChanged( ENDPOINT_PROPERTY, old, endpoint );
		}
	}

	public String getEndpoint()
	{
		if( getOperation() != null )
		{
			return getOperation().getInterface().getProject().getEndpointSupport()
					.getEndpoint( ( AbstractHttpRequest<AbstractRequestConfig> )this );
		}
		else
		{
			return getConfig().getEndpoint();
		}
	}

	public String getEncoding()
	{
		return getConfig().getEncoding();
	}

	public void setEncoding( String encoding )
	{
		String old = getEncoding();
		getConfig().setEncoding( encoding );
		notifyPropertyChanged( ENCODING_PROPERTY, old, encoding );
	}

	public String getTimeout()
	{
		return getConfig().getTimeout();
	}

	public void setTimeout( String timeout )
	{
		String old = getTimeout();
		getConfig().setTimeout( timeout );
		notifyPropertyChanged( "timeout", old, timeout );
	}

	public StringToStringsMap getRequestHeaders()
	{
		return StringToStringsMap.fromXml( getSettings().getString( REQUEST_HEADERS_PROPERTY, null ) );
	}

	public RequestIconAnimator<?> getIconAnimator()
	{
		return iconAnimator;
	}

	/**
	 * Added for backwards compatibility
	 * 
	 * @param map
	 */

	public void setRequestHeaders( StringToStringMap map )
	{
		setRequestHeaders( new StringToStringsMap( map ) );
	}

	public void setRequestHeaders( StringToStringsMap map )
	{
		StringToStringsMap old = getRequestHeaders();
		getSettings().setString( REQUEST_HEADERS_PROPERTY, map.toXml() );
		notifyPropertyChanged( REQUEST_HEADERS_PROPERTY, old, map );
	}

	@Override
	public ImageIcon getIcon()
	{
		return iconAnimator == null ? null : iconAnimator.getIcon();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( this, this );

		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "requestContent" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "endpoint" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "username" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "password" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "domain" ) );

		StringToStringsMap requestHeaders = getRequestHeaders();
		for( String key : requestHeaders.keySet() )
		{
			for( String value : requestHeaders.get( key ) )
				result.extractAndAddAll( new RequestHeaderHolder( key, value, this ), "value" );
		}

		return result.toArray();
	}

	public String getUsername()
	{
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			return null;

		return credentialsConfig.getUsername();
	}

	public String getPassword()
	{
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			return null;

		return credentialsConfig.getPassword();
	}

	public String getDomain()
	{
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			return null;

		return credentialsConfig.getDomain();
	}
	
	public String getAuthType()
	{
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			credentialsConfig = getConfig().addNewCredentials();

		initializeAuthType( credentialsConfig );

		return credentialsConfig.getAuthType().toString();
	}

	private void initializeAuthType( CredentialsConfig credentialsConfig )
	{
		try
		{
			if( credentialsConfig.getAuthType() == null )
				credentialsConfig.setAuthType( AuthType.GLOBAL_HTTP_SETTINGS );
		}
		catch( XmlValueOutOfRangeException e )
		{
			// Migration from deleted enum NTLM/Kerberos
			credentialsConfig.setAuthType( AuthType.NTLM );
		}
	}

	public void setUsername( String username )
	{
		String old = getUsername();
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			credentialsConfig = getConfig().addNewCredentials();

		credentialsConfig.setUsername( username );
		notifyPropertyChanged( "username", old, username );
	}

	public void setPassword( String password )
	{
		String old = getPassword();
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			credentialsConfig = getConfig().addNewCredentials();

		credentialsConfig.setPassword( password );
		notifyPropertyChanged( "password", old, password );
	}

	public void setDomain( String domain )
	{
		String old = getDomain();
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			credentialsConfig = getConfig().addNewCredentials();

		credentialsConfig.setDomain( domain );
		notifyPropertyChanged( "domain", old, domain );
	}
	
	public void setAuthType( String authType )
	{
		String old = getAuthType();
		CredentialsConfig credentialsConfig = getConfig().getCredentials();
		if( credentialsConfig == null )
			credentialsConfig = getConfig().addNewCredentials();

		credentialsConfig.setAuthType( AuthType.Enum.forString( authType ) );
		notifyPropertyChanged( "authType", old, authType );
	}

	public String getSslKeystore()
	{
		return getConfig().getSslKeystore();
	}

	public void setSslKeystore( String sslKeystore )
	{
		String old = getSslKeystore();
		getConfig().setSslKeystore( sslKeystore );
		notifyPropertyChanged( "sslKeystore", old, sslKeystore );
	}

	public String getBindAddress()
	{
		return getSettings().getString( BIND_ADDRESS, "" );
	}

	public void setBindAddress( String bindAddress )
	{
		String old = getSettings().getString( BIND_ADDRESS, "" );
		getSettings().setString( BIND_ADDRESS, bindAddress );
		notifyPropertyChanged( BIND_ADDRESS, old, bindAddress );
	}

	public long getMaxSize()
	{
		return getSettings().getLong( MAX_SIZE, 0 );
	}

	public void setMaxSize( long maxSize )
	{
		long old = getSettings().getLong( MAX_SIZE, 0 );
		getSettings().setLong( MAX_SIZE, maxSize );
		notifyPropertyChanged( MAX_SIZE, old, maxSize );
	}

	public String getDumpFile()
	{
		return dumpFile.get();
	}

	public void setDumpFile( String df )
	{
		String old = getDumpFile();
		dumpFile.set( df, false );
		notifyPropertyChanged( DUMP_FILE, old, getDumpFile() );
	}

	public boolean isRemoveEmptyContent()
	{
		return getSettings().getBoolean( REMOVE_EMPTY_CONTENT );
	}

	public void setRemoveEmptyContent( boolean removeEmptyContent )
	{
		boolean old = getSettings().getBoolean( REMOVE_EMPTY_CONTENT );
		getSettings().setBoolean( REMOVE_EMPTY_CONTENT, removeEmptyContent );
		notifyPropertyChanged( REMOVE_EMPTY_CONTENT, old, removeEmptyContent );
	}

	public boolean isStripWhitespaces()
	{
		return getSettings().getBoolean( STRIP_WHITESPACES );
	}

	public void setStripWhitespaces( boolean stripWhitespaces )
	{
		boolean old = getSettings().getBoolean( STRIP_WHITESPACES );
		getSettings().setBoolean( STRIP_WHITESPACES, stripWhitespaces );
		notifyPropertyChanged( STRIP_WHITESPACES, old, stripWhitespaces );
	}

	public boolean isFollowRedirects()
	{
		if( !getSettings().isSet( FOLLOW_REDIRECTS ) )
			return true;
		else
			return getSettings().getBoolean( FOLLOW_REDIRECTS );
	}

	public void setFollowRedirects( boolean followRedirects )
	{
		boolean old = getSettings().getBoolean( FOLLOW_REDIRECTS );
		getSettings().setBoolean( FOLLOW_REDIRECTS, followRedirects );
		notifyPropertyChanged( FOLLOW_REDIRECTS, old, followRedirects );
	}

	@Override
	public void beforeSave()
	{
		super.beforeSave();

		if( requestContent != null )
		{
			if( getConfig().getRequest() == null )
				getConfig().addNewRequest();

			CompressedStringSupport.setString( getConfig().getRequest(), requestContent );
			// requestContent = null;
		}
	}

	public static class RequestIconAnimator<T extends AbstractHttpRequest<?>> extends ModelItemIconAnimator<T> implements
			SubmitListener
	{
		public RequestIconAnimator( T modelItem, String baseIcon, String animIcon, int iconCounts )
		{
			super( modelItem, baseIcon, animIcon, iconCounts );
		}

		public boolean beforeSubmit( Submit submit, SubmitContext context )
		{
			if( isEnabled() && submit.getRequest() == getTarget() )
				start();
			return true;
		}

		public void afterSubmit( Submit submit, SubmitContext context )
		{
			if( submit.getRequest() == getTarget() )
				stop();
		}
	}

	public void setIconAnimator( RequestIconAnimator<?> iconAnimator )
	{
		if( this.iconAnimator != null )
			removeSubmitListener( this.iconAnimator );

		this.iconAnimator = iconAnimator;
		if( SoapUI.usingGraphicalEnvironment() )
		{
			addSubmitListener( this.iconAnimator );
		}
	}

	public HttpResponse getResponse()
	{
		return response;
	}

	public void setResponse( HttpResponse response, SubmitContext context )
	{
		HttpResponse oldResponse = getResponse();
		this.response = response;

		notifyPropertyChanged( RESPONSE_PROPERTY, oldResponse, response );
	}

	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );

		for( FileAttachment<?> attachment : attachments )
			attachment.resolve( context );
	}

	@Override
	public void addExternalDependencies( List<ExternalDependency> dependencies )
	{
		super.addExternalDependencies( dependencies );

		for( FileAttachment<?> attachment : attachments )
			attachment.addExternalDependency( dependencies );
	}

	public boolean hasEndpoint()
	{
		return StringUtils.hasContent( getEndpoint() );
	}

	public void setAfterRequestInjection( IAfterRequestInjection afterRequestInjection )
	{
		this.afterRequestInjection = afterRequestInjection;
	}

	public IAfterRequestInjection getAfterRequestInjection()
	{
		return afterRequestInjection;
	}
}
