package com.eviware.soapui.security.panels;

import javax.swing.tree.DefaultTreeModel;

import com.eviware.soapui.security.SecurityTest;

public class SecurityCheckTree extends DefaultTreeModel
{

	private SecurityTest securityTest;

	public SecurityCheckTree( SecurityTest securityTest )
	{
		super( new SecurityTreeRootNode( securityTest ) );

		this.securityTest = securityTest;

	}
	

}
