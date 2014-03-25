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

package com.eviware.soapui.model;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.PropertyChangeNotifier;

import javax.swing.ImageIcon;
import java.util.List;

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

	/**
	 * Gets the project that this ModelItem object is part of. If this model item is not part of a project,
	 * e.g. if this is a {@code Workspace} object, an {@code UnsupportedOperationException} is thrown.
	 *
	 * @return The Project object that this ModelItem is a descendant of
	 * @throws UnsupportedOperationException If this model item is not the descendant of a project
	 */
	public Project getProject();
}
