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

package com.eviware.soapui.impl.wsdl.loadtest.strategy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.LoadTestLimitTypesConfig;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * LoadStrategy allowing maximum runs and request delays
 * 
 * @author Ole.Matzura
 */

public abstract class AbstractLoadStrategy implements LoadStrategy
{
	private PropertyChangeSupport propertyChangeSupport;
	private final String type;
	private final WsdlLoadTest loadTest;

	public AbstractLoadStrategy( String type, WsdlLoadTest loadTest )
	{
		this.type = type;
		this.loadTest = loadTest;
		propertyChangeSupport = new PropertyChangeSupport( this );
	}

	public XmlObject getConfig()
	{
		return null;
	}

	public JComponent getConfigurationPanel()
	{
		return null;
	}

	public String getType()
	{
		return type;
	}

	public WsdlLoadTest getLoadTest()
	{
		return loadTest;
	}

	public void addConfigurationChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( CONFIGURATION_PROPERTY, listener );
	}

	public void removeConfigurationChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( CONFIGURATION_PROPERTY, listener );
	}

	public void notifyConfigurationChanged()
	{
		propertyChangeSupport.firePropertyChange( CONFIGURATION_PROPERTY, null, null );
	}

	public boolean allowThreadCountChangeDuringRun()
	{
		return true;
	}

	public void afterLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

	public void afterTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
			TestCaseRunContext runContext )
	{
	}

	public void afterTestStep( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
			TestCaseRunContext runContext, TestStepResult testStepResult )
	{
	}

	public void beforeLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		if( getLoadTest().getLimitType() == LoadTestLimitTypesConfig.COUNT
				&& getLoadTest().getTestLimit() < getLoadTest().getThreadCount() )
		{
			getLoadTest().setThreadCount( getLoadTest().getTestLimit() );
		}
	}

	public void beforeTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
			TestCaseRunContext runContext )
	{
	}

	public void beforeTestStep( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
			TestCaseRunContext runContext, TestStep testStep )
	{
	}

	public void loadTestStarted( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

	public void loadTestStopped( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

	public void recalculate( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
	}

	public void updateConfig( XmlObject config )
	{
	}
}
