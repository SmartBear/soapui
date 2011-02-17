package com.eviware.soapui.security.support;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.model.security.SecurityCheckedParameter;

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

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see com.eviware.soapui.security.support.SecurityChekedParameter#getXPath()
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

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see com.eviware.soapui.security.support.SecurityChekedParameter#isChecked()
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

	/* (non-Javadoc)
	 * @see com.eviware.soapui.security.support.SecurityChekedParameter#getLabel()
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
}
