package com.eviware.soapui.model.security;

/**
 * Register when parameter has been added or removed to holder.
 * 
 * @author robert
 *
 */
public interface SecurityCheckParameterHolderListener
{

	/**
	 * @param parameter
	 * 	parameter that has been added
	 */
	void parameterAdded( SecurityCheckedParameter parameter );

	/**
	 * @param parameter
	 * 	parameter that has been removed
	 */
	void parameterRemoved( SecurityCheckedParameter parameter );

}