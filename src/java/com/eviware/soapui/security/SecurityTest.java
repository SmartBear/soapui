package com.eviware.soapui.security;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestStep;

public class SecurityTest implements ModelItem, Runnable
{
	public final static String SECURITY_CHECK_MAP = "securityChecksMap"; 
	private WsdlTestCase testCase;
	private HashMap<TestStep, List<SecurityCheck>> securityChecksMap;
	private String description;
	private String label;
	private SecurityTestConfig securityTestConfig;
	public SecurityTest(WsdlTestCase testCase, SecurityTestConfig config) {
		this.testCase = testCase;
		//create securityChecksMap from config
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
	 * @param testStep
	 * @param securityCheck
	 * 
	 * @return HashMap<TestStep, List<SecurityCheck>> 
	 */
	public HashMap<TestStep, List<SecurityCheck>> addSecurityChecks( TestStep testStep, SecurityCheck securityCheck )
	{
		List<SecurityCheck> checks = null;
		if( securityChecksMap.containsKey( testStep ) )
		{
			checks = securityChecksMap.get( testStep );
		}
		else
		{
			checks = new ArrayList<SecurityCheck>();
		}
		checks.add( securityCheck );
		securityChecksMap.put( testStep, checks );
		return securityChecksMap ;
	}

	public HashMap<TestStep, List<SecurityCheck>> getSecurityChecksMap()
	{
		return securityChecksMap;
	}

	public WsdlTestCase getTestCase()
	{
		return testCase;
	}

}
