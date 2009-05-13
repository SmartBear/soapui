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

package com.eviware.soapui.impl.wsdl.panels.support;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;

/**
 * ComponentEnabler for disabling components during TestCase runs
 * 
 * @author Ole.Matzura
 */

public class TestRunComponentEnabler extends TestMonitorListenerAdapter
{
	private final List<JComponent> components = new ArrayList<JComponent>();
	private final List<Boolean> states = new ArrayList<Boolean>();
	private final TestCase testCase;

	public TestRunComponentEnabler( TestCase testCase )
	{
		this.testCase = testCase;

		SoapUI.getTestMonitor().addTestMonitorListener( this );
	}

	public void release()
	{
		SoapUI.getTestMonitor().removeTestMonitorListener( this );
	}

	public void loadTestStarted( LoadTestRunner runner )
	{
		disable();
	}

	private void disable()
	{
		if( states.isEmpty() )
		{
			for( JComponent component : components )
			{
				states.add( component.isEnabled() );
				component.setEnabled( false );
			}
		}
	}

	private void enable()
	{
		if( !states.isEmpty() )
		{
			for( int c = 0; c < components.size(); c++ )
			{
				JComponent component = components.get( c );
				component.setEnabled( states.get( c ) );
			}

			states.clear();
		}
	}

	public void loadTestFinished( LoadTestRunner runner )
	{
		if( !SoapUI.getTestMonitor().hasRunningTest( testCase ) )
			enable();
	}

	public void testCaseStarted( TestRunner runner )
	{
		disable();
	}

	public void testCaseFinished( TestRunner runner )
	{
		if( !SoapUI.getTestMonitor().hasRunningTest( testCase ) )
			enable();
	}

	public void add( JComponent component )
	{
		components.add( component );

		if( SoapUI.getTestMonitor().hasRunningTest( testCase ) )
		{
			states.add( component.isEnabled() );
			component.setEnabled( false );
		}
	}
}