package com.eviware.soapui.security;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestStep;

public class SecurityTestImpl implements SecurityTest, Runnable
{
	private WsdlTestCase testCase;
	private HashMap<TestStep,List<SecurityCheck>> securityChecksMap;
	private String description;
	private String label;
	private SecurityTestConfig securityTestConfig;
	
	private SecurityTestLog securityTestLog;

	@Override
	public List<? extends ModelItem> getChildren()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
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


}
