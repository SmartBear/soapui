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

package com.eviware.soapui.model.iface;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.ResultContainer;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * An exchange of a request and response message
 * 
 * @author ole.matzura
 */

public interface MessageExchange extends ResultContainer
{
	public Operation getOperation();

	public ModelItem getModelItem();

	public long getTimestamp();

	public long getTimeTaken();

	public StringToStringMap getProperties();

	public String getRequestContent();

	public String getResponseContent();

	public String getRequestContentAsXml();

	public String getResponseContentAsXml();

	public StringToStringMap getRequestHeaders();

	public StringToStringMap getResponseHeaders();

	public Attachment[] getRequestAttachments();

	public Attachment[] getResponseAttachments();

	public String[] getMessages();

	public boolean isDiscarded();

	public boolean hasRawData();

	public byte[] getRawRequestData();

	public byte[] getRawResponseData();

	public Attachment[] getRequestAttachmentsForPart( String partName );

	public Attachment[] getResponseAttachmentsForPart( String partName );

	public boolean hasRequest( boolean ignoreEmpty );

	public boolean hasResponse();

	public String getProperty( String name );
}
