package com.eviware.soapui.impl.wsdl.monitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.MultipartMessageSupport;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

public class JProxyServletWsdlMonitorMessageExchange extends WsdlMonitorMessageExchange {
	
	private WsdlOperation operation;
	private WsdlProject project;
	private String requestContent;
	private StringToStringMap requestHeaders;
	private String responseContent;
	private StringToStringMap responseHeaders;
	private MultipartMessageSupport requestMmSupport;
	private boolean discarded;
	private long timestampStart;
	private byte[] request;
	private byte[] response;
	private String requestHost;
	private URL targetURL;
	private String requestContentType;
	private Vector<Object> requestWssResult;
	private SoapVersion soapVersion;
	private String responseContentType;
	private MultipartMessageSupport responseMmSupport;
	private Vector<Object> responseWssResult;
	private long timestampEnd;
	private boolean capture;

	public JProxyServletWsdlMonitorMessageExchange() {
//		this.project = project;
		responseHeaders = new StringToStringMap();
		requestHeaders = new StringToStringMap();
		timestampStart = System.currentTimeMillis();
		System.err.println("Created " + timestampStart + " " + this.toString() );
		capture = true;
	}
	
	@Override
	public void discard() {
		
		operation = null;
		project = null;
		
		requestContent = null;
		requestHeaders = null;
		
		responseContent = null;
		responseHeaders = null;
		
		requestMmSupport = null;
		
		response = null;
		request = null;
		capture = false;
		
		discarded = true;

	}

	@Override
	public long getRequestContentLength() {
		return this.request.length;
	}

	@Override
	public String getRequestHost() {
		return requestHost;
	}

	@Override
	public long getResponseContentLength() {
		return response.length;
	}

	@Override
	public URL getTargetUrl() {
		return this.targetURL;
	}

	@Override
	public void prepare(IncomingWss incomingRequestWss,	IncomingWss incomingResponseWss) {

		parseRequestData( incomingRequestWss );
		parseReponseData(incomingResponseWss );
		
	}

