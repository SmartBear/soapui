package com.eviware.soapui.security.support;

import com.eviware.soapui.model.security.SecurityCheckParameterListener;
import com.eviware.soapui.model.security.SecurityCheckedParameter;

/**
 * Adapter class for SecurityCheckParameterListener
 * 
 * @author robert
 *
 */
public class SecurityCheckParameterListenerAdapter implements SecurityCheckParameterListener
{

	/* (non-Javadoc)
	 * @see com.eviware.soapui.model.security.SecurityCheckParameterListener#parameterCheckedChanged(com.eviware.soapui.model.security.SecurityCheckedParameter)
	 */
	@Override
	public void parameterCheckedChanged( SecurityCheckedParameter parameter )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eviware.soapui.model.security.SecurityCheckParameterListener#parameterLabelChanged(com.eviware.soapui.model.security.SecurityCheckedParameter, java.lang.String, java.lang.String)
	 */
	@Override
	public void parameterLabelChanged( SecurityCheckedParameter parameter, String oldLabel, String newLabel )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eviware.soapui.model.security.SecurityCheckParameterListener#parameterNameChanged(com.eviware.soapui.model.security.SecurityCheckedParameter, java.lang.String, java.lang.String)
	 */
	@Override
	public void parameterNameChanged( SecurityCheckedParameter parameter, String oldName, String newName )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eviware.soapui.model.security.SecurityCheckParameterListener#parameterTypeChanged(com.eviware.soapui.model.security.SecurityCheckedParameter, java.lang.String, java.lang.String)
	 */
	@Override
	public void parameterTypeChanged( SecurityCheckedParameter paramter, String oldType, String newType )
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eviware.soapui.model.security.SecurityCheckParameterListener#parameterXPathChanged(com.eviware.soapui.model.security.SecurityCheckedParameter, java.lang.String, java.lang.String)
	 */
	@Override
	public void parameterXPathChanged( SecurityCheckedParameter parameter, String oldXPath, String newXPath )
	{
		// TODO Auto-generated method stub

	}

}
