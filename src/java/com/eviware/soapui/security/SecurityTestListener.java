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

package com.eviware.soapui.security;

import com.eviware.soapui.model.testsuite.TestStep;

/**
 * Listener for security check events
 * 
 * @author dragica.soldo
 */

public interface SecurityTestListener
{
	public void securityCheckAdded( TestStep testStep, SecurityTest securityTest );

	public void securityCheckRemoved( TestStep testStep, SecurityTest securityTest );

	public void securityCheckMoved( TestStep testStep, SecurityTest securityTest, int ix, int offset );
}
