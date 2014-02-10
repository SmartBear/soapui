package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.support.AbstractMockDispatcher;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.commons.collections.list.TreeList;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsdlMockDispatcher extends AbstractMockDispatcher
{

	private WsdlMockService mockService;
	private WsdlMockRunContext mockContext;
	private final List<WsdlMockResult> mockResults = Collections.synchronizedList( new TreeList() );
	private long maxResults = 100;
	private int removed = 0;
	private boolean logEnabled = true;

	private final Map<String, StringToStringMap> wsdlCache = new HashMap<String, StringToStringMap>();
	private final static Logger log = Logger.getLogger( WsdlMockDispatcher.class );


	public WsdlMockDispatcher( WsdlMockService mockService, WsdlMockRunContext mockContext )
	{
		this.mockService = mockService;
		this.mockContext = mockContext;
		initWsdlCache();
	}

	private void initWsdlCache()
	{
		for( Interface iface : mockService.getMockedInterfaces() )
		{
			if( !iface.getInterfaceType().equals( WsdlInterfaceFactory.WSDL_TYPE ) )
				continue;

			try
			{
				WsdlDefinitionExporter exporter = new WsdlDefinitionExporter( ( WsdlInterface )iface );

				String wsdlPrefix = getInterfacePrefix( iface ).substring( 1 );
				StringToStringMap parts = exporter.createFilesForExport( wsdlPrefix + "&part=" );

				for( Map.Entry<String, String> partEntry : parts.entrySet() )
				{
					if( partEntry.getKey().toLowerCase().endsWith( ".wsdl" ) )
					{
						InputSource inputSource = new InputSource( new StringReader( partEntry.getValue() ) );
						String content = WsdlUtils.replacePortEndpoint( ( WsdlInterface )iface, inputSource,
								mockService.getLocalMockServiceEndpoint() );

						if( content != null )
							parts.put( partEntry.getKey(), content );
					}
				}

				wsdlCache.put( iface.getName(), parts );

				log.info( "Mounted WSDL for interface [" + iface.getName() + "] at [" + getOverviewUrl() + "]" );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}

	@Override
	public MockResult dispatchRequest( HttpServletRequest request, HttpServletResponse response )
			throws DispatchException
	{
		Object result = null;

		try
		{
			WsdlMockRequest mockRequest = new WsdlMockRequest( request, response, mockContext );
			result = mockService.runOnRequestScript( mockContext, mockRequest );
			if( !( result instanceof MockResult ) )
			{
				String method = mockRequest.getMethod();
				if( method.equals( "POST" ) )
					result = dispatchPostRequest( mockRequest );
				else
					result = super.dispatchRequest( request, response );
			}

			mockService.runAfterRequestScript( mockContext, ( MockResult )result );
			return ( MockResult )result;
		}
		catch( Throwable e )
		{
			if( e instanceof DispatchException )
				throw ( DispatchException )e;
			else
				throw new DispatchException( e );
		} finally
		{
			if( result instanceof MockResult )
			{
				for( MockRunListener listener : mockService.getMockRunListeners() )
				{
					listener.onMockResult( ( MockResult )result );
				}
			}
		}
	}

	public MockResult dispatchGetRequest( HttpServletRequest request, HttpServletResponse response )
			throws DispatchException
	{
		try
		{
			String qs = request.getQueryString();
			if( qs != null && qs.toUpperCase().startsWith( "WSDL" ) )
			{
				dispatchWsdlRequest( request, response );
			}
			else
			{
				String docroot = PropertyExpander.expandProperties( mockContext, mockService.getDocroot() );
				if( StringUtils.hasContent( docroot ) )
				{
					try
					{
						String pathInfo = request.getPathInfo();
						if( pathInfo == null )
							pathInfo = "";

						if( mockService.getPath().length() > 1 && pathInfo.startsWith( mockService.getPath() ) )
							pathInfo = pathInfo.substring( mockService.getPath().length() );

						String filename = docroot + pathInfo.replace( '/', File.separatorChar );
						File file = new File( filename );
						if( file.exists() )
						{
							returnFile( response, file );
						}
					}
					catch( Throwable e )
					{
						throw new DispatchException( e );
					}
				}
			}

			return null;
		}
		catch( Exception e )
		{
			throw new DispatchException( e );
		}
	}

	public WsdlMockResult dispatchPostRequest( WsdlMockRequest mockRequest ) throws Exception
	{
		WsdlMockResult result = null;

		try
		{
			long timestamp = System.currentTimeMillis();

			SoapVersion soapVersion = mockRequest.getSoapVersion();
			if( soapVersion == null )
				throw new DispatchException( "Unrecognized SOAP Version" );

			String soapAction = mockRequest.getSoapAction();
			WsdlOperation operation = null;

			if( SoapUtils.isSoapFault( mockRequest.getRequestContent(), soapVersion ) )
			{
				// we should inspect fault detail and try to find matching operation
				// but not for now..
				WsdlMockOperation faultMockOperation = mockService.getFaultMockOperation();
				if( faultMockOperation != null )
					operation = faultMockOperation.getOperation();
			}
			else
			{
				try
				{
					operation = SoapUtils.findOperationForRequest( soapVersion, soapAction,
							mockRequest.getRequestXmlObject(), mockService.getMockedOperations(),
							mockService.isRequireSoapVersion(), mockService.isRequireSoapAction(),
							mockRequest.getRequestAttachments() );
				}
				catch( Exception e )
				{
					if( mockService.isDispatchResponseMessages() )
					{
						try
						{
							operation = SoapUtils.findOperationForResponse( soapVersion, soapAction,
									mockRequest.getRequestXmlObject(), mockService.getMockedOperations(),
									mockService.isRequireSoapVersion(), mockService.isRequireSoapAction() );

							if( operation != null )
							{
								mockRequest.setResponseMessage( true );
							}
						}
						catch( Exception e2 )
						{
							throw e;
						}
					}
					else
					{
						throw e;
					}
				}
			}

			if( operation != null )
			{
				WsdlMockOperation mockOperation = mockService.getMockOperation( operation );
				if( mockOperation != null )
				{
					long startTime = System.nanoTime();
					// try
					// {
					result = mockOperation.dispatchRequest( mockRequest );
					// }
					// catch( DispatchException e )
					// {
					// result = new WsdlMockResult( mockRequest );
					//
					// String fault = SoapMessageBuilder.buildFault( "Server",
					// e.getMessage(), mockRequest.getSoapVersion() );
					// result.setResponseContent( fault );
					// result.setMockOperation( mockOperation );
					//
					// mockRequest.getHttpResponse().getWriter().write( fault );
					// }

					if( mockRequest.getHttpRequest() instanceof org.mortbay.jetty.Request )
						( ( org.mortbay.jetty.Request )mockRequest.getHttpRequest() ).setHandled( true );

					result.setTimeTaken( ( System.nanoTime() - startTime ) / 1000000 );
					result.setTimestamp( timestamp );
					addMockResult( result );
					return result;
				}
				else
				{
					throw new DispatchException( "Failed to find matching operation for request" );
				}
			}

			throw new DispatchException( "Missing operation for soapAction [" + soapAction + "] and body element ["
					+ XmlUtils.getQName( mockRequest.getContentElement() ) + "] with SOAP Version ["
					+ mockRequest.getSoapVersion() + "]" );
		}
		catch( Exception e )
		{
			if( e instanceof DispatchException )
				throw ( DispatchException )e;

			throw new DispatchException( e );
		}
	}

	public MockResult dispatchHeadRequest( HttpServletRequest request, HttpServletResponse response )
			throws DispatchException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		return null;
	}


	protected void dispatchWsdlRequest( HttpServletRequest request, HttpServletResponse response ) throws IOException
	{
		if( request.getQueryString().equalsIgnoreCase( "WSDL" ) )
		{
			printWsdl( response );
			return;
		}

		String ifaceName = request.getParameter( "interface" );
		WsdlInterface iface = ( WsdlInterface )mockService.getProject().getInterfaceByName( ifaceName );
		if( iface == null )
		{
			printInterfaceList( response );
			return;
		}

		StringToStringMap parts = wsdlCache.get( iface.getName() );
		String part = request.getParameter( "part" );
		String content = StringUtils.isNullOrEmpty( part ) ? null : parts.get( part );

		if( content == null )
		{
			printPartList( iface, parts, response );
			return;
		}

		if( content != null )
		{
			printOkXmlResult( response, content );
		}
	}

	public synchronized void addMockResult( WsdlMockResult mockResult )
	{
		if( maxResults > 0 && logEnabled )
			mockResults.add( mockResult );

		while( mockResults.size() > maxResults )
		{
			mockResults.remove( 0 );
			removed++;
		}
	}

	public MockResult getMockResultAt( int index )
	{
		return index <= removed ? null : mockResults.get( index - removed );
	}

	public int getMockResultCount()
	{
		return mockResults.size() + removed;
	}

	public synchronized void clearResults()
	{
		mockResults.clear();
	}

	public void release()
	{
		clearResults();
		mockContext.clear();
	}


	public long getMaxResults()
	{
		return maxResults;
	}

	public synchronized void setMaxResults( long maxNumberOfResults )
	{
		this.maxResults = maxNumberOfResults;

		while( mockResults.size() > maxNumberOfResults )
		{
			mockResults.remove( 0 );
			removed++;
		}
	}

	public void setLogEnabled( boolean logEnabled )
	{
		this.logEnabled = logEnabled;
	}

	public void printWsdl( HttpServletResponse response ) throws IOException
	{
		WsdlInterface[] mockedInterfaces = mockService.getMockedInterfaces();
		if( mockedInterfaces.length == 1 )
		{
			StringToStringMap parts = wsdlCache.get( mockedInterfaces[0].getName() );
			printOkXmlResult( response, parts.get( parts.get( "#root#" ) ) );
		}
		else
		{
			try
			{
				WSDLFactory wsdlFactory = WSDLFactory.newInstance();
				Definition def = wsdlFactory.newDefinition();
				for( WsdlInterface iface : mockedInterfaces )
				{
					StringToStringMap parts = wsdlCache.get( iface.getName() );
					Import wsdlImport = def.createImport();
					wsdlImport.setLocationURI( getInterfacePrefix( iface ) + "&part=" + parts.get( "#root#" ) );
					wsdlImport.setNamespaceURI( WsdlUtils.getTargetNamespace( iface.getWsdlContext().getDefinition() ) );

					def.addImport( wsdlImport );
				}

				response.setStatus( HttpServletResponse.SC_OK );
				response.setContentType( "text/xml" );
				response.setCharacterEncoding( "UTF-8" );

				WSDLWriter writer = wsdlFactory.newWSDLWriter();
				writer.writeWSDL( def, response.getWriter() );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
				throw new IOException( "Failed to create combined WSDL" );
			}
		}
	}


	public void printOkXmlResult( HttpServletResponse response, String content ) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/xml" );
		response.setCharacterEncoding( "UTF-8" );
		response.getWriter().print( content );
	}

	public void printPartList( WsdlInterface iface, StringToStringMap parts, HttpServletResponse response )
			throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		out.print( "<html><body><p>Parts in interface [" + iface.getName() + "]</p><ul>" );

		for( String key : parts.keySet() )
		{
			if( key.equals( "#root#" ) )
				continue;

			out.print( "<li><a href=\"" );
			out.print( getInterfacePrefix( iface ) + "&part=" + key );
			out.print( "\">" + key + "</a></li>" );
		}

		out.print( "</ul></p></body></html>" );
	}

	public void printInterfaceList( HttpServletResponse response ) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		out.print( "<html><body><p>Mocked Interfaces in project [" + mockService.getProject().getName() + "]</p><ul>" );

		for( Interface iface : ModelSupport.getChildren( mockService.getProject(), WsdlInterface.class ) )
		{
			out.print( "<li><a href=\"" );
			out.print( getInterfacePrefix( iface ) );
			out.print( "\">" + iface.getName() + "</a></li>" );
		}

		out.print( "</ul></p></body></html>" );
	}

	public void returnFile( HttpServletResponse response, File file ) throws FileNotFoundException, IOException
	{
		FileInputStream in = new FileInputStream( file );
		response.setStatus( HttpServletResponse.SC_OK );
		long length = file.length();
		response.setContentLength( ( int )length );
		response.setContentType( ContentTypeHandler.getContentTypeFromFilename( file.getName() ) );
		Tools.readAndWrite( in, length, response.getOutputStream() );
		in.close();
	}

	public String getInterfacePrefix( Interface iface )
	{
		String wsdlPrefix = getOverviewUrl() + "&interface=" + iface.getName();
		return wsdlPrefix;
	}

	public String getOverviewUrl()
	{
		return mockService.getPath() + "?WSDL";
	}

}
