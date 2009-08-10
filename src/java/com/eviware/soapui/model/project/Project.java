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

package com.eviware.soapui.model.project;

import java.io.IOException;
import java.util.List;

import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.workspace.Workspace;

/**
 * A SoapUI project
 * 
 * @author Ole.Matzura
 */

public interface Project extends TestModelItem
{
	/** The id of the JBossWS project nature */
	public static final String JBOSSWS_NATURE_ID = "com.eviware.soapui.jbosside.jbosswsNature";

	/** The id of the SoapUI project nature */
	public static final String SOAPUI_NATURE_ID = "com.eviware.soapui.soapuiNature";

	public Workspace getWorkspace();

	public Interface getInterfaceAt( int index );

	public Interface getInterfaceByName( String interfaceName );

	public int getInterfaceCount();

	public void addProjectListener( ProjectListener listener );

	public void removeProjectListener( ProjectListener listener );

	public int getTestSuiteCount();

	public TestSuite getTestSuiteAt( int index );

	public TestSuite getTestSuiteByName( String testSuiteName );

	public TestSuite addNewTestSuite( String name );

	public int getMockServiceCount();

	public MockService getMockServiceAt( int index );

	public MockService getMockServiceByName( String mockServiceName );

	public MockService addNewMockService( String name );

	public boolean save() throws IOException;

	public List<TestSuite> getTestSuiteList();

	public List<MockService> getMockServiceList();

	public List<Interface> getInterfaceList();

	public boolean hasNature( String natureId );

	public EndpointStrategy getEndpointStrategy();

	public void release();

	public boolean isOpen();

	public boolean isDisabled();

	public String getPath();

	public String getResourceRoot();

	public String getShadowPassword();

	public void setShadowPassword( String password );

	public void inspect();

	public int getIndexOfTestSuite( TestSuite testSuite );
}
