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

package com.eviware.soapui.security;

/**
 * Behavior for an object that can be securityScanned this stays for now in case
 * we decide we need it later, for now it's not used
 * 
 * @author dragica.soldo
 */

public interface Securable
{
	// public SecurityCheck addSecurityCheck( String checkType, String checkName
	// );
	//
	// public void addSecurityChecksListener( SecurityTestListener listener );
	//
	// public int getSecurityCheckCount();
	//
	// public SecurityCheck getSecurityCheckAt( int c );
	//
	// public void removeSecurityChecksListener( SecurityTestListener listener );
	//
	// public void removeSecurityCheck( SecurityCheck securityCheck );
	//
	// // public AssertionStatus getAssertionStatus();
	// //
	// // public enum AssertionStatus
	// // {
	// // UNKNOWN, VALID, FAILED
	// // }
	//
	// // public String getAssertableContent();
	//
	// // public String getDefaultAssertableContent();
	//
	// // public AssertableType getAssertableType();
	//
	// public List<SecurityCheck> getSecurityCheckList();
	//
	// public SecurityCheck getSecurityCheckByName( String name );
	//
	// public ModelItem getModelItem();
	//
	// public Interface getInterface();
	//
	// public SecurityCheck cloneSecurityCheck( SecurityCheck source, String name
	// );
	//
	// public Map<String, SecurityCheck> getSecurityChecks();
	//
	// public SecurityCheck moveSecurityCheck( int ix, int offset );
}
