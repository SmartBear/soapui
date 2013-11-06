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

package com.eviware.soapui.support.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;

/**
 * This is a JPanel subclass which provides a special functionality for its
 * children buttons components. It makes it possible to transfer focus from
 * button to button with help of arrows keys.
 * <p>
 * The following example shows how to enable cyclic focus transfer
 * 
 * <pre>
 * import org.jdesktop.swinghelper.buttonpanel.*;
 * import javax.swing.*;
 * 
 * public class SimpleDemo
 * {
 * 	public static void main( String[] args ) throws Exception
 * 	{
 * 		SwingUtilities.invokeLater( new Runnable()
 * 		{
 * 			public void run()
 * 			{
 * 				final JFrame frame = new JFrame();
 * 				frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 * 
 * 				JXButtonPanel panel = new JXButtonPanel();
 * 				panel.setCyclic( true );
 * 
 * 				panel.add( new JButton( &quot;One&quot; ) );
 * 				panel.add( new JButton( &quot;Two&quot; ) );
 * 				panel.add( new JButton( &quot;Three&quot; ) );
 * 
 * 				frame.add( panel );
 * 				frame.setSize( 200, 200 );
 * 				frame.setLocationRelativeTo( null );
 * 				frame.setVisible( true );
 * 			}
 * 		} );
 * 	}
 * }
 * </pre>
 * 
 * If your buttons inside JXButtonPanel are added to one ButtonGroup arrow keys
 * will transfer selection between them as well as they do it for focus
 * <p>
 * Note: you can control this behaviour with
 * setGroupSelectionFollowFocus(boolean)
 * 
 * <pre>
 * import org.jdesktop.swinghelper.buttonpanel.*;
 * import javax.swing.*;
 * 
 * public class RadioButtonDemo
 * {
 * 	public static void main( String[] args ) throws Exception
 * 	{
 * 		SwingUtilities.invokeLater( new Runnable()
 * 		{
 * 			public void run()
 * 			{
 * 				final JFrame frame = new JFrame();
 * 				frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 * 
 * 				JXButtonPanel panel = new JXButtonPanel();
 * 				ButtonGroup group = new ButtonGroup();
 * 
 * 				JRadioButton rb1 = new JRadioButton( &quot;One&quot; );
 * 				panel.add( rb1 );
 * 				group.add( rb1 );
 * 				JRadioButton rb2 = new JRadioButton( &quot;Two&quot; );
 * 				panel.add( rb2 );
 * 				group.add( rb2 );
 * 				JRadioButton rb3 = new JRadioButton( &quot;Three&quot; );
 * 				panel.add( rb3 );
 * 				group.add( rb3 );
 * 
 * 				rb1.setSelected( true );
 * 				frame.add( panel );
 * 
 * 				frame.setSize( 200, 200 );
 * 				frame.setLocationRelativeTo( null );
 * 				frame.setVisible( true );
 * 			}
 * 		} );
 * 	}
 * }
 * </pre>
 * 
 * @author Alexander Potochkin
 * 
 *         https://swinghelper.dev.java.net/
 *         http://weblogs.java.net/blog/alexfromsun/
 */
public class JXButtonPanel extends JPanel
{
	private boolean isCyclic;
	private boolean isGroupSelectionFollowFocus;

	/**
	 * {@inheritDoc}
	 */
	public JXButtonPanel()
	{
		super();
		init();
	}

	/**
	 * {@inheritDoc}
	 */
	public JXButtonPanel( LayoutManager layout )
	{
		super( layout );
		init();
	}

	/**
	 * {@inheritDoc}
	 */
	public JXButtonPanel( boolean isDoubleBuffered )
	{
		super( isDoubleBuffered );
		init();
	}

	/**
	 * {@inheritDoc}
	 */
	public JXButtonPanel( LayoutManager layout, boolean isDoubleBuffered )
	{
		super( layout, isDoubleBuffered );
		init();
	}

