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

package com.eviware.soapui.support.components;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.list.SelectionInList;

public class SimpleBindingForm extends SimpleForm
{
	private final PresentationModel<?> pm;

	public SimpleBindingForm( PresentationModel<?> pm )
	{
		this.pm = pm;
	}

	/**
	 * Appends a label and a text field to the form
	 *
	 * @param propertyName The name of the property the field should be bound to
	 * @param label The value of the label
	 * @param tooltip The value of the text field tool tip
	 */
	public JTextField appendTextField( String propertyName, String label, String tooltip )
	{
		JTextField textField = super.appendTextField( label, tooltip );
		Bindings.bind( textField, pm.getModel( propertyName ) );
		return textField;
	}

	/**
	 * Appends a label and a text field to the form
	 *
	 * @param propertyName The name of the property the field should be bound to
	 * @param label The value of the label
	 * @param tooltip The value of the text field tool tip
	 * @param textFieldColumns The number of columns to display for the text field. Should be a constant defined in SimpleForm
	 * @see com.eviware.soapui.support.components.SimpleForm
	 */
	public JTextField appendTextField( String propertyName, String label, String tooltip, int textFieldColumns )
	{
		JTextField textField = super.appendTextField( label, tooltip, textFieldColumns );
		Bindings.bind( textField, pm.getModel( propertyName ) );
		return textField;
	}

	public JTextArea appendTextArea( String propertyName, String label, String tooltip )
	{
		JTextArea textArea = super.appendTextArea( label, tooltip );
		Bindings.bind( textArea, pm.getModel( propertyName ) );
		return textArea;
	}

	public JPasswordField appendPasswordField( String propertyName, String label, String tooltip )
	{
		JPasswordField textField = super.appendPasswordField( label, tooltip );
		Bindings.bind( textField, pm.getModel( propertyName ) );
		return textField;
	}

	public JCheckBox appendCheckBox( String propertyName, String label, String tooltip )
	{
		JCheckBox checkBox = super.appendCheckBox( label, tooltip, false );
		Bindings.bind( checkBox, pm.getModel( propertyName ) );
		return checkBox;
	}

	public void appendComponent( String propertyName, String label, JComponent component )
	{
		super.append( label, component );
		Bindings.bind( component, propertyName, pm.getModel( propertyName ) );
	}

	public JComboBox appendComboBox( String propertyName, String label, Object[] values, String tooltip )
	{
		JComboBox comboBox = super.appendComboBox( label, values, tooltip );
		Bindings.bind( comboBox, new SelectionInList<Object>( values, pm.getModel( propertyName ) ) );

		return comboBox;
	}

	public JComboBox appendComboBox( String propertyName, String label, ComboBoxModel model, String tooltip )
	{
		JComboBox comboBox = super.appendComboBox( label, model, tooltip );
		Bindings.bind( comboBox, new SelectionInList<Object>( model, pm.getModel( propertyName ) ) );
		return comboBox;
	}

	public void setComboBoxItems( String propertyName, JComboBox comboBox, String[] values )
	{
		Bindings.bind( comboBox, new SelectionInList<Object>( values, pm.getModel( propertyName ) ) );
	}

	public PresentationModel<?> getPresentationModel()
	{
		return pm;
	}

}
