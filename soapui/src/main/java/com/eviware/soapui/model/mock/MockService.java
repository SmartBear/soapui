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

import java.util.List;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.Releasable;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.project.Project;

/**
 * ModelItem for mocking a number of Interfaces and their Operations
 * 
 * @author ole.matzura
 */

// TODO: some things in AbstractMockRunner that is inherited from far above should probably makes its way in here
public interface MockService extends TestModelItem, Releasable
{
	public final static String PATH_PROPERTY = WsdlMockService.class.getName() + "@path";
	public final static String PORT_PROPERTY = MockService.class.getName() + "@port";

	public Project getProject();

	public int getMockOperationCount();

	public MockOperation getMockOperationAt( int index );

	public MockOperation getMockOperationByName( String name );

	public String getPath();

	public int getPort();

	public MockRunner getMockRunner();

	public MockRunner start() throws Exception;

	public void addMockRunListener( MockRunListener listener );

	public void removeMockRunListener( MockRunListener listener );

	public void addMockServiceListener( MockServiceListener listener );

	public void removeMockServiceListener( MockServiceListener listener );

	public void fireMockOperationAdded( MockOperation mockOperation );

	public void fireMockOperationRemoved( MockOperation mockOperation );

	public void fireMockResponseAdded( MockResponse mockResponse );

	public void fireMockResponseRemoved( MockResponse mockResponse );

	public List<MockOperation> getMockOperationList();

	public boolean getBindToHostOnly();
}
