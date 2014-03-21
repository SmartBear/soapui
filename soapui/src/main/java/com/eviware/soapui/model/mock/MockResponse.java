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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.model.Releasable;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringsMap;

/**
 * A MockResponse returned by a MockOperation. An instance of this interface represents an actual response sent
 * from a MockService. It is (together with MockRequest) a part of a MockResult.
 *
 * @author ole.matzura
 */

public interface MockResponse extends TestModelItem, Releasable
{
	public final static String RESPONSE_CONTENT_PROPERTY = MockResponse.class.getName() + "@responsecontent";
	public final static String MTOM_NABLED_PROPERTY = MockResponse.class.getName() + "@mtom_enabled";

	public String getResponseContent();

	public void setResponseContent( String responseContent );

	public String getContentType();

	public String getEncoding();

	public boolean isMtomEnabled();

	public Attachment[] getAttachments();

	public int getAttachmentCount();

	public MockOperation getMockOperation();

	/**
	 * Gets HTTP Headers for this response.
	 *
	 * This is the persisted set of headers for a mock response. More headers may be added when doing a real
	 * request to a mock service.
	 *
	 * @return StringToStringsMap with all the headers.
	 */
	public StringToStringsMap getResponseHeaders();

	/**
	 * Sets ALL the response headers for this mock response. The headers should be persisted along with
	 * this response when it is saved in a project.
	 *
	 * @param headers a StringToStringsMap containing all the headers. A current version of persisted headers can be
	 * fetched with getResponseHeaders.
	 */
	public void setResponseHeaders( StringToStringsMap headers );

	public MockResult getMockResult();

	public void evaluateScript( MockRequest request ) throws Exception;

	public String getScript();

	public void setScript( String script );

	/**
	 * Sets the HTTP status for this response. This should be a valid status code as documented in
	 * RFC1945 and RFC2616
	 *
	 * @param httpStatus a valid status code.
	 */
	public void setResponseHttpStatus( int httpStatus );

	/**
	 * Gets the HTTP status for this response.
	 *
	 * @return a valid status code.
	 */
	public int getResponseHttpStatus();

	public MockResult execute( MockRequest request, MockResult result ) throws DispatchException;

	String getScriptHelpUrl();
}
