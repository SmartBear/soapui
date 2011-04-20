package com.eviware.soapui.security.panels;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestStep;

public class TestStepNode extends DefaultMutableTreeNode
{

	private TestStep testStep;

	public TestStepNode( SecurityTreeRootNode securityTreeRootNode, TestStep step, List<SecurityCheck> list )
	{
		this.testStep = step;
		if( step instanceof SamplerTestStep )
			setAllowsChildren( true );
		else
		{
			setAllowsChildren( false );
			children = null;
		}
		if( list != null )
			for( SecurityCheck sc : list )
				add( new SecurityCheckNode( sc ) );
	}

	@Override
	public String toString()
	{
		return testStep.toString();
	}

	public TestStep getTestStep()
	{
		return testStep;
	}

}
