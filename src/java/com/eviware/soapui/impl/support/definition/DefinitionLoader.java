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

package com.eviware.soapui.impl.support.definition;

import com.eviware.soapui.impl.wsdl.support.xsd.SchemaLoader;
import com.eviware.x.dialogs.XProgressMonitor;

public interface DefinitionLoader extends SchemaLoader
{
	void setProgressMonitor( XProgressMonitor monitor, int i );

	void setProgressInfo( String info );

	boolean isAborted();

	boolean abort();

	void setNewBaseURI( String uri );

	String getFirstNewURI();
}
