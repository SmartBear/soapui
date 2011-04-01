package com.eviware.soapui.security.panels;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.tree.DefaultMutableTreeNode;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;

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
}
