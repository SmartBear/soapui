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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.Action;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;

public abstract class AbstractGroovyEditorModel implements GroovyEditorModel
{
	private final String[] keywords;
	private Action runAction;
	private final String name;
	
	private PropertyChangeSupport propertyChangeSupport;
	private final ModelItem modelItem;

	public AbstractGroovyEditorModel( String[] keywords, ModelItem modelItem, String name )
	{
		this.keywords = keywords;
		this.modelItem = modelItem;
		this.name = name;

		runAction = createRunAction();
		
		propertyChangeSupport = new PropertyChangeSupport( this );
	}

	public String[] getKeywords()
	{
		return keywords;
	}

	public Action getRunAction()
	{
		return runAction;
	}

	public Action createRunAction()
	{
		return null;
	}

	public abstract String getScript();

	public ModelItem getModelItem()
	{
		return modelItem;
	}

	public Settings getSettings()
	{
		return modelItem.getSettings();
	}

	public abstract void setScript( String text );

	public String getScriptName()
	{
		return name;
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}
	
	public void notifyPropertyChanged( String name, Object oldValue, Object newValue )
	{
		propertyChangeSupport.firePropertyChange( name, oldValue, newValue );
	}
}
