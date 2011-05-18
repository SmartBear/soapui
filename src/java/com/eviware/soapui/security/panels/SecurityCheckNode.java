package com.eviware.soapui.security.panels;

import javax.swing.tree.DefaultMutableTreeNode;

import com.eviware.soapui.model.security.SecurityScan;

public class SecurityCheckNode extends DefaultMutableTreeNode
{

	private SecurityScan securityCheck;

	public SecurityCheckNode( SecurityScan sc )
	{
		this.securityCheck = sc;
	}

	@Override
	public String toString()
	{
		return securityCheck.toString();
	}

	public SecurityScan getSecurityCheck()
	{
		return securityCheck;
	}
}
