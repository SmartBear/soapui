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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.TransferResponseValuesTestStep;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for TransferResponseValuesTestStep
 * 
 * @author Ole.Matzura
 */

public class TransferResponseValuesTestStepPanelBuilder 
	extends EmptyPanelBuilder<TransferResponseValuesTestStep>
{
	public TransferResponseValuesTestStepPanelBuilder()
   {
   }

   public DesktopPanel buildDesktopPanel(TransferResponseValuesTestStep testStep)
   {
      return new TransferResponseValuesDesktopPanel( testStep );
   }

   public boolean hasDesktopPanel()
   {
      return true;
   }

	@Override
	public Component buildOverviewPanel( TransferResponseValuesTestStep modelItem )
	{
		return buildDefaultProperties( modelItem, "PropertyTransfer Properties" );
	}
}
