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

import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.support.swing.JTextComponentPopupMenu;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Utility-class for creating forms
 */

public class SimpleForm
{
	public static final String ENABLED_PROPERTY_NAME = "enabled";
	private JPanel panel;
	private CellConstraints cc = new CellConstraints();
	private FormLayout layout;
	private RowSpec rowSpec;
	private int rowSpacing = 5;
	private Map<String, JComponent> components = new HashMap<String, JComponent>();
	private Map<JComboBox, Object[]> comboBoxMaps = new HashMap<JComboBox, Object[]>();
	private String rowAlignment = "top";
	private Map<String, String> hiddenValues;
	private boolean appended;
	private Font labelFont;
	private int defaultTextAreaColumns = 30;
	private int defaultTextAreaRows = 3;
	private int defaultTextFieldColumns = 30;

	public SimpleForm()
	{
		this( 5 );
	}

	public SimpleForm( String layout )
	{
		this( new FormLayout( layout ) );
	}

	public SimpleForm( FormLayout layout )
	{
		this.layout = layout;
		panel = new JPanel( layout );
		rowSpec = new RowSpec( rowAlignment + ":pref" );
	}

	public SimpleForm( int indent )
	{
		this( indent + "px:none,left:pref,10px,left:default,5px:grow(1.0)" );
	}

	public JPanel getPanel()
	{
		return panel;
	}

	public String getRowAlignment()
	{
		return rowAlignment;
	}

	public Font getLabelFont()
	{
		return labelFont;
	}

	public void setLabelFont( Font labelFont )
	{
		this.labelFont = labelFont;
	}

	public void setRowAlignment( String rowAlignment )
	{
		this.rowAlignment = rowAlignment;
		rowSpec = new RowSpec( rowAlignment + ":pref" );
	}

	public void setRowAlignment( String alignment, String size, String resize )
	{
		this.rowAlignment = alignment + ":" + size + ":" + resize;
		rowSpec = new RowSpec( rowAlignment );
	}

	public int getRowSpacing()
	{
		return rowSpacing;
	}

	public void setRowSpacing( int rowSpacing )
	{
		this.rowSpacing = rowSpacing;
	}

	public void addHiddenValue( String name, String value )
	{
		if( hiddenValues == null )
			hiddenValues = new HashMap<String, String>();

		hiddenValues.put( name, value );
	}

	public JButton addRightButton( Action action )
	{
		if( rowSpacing > 0 && !components.isEmpty() )
			addSpace( rowSpacing );

		layout.appendRow( rowSpec );
		int row = layout.getRowCount();

		JButton button = new JButton( action );
		panel.add( button, cc.xy( 4, row, "right,bottom" ) );
		return button;
	}

	public void addSpace()
	{
		addSpace( rowSpacing );
	}

	public void addSpace( int size )
	{
		if( size > 0 )
			layout.appendRow( new RowSpec( size + "px" ) );
	}

	public void addRightComponent( JComponent component )
	{
		if( rowSpacing > 0 && !components.isEmpty() )
			addSpace( rowSpacing );

		layout.appendRow( rowSpec );

		int row = layout.getRowCount();
		panel.add( component, cc.xy( 4, row, "right,bottom" ) );
	}

	public JCheckBox appendCheckBox( String caption, String label, boolean selected )
	{
		JCheckBox checkBox = new JCheckBox( label, selected );
		checkBox.getAccessibleContext().setAccessibleDescription( caption );
		components.put( caption, checkBox );
		append( caption, checkBox );
		return checkBox;
	}

	public JRadioButton appendRadioButton( String caption, String label, ButtonGroup group, boolean selected )
	{
		JRadioButton radioButton = new JRadioButton( label, selected );
		radioButton.getAccessibleContext().setAccessibleDescription( caption );
		if (group != null)
		{
			group.add( radioButton );
		}
		components.put( caption, radioButton );
		append( caption, radioButton );
		return radioButton;
	}


	public void append( String label, JComponent component )
	{
		append( label, component, null );
	}

	public JComboBox appendComboBox( String label, Map<?, ?> values )
	{
		Object[] valueArray = new Object[values.size()];
		Object[] keyArray = new Object[values.size()];

		int ix = 0;
		for( Iterator<?> i = values.keySet().iterator(); i.hasNext(); ix++ )
		{
			keyArray[ix] = i.next();
			valueArray[ix] = values.get( keyArray[ix] );
		}

		JComboBox comboBox = new JComboBox( valueArray );

		comboBoxMaps.put( comboBox, keyArray );

		append( label, comboBox );
		return comboBox;
	}

	public JComboBox appendComboBox( String label, Object[] values, String tooltip )
	{
		JComboBox comboBox = new JComboBox( values );
		comboBox.setToolTipText( tooltip );
		comboBox.getAccessibleContext().setAccessibleDescription( tooltip );
		append( label, comboBox );
		return comboBox;
	}

	public JComboBox appendComboBox( String label, ComboBoxModel model, String tooltip )
	{
		JComboBox comboBox = new JComboBox( model );
		comboBox.setToolTipText( tooltip );
		comboBox.getAccessibleContext().setAccessibleDescription( tooltip );
		append( label, comboBox );
		return comboBox;
	}

