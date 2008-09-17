/*
 *  soapUI, copyright (C) 2004-2008 eviware.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver.defaultaction;

import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class DisablePropertyTransferAction extends AbstractSoapUIAction<PropertyTransfersTestStep>
{
	PropertyTransfer transfer = null;

	public DisablePropertyTransferAction(PropertyTransfer transfer)
	{
		super("Default resolver", "Disable this property transfer");
		this.transfer = transfer;
	}

	public void perform(PropertyTransfersTestStep target, Object param)
	{
		if (transfer != null)
			transfer.setDisabled(true);
	}

	@Override
	public String toString()
	{
		return getDescription();
	}

}
