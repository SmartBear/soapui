/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

import com.eviware.soapui.security.scan.ParameterExposureCheck;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleForm;

/**
 * 
 * NOT USED
 * 
 */
public class ParameterExposureCheckPanel extends SecurityCheckConfigPanel
{
	protected static final String MINIMUM_CHARACTERS_FIELD = "Minimum Characters";

	private ParameterExposureCheck parameterCheck;

	public ParameterExposureCheckPanel( ParameterExposureCheck parameterCheck )
	{
		super( new BorderLayout() );

		this.parameterCheck = parameterCheck;
		form = new SimpleForm();
		form.addSpace( 5 );

		// form.setDefaultTextFieldColumns(40);

		JTextField minimumCharactersTextField = form.appendTextField( MINIMUM_CHARACTERS_FIELD, "Minimum characters" );
		minimumCharactersTextField.setMaximumSize( new Dimension( 40, 10 ) );
		minimumCharactersTextField.setColumns( 4 );

		minimumCharactersTextField.addKeyListener( new MinimumListener() );

		// }
		add( form.getPanel() );
	}

	@Override
	public void save()
	{
		String minCharsStr = form.getComponentValue( MINIMUM_CHARACTERS_FIELD );
		int minimumLength = StringUtils.isNullOrEmpty( minCharsStr ) ? 0 : Integer.valueOf( minCharsStr );
		// queryArea.setText( "" );
		// saveConfig();
		if( minimumLength > 0 )
		{
			// parameterCheck.setMinimumLength(minimumLength);
		}

	}

	private class MinimumListener implements KeyListener
	{

		@Override
		public void keyPressed( KeyEvent arg0 )
		{

		}

		@Override
		public void keyReleased( KeyEvent arg0 )
		{

		}

		@Override
		public void keyTyped( KeyEvent ke )
		{
			char c = ke.getKeyChar();
			if( !Character.isDigit( c ) )
				ke.consume();
		}
	}
}
