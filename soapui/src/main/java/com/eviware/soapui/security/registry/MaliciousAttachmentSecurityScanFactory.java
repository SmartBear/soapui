/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.MaliciousAttachmentSecurityScanConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.scan.MaliciousAttachmentSecurityScan;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author SoapUI team
 */

public class MaliciousAttachmentSecurityScanFactory extends AbstractSecurityScanFactory
{

	public MaliciousAttachmentSecurityScanFactory()
	{
		super( MaliciousAttachmentSecurityScan.TYPE, MaliciousAttachmentSecurityScan.NAME,
				"Performs a scan for Malicious Attachment Vulnerabilities", "/malicious_attachment_scan.gif" );
	}

	public boolean canCreate( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public AbstractSecurityScan buildSecurityScan( TestStep testStep, SecurityScanConfig config, ModelItem parent )
	{
		return new MaliciousAttachmentSecurityScan( testStep, config, parent, "/malicious_attachment_scan.gif" );
	}

	@Override
	public SecurityScanConfig createNewSecurityScan( String name )
	{
		SecurityScanConfig securityCheckConfig = SecurityScanConfig.Factory.newInstance();
		securityCheckConfig.setType( MaliciousAttachmentSecurityScan.TYPE );
		securityCheckConfig.setName( name );
		MaliciousAttachmentSecurityScanConfig sic = MaliciousAttachmentSecurityScanConfig.Factory.newInstance();
		securityCheckConfig.setConfig( sic );
		return securityCheckConfig;
	}

}
