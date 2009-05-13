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

package com.eviware.soapui.model;

import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.PropertyChangeNotifier;

/**
 * General behaviour for all soapui model items
 * 
 * @author Ole.Matzura
 */

public interface ModelItem extends PropertyChangeNotifier
{
	public final static String NAME_PROPERTY = ModelItem.class.getName() + "@name";
	public final static String ICON_PROPERTY = ModelItem.class.getName() + "@icon";
	public final static String DESCRIPTION_PROPERTY = ModelItem.class.getName() + "@description";
	public final static String LABEL_PROPERTY = ModelItem.class.getName() + "@label";

	public String getName();

	public String getId();

	public ImageIcon getIcon();

	public String getDescription();

	public Settings getSettings();

	public List<? extends ModelItem> getChildren();

	public ModelItem getParent();
}
