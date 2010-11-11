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
package com.eviware.soapui.security.check;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestContext;
import com.eviware.soapui.security.log.SecurityTestLog;
import com.eviware.soapui.security.log.SecurityTestLogEntry;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

public abstract class AbstractSecurityCheck extends SecurityCheck
{
	// configuration of specific request modification
	protected SecurityCheckConfig config;
	protected String startupScript;
	protected String tearDownScript;
	protected SoapUIScriptEngine scriptEngine;
	protected boolean monitorApplicable = false;
	protected List<SecurityTestLogEntry> logEntries = new ArrayList<SecurityTestLogEntry>();

	// private
	public AbstractSecurityCheck( SecurityCheckConfig config )
	{
		this.config = config;
		this.startupScript = config.getStartupScript()!=null ? config.getStartupScript().getStringValue():"";
		this.tearDownScript = config.getTearDownScript()!=null ? config.getTearDownScript().getStringValue():"";
		scriptEngine = SoapUIScriptEngineRegistry.create( this );
	}

	abstract protected void execute( TestStep testStep, SecurityTestContext context, SecurityTestLog securityTestLog );

	@Override
	abstract public void analyze( TestStep testStep, SecurityTestContext context, SecurityTestLog securityTestLog );
	

	@Override
	public void run( TestStep testStep, SecurityTestContext context, SecurityTestLog securityTestLog )
	{
		runStartupScript( testStep );
		execute( testStep, context, securityTestLog );
		runTearDownScript( testStep );
	}

	private void runTearDownScript( TestStep testStep )
	{
		scriptEngine.setScript( tearDownScript );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		// scriptEngine.setVariable( "context", context );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			scriptEngine.clearVariables();
		}

	}

	private void runStartupScript( TestStep testStep )
	{
		scriptEngine.setScript( startupScript );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		// scriptEngine.setVariable( "context", context );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			scriptEngine.clearVariables();
		}
	}

	@Override
	public SecurityCheckConfig getConfig()
	{
		return config;
	}

	@Override
	public boolean isMonitorApplicable()
	{
		return monitorApplicable;
	}

	@Override
	public List<? extends ModelItem> getChildren()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription()
	{
		return config.getDescription();
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
		return config.getId();
	}

	@Override
	public String getName()
	{
		return config.getName();
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
}
