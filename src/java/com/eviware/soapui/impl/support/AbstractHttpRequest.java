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

package com.eviware.soapui.impl.support;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.MutableAttachmentContainer;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.impl.wsdl.teststeps.SettingPathPropertySupport;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.settings.CommonSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringToStringMap;

public abstract class AbstractHttpRequest<T extends AbstractRequestConfig> extends AbstractWsdlModelItem<T> implements
		Request, PropertyExpansionContainer, MutableAttachmentContainer
{
	public final static Logger log = Logger.getLogger( AbstractHttpRequest.class );

	public static final String RESPONSE_PROPERTY = WsdlRequest.class.getName() + "@response";
	public static final String REMOVE_EMPTY_CONTENT = WsdlRequest.class.getName() + "@remove_empty_content";
	public static final String STRIP_WHITESPACES = WsdlRequest.class.getName() + "@strip-whitespaces";
	public static final String REQUEST_HEADERS_PROPERTY = WsdlRequest.class.getName() + "@request-headers";
	public static final String BIND_ADDRESS = WsdlRequest.class.getName() + "@bind_address";
	public static final String DISABLE_MULTIPART_ATTACHMENTS = WsdlRequest.class.getName()
			+ "@disable-multipart-attachments";
	public static final String DUMP_FILE = AbstractHttpRequest.class.getName() + "@dump-file";
	public static final String MAX_SIZE = AbstractHttpRequest.class.getName() + "@max-size";
	public static final String FOLLOW_REDIRECTS = AbstractHttpRequest.class.getName() + "@follow-redirects";

	public enum RequestMethod
	{
		GET, POST, PUT, DELETE, HEAD;

		public static String[] getMethodsAsString()
		{
			return new String[] {};
		}
	}

	private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();
	private String requestContent;
	private RequestIconAnimator<?> iconAnimator;
	private HttpResponse response;
	private SettingPathPropertySupport dumpFile;
	private List<FileAttachment<?>> attachments = new ArrayList<FileAttachment<?>>();

	protected AbstractHttpRequest( T config, AbstractHttpOperation parent, String icon, boolean forLoadTest )
	{
		super( config, parent, icon );

		if( !forLoadTest && !UISupport.isHeadless() )
		{
			iconAnimator = initIconAnimator();
			addSubmitListener( iconAnimator );
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

	public abstract RequestMethod getMethod();

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
		return new RequestIconAnimator<AbstractHttpRequest<?>>( this, "/request.gif", "/exec_request", 4, "gif" );
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
		if( attachment instanceof FileAttachment )
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

		if( StringUtils.isNullOrEmpty( request ) && StringUtils.isNullOrEmpty( old )
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
		String old = getEndpoint();
		if( old != null && old.equals( endpoint ) )
			return;

		getConfig().setEndpoint( endpoint );
		notifyPropertyChanged( ENDPOINT_PROPERTY, old, endpoint );
	}

	public String getEndpoint()
	{
		return getConfig().getEndpoint();
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

	public StringToStringMap getRequestHeaders()
	{
		return StringToStringMap.fromXml( getSettings().getString( REQUEST_HEADERS_PROPERTY, null ) );
	}

	public RequestIconAnimator<?> getIconAnimator()
	{
		return iconAnimator;
	}

	public void setRequestHeaders( StringToStringMap map )
	{
		StringToStringMap old = getRequestHeaders();
		getSettings().setString( REQUEST_HEADERS_PROPERTY, map.toXml() );
		notifyPropertyChanged( REQUEST_HEADERS_PROPERTY, old, map );
	}

	@Override
	public ImageIcon getIcon()
	{
		return iconAnimator == null || UISupport.isHeadless() ? null : iconAnimator.getIcon();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( this, this );

		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "requestContent" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "endpoint" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "username" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "password" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "domain" ) );

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
		public RequestIconAnimator( T modelItem, String baseIcon, String animIconRoot, int iconCount, String iconExtension )
		{
			super( modelItem, baseIcon, animIconRoot, iconCount, iconExtension );
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
		addSubmitListener( this.iconAnimator );
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

	@Override
	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );

		for( FileAttachment<?> attachment : attachments )
			attachment.resolve( context );
	}

	public boolean hasEndpoint()
	{
		return StringUtils.hasContent( getEndpoint() );
	}

	public abstract String getResponseContentAsXml();
}
