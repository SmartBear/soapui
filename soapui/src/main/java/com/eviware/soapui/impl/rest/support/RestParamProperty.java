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

package com.eviware.soapui.impl.rest.support;

import java.beans.PropertyChangeListener;

import com.eviware.soapui.model.testsuite.RenameableTestProperty;

public interface RestParamProperty extends RenameableTestProperty, RestParameter
{
	public abstract void addPropertyChangeListener( PropertyChangeListener listener );

	public abstract void addPropertyChangeListener( String propertyName, PropertyChangeListener listener );

	public abstract void removePropertyChangeListener( PropertyChangeListener listener );

	public abstract void removePropertyChangeListener( String propertyName, PropertyChangeListener listener );

	public abstract boolean isDisableUrlEncoding();

	public abstract void setDisableUrlEncoding( boolean encode );
}
