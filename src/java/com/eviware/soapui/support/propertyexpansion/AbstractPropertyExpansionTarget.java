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

package com.eviware.soapui.support.propertyexpansion;

import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.scripting.SoapUIScriptGenerator;

public abstract class AbstractPropertyExpansionTarget implements PropertyExpansionTarget
{
	private ModelItem modelItem;

	public AbstractPropertyExpansionTarget( ModelItem modelItem )
	{
		this.modelItem = modelItem;
	}

	public ModelItem getContextModelItem()
	{
		if( modelItem instanceof WsdlTestRequest )
		{
			modelItem = ( ( WsdlTestRequest )modelItem ).getTestStep();
		}
		else if( modelItem instanceof HttpTestRequestInterface )
		{
			modelItem = ( ( HttpTestRequestInterface<?> )modelItem ).getTestStep();
		}
		// else if( modelItem instanceof WsdlMockResponse &&
		// ((WsdlMockResponse)modelItem).getMockOperation().getMockService()
		// instanceof WsdlTestMockService )
		// {
		// modelItem =
		// ((WsdlTestMockService)((WsdlMockResponse)modelItem).getMockOperation().getMockService()).getMockResponseStep();
		// }

		return modelItem;
	}

	public ModelItem getModelItem()
	{
		return modelItem;
	}

	protected String createContextExpansion( String name, PropertyExpansion expansion )
	{
		SoapUIScriptGenerator scriptGenerator = SoapUIScriptEngineRegistry.createScriptGenerator( getModelItem() );
		return scriptGenerator.createContextExpansion( name, expansion );
	}
}