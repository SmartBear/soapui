package com.eviware.soapui.impl.rest.panels.request;

import javax.swing.*;
import java.awt.*;

public class TextPanelWithTopLabel extends JPanel
{

	JLabel textLabel;
	JTextField textField;


	public TextPanelWithTopLabel( String label, String text, JTextField textField )
	{
		textLabel = new JLabel( label );
		this.textField = textField;
		textField.setText( text );
		setToolTipText( text );
		super.setLayout( new BorderLayout() );
		super.add( textLabel, BorderLayout.NORTH );
		super.add( textField, BorderLayout.SOUTH );

	}

	public String getText()
	{
		return textField.getText();
	}

	public void setText( String text )
	{
		textField.setText( text );
		setToolTipText( text );
	}

	@Override
	public void setToolTipText( String text )
	{
		super.setToolTipText( text );
		textLabel.setToolTipText( text );
		textField.setToolTipText( text );
	}
}