	public JButton appendButton( String label, String tooltip )
	{
		JButton button = new JButton();
		button.setToolTipText( tooltip );
		button.getAccessibleContext().setAccessibleDescription( tooltip );
		append( label, button );
		return button;
	}

	public void appendFixed( String label, JComponent component )
	{
		append( label, component, "left:pref" );
	}

	public <T extends JComponent> T append( String label, T component, String alignments )
	{
		JLabel jlabel = null;
		if( label != null )
		{
			jlabel = new JLabel( label.endsWith( ":" )|| label.isEmpty() ? label : label + ":" );
			jlabel.setBorder( BorderFactory.createEmptyBorder( 3, 0, 0, 0 ) );
			if( labelFont != null )
				jlabel.setFont( labelFont );
		}

		return append( label, jlabel, component, alignments );
	}

	public <T extends JComponent> T append( String name, JComponent label, T component, String alignments )
	{
		int spaceRowIndex = -1;

		if( rowSpacing > 0 && appended )
		{
			addSpace( rowSpacing );
			spaceRowIndex = layout.getRowCount();
		}

		layout.appendRow( rowSpec );
		int row = layout.getRowCount();

		if( label != null )
		{
			panel.add( label, cc.xy( 2, row ) );
			component.addComponentListener( new LabelHider( label, spaceRowIndex ) );
			component.addPropertyChangeListener( ENABLED_PROPERTY_NAME, new LabelEnabler( label ) );

			if( label instanceof JLabel )
			{
				JLabel jl = ( ( JLabel )label );
				jl.setLabelFor( component );
				String text = jl.getText();
				int ix = text.indexOf( '&' );
				if( ix >= 0 )
				{
					jl.setText( text.substring( 0, ix ) + text.substring( ix + 1 ) );
					jl.setDisplayedMnemonicIndex( ix );
					jl.setDisplayedMnemonic( text.charAt( ix + 1 ) );
				}

				if( component.getAccessibleContext() != null )
					component.getAccessibleContext().setAccessibleName( text );
			}
		}
		else
			component.addComponentListener( new LabelHider( null, spaceRowIndex ) );

		if( alignments == null )
			panel.add( component, cc.xy( 4, row ) );
		else
			panel.add( component, cc.xy( 4, row, alignments ) );

		components.put( name, component );
		appended = true;

		return component;
	}

	public boolean hasComponents()
	{
		return !components.isEmpty();
	}

	public void appendSeparator()
	{
		if( appended && rowSpacing > 0 )
			addSpace( rowSpacing );

		layout.appendRow( rowSpec );
		int row = layout.getRowCount();

		panel.add( new JSeparator(), cc.xywh( 2, row, 3, 1 ) );
		appended = true;
	}

	public JTextField appendTextField( String label, String tooltip )
	{
		JTextField textField = new JUndoableTextField();
		textField.setColumns( defaultTextFieldColumns );
		textField.setToolTipText( tooltip );
		textField.getAccessibleContext().setAccessibleDescription( tooltip );
		JTextComponentPopupMenu.add( textField );
		append( label, textField );
		return textField;
	}

	public JTextArea appendTextArea( String label, String tooltip )
	{
		JTextArea textArea = new JUndoableTextArea();
		textArea.setColumns( defaultTextAreaColumns );
		textArea.setRows( defaultTextAreaRows );
		textArea.setAutoscrolls( true );
		textArea.add( new JScrollPane() );
		textArea.setToolTipText( tooltip );
		textArea.getAccessibleContext().setAccessibleDescription( tooltip );
		JTextComponentPopupMenu.add( textArea );
		append( label, new JScrollPane( textArea ) );
		return textArea;
	}

	public int getDefaultTextAreaColumns()
	{
		return defaultTextAreaColumns;
	}

	public void setDefaultTextAreaColumns( int defaultTextAreaColumns )
	{
		this.defaultTextAreaColumns = defaultTextAreaColumns;
	}

	public int getDefaultTextAreaRows()
	{
		return defaultTextAreaRows;
	}

	public void setDefaultTextAreaRows( int defaultTextAreaRows )
	{
		this.defaultTextAreaRows = defaultTextAreaRows;
	}

	public JPasswordField appendPasswordField( String label, String tooltip )
	{
		JPasswordField textField = new JPasswordField();
		textField.setColumns( defaultTextFieldColumns );
		textField.setToolTipText( tooltip );
		textField.getAccessibleContext().setAccessibleDescription( tooltip );
		append( label, textField );
		return textField;
	}

	public void setComponentValue( String label, String value )
	{
		JComponent component = getComponent( label );

		if( component instanceof JScrollPane )
			component = ( JComponent )( ( JScrollPane )component ).getViewport().getComponent( 0 );

		if( component instanceof JTextComponent )
		{
			( ( JTextComponent )component ).setText( value );
		}
		else if( component instanceof JComboBox )
		{
			JComboBox comboBox = ( ( JComboBox )component );
			comboBox.setSelectedItem( value );
		}
		else if( component instanceof JList )
		{
			( ( JList )component ).setSelectedValue( value, true );
		}
		else if( component instanceof JCheckBox )
		{
			( ( JCheckBox )component ).setSelected( Boolean.valueOf( value ) );
		}
		else if( component instanceof JFormComponent )
		{
			( ( JFormComponent )component ).setValue( value );
		}
		else if( component instanceof RSyntaxTextArea )
		{
			( ( RSyntaxTextArea )component ).setText( value );
		}
	}

