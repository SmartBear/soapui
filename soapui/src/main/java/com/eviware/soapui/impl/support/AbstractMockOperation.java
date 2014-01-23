package com.eviware.soapui.impl.support;

import com.eviware.soapui.config.BaseMockResponseConfig;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.model.mock.MockOperation;

import java.beans.PropertyChangeListener;

public abstract class AbstractMockOperation
		<ModelItemConfigType extends ModelItemConfig>
		extends AbstractWsdlModelItem<ModelItemConfigType>
		implements MockOperation, PropertyChangeListener
{

	protected AbstractMockOperation( ModelItemConfigType config, AbstractMockService parent, String icon )
	{
		super( config, parent, icon );
	}
}
