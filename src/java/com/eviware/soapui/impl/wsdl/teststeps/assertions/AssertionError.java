/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import org.apache.xmlbeans.XmlError;

/**
 * Holder for an assertion error, deprecated since this class has moved
 * 
 * @deprecated moved to com.eviware.soapui.model.testsuite.AssertionError
 * @author Ole.Matzura
 */

public class AssertionError extends com.eviware.soapui.model.testsuite.AssertionError
{
	public AssertionError( String message )
	{
		super( message );
	}

	public AssertionError( XmlError xmlError )
	{
		super( xmlError );
	}
}
