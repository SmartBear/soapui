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

package com.eviware.soapui.impl.wsdl;

import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

/**
 * Abstract base class for WSDL-implementation classes
 * 
 * @author Ole.Matzura
 */

public abstract class AbstractWsdlModelItem<T extends ModelItemConfig> extends AbstractModelItem
{
	private XmlBeansSettingsImpl settings;
	private T config;
	private ImageIcon icon;
	private final ModelItem parent;

	protected AbstractWsdlModelItem( T config, ModelItem parent, String icon )
	{
		this.parent = parent;
		if( config != null )
			setConfig( config );

		if( icon != null )
			this.icon = UISupport.createImageIcon( icon );
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return ModelSupport.dependsOn( this, modelItem );
	}

	public ModelItem getParent()
	{
		return parent;
	}

	public ImageIcon getIcon()
	{
		return icon;
	}

	public void setIcon( ImageIcon icon )
	{
		if( icon == this.icon )
			return;

		ImageIcon oldIcon = this.icon;
		this.icon = icon;
		notifyPropertyChanged( ICON_PROPERTY, oldIcon, icon );
	}

	public String getDescription()
	{
		String description = config.getDescription();
		return description == null || description.trim().length() == 0 ? null : description;
	}

	public void setDescription( String description )
	{
		String old = getDescription();
		config.setDescription( description );
		notifyPropertyChanged( DESCRIPTION_PROPERTY, old, description );
	}

	public String getName()
	{
		return config.getName();
	}

	public void setName( String name )
	{
		String old = getName();
		config.setName( name );
		notifyPropertyChanged( NAME_PROPERTY, old, name );
	}

	public XmlBeansSettingsImpl getSettings()
	{
		return settings;
	}

	public T getConfig()
	{
		return config;
	}

	public void setConfig( T config )
	{
		this.config = config;

		if( settings != null )
			settings.release();

		if( !config.isSetSettings() )
			config.addNewSettings();

		settings = new XmlBeansSettingsImpl( this, parent == null ? SoapUI.getSettings() : parent.getSettings(),
				this.config.getSettings() );
	}

	public String getId()
	{
		if( !config.isSetId() )
			config.setId( ModelSupport.generateModelItemID() );

		return config.getId();
	}

	protected void setSettings( XmlBeansSettingsImpl settings )
	{
		if( this.settings != null )
			this.settings.release();

		this.settings = settings;
	}

	public AbstractWsdlModelItem<?> getWsdlModelItemByName( Collection<? extends AbstractWsdlModelItem<?>> items,
			String name )
	{
		for( AbstractWsdlModelItem<?> item : items )
		{
			if( item.getName().equals( name ) )
				return item;
		}

		return null;
	}

	public void release()
	{
		if( settings != null )
		{
			settings.release();
		}
	}

	public void resolve( ResolveContext<?> context )
	{
		List<? extends ModelItem> children = getChildren();
		if( children == null )
			return;

		for( ModelItem modelItem : children )
		{
			if( modelItem instanceof AbstractWsdlModelItem )
			{
				( ( AbstractWsdlModelItem<?> )modelItem ).resolve( context );
			}
		}
	}

	public void beforeSave()
	{
		List<? extends ModelItem> children = getChildren();
		if( children == null )
			return;

		for( ModelItem modelItem : children )
		{
			if( modelItem instanceof AbstractWsdlModelItem )
			{
				( ( AbstractWsdlModelItem<?> )modelItem ).beforeSave();
			}
		}
	}

	public void afterLoad()
	{
		List<? extends ModelItem> children = getChildren();
		if( children == null )
			return;

		for( ModelItem modelItem : children )
		{
			if( modelItem instanceof AbstractWsdlModelItem )
			{
				( ( AbstractWsdlModelItem<?> )modelItem ).afterLoad();
			}
		}
	}
}
