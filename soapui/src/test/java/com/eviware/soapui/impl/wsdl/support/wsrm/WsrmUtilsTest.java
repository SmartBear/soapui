package com.eviware.soapui.impl.wsdl.support.wsrm;

import com.eviware.soapui.impl.support.wsa.WsaRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static com.eviware.soapui.utils.CommonMatchers.compliantWithSchema;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for WS-RM functionality in WsrmUtils
 */
public class WsrmUtilsTest
{

	public static final String WSRM_1_0_SCHEMA_200502_LOCATION = "/xsds/wsrm-1.0-schema-200502.xsd";
	public static final String WSRM_1_1_SCHEMA_200702_LOCATION = "/xsds/wsrm-1.1-schema-200702.xsd";

	@Before
	public void setUp() throws Exception
	{

	}

	@Test
	public void buildsValidStartSequenceRequest() throws Exception
	{
		WsrmUtils requestBuilder = new WsrmUtils( SoapVersion.Soap12 );
		WsaRequest wsaRequest = requestBuilder.buildStartSequenceRequest( "http://example.com", SoapVersion.Soap12, WsrmUtils.WSRM_NS_1_1, "http://example.com",
				0l, ModelItemFactory.makeWsdlOperation(), UUID.randomUUID().toString(), null);
		Document soapEnvelope = getContentAsDocument( wsaRequest );
		NodeList nodeList = soapEnvelope.getElementsByTagNameNS( WsrmUtils.WSRM_NS_1_1, "CreateSequence" );

		assertThat( nodeList.item( 0 ), is( compliantWithSchema( WSRM_1_1_SCHEMA_200702_LOCATION ) ) );
	}

	@Test
	public void buildsValidStartSequenceRequestWithVersion10() throws Exception
	{
		WsrmUtils requestBuilder = new WsrmUtils( SoapVersion.Soap12 );
		WsaRequest wsaRequest = requestBuilder.buildStartSequenceRequest( "http://example.com", SoapVersion.Soap12, WsrmUtils.WSRM_NS_1_0, "http://example.com",
				0l, ModelItemFactory.makeWsdlOperation(), UUID.randomUUID().toString(), null);
		Document soapEnvelope = getContentAsDocument( wsaRequest );
		NodeList nodeList = soapEnvelope.getElementsByTagNameNS( WsrmUtils.WSRM_NS_1_0, "CreateSequence" );

		assertThat( nodeList.item( 0 ), is( compliantWithSchema( WSRM_1_0_SCHEMA_200502_LOCATION ) ) );
	}

	@Test
	public void buildsValidStartSequenceRequestWhenOfferEndpointIsSet() throws Exception
	{
		WsrmUtils requestBuilder = new WsrmUtils( SoapVersion.Soap12 );
		WsaRequest wsaRequest = requestBuilder.buildStartSequenceRequest( "http://example.com", SoapVersion.Soap12, WsrmUtils.WSRM_NS_1_1, "http://example.com",
				0l, ModelItemFactory.makeWsdlOperation(), UUID.randomUUID().toString(), "http://some.endpoint.com");
		Document soapEnvelope = getContentAsDocument( wsaRequest );
		NodeList nodeList = soapEnvelope.getElementsByTagNameNS( WsrmUtils.WSRM_NS_1_1, "CreateSequence" );

		assertThat( nodeList.item( 0 ), is(compliantWithSchema(WSRM_1_1_SCHEMA_200702_LOCATION )));
	}

	@Test
	public void buildsValidStartSequenceRequestWhenOfferEndpointIsSetWithVersion10() throws Exception
	{
		WsrmUtils requestBuilder = new WsrmUtils( SoapVersion.Soap12 );
		WsaRequest wsaRequest = requestBuilder.buildStartSequenceRequest( "http://example.com", SoapVersion.Soap12, WsrmUtils.WSRM_NS_1_0, "http://example.com",
				0l, ModelItemFactory.makeWsdlOperation(), UUID.randomUUID().toString(), "http://some.endpoint.com");
		Document soapEnvelope = getContentAsDocument( wsaRequest );
		NodeList nodeList = soapEnvelope.getElementsByTagNameNS( WsrmUtils.WSRM_NS_1_0, "CreateSequence" );

		assertThat( nodeList.item( 0 ), is(compliantWithSchema(WSRM_1_0_SCHEMA_200502_LOCATION )));
	}

	private Document getContentAsDocument( WsaRequest wsaRequest ) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating( false );
		documentBuilderFactory.setNamespaceAware( true );
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		return documentBuilder.parse(
				new ByteArrayInputStream( wsaRequest.getRequestContent().getBytes() ) );
	}


}
