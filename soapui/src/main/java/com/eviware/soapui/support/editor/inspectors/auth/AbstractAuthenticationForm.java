/*
 * SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public abstract class AbstractAuthenticationForm
{
	public static final ColumnSpec LABEL_COLUMN = new ColumnSpec( "left:72dlu" );
	public static final ColumnSpec RIGHTMOST_COLUMN = new ColumnSpec( "5px" );
	public static final Color CARD_BORDER_COLOR = new Color( 121, 121, 121 );
	public static final Color CARD_BACKGROUND_COLOR = new Color( 228, 228, 228 );
	public static final int TOP_SPACING = 10;

	public JPanel getComponent()
	{
		return buildUI();
	}

	protected abstract JPanel buildUI();

	protected void setBorderOnPanel( JPanel card )
	{
		card.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, CARD_BORDER_COLOR ),
				BorderFactory.createMatteBorder( 10, 10, 10, 10, CARD_BACKGROUND_COLOR ) ) );
	}

	protected void setBackgroundColorOnPanel( JPanel panel )
	{
		panel.setBackground( CARD_BACKGROUND_COLOR );
	}

	protected void setBorderAndBackgroundColorOnPanel( JPanel panel )
	{
		panel.setBorder( BorderFactory.createLineBorder( CARD_BORDER_COLOR ) );
		setBackgroundColorOnPanel( panel );
	}

	void initForm( SimpleBindingForm form )
	{
		FormLayout formLayout = ( FormLayout )form.getPanel().getLayout();
		formLayout.setColumnSpec( 2, LABEL_COLUMN );
		formLayout.setColumnSpec( 5, RIGHTMOST_COLUMN );
	}
}