	public String getComponentValue( String label )
	{
		JComponent component = getComponent( label );
		if( component == null )
		{
			return ( String )( hiddenValues == null ? null : hiddenValues.get( label ) );
		}

		if( component instanceof JTextComponent )
		{
			return ( ( JTextComponent )component ).getText();
		}

		if( component instanceof JComboBox )
		{
			JComboBox comboBox = ( ( JComboBox )component );
			int selectedIndex = comboBox.getSelectedIndex();
			if( selectedIndex != -1 )
			{
				if( comboBoxMaps.containsKey( component ) )
				{
					Object[] keys = ( Object[] )comboBoxMaps.get( comboBox );
					Object value = keys[selectedIndex];
					return ( String )value == null ? null : value.toString(); // Added
					// support
					// for
					// enums
				}
				else
				{
					Object value = comboBox.getSelectedItem();
					return ( String )value == null ? null : value.toString(); // Added
					// support
					// for
					// enums
				}
			}
		}

		if( component instanceof JList )
		{
			return ( String )( ( JList )component ).getSelectedValue();
		}

		if( component instanceof JCheckBox )
		{
			return String.valueOf( ( ( JCheckBox )component ).isSelected() );
		}

		else if( component instanceof JFormComponent )
		{
			return ( ( JFormComponent )component ).getValue();
		}

		return null;
	}

	public JComponent getComponent( String label )
	{
		return ( JComponent )components.get( label );
	}

	public void setBorder( Border border )
	{
		panel.setBorder( border );
	}

	public int getRowCount()
	{
		return layout.getRowCount();
	}

	public void addComponent( JComponent component )
	{
		layout.appendRow( rowSpec );
		int row = layout.getRowCount();

		panel.add( component, cc.xyw( 2, row, 4 ) );
	}

	public void removeComponent( JComponent component )
	{
		panel.remove( component );
	}

	public void setValues( Map<String, String> values )
	{
		for( Iterator<String> i = values.keySet().iterator(); i.hasNext(); )
		{
			String key = i.next();
			setComponentValue( key, values.get( key ) );
		}
	}

	public void getValues( Map<String, String> values )
	{
		for( Iterator<String> i = components.keySet().iterator(); i.hasNext(); )
		{
			String key = i.next();
			values.put( key, getComponentValue( key ) );
		}
	}

	public void append( JComponent component )
	{
		int spaceRowIndex = -1;

		if( rowSpacing > 0 && appended )
		{
			addSpace( rowSpacing );
			spaceRowIndex = layout.getRowCount();
		}

		layout.appendRow( rowSpec );
		int row = layout.getRowCount();

		panel.add( component, cc.xyw( 2, row, 4 ) );

		component.addComponentListener( new LabelHider( null, spaceRowIndex ) );

		appended = true;
	}

	public void setDefaultTextFieldColumns( int defaultTextFieldColumns )
	{
		this.defaultTextFieldColumns = defaultTextFieldColumns;
	}

	private static class LabelEnabler implements PropertyChangeListener
	{
		private final JComponent label;

		public LabelEnabler( JComponent label )
		{
			this.label = label;
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			label.setEnabled( ( Boolean )evt.getNewValue() );
		}
	}

	private final class LabelHider extends ComponentAdapter
	{
		private final JComponent jlabel;
		private final int rowIndex;

		public LabelHider( JComponent label, int i )
		{
			this.jlabel = label;
			this.rowIndex = i;
		}

		public void componentHidden( ComponentEvent e )
		{
			if( jlabel != null )
				jlabel.setVisible( false );

			if( rowIndex >= 0 && rowIndex < layout.getRowCount() )
				layout.setRowSpec( rowIndex, new RowSpec( "0px" ) );
		}

		public void componentShown( ComponentEvent e )
		{
			if( jlabel != null )
				jlabel.setVisible( true );

			if( rowIndex >= 0 && rowIndex < layout.getRowCount() )
				layout.setRowSpec( rowIndex, new RowSpec( rowSpacing + "px" ) );
		}
	}

	public <T extends JComponent> T append( String name, JLabel label, T field )
	{
		return append( name, label, field, null );
	}

	public void setEnabled( boolean b )
	{
		for( JComponent component : components.values() )
		{
			if( component instanceof JScrollPane )
				( ( JScrollPane )component ).getViewport().getView().setEnabled( b );

			component.setEnabled( b );
		}
	}

	public <T extends JComponent> T addLeftComponent( T component )
	{
		if( rowSpacing > 0 && !components.isEmpty() )
			addSpace( rowSpacing );

		layout.appendRow( rowSpec );

		int row = layout.getRowCount();
		panel.add( component, cc.xy( 4, row ) );

		return component;
	}
}
