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

package com.eviware.soapui.model.iface;

import java.util.List;

import com.eviware.soapui.impl.support.DefinitionContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;

/**
 * An Interface exposing operations
 * 
 * @author Ole.Matzura
 */

public interface Interface extends ModelItem
{
	public final static String ENDPOINT_PROPERTY = Interface.class.getName() + "@endpoint";

	public final static String DEFINITION_PROPERTY = Interface.class.getName() + "@definition";

	public final static String UPDATING_PROPERTY = Interface.class.getName() + "@updating";

	public String[] getEndpoints();

	public Operation getOperationAt( int index );

	public int getOperationCount();

	public Operation getOperationByName( String name );

	public Project getProject();

	public void addInterfaceListener( InterfaceListener listener );

	public void removeInterfaceListener( InterfaceListener listener );

	public String getTechnicalId();

	public List<Operation> getOperationList();

	public String getInterfaceType();

	public void addEndpoint( String endpoint );

	public void removeEndpoint( String ep );

	public void changeEndpoint( String endpoint, String string );

	public DefinitionContext getDefinitionContext();

	public Operation[] getAllOperations();
}
