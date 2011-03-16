package com.eviware.soapui.security.support;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.settings.Settings;

/**
 * ... holds information on parameter which is excluded from request and
 * security test is applied on.
 * 
 * @author robert
 * 
 */
public class SecurityCheckedParameterImpl implements SecurityCheckedParameter
{

	private CheckedParameterConfig config;

	public SecurityCheckedParameterImpl( CheckedParameterConfig param )
	{
		this.config = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.support.SecurityChekedParameter#getName()
	 */
	public String getName()
	{
		return config.getParameterName();
	}

	/**
	 * @param name
	 *           parameter name
	 */
	public void setName( String name )
	{
		config.setParameterName( name );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.support.SecurityChekedParameter#getXPath()
	 */
	public String getXPath()
	{
		return config.getXpath();
	}

	/**
	 * @param xpath
	 *           parameter XPath
	 */
	public void setXPath( String xpath )
	{
		config.setXpath( xpath );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.support.SecurityChekedParameter#getType()
	 */
	public String getType()
	{
		return config.getType();
	}

	/**
	 * @param schemaType
	 *           parameter xml type
	 */
	public void setType( SchemaType schemaType )
	{
		config.setType( schemaType.toString() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.support.SecurityChekedParameter#isChecked()
	 */
	public boolean isChecked()
	{
		return config.getChecked();
	}

	/**
	 * Enable/dissable using this parameter in security check..
	 * 
	 * @param checked
	 * 
	 */
	public void setChecked( boolean checked )
	{
		config.setChecked( checked );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.support.SecurityChekedParameter#getLabel()
	 */
	public String getLabel()
	{
		return config.getLabel();
	}

	/**
	 * @param label
	 *           parameter label
	 */
	public void setLabel( String label )
	{
		config.setLabel( label );
	}

	/**
	 * @param config
	 *           parameter config
	 */
	public void setConfig( CheckedParameterConfig config )
	{
		this.config = config;
	}

	@Override
	public String getId()
	{
		return null;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public Settings getSettings()
	{
		return null;
	}

	@Override
	public List<? extends ModelItem> getChildren()
	{
		return null;
	}

	@Override
	public ModelItem getParent()
	{
		return null;
	}

	@Override
	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
	}

	@Override
	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
	}

	@Override
	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
	}

	@Override
	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
	}
}
