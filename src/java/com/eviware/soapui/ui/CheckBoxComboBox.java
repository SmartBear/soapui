package com.eviware.soapui.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class CheckBoxComboBox extends JComboBox
{

	private JCheckBox[] elements;

	CheckBoxElementCellRender cellRender = new CheckBoxElementCellRender( this );

	private String title;

	public CheckBoxComboBox( String title, JCheckBox... elements )
	{
		super( new String[] { title } );
		this.title = title;
		this.elements = elements;
		setRenderer( cellRender );
		BasicComboBoxUI bui = ( BasicComboBoxUI )getUI();
		
		
	}

	public class CheckBoxComboUI extends BasicComboBoxUI
	{

		JPopupMenu mm = new JPopupMenu();

		public CheckBoxComboUI()
		{
			mm.add( "XXXXXXXXXXXXXXXXx" );
		}

		@Override
		public void setPopupVisible( JComboBox c, boolean v )
		{
			if( v )
				mm.show( c, c.getX(), c.getY() + c.getHeight() );
			else
				mm.setVisible( false );
		}

		@Override
		public boolean isPopupVisible( JComboBox c )
		{
			return mm.isVisible();
		}

	}

	public Component getElementAt( int index )
	{
		if( index > -1 && index < elements.length )
			return elements[index];
		else
			return new JLabel(title);
	}

}
