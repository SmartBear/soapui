package com.eviware.soapui.model.security;

public interface SecurityCheckedParameter
{

	/**
	 * @return parameter name
	 */
	String getName();

	/**
	 * @return parameter xpath
	 */
	String getXpath();

	/**
	 * @return parameter type
	 */
	String getType();

	/**
	 * Do we apply this parameter in security check.
	 * 
	 * @return is parameter checked
	 */
	boolean isChecked();

	/**
	 * @return parameter label
	 */
	String getLabel();

}