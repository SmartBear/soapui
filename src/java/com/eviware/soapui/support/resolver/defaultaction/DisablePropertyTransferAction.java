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

	@Override
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
