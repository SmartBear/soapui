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

package com.eviware.soapui.impl.wsdl.support.soap;

import java.text.Collator;
import java.util.Comparator;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.BindingImporter;
import com.eviware.soapui.impl.wsdl.support.policy.PolicyUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;

public abstract class AbstractSoapBindingImporter implements BindingImporter
{

	protected static final class BindingOperationComparator implements Comparator<BindingOperation>
	{
		public int compare( BindingOperation o1, BindingOperation o2 )
		{
			return Collator.getInstance().compare( o1.getOperation().getName(), o2.getOperation().getName() );
		}
	}

	protected void initWsAddressing( Binding binding, WsdlInterface iface, Definition def ) throws Exception
	{
		iface.setWsaVersion( WsdlUtils.getUsingAddressing( binding ) );
		// if (iface.getWsaVersion().equals(WsaVersionTypeConfig.NONE.toString()))
		// {
		iface.processPolicy( PolicyUtils.getAttachedPolicy( binding, def ) );
		// }
	}

	public AbstractSoapBindingImporter()
	{
		super();
	}

}