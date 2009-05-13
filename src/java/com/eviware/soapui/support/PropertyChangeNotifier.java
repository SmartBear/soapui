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

package com.eviware.soapui.support;

import java.beans.PropertyChangeListener;

/**
 * Behaviour for a class that notifies property changes
 * 
 * @author Ole.Matzura
 */

public interface PropertyChangeNotifier
{
	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener );

	public void addPropertyChangeListener( PropertyChangeListener listener );

	public void removePropertyChangeListener( PropertyChangeListener listener );

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener );
}
