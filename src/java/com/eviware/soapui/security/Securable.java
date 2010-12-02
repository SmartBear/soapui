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

package com.eviware.soapui.security;

import java.util.List;
import java.util.Map;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.security.check.SecurityCheck;

/**
 * Behavior for an object that can be securityChecked
 * 
 * @author dragica.soldo
 */

public interface Securable
{
	public SecurityCheck addSecurityCheck( String checkType, String checkName );

	 public void addSecurityChecksListener( SecurityChecksListener listener );

	public int getSecurityCheckCount();

	public SecurityCheck getSecurityCheckAt( int c );

	 public void removeSecurityChecksListener( SecurityChecksListener listener );

	public void removeSecurityCheck( SecurityCheck securityCheck );

	// public AssertionStatus getAssertionStatus();
	//
	// public enum AssertionStatus
	// {
	// UNKNOWN, VALID, FAILED
	// }

	// public String getAssertableContent();

	// public String getDefaultAssertableContent();

	// public AssertableType getAssertableType();

	public List<SecurityCheck> getSecurityCheckList();

	public SecurityCheck getSecurityCheckByName( String name );

	public ModelItem getModelItem();

	public Interface getInterface();

	public SecurityCheck cloneSecurityCheck( SecurityCheck source, String name );

	public Map<String, SecurityCheck> getSecurityChecks();

	public SecurityCheck moveSecurityCheck( int ix, int offset );
}
