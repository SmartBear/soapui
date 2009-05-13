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

package com.eviware.soapui.model.mock;

import java.util.List;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;

/**
 * A MockOperation for mocking an Interfaces Operation and returning a
 * MockResponse
 * 
 * @author ole.matzura
 */

public interface MockOperation extends ModelItem
{
	public MockService getMockService();

	public int getMockResponseCount();

	public MockResponse getMockResponseAt( int index );

	public MockResponse getMockResponseByName( String name );

	public Operation getOperation();

	public MockResult getLastMockResult();

	public List<MockResponse> getMockResponses();
}
