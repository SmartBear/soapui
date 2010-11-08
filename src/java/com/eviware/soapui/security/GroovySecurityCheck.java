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
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestStep;

/**
 * GroovySecurityCheck
 * 
 * @author soapui team
 */

public class GroovySecurityCheck implements SecurityCheck
{

	//configuration of specific request modification
	private SecurityCheckConfig config;
	public GroovySecurityCheck( SecurityCheckConfig config )
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void analyze( TestStep testStep )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void execute( TestStep testStep )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public SecurityCheckConfig getConfig()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SecurityTestLogEntry> getResults()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMonitorApplicable()
	{
		// TODO Auto-generated method stub
		return false;
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

}
