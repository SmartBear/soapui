/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.panels;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.tree.DefaultMutableTreeNode;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;

@SuppressWarnings( "serial" )
public class SecurityTreeRootNode extends DefaultMutableTreeNode implements PropertyChangeListener
{

	private SecurityTest securityTest;

	public SecurityTreeRootNode( SecurityTest securityTest )
	{
		this.securityTest = securityTest;
		
		securityTest.addPropertyChangeListener( this );

		initRoot();
	}

	private void initRoot()
	{
		parent = null;
		initChildren();
		allowsChildren = true;
	}

	private void initChildren()
	{
		for( TestStep step : securityTest.getTestCase().getTestStepList() ) {
			add( new TestStepNode( this, step, securityTest.getSecurityChecksMap().get( step.getId() ) ));
		}
	}

	@Override
	public String toString()
	{
		return securityTest.toString();
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		System.out.println(evt.toString());
	}

	public SecurityTest getSecurityTest()
	{
		return securityTest;
	}

	public void add( TestStep testStep )
	{
		new TestStepNode( this, testStep, securityTest.getSecurityChecksMap().get( testStep.getId() ) );
	}
}
