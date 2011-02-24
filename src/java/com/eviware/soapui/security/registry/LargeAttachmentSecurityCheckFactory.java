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

package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.LargeAttachmentSecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.LargeAttachmentSecurityCheck;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author soapUI team
 */

public class LargeAttachmentSecurityCheckFactory extends AbstractSecurityCheckFactory
{

	public LargeAttachmentSecurityCheckFactory()
	{
		super( LargeAttachmentSecurityCheck.TYPE, "LargeAttachmentSecurityCheck",
				"Preforms a check to see if the target can deal with extremely large attachments",
				"/large_attachement_check_script.gif" );
	}

	public boolean canCreate( TestStep testStep )
	{
		return true;
	}

	@Override
	public AbstractSecurityCheck buildSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent )
	{
		return new LargeAttachmentSecurityCheck( config, parent, null, testStep );
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( LargeAttachmentSecurityCheck.TYPE );
		securityCheckConfig.setName( name );
		LargeAttachmentSecurityCheckConfig sic = LargeAttachmentSecurityCheckConfig.Factory.newInstance();
		sic.setSize( 4 * 1024 * 1024 * 1024 );
		securityCheckConfig.setConfig( sic );
		return securityCheckConfig;
	}

	@Override
	public boolean isHttpMonitor()
	{
		return false;
	}
}
