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

package com.eviware.soapui.support.components;

import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public interface Inspector
{
	public final static String TITLE_PROPERTY = Inspector.class.getName() + "@title";
	public final static String ICON_PROPERTY = Inspector.class.getName() + "@icon";
	public final static String DESCRIPTION_PROPERTY = Inspector.class.getName() + "@description";
	public final static String ENABLED_PROPERTY = Inspector.class.getName() + "@enabled";

	public abstract String getTitle();

	public abstract ImageIcon getIcon();

	public abstract JComponent getComponent();

	public abstract String getDescription();

	public abstract boolean isEnabled();

	public abstract void addPropertyChangeListener( PropertyChangeListener listener );

	public abstract void removePropertyChangeListener( PropertyChangeListener listener );

	public abstract String getInspectorId();

	public abstract void release();

	public abstract void activate();

	public abstract void deactivate();
}