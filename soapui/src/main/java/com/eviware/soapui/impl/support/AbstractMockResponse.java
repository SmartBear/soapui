package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.BaseMockResponseConfig;
import com.eviware.soapui.config.HeaderConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.MutableWsdlAttachmentContainer;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.mock.*;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.util.List;

public abstract class AbstractMockResponse<MockResponseConfigType extends BaseMockResponseConfig>
		extends AbstractWsdlModelItem<MockResponseConfigType>
		implements MockResponse, MutableWsdlAttachmentContainer, PropertyExpansionContainer, TestPropertyHolder
{
	public static final String AUTO_RESPONSE_COMPRESSION = "<auto>";
	public static final String NO_RESPONSE_COMPRESSION = "<none>";

	private String responseContent;
	private MockResult mockResult;
	private ScriptEnginePool scriptEnginePool;


	public AbstractMockResponse( MockResponseConfigType config, MockOperation operation, String icon )
	{
		super( config, operation, "/mockResponse.gif" )
		;
		scriptEnginePool = new ScriptEnginePool( this );
		scriptEnginePool.setScript( getScript() );
	}

	@Override
	public void setConfig( MockResponseConfigType config )
	{
		super.setConfig( config );

		if( scriptEnginePool != null )
			scriptEnginePool.setScript( getScript() );
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
		if( responseContent != null && responseContent.equals( oldContent ) )
			return;

		this.responseContent = responseContent;
		notifyPropertyChanged( RESPONSE_CONTENT_PROPERTY, oldContent, responseContent );
	}

	public StringToStringsMap getResponseHeaders()
	{
		StringToStringsMap result = new StringToStringsMap();
		List<HeaderConfig> headerList = getConfig().getHeaderList();
		for( HeaderConfig header : headerList )
		{
			result.add( header.getName(), header.getValue() );
		}

		return result;
	}

	public void setResponseHttpStatus( String httpStatus )
	{
		String oldStatus = getResponseHttpStatus();

		getConfig().setHttpResponseStatus( httpStatus );
	}

	public String getResponseHttpStatus()
	{
		return getConfig().getHttpResponseStatus();
	}

	public String getResponseCompression()
	{
		if( getConfig().isSetCompression() )
			return getConfig().getCompression();
		else
			return AUTO_RESPONSE_COMPRESSION;
	}

	public void setMockResult( MockResult mockResult )
	{
		MockResult oldResult = this.mockResult;
		this.mockResult = mockResult;
		notifyPropertyChanged( mockresultProperty(), oldResult, mockResult );
	}

	public MockResult getMockResult()
	{
		return mockResult;
	}

	protected abstract String mockresultProperty();

	public String getScript()
	{
		return getConfig().isSetScript() ? getConfig().getScript().getStringValue() : null;
	}

	public void evaluateScript( MockRequest request ) throws Exception
	{
		String script = getScript();
		if( script == null || script.trim().length() == 0 )
			return;

		MockService mockService = getMockOperation().getMockService();
		MockRunner mockRunner = mockService.getMockRunner();
		MockRunContext context =
				mockRunner == null ? new WsdlMockRunContext( mockService, null ) : mockRunner.getMockContext();

		context.setMockResponse( this );

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

	public void setScript( String script )
	{
		String oldScript = getScript();
		if( !script.equals( oldScript ) )
		{
			if( !getConfig().isSetScript() )
				getConfig().addNewScript();
			getConfig().getScript().setStringValue( script );

			scriptEnginePool.setScript( script );
		}
	}

	@Override
	public void release()
	{
		super.release();
		scriptEnginePool.release();
	}
}