	private void parseReponseData(IncomingWss incomingResponseWss) {
		
		ByteArrayInputStream in = new ByteArrayInputStream( response );
		try
		{

			responseContentType = responseHeaders.get( "Content-Type" );
			if( responseContentType != null &&	responseContentType.toUpperCase().startsWith( "MULTIPART" ))
			{
				StringToStringMap values = StringToStringMap.fromHttpHeader( responseContentType );
				responseMmSupport = new MultipartMessageSupport( 
							new MonitorMessageExchangeDataSource( "monitor response", in, responseContentType ), values.get( "start" ), null, true,
							SoapUI.getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES ));
				responseContentType = responseMmSupport.getRootPart().getContentType();
			}
			else
			{
				this.responseContent = XmlUtils.prettyPrintXml( Tools.readAll( in, 0 ).toString());
			}
			
			processResponseWss( incomingResponseWss );
		}
		catch( Exception e )
		{
			try
			{
				in.close();
			}
			catch( IOException e1 )
			{
				e1.printStackTrace();
			}
		}
		
	}

	private void processResponseWss(IncomingWss incomingResponseWss) throws IOException {

		if( incomingResponseWss != null )
		{
			Document dom = XmlUtils.parseXml( responseContent );
			try
			{
				responseWssResult = incomingResponseWss.processIncoming( dom, new DefaultPropertyExpansionContext(project ) );
				if( responseWssResult != null && responseWssResult.size() > 0 )
				{
					StringWriter writer = new StringWriter();
					XmlUtils.serialize( dom, writer );
					responseContent = writer.toString();
				}
			}
			catch( Exception e )
			{
				if( responseWssResult == null )
					responseWssResult = new Vector<Object>();
				responseWssResult.add( e );
			}
		}
		
	}

	private void parseRequestData(IncomingWss incomingRequestWss) {
		
		ByteArrayInputStream in = new ByteArrayInputStream( request );
		try
		{

			requestContentType = requestHeaders.get( "Content-Type" );
			if( requestContentType != null &&	requestContentType.toUpperCase().startsWith( "MULTIPART" ))
			{
				StringToStringMap values = StringToStringMap.fromHttpHeader( requestContentType );
				requestMmSupport = new MultipartMessageSupport( 
							new MonitorMessageExchangeDataSource( "monitor request", in, requestContentType ), values.get( "start" ), null, true,
							SoapUI.getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES ));
				requestContentType = requestMmSupport.getRootPart().getContentType();
			}
			else
			{
				this.requestContent = XmlUtils.prettyPrintXml( Tools.readAll( in, 0 ).toString() );
			}
			
			processRequestWss( incomingRequestWss );
			
			operation = findOperation();
		}
		catch( Exception e )
		{
			try
			{
				in.close();
			}
			catch( IOException e1 )
			{
				e1.printStackTrace();
			}
		}
		
	}

	private WsdlOperation findOperation() throws Exception {
		soapVersion = SoapUtils.initSoapVersion( requestContentType );
		if( soapVersion == null )
			throw new Exception( "Unrecognized SOAP Version" );

		String soapAction = SoapUtils.getSoapAction( soapVersion, requestHeaders );

		List<WsdlOperation> operations = new ArrayList<WsdlOperation>();
		for( Interface iface : project.getInterfaceList() )
		{
			for( Operation operation : iface.getOperationList())
				operations.add( ( WsdlOperation ) operation );
		}
		
		return SoapUtils.findOperationForRequest( soapVersion, soapAction, 
					XmlObject.Factory.parse( getRequestContent() ), operations, true, false );
	}

	private void processRequestWss(IncomingWss incomingRequestWss) throws IOException {
		
		if( incomingRequestWss != null )
		{
			Document dom = XmlUtils.parseXml( requestContent );
			try
			{
				requestWssResult = incomingRequestWss.processIncoming( dom, new DefaultPropertyExpansionContext(project ) );
				if( requestWssResult != null && requestWssResult.size() > 0 )
				{
					StringWriter writer = new StringWriter();
					XmlUtils.serialize( dom, writer );
					requestContent = writer.toString();
				}
			}
			catch( Exception e )
			{
				if( requestWssResult == null )
					requestWssResult = new Vector<Object>();
				requestWssResult.add( e );
			}
		}
		
	}

	@Override
	public WsdlOperation getOperation() {
		return operation;
	}

	@Override
	public Vector<?> getRequestWssResult() {
		return requestWssResult;
	}

	@Override
	public Vector<?> getResponseWssResult() {
		return responseWssResult;
	}

	@Override
	public ModelItem getModelItem() {
		return null;
	}

	@Override
	public Attachment[] getRequestAttachments() {
		return requestMmSupport == null ? new Attachment[0] : requestMmSupport.getAttachments();
	}

	@Override
	public String getRequestContent() {
		return requestMmSupport == null ? requestContent : requestMmSupport.getContentAsString();
	}

	@Override
	public StringToStringMap getRequestHeaders() {
		return requestHeaders;
	}

	@Override
	public Attachment[] getResponseAttachments() {
		return requestMmSupport == null ? new Attachment[0] : requestMmSupport.getAttachments();
	}

	@Override
	public String getResponseContent() {
		return responseContent;
	}

	@Override
	public StringToStringMap getResponseHeaders() {
		return responseHeaders;
	}

	@Override
	public long getTimeTaken() {
		return timestampEnd - timestampStart;
	}

	@Override
	public long getTimestamp() {
		return timestampStart;
	}

	@Override
	public boolean isDiscarded() {
		return discarded;
	}

	public void stopCapture() {
		
		timestampEnd = System.currentTimeMillis();
		capture = false;
		
	}

	public boolean isStopCapture() {
		return capture;
	}

	public void setRequest(byte[] capturedData) {
		this.request = capturedData;
//		this.requestContent = XmlUtils.prettyPrintXml( new String(request) ); 
	}

	public void setResponse(byte[] response) {
//		this.response = response;
		if (this.response == null) {
			this.response = response;
		} else {
			byte[] newResponse = new byte[this.response.length + response.length];
			for( int i = 0 ; i < this.response.length ; i++ ) {
				newResponse[i] = this.response[i];
			}
			for( int i = this.response.length; i < newResponse.length ; i++) {
				newResponse[i] = response[i - this.response.length];
			}
			this.response = newResponse;
		}
	}

	public void setResponseHeader(String name, String value) {
		responseHeaders.put(name, value);
	}

	public void setRequestHost(String serverName) {
		requestHost = serverName;
	}

	public void setTargetHost(String remoteHost) {
		// TODO Auto-generated method stub
		
	}

	public void setRequestHeader(HttpServletRequest httpRequest) {
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			Enumeration<String> header = httpRequest.getHeaders(name);
			while (header.hasMoreElements()) {
				String value = header.nextElement();
				if (value != null) {
					requestHeaders.put(name, value);
				}
			}
		}
	}

	public void setTargetURL(String url) {
		try {
			this.targetURL = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
