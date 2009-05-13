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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Sets the delay to of a WsdlDelayTestStep
 * 
 * @author ole.matzura
 */

public class SetWaitTimeAction extends AbstractSoapUIAction<WsdlDelayTestStep>
{
	public SetWaitTimeAction()
	{
		super( "Set Delay Time", "Sets the Delay for this DelayStep" );
	}

	public void perform( WsdlDelayTestStep target, Object param )
	{
		String value = UISupport.prompt( "Specify delay in milliseconds", "Set Delay", String.valueOf( target
				.getDelayString() ) );
		if( value != null )
		{
			try
			{
				target.setDelayString( value );
			}
			catch( NumberFormatException e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
	}
}