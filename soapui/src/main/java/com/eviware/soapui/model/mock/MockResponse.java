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

import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.support.types.StringToStringsMap;

/**
 * A MockResponse returned by a MockOperation
 * 
 * @author ole.matzura
 */

public interface MockResponse extends TestModelItem
{
	public final static String RESPONSE_CONTENT_PROPERTY = MockResponse.class.getName() + "@responsecontent";
	public final static String ENCODING_PROPERTY = MockResponse.class.getName() + "@encoding";
	public final static String MTOM_NABLED_PROPERTY = MockResponse.class.getName() + "@mtom_enabled";

	public String getResponseContent();

	public void setResponseContent( String responseContent );

	public String getEncoding();

	public boolean isMtomEnabled();

	public Attachment[] getAttachments();

	public int getAttachmentCount();

	public MockOperation getMockOperation();

	public StringToStringsMap getResponseHeaders();

	public MockResult getMockResult();

	public void release();

	public void evaluateScript( MockRequest request ) throws Exception;

	public String getScript();

	public void setScript(String script);
}