	private void init()
	{
		setFocusTraversalPolicyProvider( true );
		setFocusTraversalPolicy( new JXButtonPanelFocusTraversalPolicy() );
		ActionListener actionHandler = new ActionHandler();
		registerKeyboardAction( actionHandler, ActionHandler.FORWARD, KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 ),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		registerKeyboardAction( actionHandler, ActionHandler.FORWARD, KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		registerKeyboardAction( actionHandler, ActionHandler.BACKWARD, KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 ),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		registerKeyboardAction( actionHandler, ActionHandler.BACKWARD, KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		setGroupSelectionFollowFocus( true );
	}

	/**
	 * Returns whether arrow keys should support cyclic focus traversal ordering
	 * for for this JXButtonPanel.
	 */
	public boolean isCyclic()
	{
		return isCyclic;
	}

	/**
	 * Sets whether arrow keys should support cyclic focus traversal ordering for
	 * this JXButtonPanel.
	 */
	public void setCyclic( boolean isCyclic )
	{
		this.isCyclic = isCyclic;
	}

	/**
	 * Returns whether arrow keys should transfer button's selection as well as
	 * focus for this JXButtonPanel.
	 * <p>
	 * 
	 * Note: this property affects buttons which are added to a ButtonGroup
	 */
	public boolean isGroupSelectionFollowFocus()
	{
		return isGroupSelectionFollowFocus;
	}

	/**
	 * Sets whether arrow keys should transfer button's selection as well as
	 * focus for this JXButtonPanel.
	 * <p>
	 * 
	 * Note: this property affects buttons which are added to a ButtonGroup
	 */
	public void setGroupSelectionFollowFocus( boolean groupSelectionFollowFocus )
	{
		isGroupSelectionFollowFocus = groupSelectionFollowFocus;
	}

	private static ButtonGroup getButtonGroup( AbstractButton button )
	{
		ButtonModel model = button.getModel();
		if( model instanceof DefaultButtonModel )
		{
			return ( ( DefaultButtonModel )model ).getGroup();
		}
		return null;
	}

	private class ActionHandler implements ActionListener
	{
		private static final String FORWARD = "moveSelectionForward";
		private static final String BACKWARD = "moveSelectionBackward";

		public void actionPerformed( ActionEvent e )
		{
			FocusTraversalPolicy ftp = JXButtonPanel.this.getFocusTraversalPolicy();

			if( ftp instanceof JXButtonPanelFocusTraversalPolicy )
			{
				JXButtonPanelFocusTraversalPolicy xftp = ( JXButtonPanelFocusTraversalPolicy )ftp;

				String actionCommand = e.getActionCommand();
				Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				Component next;

				xftp.setAlternativeFocusMode( true );

				if( FORWARD.equals( actionCommand ) )
				{
					next = xftp.getComponentAfter( JXButtonPanel.this, fo );
				}
				else if( BACKWARD.equals( actionCommand ) )
				{
					next = xftp.getComponentBefore( JXButtonPanel.this, fo );
				}
				else
				{
					throw new AssertionError( "Unexpected action command: " + actionCommand );
				}

				xftp.setAlternativeFocusMode( false );

				if( fo instanceof AbstractButton )
				{
					AbstractButton b = ( AbstractButton )fo;
					b.getModel().setPressed( false );
				}
				if( next != null )
				{
					if( fo instanceof AbstractButton && next instanceof AbstractButton )
					{
						ButtonGroup group = getButtonGroup( ( AbstractButton )fo );
						AbstractButton nextButton = ( AbstractButton )next;
						if( group != getButtonGroup( nextButton ) )
						{
							return;
						}
						if( isGroupSelectionFollowFocus() && group != null && group.getSelection() != null
								&& !nextButton.isSelected() )
						{
							nextButton.setSelected( true );
						}
						next.requestFocusInWindow();
					}
				}
			}
		}
	}

	private class JXButtonPanelFocusTraversalPolicy extends LayoutFocusTraversalPolicy
	{
		private boolean isAlternativeFocusMode;

		public boolean isAlternativeFocusMode()
		{
			return isAlternativeFocusMode;
		}

		public void setAlternativeFocusMode( boolean alternativeFocusMode )
		{
			isAlternativeFocusMode = alternativeFocusMode;
		}

		protected boolean accept( Component c )
		{
			if( !isAlternativeFocusMode() && c instanceof AbstractButton )
			{
				AbstractButton button = ( AbstractButton )c;
				ButtonGroup group = JXButtonPanel.getButtonGroup( button );
				if( group != null && group.getSelection() != null && !button.isSelected() )
				{
					return false;
				}
			}
			return super.accept( c );
		}

		public Component getComponentAfter( Container aContainer, Component aComponent )
		{
			Component componentAfter = super.getComponentAfter( aContainer, aComponent );
			if( !isAlternativeFocusMode() )
			{
				return componentAfter;
			}
			if( JXButtonPanel.this.isCyclic() )
			{
				return componentAfter == null ? getFirstComponent( aContainer ) : componentAfter;
			}
			if( aComponent == getLastComponent( aContainer ) )
			{
				return aComponent;
			}
			return componentAfter;
		}

		public Component getComponentBefore( Container aContainer, Component aComponent )
		{
			Component componentBefore = super.getComponentBefore( aContainer, aComponent );
			if( !isAlternativeFocusMode() )
			{
				return componentBefore;
			}
			if( JXButtonPanel.this.isCyclic() )
			{
				return componentBefore == null ? getLastComponent( aContainer ) : componentBefore;
			}
			if( aComponent == getFirstComponent( aContainer ) )
			{
				return aComponent;
			}
			return componentBefore;
		}
	}
}
