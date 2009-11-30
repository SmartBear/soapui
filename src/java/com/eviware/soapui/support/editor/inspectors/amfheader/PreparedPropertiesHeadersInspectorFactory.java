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

package com.eviware.soapui.impl.wsdl.teststeps.datasource.propertiesheader;

import javax.swing.JComponent;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlDataSourceTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.datasource.propertiesheader.PreparedPropertiesHeadersInspectorModel.AbstractPreparedHeadersModel;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.types.StringToStringMap;

public class PreparedPropertiesHeadersInspectorFactory
{
	public static final String INSPECTOR_ID = "Prepared Properties Headers";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public JComponent createRequestInspector( ModelItem modelItem )
	{
		if( modelItem instanceof WsdlDataSourceTestStep )
		{
			PreparedPropertiesHeadersInspector inspector = new PreparedPropertiesHeadersInspector(
					new PreparedPropertiesHeadersModel( ( WsdlDataSourceTestStep )modelItem ) );
			// inspector.setEnabled( true );
			return inspector.getComponent();
		}
		return null;
	}

	private class PreparedPropertiesHeadersModel extends AbstractPreparedHeadersModel<WsdlDataSourceTestStep>
	{
		public PreparedPropertiesHeadersModel( WsdlDataSourceTestStep request )
		{
			super( false, request, WsdlDataSourceTestStep.DB_PREPARED_PROPERTIES_PROPERTY );
		}

		public StringToStringMap getHeaders()
		{
			return getModelItem().getPreparedProperties();
		}

		public void setHeaders( StringToStringMap headers )
		{
			getModelItem().setPreparedProperties( headers );
		}
	}
}
