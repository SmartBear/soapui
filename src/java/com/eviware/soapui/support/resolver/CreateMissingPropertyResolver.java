/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class CreateMissingPropertyResolver implements Resolver
{
	private boolean resolved = false;
	private PropertyTransfersTestStep parentPropertyTestStep = null;
	private PropertyTransfer badTransfer = null;

	public CreateMissingPropertyResolver( PropertyTransfer transfer, PropertyTransfersTestStep parent )
	{
		parentPropertyTestStep = parent;
		badTransfer = transfer;
	}

	public String getDescription()
	{
		return "Create new property";
	}

	@Override
	public String toString()
	{
		return getDescription();
	}

	public String getResolvedPath()
	{
		return null;
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{
		WsdlProject project = parentPropertyTestStep.getTestCase().getTestSuite().getProject();

		String name = UISupport.prompt( "Specify unique property name", "Add Property", "" );
		if( StringUtils.hasContent( name ) )
		{
			if( project.hasProperty( name ) )
			{
				UISupport.showErrorMessage( "Property name [" + name
						+ "] already exists. Property transfer will be disabled." );
				badTransfer.setDisabled( true );

			}
			else
			{
				TestProperty newProperty = project.addProperty( name );
				name = UISupport.prompt( "What is default value for property " + name, "Add Property Value", "" );
				if( StringUtils.hasContent( name ) )
					newProperty.setValue( name );
				else
					newProperty.setValue( newProperty.getName() );
				badTransfer.setSourcePropertyName( newProperty.getName() );
				resolved = true;
			}
		}
		else
		{
			UISupport.showInfoMessage( "Canceled. Property transfer will be disabled." );
			badTransfer.setDisabled( true );
		}
		return resolved;
	}

}
