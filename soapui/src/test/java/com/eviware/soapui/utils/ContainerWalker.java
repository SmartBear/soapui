package com.eviware.soapui.utils;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Helper to find Swing/AWT components in a Container.
 */
public class ContainerWalker
{

	private java.util.List<Component> containedComponents;

	public ContainerWalker( Container container )
	{
		containedComponents = findAllComponentsIn( container );
	}

	public AbstractButton findButtonWithIcon( String iconFile )
	{
		for( Component component : containedComponents )
		{
			if( component instanceof AbstractButton )
			{
				AbstractButton button = ( AbstractButton )component;
				// Hack: this relies on the toString() method of Icon to include the URL of the icon file
				if( String.valueOf( button.getIcon() ).endsWith( "/" + iconFile ) )
				{
					return button;
				}
			}
		}
		throw new NoSuchElementException( "No button found with icon file " + iconFile );
	}

	public <T> JComboBox findComboBoxWithValue( T value )
	{
		for( Component component : containedComponents )
		{
			if( component instanceof JComboBox )
			{
				JComboBox comboBox = ( JComboBox )component;
				for( int i = 0; i < comboBox.getItemCount(); i++ )
				{
					if( comboBox.getItemAt( i ).equals( value ) )
					{
						return comboBox;
					}
				}
			}
		}
		throw new NoSuchElementException( "No combo box found with item " + value );
	}

	// Currently unused, but probably useful
	public AbstractButton findCheckBoxWithLabel( String labelText )
	{
		for( Component component : containedComponents )
		{
			if( component instanceof JCheckBox )
			{
				JCheckBox checkBox = ( JCheckBox )component;
				if( String.valueOf( checkBox.getText() ).equals( labelText ) )
				{
					return checkBox;
				}
			}
		}
		throw new NoSuchElementException( "No checkbox found with label " + labelText );
	}

	private java.util.List<Component> findAllComponentsIn( Container container )
	{
		java.util.List<Component> components = new ArrayList<Component>();
		for( Component component : container.getComponents() )
		{
			components.add( component );
			if( component instanceof Container )
			{
				components.addAll( findAllComponentsIn( ( Container )component ) );
			}
		}
		return components;
	}
}
