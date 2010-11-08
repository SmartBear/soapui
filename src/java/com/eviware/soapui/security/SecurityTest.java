/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.config.SecurityCheckEntryConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
/**
 * SecurityTest
 * 
 * @author soapUI team
 */
public class SecurityTest implements ModelItem, Runnable
{
	public final static String SECURITY_CHECK_MAP = "securityChecksMap"; 
	private WsdlTestCase testCase;
	private HashMap<String, List<SecurityCheck>> securityChecksMap;
	private String description;
	private String label;
	private SecurityTestConfig securityTestConfig;
	public SecurityTest(WsdlTestCase testCase, SecurityTestConfig config) {
		this.testCase = testCase;
		securityChecksMap = createSecurityCheckMap(config);
		
		//create securityChecksMap from config
	}

	private HashMap<String, List<SecurityCheck>> createSecurityCheckMap( SecurityTestConfig config )
	{
		HashMap<String, List<SecurityCheck>> scm = new HashMap<String, List<SecurityCheck>>();
		for(SecurityCheckEntryConfig sceConfig: config.getSecurityCheckMapList()){
//			scm.put( sceConfig.getTestStepName(),sceConfig.get
		}
		return scm;
	}

	private SecurityTestLog securityTestLog;

	@Override
	public List<? extends ModelItem> getChildren()
	{
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public ImageIcon getIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModelItem getParent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Settings getSettings()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Adds new securityCheck for the specific TestStep
	 * @param testStepName
	 * @param securityCheck
	 * 
	 * @return HashMap<TestStep, List<SecurityCheck>> 
	 */
	public HashMap<String, List<SecurityCheck>> addSecurityChecks( String testStepName, SecurityCheck securityCheck )
	{
		List<SecurityCheck> checks = null;
		if( securityChecksMap.containsKey( testStepName ) )
		{
			checks = securityChecksMap.get( testStepName );
		}
		else
		{
			checks = new ArrayList<SecurityCheck>();
		}
		checks.add( securityCheck );
		securityChecksMap.put( testStepName, checks );
		return securityChecksMap ;
	}

	public HashMap<String, List<SecurityCheck>> getSecurityChecksMap()
	{
		return securityChecksMap;
	}

	public WsdlTestCase getTestCase()
	{
		return testCase;
	}

}
