package com.eviware.soapui.security.support;

import org.apache.xmlbeans.SchemaType;

import com.eviware.soapui.config.CheckedParameterConfig;

/**
 * ... holds information on parameter which is excluded from request and security
 * test is applied on. 
 * 
 * @author robert
 *
 */
public class SecurityCheckedParameter
{

	private CheckedParameterConfig config;

	public SecurityCheckedParameter( CheckedParameterConfig param )
	{
		this.config = param;
	}

	
	/**
	 * @return
	 * 	parameter name
	 */
	public String getName()
	{
		return config.getParameterName();
	}
	
	/**
	 * @param name
	 * 	parameter name
	 */
	public void setName(String name) {
		config.setParameterName( name );
	}
	
	/**
	 * @return
	 * 	parameter XPath
	 */
	public String getXPath() {
		return config.getXpath();
	}
	
	/**
	 * @param xpath
	 * 	parameter XPath
	 */
	public void setXPath(String xpath) {
		config.setXpath( xpath );
	}
	
	/**
	 * @return
	 * 	parameter type
	 */
	public String getType() {
		
		return config.getType();
	}

	/**
	 * @param schemaType
	 * 	parameter xml type
	 */
	public void setType(SchemaType schemaType) {
		config.setType( schemaType.toString() );
	}
}
