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

package com.eviware.soapui.model.iface;

import com.eviware.soapui.model.ModelItem;

import java.util.List;

/**
 * Operation interface
 * 
 * @author Ole.Matzura
 */

public interface Operation extends ModelItem
{
	public final static String ACTION_PROPERTY = Operation.class.getName() + "@action";

	public boolean isUnidirectional();

	public boolean isBidirectional();

	public Request getRequestAt( int index );

	public Request getRequestByName( String requestName );

	public List<Request> getRequestList();

	public int getRequestCount();

	public Interface getInterface();

	public MessagePart[] getDefaultRequestParts();

	public MessagePart[] getDefaultResponseParts();

	public String createRequest( boolean b );

	public String createResponse( boolean b );
}
