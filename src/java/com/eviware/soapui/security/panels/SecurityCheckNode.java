package com.eviware.soapui.security.panels;

import javax.swing.tree.DefaultMutableTreeNode;

import com.eviware.soapui.model.security.SecurityCheck;

public class SecurityCheckNode extends DefaultMutableTreeNode
{

	private SecurityCheck securityCheck;

	public SecurityCheckNode( SecurityCheck sc )
	{
		this.securityCheck = sc;
	}

	@Override
	public String toString()
	{
		return securityCheck.toString();
	}

	public SecurityCheck getSecurityCheck()
	{
		return securityCheck;
	}
}
