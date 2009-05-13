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

import javax.swing.JComponent;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;

/**
 * Strategy used by WsdlLoadTest for controlling requests in each thread
 * 
 * @author Ole.Matzura
 */

public interface LoadStrategy extends LoadTestRunListener
{
	public final static String CONFIGURATION_PROPERTY = "configuration_property";

	public void addConfigurationChangeListener( PropertyChangeListener listener );

	public void removeConfigurationChangeListener( PropertyChangeListener listener );

	public XmlObject getConfig();

	public String getType();

	public JComponent getConfigurationPanel();

	public void updateConfig( XmlObject config );

	public boolean allowThreadCountChangeDuringRun();

	public void recalculate( LoadTestRunner loadTestRunner, LoadTestRunContext context );
}
