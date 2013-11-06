/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package soapui.demo;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class DemoAction extends AbstractSoapUIAction<WsdlProject>
{
	public DemoAction()
	{
		super( "Demo Action", "Demonstrates an extension to SoapUI" );
	}
	
	public void perform( WsdlProject target, Object param )
	{
		UISupport.showInfoMessage( "Welcome to my action in project [" + target.getName() + "]" );
	}
}
