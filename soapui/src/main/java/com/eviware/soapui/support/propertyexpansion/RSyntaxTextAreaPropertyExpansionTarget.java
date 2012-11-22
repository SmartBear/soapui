package com.eviware.soapui.support.propertyexpansion;

import java.awt.Point;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;

public class RSyntaxTextAreaPropertyExpansionTarget extends AbstractPropertyExpansionTarget
{

	private final RSyntaxTextArea textField;

	public RSyntaxTextAreaPropertyExpansionTarget( RSyntaxTextArea textField, ModelItem modelItem )
	{
		super( modelItem );
		this.textField = textField;
	}

	@Override
	public void insertPropertyExpansion( PropertyExpansion expansion, Point pt )
	{
		int pos = pt == null ? -1 : textField.viewToModel( pt );
		if( pos == -1 )
			pos = textField.getCaretPosition();

		try
		{
			textField.setText( textField.getText( 0, pos ) + expansion.toString()
					+ textField.getText( pos, textField.getText().length() - textField.getText( 0, pos ).length()  ) );
		}
		catch( BadLocationException e )
		{
			SoapUI.logError( e, "Unable to insert property expansion" );
		}

		if( pos >= 0 )
		{
			textField.setCaretPosition( pos );
			textField.requestFocusInWindow();
		}
	}

	@Override
	public String getValueForCreation()
	{
		return textField.getSelectedText();
	}

	@Override
	public String getNameForCreation()
	{
		return textField.getName();
	}
}
