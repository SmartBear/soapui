/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.eviware.soapui.SoapUI;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.Sizes;

/**
 * A simple status bar for editors
 * 
 * @author Ole.Matzura
 */

public class JEditorStatusBar extends JPanel implements CaretListener
{
	private JLabel caretLabel;
	private JLabel infoLabel;
	private JEditorStatusBarTarget target;
	private JPanel statusPanel;

	public JEditorStatusBar()
	{
		this( null );
	}

	public JEditorStatusBar( JEditorStatusBarTarget target )
	{
		this.target = target;

		caretLabel = new JLabel();
		caretLabel.setPreferredSize( new Dimension( 60, 16 ) );

		infoLabel = new JLabel();
		infoLabel.setVisible( false );

		caretLabel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 0, 1, 0, 0,
				Color.LIGHT_GRAY ), BorderFactory.createMatteBorder( 0, 1, 0, 0, Color.WHITE ) ) );

		ButtonBarBuilder builder = new ButtonBarBuilder( this );
		builder.addGriddedGrowing( infoLabel );
		builder.addStrut( Sizes.pixel( 2 ) );

		statusPanel = new JPanel( new BorderLayout() );
		statusPanel.setPreferredSize( new Dimension( 60, 16 ) );

		statusPanel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 0, 1, 0, 0,
				Color.LIGHT_GRAY ), BorderFactory.createMatteBorder( 0, 1, 0, 0, Color.WHITE ) ) );

		builder.addFixed( statusPanel );
		builder.addFixed( caretLabel );
		builder.getPanel();
	}

	public void addNotify()
	{
		super.addNotify();

		if( target != null )
			target.addCaretListener( this );
	}

	public void removeNotify()
	{
		super.removeNotify();

		if( target != null )
			target.removeCaretListener( this );
	}

	public void caretUpdate( CaretEvent e )
	{
		try
		{
			if( target == null )
				caretLabel.setText( "" );

			int offset = target.getCaretPosition();
			int line = target.getLineOfOffset( offset );
			int column = offset - target.getLineStartOffset( line );

			caretLabel.setText( " " + ( line + 1 ) + " : " + ( column + 1 ) );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}
	}

	public void setTarget( JEditorStatusBarTarget target )
	{
		if( this.target != null )
			this.target.removeCaretListener( this );

		this.target = target;
		this.target.addCaretListener( this );

		caretUpdate( null );
	}

	public void setInfo( String txt )
	{
		infoLabel.setText( txt );
		infoLabel.setVisible( txt != null );
	}

	public void setStatusComponent( JComponent statusComponent )
	{
		statusPanel.removeAll();
		statusPanel.add( statusComponent, BorderLayout.CENTER );
		statusPanel.revalidate();
	}

	/**
	 * Target for caret-status
	 * 
	 * @author Ole.Matzura
	 */

	public interface JEditorStatusBarTarget
	{
		void addCaretListener( CaretListener listener );

		int getCaretPosition();

		void removeCaretListener( CaretListener listener );

		int getLineStartOffset( int line ) throws Exception;

		int getLineOfOffset( int offset ) throws Exception;;
	}
}
