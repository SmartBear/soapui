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

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractHttpRequest.RequestIconAnimator;
import com.eviware.soapui.impl.wsdl.MutableAttachmentContainer;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.IAfterRequestInjection;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.types.StringToStringMap;

public interface AbstractHttpRequestInterface<T extends AbstractRequestConfig> extends Request,
		PropertyExpansionContainer, MutableAttachmentContainer
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

	public abstract RestRequestInterface.RequestMethod getMethod();

	public abstract void setMultipartEnabled( boolean multipartEnabled );

	public abstract boolean isEntitizeProperties();

	public abstract void setEntitizeProperties( boolean entitizeProperties );

	public abstract void release();

	public abstract SubmitListener[] getSubmitListeners();

	public abstract void copyAttachmentsTo( WsdlRequest newRequest );

	public abstract Attachment importAttachment( Attachment attachment );

	public abstract boolean isReadOnly();

	public abstract void setRequestContent( String request );

	public abstract boolean isPrettyPrint();

	public abstract void setPrettyPrint( boolean prettyPrint );

	public abstract StringToStringMap getRequestHeaders();

	public abstract RequestIconAnimator<?> getIconAnimator();

	public abstract void setRequestHeaders( StringToStringMap map );

	public abstract ImageIcon getIcon();

	public abstract String getUsername();

	public abstract String getPassword();

	public abstract String getDomain();

	public abstract void setUsername( String username );

	public abstract void setPassword( String password );

	public abstract void setDomain( String domain );

	public abstract String getSslKeystore();

	public abstract void setSslKeystore( String sslKeystore );

	public abstract String getBindAddress();

	public abstract void setBindAddress( String bindAddress );

	public abstract long getMaxSize();

	public abstract void setMaxSize( long maxSize );

	public abstract String getDumpFile();

	public abstract void setDumpFile( String df );

	public abstract boolean isRemoveEmptyContent();

	public abstract void setRemoveEmptyContent( boolean removeEmptyContent );

	public abstract boolean isStripWhitespaces();

	public abstract void setStripWhitespaces( boolean stripWhitespaces );

	public abstract boolean isFollowRedirects();

	public abstract void setFollowRedirects( boolean followRedirects );

	public abstract void beforeSave();

	public abstract void setIconAnimator( RequestIconAnimator<?> iconAnimator );

	public abstract HttpResponse getResponse();

	public abstract void setResponse( HttpResponse response, SubmitContext context );

	public abstract void resolve( ResolveContext<?> context );

	public abstract boolean hasEndpoint();

	public IAfterRequestInjection getAfterRequestInjection();

}