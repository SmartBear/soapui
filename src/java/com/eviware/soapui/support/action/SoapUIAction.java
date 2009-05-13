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

package com.eviware.soapui.support.action;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.PropertyChangeNotifier;

/**
 * An action for a ModelItem
 * 
 * @author ole.matzura
 */

public interface SoapUIAction<T extends ModelItem> extends PropertyChangeNotifier
{
	public final static String ENABLED_PROPERTY = SoapUIAction.class.getName() + "@enabled";

	public void perform( T target, Object param );

	public String getId();

	public String getName();

	public String getDescription();

	public boolean isEnabled();

	public boolean isDefault();

	public boolean applies( T target );
}
