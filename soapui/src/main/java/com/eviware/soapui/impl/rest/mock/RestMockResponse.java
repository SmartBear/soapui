package com.eviware.soapui.impl.rest.mock;


import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RESTMockResponseConfig;
import com.eviware.soapui.impl.support.AbstractMockResponse;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RestMockResponse extends AbstractMockResponse<RESTMockResponseConfig>
{

	private String responseContent;
	private RestMockResult mockResult;
	public final static String MOCKRESULT_PROPERTY = RestMockResponse.class.getName() + "@mockresult";

	public RestMockResponse( RestMockAction action, RESTMockResponseConfig config )
	{
		super( config, action, "/mockResponse.gif" );
	}

	@Override
	public int getAttachmentCount()
	{
		return 0;
	}

	@Override
	public Attachment getAttachmentAt( int index )
	{
		return null;
	}

	@Override
	public Attachment[] getAttachmentsForPart( String partName )
	{
		return new Attachment[0];
	}

	@Override
	public MessagePart.AttachmentPart[] getDefinedAttachmentParts()
	{
		return new MessagePart.AttachmentPart[0];
	}

	@Override
	public MessagePart.AttachmentPart getAttachmentPart( String partName )
	{
		return null;
	}

	@Override
	public void addAttachmentsChangeListener( PropertyChangeListener listener )
	{

	}

	@Override
	public void removeAttachmentsChangeListener( PropertyChangeListener listener )
	{

	}

	@Override
	public boolean isMultipartEnabled()
	{
		return false;
	}

	@Override
	public String getEncoding()
	{
		return null;
	}

	@Override
	public boolean isMtomEnabled()
	{
		return false;
	}

	@Override
	public boolean isInlineFilesEnabled()
	{
		return false;
	}

	@Override
	public boolean isEncodeAttachments()
	{
		return false;
	}

	@Override
	public Attachment.AttachmentEncoding getAttachmentEncoding( String partName )
	{
		return null;
	}

	@Override
	public Attachment[] getAttachments()
	{
		return new Attachment[0];
	}

	@Override
	public MockOperation getMockOperation()
	{
		return ( MockOperation )getParent();
	}

	@Override
	public MockResult getMockResult()
	{
		return null;
	}

	@Override
	public Attachment attachFile( File file, boolean cache ) throws IOException
	{
		return null;
	}

	@Override
	public void removeAttachment( Attachment attachment )
	{

	}

	@Override
	public PropertyExpansion[] getPropertyExpansions()
	{
		return new PropertyExpansion[0];
	}

	@Override
	public String[] getPropertyNames()
	{
		return new String[0];
	}

	@Override
	public void setPropertyValue( String name, String value )
	{

	}

	@Override
	public String getPropertyValue( String name )
	{
		return null;
	}

	@Override
	public TestProperty getProperty( String name )
	{
		return null;
	}

	@Override
	public Map<String, TestProperty> getProperties()
	{
		return null;
	}

	@Override
	public void addTestPropertyListener( TestPropertyListener listener )
	{

	}

	@Override
	public void removeTestPropertyListener( TestPropertyListener listener )
	{

	}

	@Override
	public boolean hasProperty( String name )
	{
		return false;
	}

	@Override
	public ModelItem getModelItem()
	{
		return null;
	}

	@Override
	public int getPropertyCount()
	{
		return 0;
	}

	@Override
	public List<TestProperty> getPropertyList()
	{
		return null;
	}

	@Override
	public TestProperty getPropertyAt( int index )
	{
		return null;
	}

	@Override
	public String getPropertiesLabel()
	{
		return null;
	}

	public RestMockResult execute( RestMockRequest request, RestMockResult result ) throws DispatchException
	{
		try
		{
			// iconAnimator.start();
         /*
			TODO: break this out into base class (also remove it from Wsdl counterpart)
			getProperty( "Request" ).setValue( request.getRequestContent() );


			long delay = getResponseDelay();
			if( delay > 0 )
				Thread.sleep( delay );

			String script = getScript();
			if( script != null && script.trim().length() > 0 )
			{
				evaluateScript( request );
			}*/

			String responseContent = getResponseContent();

			// create merged context
			WsdlMockRunContext context = new WsdlMockRunContext( request.getContext().getMockService(), null );
			context.setMockResponse( this );

			context.putAll( request.getContext() );
			context.putAll( request.getRequestContext() );

			StringToStringsMap responseHeaders = getResponseHeaders();
			for( Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet() )
			{
				for( String value : headerEntry.getValue() )
					result.addHeader( headerEntry.getKey(), PropertyExpander.expandProperties( context, value ) );
			}


			/* TODO: break this out into base class (also remove it from Wsdl counterpart)
			responseContent = PropertyExpander.expandProperties( context, responseContent, isEntitizeProperties() );
         */


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

	}

	private String writeResponse( RestMockResult restMockResult, String responseContent ) throws Exception
	{
		MimeMultipart mp = null;
		RestMockAction operation = ( RestMockAction )getMockOperation();

		if( operation == null )
			throw new Exception( "Missing RestMockAction for mock response" );


		StringToStringMap contentIds = new StringToStringMap();


		String status = getResponseHttpStatus();
		RestMockRequest request = restMockResult.getMockRequest();

		if( status == null || status.trim().length() == 0 )
		{

			request.getHttpResponse().setStatus( HttpServletResponse.SC_OK );
			restMockResult.setResponseStatus( HttpServletResponse.SC_OK );
		}
		else
		{
			try
			{
				int statusCode = Integer.parseInt( status );
				request.getHttpResponse().setStatus( statusCode );
				restMockResult.setResponseStatus( statusCode );
			}
			catch( RuntimeException e )
			{
				SoapUI.logError( e );
			}
		}

		ByteArrayOutputStream outData = new ByteArrayOutputStream();

		// non-multipart request?

		String responseCompression = getResponseCompression();
		String encoding = getEncoding();
		byte[] content = encoding == null ? responseContent.getBytes() : responseContent.getBytes( encoding );
		outData.write( content );


		/* TODO: break this out into base class (also remove it from Wsdl counterpart)
		if( !isXOP && ( mp == null || mp.getCount() == 0 ) && getAttachmentCount() == 0 )
		{
			String encoding = getEncoding();
			if( responseContent == null )
				responseContent = "";

			byte[] content = encoding == null ? responseContent.getBytes() : responseContent.getBytes( encoding );

			String acceptEncoding = restMockResult.getMockRequest().getRequestHeaders().get( "Accept-Encoding", "" );
			if( AUTO_RESPONSE_COMPRESSION.equals( responseCompression ) && acceptEncoding != null
					&& acceptEncoding.toUpperCase().contains( "GZIP" ) )
			{
				restMockResult.addHeader( "Content-Encoding", "gzip" );
				outData.write( CompressionSupport.compress( CompressionSupport.ALG_GZIP, content ) );
			}
			else if( AUTO_RESPONSE_COMPRESSION.equals( responseCompression ) && acceptEncoding != null
					&& acceptEncoding.toUpperCase().contains( "DEFLATE" ) )
			{
				restMockResult.addHeader( "Content-Encoding", "deflate" );
				outData.write( CompressionSupport.compress( CompressionSupport.ALG_DEFLATE, content ) );
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

			restMockResult.addHeader( "Content-Type", mimeMessageRequestEntity.getContentType().getValue() );
			restMockResult.addHeader( "MIME-Version", "1.0" );
			mimeMessageRequestEntity.writeTo( outData );
		}*/

		if( outData.size() > 0 )
		{
			byte[] data = outData.toByteArray();

			if( responseCompression.equals( CompressionSupport.ALG_DEFLATE )
					|| responseCompression.equals( CompressionSupport.ALG_GZIP ) )
			{
				restMockResult.addHeader( "Content-Encoding", responseCompression );
				data = CompressionSupport.compress( responseCompression, data );
			}

			restMockResult.writeRawResponseData( data );
		}

		return responseContent;
	}

	protected String mockresultProperty()
	{
		return MOCKRESULT_PROPERTY;
	}

}