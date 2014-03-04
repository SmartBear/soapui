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

import javax.swing.*;
import javax.swing.border.Border;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;

public class SimpleBindingForm extends SimpleForm
{
	private final PresentationModel<?> pm;

	public SimpleBindingForm( PresentationModel<?> pm )
	{
		this.pm = pm;
	}

	public SimpleBindingForm( PresentationModel<?> pm, String columnSpecs)
	{
		super( columnSpecs );
		this.pm = pm;
	}

	public SimpleBindingForm( PresentationModel<?> pm, String columnSpecs, Border border)
	{
		super( columnSpecs, border );
		this.pm = pm;
	}

	public PresentationModel<?> getPresentationModel()
	{
		return pm;
	}

	public JCheckBox appendCheckBox( String propertyName, String label, String tooltip )
	{
		JCheckBox checkBox = super.appendCheckBox( label, tooltip, false );
		Bindings.bind( checkBox, pm.getModel( propertyName ) );
		return checkBox;
	}

	public JComboBox appendComboBox( String propertyName, String label, Object[] values, String tooltip )
	{
		JComboBox comboBox = super.appendComboBox( label, values, tooltip );
		Bindings.bind( comboBox, new SelectionInList<Object>( values, pm.getModel( propertyName ) ) );

		return comboBox;
	}

	public JComboBox appendComboBox( String label, ComboBoxModel model, String tooltip, ValueModel valueModel )
	{
		JComboBox comboBox = super.appendComboBox( label, model, tooltip );
		Bindings.bind( comboBox, new SelectionInList<Object>( model, valueModel ) );

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

	public JLabel appendLabel( String propertyName, String label )
	{
		JLabel jLabel = new JLabel();
		super.append( label, jLabel, "left,bottom" );
		Bindings.bind( jLabel, pm.getModel( propertyName ) );
		return jLabel;
	}

	public JPasswordField appendPasswordField( String propertyName, String label, String tooltip )
	{
		JPasswordField textField = super.appendPasswordField( label, tooltip );
		Bindings.bind( textField, pm.getModel( propertyName ) );
		return textField;
	}

	public JTextArea appendTextArea( String propertyName, String label, String tooltip )
	{
		JTextArea textArea = super.appendTextArea( label, tooltip );
		Bindings.bind( textArea, pm.getModel( propertyName ) );
		return textArea;
	}

	/**
	 * Appends a label and a text field to the form
	 *
	 * @param propertyName The name of the property the field should be bound to. Will also be the name of the text field.
	 * @param label The value of the label
	 * @param tooltip The value of the text field tool tip
	 */
	public JTextField appendTextField( String propertyName, String label, String tooltip )
	{
		return appendTextField( propertyName, label, tooltip, SimpleForm.DEFAULT_TEXT_FIELD_COLUMNS );
	}

	/**
	 * Appends a label and a text field to the form
	 *
	 * @param propertyName The name of the property the field should be bound to. Will also be the name of the text field.
	 * @param label The value of the label
	 * @param tooltip The value of the text field tool tip
	 * @param textFieldColumns The number of columns to display for the text field. Should be a constant defined in SimpleForm
	 * @see com.eviware.soapui.support.components.SimpleForm
	 */
	public JTextField appendTextField( String propertyName, String label, String tooltip, int textFieldColumns )
	{
		JTextField textField = super.appendTextField( label, propertyName, tooltip, textFieldColumns );
			Bindings.bind( textField, pm.getModel( propertyName ) );
		return textField;
	}

	public void appendComponentsInOneRow( PropertyComponent... propertyComponents )
	{
		for( PropertyComponent propertyComponent : propertyComponents )
		{
			if( propertyComponent.hasProperty() )
			{
				// TODO Add support for more components if there is a need for it
				if( propertyComponent.getComponent() instanceof JLabel )
				{
					Bindings.bind( ( JLabel )propertyComponent.getComponent(), pm.getModel( propertyComponent.getProperty() ) );
				}
				else
				{
					throw new RuntimeException( "Components of type " + propertyComponent.getComponent().getClass() + " is not supported" );
				}
			}
		}
		super.appendInOneRow( propertyComponents );
	}

	public void appendComponent( String propertyName, String label, JComponent component )
	{
		super.append( label, component );
		Bindings.bind( component, propertyName, pm.getModel( propertyName ) );
	}
}