package com.eviware.soapui.impl.support;

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.support.DateUtil;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.mortbay.jetty.HttpFields;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

public class BaseMockResult<MockRequestType extends MockRequest,
		MockOperationType extends MockOperation,
		MockResponseType extends MockResponse> implements MockResult
{
	private MockResponseType mockResponse;
	private String responseContent;
	private long timeTaken;
	private long timestamp;
	private DefaultActionList actions;
	private StringToStringsMap responseHeaders = new StringToStringsMap();
	private MockRequestType mockRequest;
	private byte[] rawResponseData;
	private MockOperationType mockOperation;
	private String responseContentType;
	private int responseStatus = 200;

	public BaseMockResult( MockRequestType request ) throws Exception
	{
		timestamp = System.currentTimeMillis();
		mockRequest = request;
	}

	public MockRequestType getMockRequest()
	{
		return mockRequest;
	}

	public ActionList getActions()
	{
		if( actions == null )
		{
			actions = new DefaultActionList( "MockResult" );
		}

		return actions;
	}

	public MockResponse getMockResponse()
	{
		return mockResponse;
	}

	public String getResponseContent()
	{
		return responseContent;
	}

	public long getTimeTaken()
	{
		return timeTaken;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( long timestamp )
	{
		this.timestamp = timestamp;
	}

	public void setTimeTaken( long timeTaken )
	{
		this.timeTaken = timeTaken;
	}

	public StringToStringsMap getResponseHeaders()
	{
		return responseHeaders;
	}

	public void setMockResponse( MockResponseType mockResponse )
	{
		this.mockResponse = mockResponse;
		mockRequest.getRequestContext().setMockResponse( mockResponse );
	}

	/**
	 * @deprecated
	 */

	public void setReponseContent( String responseContent )
	{
		this.responseContent = responseContent;
	}

	public void setResponseContent( String responseContent )
	{
		this.responseContent = responseContent;
	}

	@SuppressWarnings("unchecked")
	public void finish()
	{
		if( mockRequest.getHttpResponse() instanceof org.mortbay.jetty.Response )
		{
			HttpFields httpFields = ( ( org.mortbay.jetty.Response )mockRequest.getHttpResponse() ).getHttpFields();

			Enumeration<String> e = httpFields.getFieldNames();
			while( e.hasMoreElements() )
			{
				String nextElement = e.nextElement();
				responseHeaders.add( nextElement, httpFields.getStringField( nextElement ) );
			}
		}
	}

	public void addHeader( String name, String value )
	{
		if( mockRequest.getHttpResponse() != null )
			mockRequest.getHttpResponse().addHeader( name, value );

		responseHeaders.add( name, value );
	}

	public boolean isCommitted()
	{
		return mockRequest.getHttpResponse().isCommitted();
	}

	public void setContentType( String contentType )
	{
		mockRequest.getHttpResponse().setContentType( contentType );
		responseContentType = contentType;
	}

	public OutputStream getOutputStream() throws IOException
	{
		return mockRequest.getHttpResponse().getOutputStream();
	}

	public void initResponse()
	{
		mockRequest.getHttpResponse().setStatus( HttpServletResponse.SC_OK );
		responseStatus = HttpServletResponse.SC_OK;
	}

	public boolean isDiscarded()
	{
		return false;
	}

	public byte[] getRawResponseData()
	{
		return rawResponseData;
	}

	public void setRawResponseData( byte[] rawResponseData )
	{
		this.rawResponseData = rawResponseData;
	}

	public void writeRawResponseData( byte[] bs ) throws IOException
	{
		getOutputStream().write( bs );
		setRawResponseData( bs );
	}

	public void setMockOperation( MockOperationType mockOperation )
	{
		this.mockOperation = mockOperation;
	}

	public MockOperation getMockOperation()
	{
		if( mockOperation != null )
			return mockOperation;

		return mockResponse == null ? null : mockResponse.getMockOperation();
	}

	public String getResponseContentType()
	{
		return responseContentType;
	}

	public int getResponseStatus()
	{
		return responseStatus;
	}

	public void setResponseStatus( int responseStatus )
	{
		this.responseStatus = responseStatus;
	}

	public void setResponseContentType( String responseContentType )
	{
		this.responseContentType = responseContentType;
	}

	public String toString()
	{
		StringBuilder msg = new StringBuilder( DateUtil.formatExtraFull( new Date( getTimestamp() ) ) );

		MockResponse mockResponse = getMockResponse();

		if( mockResponse == null )
		{
			msg.append( ": [dispatch error; missing response]" );
		}
		else
		{
			try
			{
				msg.append( ": [" + mockResponse.getMockOperation().getName() );
			}
			catch( Throwable e )
			{
				msg.append( ": [removed operation?]" );
			}

			msg.append( "] " + getTimeTaken() + "ms" );
		}
		return msg.toString();
	}
}
