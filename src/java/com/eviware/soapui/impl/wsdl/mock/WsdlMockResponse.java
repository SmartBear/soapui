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

package com.eviware.soapui.impl.wsdl.mock;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Message;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.HeaderConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.MutableWsdlAttachmentContainer;
import com.eviware.soapui.impl.wsdl.WsdlContentPart;
import com.eviware.soapui.impl.wsdl.WsdlHeaderPart;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.filters.RemoveEmptyContentRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.AttachmentUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.BodyPartAttachment;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MimeMessageMockResponseEntity;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MockResponseDataSource;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.FileAttachment;
import com.eviware.soapui.impl.wsdl.support.MapTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.MessageXmlObject;
import com.eviware.soapui.impl.wsdl.support.MessageXmlPart;
import com.eviware.soapui.impl.wsdl.support.MockFileAttachment;
import com.eviware.soapui.impl.wsdl.support.ModelItemIconAnimator;
import com.eviware.soapui.impl.wsdl.support.WsdlAttachment;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils.SoapHeader;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockRunContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.settings.CommonSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * A WsdlMockResponse contained by a WsdlMockOperation
 * 
 * @author ole.matzura
 */

public class WsdlMockResponse extends AbstractWsdlModelItem<MockResponseConfig> implements MockResponse,
		MutableWsdlAttachmentContainer, PropertyExpansionContainer, TestPropertyHolder, WsaContainer
{
	private final static Logger log = Logger.getLogger( WsdlMockResponse.class );

	public final static String MOCKRESULT_PROPERTY = WsdlMockResponse.class.getName() + "@mockresult";
	public final static String SCRIPT_PROPERTY = WsdlMockResponse.class.getName() + "@script";
	public final static String HEADERS_PROPERTY = WsdlMockResponse.class.getName() + "@headers";
	public final static String DISABLE_MULTIPART_ATTACHMENTS = WsdlMockResponse.class.getName()
			+ "@disable-multipart-attachments";
	public static final String FORCE_MTOM = WsdlMockResponse.class.getName() + "@force_mtom";
	public static final String ENABLE_INLINE_FILES = WsdlMockResponse.class.getName() + "@enable_inline_files";
	public final static String RESPONSE_DELAY_PROPERTY = WsdlMockResponse.class.getName() + "@response-delay";
	public static final String STRIP_WHITESPACES = WsdlMockResponse.class.getName() + "@strip-whitespaces";
	public static final String REMOVE_EMPTY_CONTENT = WsdlMockResponse.class.getName() + "@remove_empty_content";
	public static final String ENCODE_ATTACHMENTS = WsdlMockResponse.class.getName() + "@encode_attachments";
	public static final String RESPONSE_HTTP_STATUS = WsdlMockResponse.class.getName() + "@response-http-status";
	public static final String OUGOING_WSS = WsdlMockResponse.class.getName() + "@outgoing-wss";

	protected List<FileAttachment<WsdlMockResponse>> attachments = new ArrayList<FileAttachment<WsdlMockResponse>>();
	private List<HttpAttachmentPart> definedAttachmentParts;
	private ModelItemIconAnimator<WsdlMockResponse> iconAnimator;
	private WsdlMockResult mockResult;
	private String responseContent;
	private ScriptEnginePool scriptEnginePool;
	private MapTestPropertyHolder propertyHolder;
	private WsaConfig wsaConfig;

	public WsdlMockResponse( WsdlMockOperation operation, MockResponseConfig config )
	{
		super( config, operation, "/mockResponse.gif" );

		for( AttachmentConfig ac : getConfig().getAttachmentList() )
		{
			attachments.add( new MockFileAttachment( ac, this ) );
		}

		if( !config.isSetEncoding() )
			config.setEncoding( "UTF-8" );

		iconAnimator = new ModelItemIconAnimator<WsdlMockResponse>( this, "/mockResponse.gif", "/exec_request", 4, "gif" );

		scriptEnginePool = new ScriptEnginePool( this );
		scriptEnginePool.setScript( getScript() );

		propertyHolder = new MapTestPropertyHolder( this );
		propertyHolder.addProperty( "Request" );
	}

	@Override
	public void setConfig( MockResponseConfig config )
	{
		super.setConfig( config );

		if( wsaConfig != null )
		{
			if( config.isSetWsaConfig() )
				wsaConfig.setConfig( config.getWsaConfig() );
			else
				wsaConfig = null;
		}

		if( scriptEnginePool != null )
			scriptEnginePool.setScript( getScript() );
	}

	public Attachment[] getAttachments()
	{
		return attachments.toArray( new Attachment[attachments.size()] );
	}

	public String getScript()
	{
		return getConfig().isSetScript() ? getConfig().getScript().getStringValue() : null;
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

	public String getResponseContent()
	{
		if( getConfig().getResponseContent() == null )
			getConfig().addNewResponseContent();

		if( responseContent == null )
			responseContent = CompressedStringSupport.getString( getConfig().getResponseContent() );

		return responseContent;
	}

	public void setResponseContent( String responseContent )
	{
		String oldContent = getResponseContent();
		if( responseContent.equals( oldContent ) )
			return;

		this.responseContent = responseContent;
		notifyPropertyChanged( RESPONSE_CONTENT_PROPERTY, oldContent, responseContent );
	}

	@Override
	public ImageIcon getIcon()
	{
		return iconAnimator.getIcon();
	}

	public WsdlMockOperation getMockOperation()
	{
		return ( WsdlMockOperation )getParent();
	}

	public WsdlMockResult execute( WsdlMockRequest request, WsdlMockResult result ) throws DispatchException
	{
		try
		{
			iconAnimator.start();

			getProperty( "Request" ).setValue( request.getRequestContent() );

			long delay = getResponseDelay();
			if( delay > 0 )
				Thread.sleep( delay );

			String script = getScript();
			if( script != null && script.trim().length() > 0 )
			{
				evaluateScript( request );
			}

			String responseContent = getResponseContent();

			// create merged context
			WsdlMockRunContext context = new WsdlMockRunContext( request.getContext().getMockService(), null );
			context.setMockResponse( this );
			context.putAll( request.getContext() );
			context.putAll( request.getRequestContext() );

			StringToStringMap responseHeaders = getResponseHeaders();
			for( String name : responseHeaders.keySet() )
			{
				result.addHeader( name, PropertyExpansionUtils.expandProperties( context, responseHeaders.get( name ) ) );
			}

			responseContent = PropertyExpansionUtils.expandProperties( context, responseContent, isEntitizeProperties() );

			if( this.getWsaConfig().isWsaEnabled() )
			{
				responseContent = new WsaUtils( responseContent, getSoapVersion(), getMockOperation().getOperation(),
						context ).addWSAddressingMockResponse( this, request );
			}

			String outgoingWss = getOutgoingWss();
			if( StringUtils.isNullOrEmpty( outgoingWss ) )
				outgoingWss = getMockOperation().getMockService().getOutgoingWss();

			if( StringUtils.hasContent( outgoingWss ) )
			{
				OutgoingWss outgoing = getMockOperation().getMockService().getProject().getWssContainer()
						.getOutgoingWssByName( outgoingWss );
				if( outgoing != null )
				{
					Document dom = XmlUtils.parseXml( responseContent );
					outgoing.processOutgoing( dom, context );
					StringWriter writer = new StringWriter();
					XmlUtils.serialize( dom, writer );
					responseContent = writer.toString();
				}
			}

			if( !result.isCommitted() )
			{
				responseContent = writeResponse( result, responseContent );
			}

			result.setResponseContent( responseContent );

			setMockResult( result );

			return mockResult;
		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
			throw new DispatchException( e );
		}
		finally
		{
			iconAnimator.stop();
		}
	}

	public void evaluateScript( WsdlMockRequest request ) throws Exception
	{
		String script = getScript();
		if( script == null || script.trim().length() == 0 )
			return;

		WsdlMockService mockService = getMockOperation().getMockService();
		WsdlMockRunner mockRunner = mockService.getMockRunner();
		MockRunContext context = mockRunner == null ? new WsdlMockRunContext( mockService, null ) : mockRunner
				.getMockContext();

		SoapUIScriptEngine scriptEngine = scriptEnginePool.getScriptEngine();

		try
		{
			scriptEngine.setVariable( "context", context );
			scriptEngine.setVariable( "requestContext", request == null ? null : request.getRequestContext() );
			scriptEngine.setVariable( "mockContext", context );
			scriptEngine.setVariable( "mockRequest", request );
			scriptEngine.setVariable( "mockResponse", this );
			scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

			scriptEngine.run();
		}
		catch( RuntimeException e )
		{
			throw new Exception( e.getMessage(), e );
		}
		finally
		{
			scriptEnginePool.returnScriptEngine( scriptEngine );
		}
	}

	@Override
	public void release()
	{
		super.release();
		scriptEnginePool.release();
	}

	public void setScript( String script )
	{
		String oldScript = getScript();
		if( !script.equals( oldScript ) )
		{
			if( !getConfig().isSetScript() )
				getConfig().addNewScript();
			getConfig().getScript().setStringValue( script );

			scriptEnginePool.setScript( script );

			notifyPropertyChanged( SCRIPT_PROPERTY, oldScript, script );
		}
	}

	public void setResponseHeaders( StringToStringMap headers )
	{
		StringToStringMap oldHeaders = getResponseHeaders();

		HeaderConfig[] headerConfigs = new HeaderConfig[headers.size()];
		int ix = 0;
		for( String header : headers.keySet() )
		{
			headerConfigs[ix] = HeaderConfig.Factory.newInstance();
			headerConfigs[ix].setName( header );
			headerConfigs[ix].setValue( headers.get( header ) );
			ix++ ;
		}

		getConfig().setHeaderArray( headerConfigs );

		notifyPropertyChanged( HEADERS_PROPERTY, oldHeaders, headers );
	}

	public StringToStringMap getResponseHeaders()
	{
		StringToStringMap result = new StringToStringMap();
		List<HeaderConfig> headerList = getConfig().getHeaderList();
		for( HeaderConfig header : headerList )
		{
			result.put( header.getName(), header.getValue() );
		}

		return result;
	}

	public MessagePart[] getRequestParts()
	{
		try
		{
			List<MessagePart> result = new ArrayList<MessagePart>();
			result.addAll( Arrays.asList( getMockOperation().getOperation().getDefaultRequestParts() ) );

			if( getMockResult() != null )
				result.addAll( AttachmentUtils.extractAttachmentParts( getMockOperation().getOperation(), getMockResult()
						.getMockRequest().getRequestContent(), true, false, isMtomEnabled() ) );

			return result.toArray( new MessagePart[result.size()] );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return new MessagePart[0];
		}
	}

	public MessagePart[] getResponseParts()
	{
		try
		{
			// init
			WsdlOperation op = getMockOperation().getOperation();
			if( op == null || op.isUnidirectional() )
				return new MessagePart[0];

			List<MessagePart> result = new ArrayList<MessagePart>();
			WsdlContext wsdlContext = op.getInterface().getWsdlContext();
			BindingOperation bindingOperation = op.findBindingOperation( wsdlContext.getDefinition() );

			if( bindingOperation == null )
				return new MessagePart[0];

			// header parts
			BindingOutput bindingOutput = bindingOperation.getBindingOutput();
			List<SoapHeader> headers = bindingOutput == null ? new ArrayList<SoapHeader>() : WsdlUtils
					.getSoapHeaders( bindingOutput.getExtensibilityElements() );

			for( int i = 0; i < headers.size(); i++ )
			{
				SoapHeader header = headers.get( i );

				Message message = wsdlContext.getDefinition().getMessage( header.getMessage() );
				if( message == null )
				{
					log.error( "Missing message for header: " + header.getMessage() );
					continue;
				}

				javax.wsdl.Part part = message.getPart( header.getPart() );

				if( part != null )
				{
					SchemaType schemaType = WsdlUtils.getSchemaTypeForPart( wsdlContext, part );
					SchemaGlobalElement schemaElement = WsdlUtils.getSchemaElementForPart( wsdlContext, part );
					if( schemaType != null )
						result.add( new WsdlHeaderPart( part.getName(), schemaType, part.getElementName(), schemaElement ) );
				}
				else
					log.error( "Missing part for header; " + header.getPart() );
			}

			// content parts
			javax.wsdl.Part[] parts = WsdlUtils.getOutputParts( bindingOperation );

			for( int i = 0; i < parts.length; i++ )
			{
				javax.wsdl.Part part = parts[i];

				if( !WsdlUtils.isAttachmentOutputPart( part, bindingOperation ) )
				{
					SchemaType schemaType = WsdlUtils.getSchemaTypeForPart( wsdlContext, part );
					SchemaGlobalElement schemaElement = WsdlUtils.getSchemaElementForPart( wsdlContext, part );
					if( schemaType != null )
						result.add( new WsdlContentPart( part.getName(), schemaType, part.getElementName(), schemaElement ) );
				}
			}

			result.addAll( Arrays.asList( getDefinedAttachmentParts() ) );

			return result.toArray( new MessagePart[result.size()] );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
			return new MessagePart[0];
		}
	}

	public Attachment attachFile( File file, boolean cache ) throws IOException
	{
		FileAttachment<WsdlMockResponse> fileAttachment = new MockFileAttachment( file, cache, this );
		attachments.add( fileAttachment );
		notifyPropertyChanged( ATTACHMENTS_PROPERTY, null, fileAttachment );
		return fileAttachment;
	}

	public int getAttachmentCount()
	{
		return attachments.size();
	}

	public WsdlAttachment getAttachmentAt( int index )
	{
		return attachments.get( index );
	}

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

	public HttpAttachmentPart[] getDefinedAttachmentParts()
	{
		if( definedAttachmentParts == null )
		{
			try
			{
				WsdlOperation operation = getMockOperation().getOperation();
				if( operation == null )
				{
					definedAttachmentParts = new ArrayList<HttpAttachmentPart>();
				}
				else
				{
					UISupport.setHourglassCursor();
					definedAttachmentParts = AttachmentUtils.extractAttachmentParts( operation, getResponseContent(), true,
							true, isMtomEnabled() );
				}
			}
			catch( Exception e )
			{
				log.warn( e.toString() );
			}
			finally
			{
				UISupport.resetCursor();
			}
		}

		return definedAttachmentParts.toArray( new HttpAttachmentPart[definedAttachmentParts.size()] );
	}

	public HttpAttachmentPart getAttachmentPart( String partName )
	{
		HttpAttachmentPart[] parts = getDefinedAttachmentParts();
		for( HttpAttachmentPart part : parts )
		{
			if( part.getName().equals( partName ) )
				return part;
		}

		return null;
	}

	public Attachment[] getAttachmentsForPart( String partName )
	{
		List<Attachment> result = new ArrayList<Attachment>();

		for( Attachment attachment : attachments )
		{
			if( attachment.getPart().equals( partName ) )
				result.add( attachment );
		}

		return result.toArray( new Attachment[result.size()] );
	}

	public boolean isMtomEnabled()
	{
		return getSettings().getBoolean( WsdlSettings.ENABLE_MTOM );
	}

	public void setMtomEnabled( boolean mtomEnabled )
	{
		boolean old = isMtomEnabled();
		getSettings().setBoolean( WsdlSettings.ENABLE_MTOM, mtomEnabled );
		definedAttachmentParts = null;
		notifyPropertyChanged( MTOM_NABLED_PROPERTY, old, mtomEnabled );
	}

	private String writeResponse( WsdlMockResult response, String responseContent ) throws Exception
	{
		MimeMultipart mp = null;
		WsdlOperation operation = getMockOperation().getOperation();
		if( operation == null )
			throw new Exception( "Missing WsdlOperation for mock response" );

		SoapVersion soapVersion = operation.getInterface().getSoapVersion();

		StringToStringMap contentIds = new StringToStringMap();
		boolean isXOP = isMtomEnabled() && isForceMtom();

		// preprocess only if neccessary
		if( isMtomEnabled() || isInlineFilesEnabled() || getAttachmentCount() > 0 )
		{
			try
			{
				mp = new MimeMultipart();

				MessageXmlObject requestXmlObject = new MessageXmlObject( operation, responseContent, false );
				MessageXmlPart[] requestParts = requestXmlObject.getMessageParts();
				for( MessageXmlPart requestPart : requestParts )
				{
					if( AttachmentUtils.prepareMessagePart( this, mp, requestPart, contentIds ) )
						isXOP = true;
				}
				responseContent = requestXmlObject.getMessageContent();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		if( isRemoveEmptyContent() )
		{
			responseContent = RemoveEmptyContentRequestFilter.removeEmptyContent( responseContent, getSoapVersion()
					.getEnvelopeNamespace() );
		}

		if( isStripWhitespaces() )
		{
			responseContent = XmlUtils.stripWhitespaces( responseContent );
		}

		String status = getResponseHttpStatus();
		WsdlMockRequest request = response.getMockRequest();

		if( status == null || status.trim().length() == 0 )
		{
			if( SoapUtils.isSoapFault( responseContent, request.getSoapVersion() ) )
			{
				request.getHttpResponse().setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
				response.setResponseStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			}
			else
			{
				request.getHttpResponse().setStatus( HttpServletResponse.SC_OK );
				response.setResponseStatus( HttpServletResponse.SC_OK );
			}
		}
		else
		{
			try
			{
				int statusCode = Integer.parseInt( status );
				request.getHttpResponse().setStatus( statusCode );
				response.setResponseStatus( statusCode );
			}
			catch( RuntimeException e )
			{
				SoapUI.logError( e );
			}
		}

		ByteArrayOutputStream outData = new ByteArrayOutputStream();

		// non-multipart request?
		if( !isXOP && ( mp == null || mp.getCount() == 0 ) && getAttachmentCount() == 0 )
		{
			String encoding = getEncoding();
			if( responseContent == null )
				responseContent = "";

			byte[] content = encoding == null ? responseContent.getBytes() : responseContent.getBytes( encoding );

			response.setContentType( soapVersion.getContentTypeHttpHeader( encoding, null ) );

			String acceptEncoding = response.getMockRequest().getRequestHeaders().get( "Accept-Encoding" );
			if( acceptEncoding != null && acceptEncoding.toUpperCase().contains( "GZIP" ) )
			{
				response.addHeader( "Content-Encoding", "gzip" );
				GZIPOutputStream zipOut = new GZIPOutputStream( outData );
				zipOut.write( content );
				zipOut.close();
			}
			else
			{
				outData.write( content );
			}
		}
		else
		{
			// make sure..
			if( mp == null )
				mp = new MimeMultipart();

			// init root part
			initRootPart( responseContent, mp, isXOP );

			// init mimeparts
			AttachmentUtils.addMimeParts( this, Arrays.asList( getAttachments() ), mp, contentIds );

			// create request message
			MimeMessage message = new MimeMessage( AttachmentUtils.JAVAMAIL_SESSION );
			message.setContent( mp );
			message.saveChanges();
			MimeMessageMockResponseEntity mimeMessageRequestEntity = new MimeMessageMockResponseEntity( message, isXOP,
					this );

			response.addHeader( "Content-Type", mimeMessageRequestEntity.getContentType() );
			response.addHeader( "MIME-Version", "1.0" );
			mimeMessageRequestEntity.writeRequest( outData );
		}

		if( outData.size() > 0 )
			response.writeRawResponseData( outData.toByteArray() );

		return responseContent;
	}

	private void initRootPart( String requestContent, MimeMultipart mp, boolean isXOP ) throws MessagingException
	{
		MimeBodyPart rootPart = new PreencodedMimeBodyPart( "8bit" );
		rootPart.setContentID( AttachmentUtils.ROOTPART_SOAPUI_ORG );
		mp.addBodyPart( rootPart, 0 );

		DataHandler dataHandler = new DataHandler( new MockResponseDataSource( this, requestContent, isXOP ) );
		rootPart.setDataHandler( dataHandler );
	}

	@SuppressWarnings( "unchecked" )
	public Attachment addAttachment( Attachment attachment )
	{
		if( attachment instanceof BodyPartAttachment )
		{
			try
			{
				BodyPartAttachment att = ( BodyPartAttachment )attachment;

				AttachmentConfig newConfig = getConfig().addNewAttachment();
				newConfig.setData( Tools.readAll( att.getInputStream(), 0 ).toByteArray() );
				newConfig.setContentId( att.getContentID() );
				newConfig.setContentType( att.getContentType() );
				newConfig.setName( att.getName() );

				FileAttachment<WsdlMockResponse> newAttachment = new MockFileAttachment( newConfig, this );
				attachments.add( newAttachment );
				return newAttachment;
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
		else if( attachment instanceof FileAttachment )
		{
			AttachmentConfig oldConfig = ( ( FileAttachment<WsdlMockResponse> )attachment ).getConfig();
			AttachmentConfig newConfig = ( AttachmentConfig )getConfig().addNewAttachment().set( oldConfig );
			FileAttachment<WsdlMockResponse> newAttachment = new MockFileAttachment( newConfig, this );
			attachments.add( newAttachment );
			return newAttachment;
		}

		return null;
	}

	public void setResponseDelay( long delay )
	{
		long oldDelay = getResponseDelay();

		if( delay == 0 )
			getSettings().clearSetting( RESPONSE_DELAY_PROPERTY );
		else
			getSettings().setLong( RESPONSE_DELAY_PROPERTY, delay );

		notifyPropertyChanged( RESPONSE_DELAY_PROPERTY, oldDelay, delay );
	}

	public long getResponseDelay()
	{
		return getSettings().getLong( RESPONSE_DELAY_PROPERTY, 0 );
	}

	public void setResponseHttpStatus( String httpStatus )
	{
		String oldStatus = getResponseHttpStatus();

		getConfig().setHttpResponseStatus( httpStatus );

		notifyPropertyChanged( RESPONSE_HTTP_STATUS, oldStatus, httpStatus );
	}

	public String getResponseHttpStatus()
	{
		return getConfig().getHttpResponseStatus();
	}

	public void setMockResult( WsdlMockResult mockResult )
	{
		WsdlMockResult oldResult = this.mockResult;
		this.mockResult = mockResult;
		notifyPropertyChanged( MOCKRESULT_PROPERTY, oldResult, mockResult );
	}

	public WsdlMockResult getMockResult()
	{
		return mockResult;
	}

	public long getContentLength()
	{
		return getResponseContent() == null ? 0 : getResponseContent().length();
	}

	public boolean isMultipartEnabled()
	{
		return !getSettings().getBoolean( DISABLE_MULTIPART_ATTACHMENTS );
	}

	public void setMultipartEnabled( boolean multipartEnabled )
	{
		getSettings().setBoolean( DISABLE_MULTIPART_ATTACHMENTS, multipartEnabled );
	}

	public boolean isEntitizeProperties()
	{
		return getSettings().getBoolean( CommonSettings.ENTITIZE_PROPERTIES );
	}

	public void setEntitizeProperties( boolean entitizeProperties )
	{
		getSettings().setBoolean( CommonSettings.ENTITIZE_PROPERTIES, entitizeProperties );
	}

	public boolean isForceMtom()
	{
		return getSettings().getBoolean( FORCE_MTOM );
	}

	public void setForceMtom( boolean forceMtom )
	{
		boolean old = getSettings().getBoolean( FORCE_MTOM );
		getSettings().setBoolean( FORCE_MTOM, forceMtom );
		notifyPropertyChanged( FORCE_MTOM, old, forceMtom );
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

	public boolean isEncodeAttachments()
	{
		return getSettings().getBoolean( ENCODE_ATTACHMENTS );
	}

	public void setEncodeAttachments( boolean encodeAttachments )
	{
		boolean old = getSettings().getBoolean( ENCODE_ATTACHMENTS );
		getSettings().setBoolean( ENCODE_ATTACHMENTS, encodeAttachments );
		notifyPropertyChanged( ENCODE_ATTACHMENTS, old, encodeAttachments );
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

	public boolean isInlineFilesEnabled()
	{
		return getSettings().getBoolean( WsdlMockResponse.ENABLE_INLINE_FILES );
	}

	public void setInlineFilesEnabled( boolean inlineFilesEnabled )
	{
		getSettings().setBoolean( WsdlMockResponse.ENABLE_INLINE_FILES, inlineFilesEnabled );
	}

	@Override
	public void beforeSave()
	{
		super.beforeSave();

		if( responseContent != null )
		{
			CompressedStringSupport.setString( getConfig().getResponseContent(), responseContent );
		}
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

	public SoapVersion getSoapVersion()
	{
		return getMockOperation().getOperation() == null ? SoapVersion.Soap11 : getMockOperation().getOperation()
				.getInterface().getSoapVersion();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, this, "responseContent" ) );

		StringToStringMap responseHeaders = getResponseHeaders();
		for( String key : responseHeaders.keySet() )
		{
			result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, new ResponseHeaderHolder(
					responseHeaders, key ), "value" ) );
		}

		addWsaPropertyExpansions( result, getWsaConfig(), this );
		return result.toArray( new PropertyExpansion[result.size()] );
	}

	public void addWsaPropertyExpansions( List<PropertyExpansion> result, WsaConfig wsaConfig, ModelItem modelItem )
	{
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "action" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "from" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "to" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "replyTo" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "replyToRefParams" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "faultTo" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "faultToRefParams" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "relatesTo" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "relationshipType" ) );
		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( modelItem, wsaConfig, "messageID" ) );
	}

	public class ResponseHeaderHolder
	{
		private final StringToStringMap valueMap;
		private final String key;

		public ResponseHeaderHolder( StringToStringMap valueMap, String key )
		{
			this.valueMap = valueMap;
			this.key = key;
		}

		public String getValue()
		{
			return valueMap.get( key );
		}

		public void setValue( String value )
		{
			valueMap.put( key, value );
			setResponseHeaders( valueMap );
		}
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolder.addTestPropertyListener( listener );
	}

	public ModelItem getModelItem()
	{
		return propertyHolder.getModelItem();
	}

	public Map<String, TestProperty> getProperties()
	{
		return propertyHolder.getProperties();
	}

	public TestProperty getProperty( String name )
	{
		return propertyHolder.getProperty( name );
	}

	public String[] getPropertyNames()
	{
		return propertyHolder.getPropertyNames();
	}

	public String getPropertyValue( String name )
	{
		return propertyHolder.getPropertyValue( name );
	}

	public boolean hasProperty( String name )
	{
		return propertyHolder.hasProperty( name );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolder.removeTestPropertyListener( listener );
	}

	public void setPropertyValue( String name, String value )
	{
		propertyHolder.setPropertyValue( name, value );
	}

	public String getOutgoingWss()
	{
		return getConfig().getOutgoingWss();
	}

	public void setOutgoingWss( String outgoingWss )
	{
		String old = getOutgoingWss();
		getConfig().setOutgoingWss( outgoingWss );
		notifyPropertyChanged( OUGOING_WSS, old, outgoingWss );
	}

	public TestProperty getPropertyAt( int index )
	{
		return propertyHolder.getPropertyAt( index );
	}

	public int getPropertyCount()
	{
		return propertyHolder.getPropertyCount();
	}

	public List<TestProperty> getPropertyList()
	{
		return propertyHolder.getPropertyList();
	}

	public String getPropertiesLabel()
	{
		return "Custom Properties";
	}

	public AttachmentEncoding getAttachmentEncoding( String partName )
	{
		HttpAttachmentPart attachmentPart = getAttachmentPart( partName );
		if( attachmentPart == null )
			return AttachmentUtils.getAttachmentEncoding( getOperation(), partName, true );
		else
			return AttachmentUtils.getAttachmentEncoding( getOperation(), attachmentPart, true );
	}

	public WsaConfig getWsaConfig()
	{
		if( wsaConfig == null )
		{
			if( !getConfig().isSetWsaConfig() )
			{
				getConfig().addNewWsaConfig();
			}
			wsaConfig = new WsaConfig( getConfig().getWsaConfig(), this );
			// wsaConfig.setGenerateMessageId(true);
		}
		return wsaConfig;
	}

	public boolean isWsAddressing()
	{
		return getConfig().getUseWsAddressing();
	}

	public void setWsAddressing( boolean wsAddressing )
	{
		boolean old = getConfig().getUseWsAddressing();
		getConfig().setUseWsAddressing( wsAddressing );
		notifyPropertyChanged( "wsAddressing", old, wsAddressing );
	}

	public boolean isWsaEnabled()
	{
		return isWsAddressing();
	}

	public void setWsaEnabled( boolean arg0 )
	{
		setWsAddressing( arg0 );

	}

	public WsdlOperation getOperation()
	{
		return getMockOperation().getOperation();
	}

}
