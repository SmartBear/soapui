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

package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.eviware.soapui.support.components.SimpleForm;

public abstract class SecurityScanConfigPanel extends JPanel
{
	protected SimpleForm form;

	public SecurityScanConfigPanel( BorderLayout borderLayout )
	{
		super( borderLayout );
	}

	public abstract void save();
}
