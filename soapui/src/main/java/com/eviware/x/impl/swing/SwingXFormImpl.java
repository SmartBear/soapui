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

package com.eviware.x.impl.swing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.Border;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.NamespaceTable;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.XFormTextField;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SwingXFormImpl implements XForm
{
	private JPanel panel;
	private CellConstraints cc = new CellConstraints();
	private FormLayout layout;
	private RowSpec rowSpec;
	private int rowSpacing = 5;
	private Map<String, XFormField> components = new HashMap<String, XFormField>();
	private String rowAlignment = "top";
	private String name;

	public SwingXFormImpl( String name )
	{
		this( name, 5 );
	}

	public SwingXFormImpl( String name, int leftIndent )
	{
		this.name = name;
		layout = new FormLayout( leftIndent + "px,left:pref,5px,left:default,5px:grow(1.0)" );
		panel = new JPanel( layout );
		rowSpec = new RowSpec( rowAlignment + ":pref" );
	}

	public String getName()
	{
		return name;
	}

	public int getRowSpacing()
	{
		return rowSpacing;
	}

	public void setRowSpacing( int rowSpacing )
	{
		this.rowSpacing = rowSpacing;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public JPanel getPanel()
	{
		return panel;
	}

	public void addSpace( int size )
	{
		if( size > 0 )
			layout.appendRow( new RowSpec( size + "px" ) );
	}

	public XFormField addCheckBox( String name, String description )
	{
		JCheckBoxFormField checkBox = new JCheckBoxFormField( description == null ? name : description );
		if( name != null && description != null )
			checkBox.setToolTip( description );

		addComponent( name, checkBox );
		return checkBox;
	}

	/*
	 * If label starts with '###' do not show them. (non-Javadoc) if label ends
	 * with '___' (3) right border is 30 and no ':' at the end
	 * 
	 * @see com.eviware.x.form.XForm#addComponent(java.lang.String,
	 * com.eviware.x.form.XFormField)
	 */
	public XFormField addComponent( String label, XFormField formComponent )
	{
		if( rowSpacing > 0 && !components.isEmpty() )
			addSpace( rowSpacing );

		components.put( label, formComponent );

		layout.appendRow( rowSpec );

		int row = layout.getRowCount();

		AbstractSwingXFormField<?> swingFormComponent = ( AbstractSwingXFormField<?> )formComponent;

		if( label != null && !label.startsWith( "###" ) )
		{
			JLabel jlabel = null;
			if( label.endsWith( "___" ) )
			{
				jlabel = new JLabel( label.substring( 0, label.length() - 3 ) );
				jlabel.setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 30 ) );
			}
			else
			{
				jlabel = new JLabel( label.endsWith( ":" ) ? label : label + ":" );
				jlabel.setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
			}
			panel.add( jlabel, cc.xy( 2, row ) );

			jlabel.setLabelFor( swingFormComponent.getComponent() );
			int ix = label.indexOf( '&' );
			if( ix >= 0 )
			{
				jlabel.setText( label.substring( 0, ix ) + label.substring( ix + 1 ) );
				jlabel.setDisplayedMnemonicIndex( ix );
				jlabel.setDisplayedMnemonic( label.charAt( ix + 1 ) );
			}

			swingFormComponent.getComponent().getAccessibleContext().setAccessibleDescription( label );
		}

		if( label != null && label.startsWith( "###" ) )
			panel.add( swingFormComponent.getComponent(), cc.xyw( 2, row, 4 ) );
		else
			panel.add( swingFormComponent.getComponent(), cc.xy( 4, row ) );

		components.put( label, formComponent );

		return formComponent;
	}

	public XFormOptionsField addComboBox( String name, Object[] values, String description )
	{
		JComboBoxFormField comboBox = new JComboBoxFormField( values );
		comboBox.setToolTip( description );
		addComponent( name, comboBox );
		return comboBox;
	}

	public void addSeparator()
	{
		addSeparator( null );
	}

	public void addSeparator( String label )
	{
		addSpace( rowSpacing );
		addSpace( rowSpacing );

		layout.appendRow( rowSpec );
		int row = layout.getRowCount();

		if( StringUtils.isNullOrEmpty( label ) )
			panel.add( new JSeparator(), cc.xywh( 2, row, 3, 1 ) );
		else
			panel.add( new JLabel( label ), cc.xywh( 2, row, 3, 1 ) );

		addSpace( rowSpacing );
	}

	public XFormTextField addTextField( String name, String description, FieldType type )
	{
		if( type == FieldType.FOLDER || type == FieldType.FILE || type == FieldType.PROJECT_FOLDER
				|| type == FieldType.PROJECT_FILE || type == FieldType.FILE_OR_FOLDER )
		{
			return ( XFormTextField )addComponent( name, new FileFormField( description, type ) );
		}
		else if( type == FieldType.PASSWORD )
		{
			JPasswordFieldFormField pwdField = new JPasswordFieldFormField();
			pwdField.getComponent().setColumns( 30 );
			pwdField.setToolTip( description );
			addComponent( name, pwdField );
			return pwdField;
		}
		else if( type == FieldType.TEXTAREA )
		{
			JTextAreaFormField field = new JTextAreaFormField();
			field.getTextArea().setColumns( 40 );
			field.getTextArea().setRows( 5 );
			field.setToolTip( description );
			addComponent( name, field );
			return field;
		}
		else
		{
			JTextFieldFormField textField = new JTextFieldFormField();
			textField.getComponent().setColumns( 40 );
			textField.setToolTip( description );
			addComponent( name, textField );
			return textField;
		}
	}

	public void setComponentValue( String label, String value )
	{
		XFormField component = getComponent( label );
		if( component != null )
			component.setValue( value );
	}

	public String getComponentValue( String name )
	{
		XFormField component = getComponent( name );
		return component == null ? null : component.getValue();
	}

	public XFormField getComponent( String label )
	{
		return components.get( label );
	}

	public void setBorder( Border border )
	{
		panel.setBorder( border );
	}

	public XFormField addComponent( XFormField component )
	{
		if( rowSpacing > 0 && !components.isEmpty() )
			addSpace( rowSpacing );

		layout.appendRow( rowSpec );
		int row = layout.getRowCount();

		AbstractSwingXFormField<?> swingFormComponent = ( AbstractSwingXFormField<?> )component;
		panel.add( swingFormComponent.getComponent(), cc.xyw( 1, row, 4 ) );

		return component;
	}

	public void setValues( StringToStringMap values )
	{
		for( Iterator<String> i = values.keySet().iterator(); i.hasNext(); )
		{
			String key = i.next();
			setComponentValue( key, values.get( key ) );
		}
	}

	public StringToStringMap getValues()
	{
		StringToStringMap values = new StringToStringMap();

		for( Iterator<String> i = components.keySet().iterator(); i.hasNext(); )
		{
			String key = i.next();
			values.put( key, getComponentValue( key ) );
		}

		return values;
	}

	public XFormField addNameSpaceTable( String label, Interface modelItem )
	{
		return addComponent( label, new NamespaceTable( ( WsdlInterface )modelItem ) );
	}

	public void setOptions( String name, Object[] values )
	{
		XFormOptionsField combo = ( XFormOptionsField )getComponent( name );
		if( combo != null )
			combo.setOptions( values );
	}

	public void addLabel( String name, String label )
	{
		addComponent( name, new JLabelFormField( label ) );
	}

	public XFormField[] getFormFields()
	{
		return components.values().toArray( new XFormField[components.size()] );
	}

	public void setFormFieldProperty( String name, Object value )
	{
		for( XFormField field : components.values() )
		{
			field.setProperty( name, value );
		}
	}

	public Object[] getOptions( String name )
	{
		XFormField combo = getComponent( name );
		if( combo instanceof XFormOptionsField )
			return ( ( XFormOptionsField )combo ).getOptions();

		return null;
	}

	public XFormField getFormField( String name )
	{
		return components.get( name );
	}
}
