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

package com.eviware.soapui.ui.desktop.standalone;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.util.PanelBuilderRegistry;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.ui.URLDesktopPanel;
import com.eviware.soapui.ui.desktop.AbstractSoapUIDesktop;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The default standalone SoapUI desktop using a JDesktopPane
 *
 * @author Ole.Matzura
 */

public class StandaloneDesktop extends AbstractSoapUIDesktop
{
	private JDesktopPane desktop;
	private Map<ModelItem, JInternalFrame> modelItemToInternalFrameMap = new HashMap<ModelItem, JInternalFrame>();
	private Map<JInternalFrame, DesktopPanel> internalFrameToDesktopPanelMap = new HashMap<JInternalFrame, DesktopPanel>();
	private DesktopPanelPropertyChangeListener desktopPanelPropertyChangeListener = new DesktopPanelPropertyChangeListener();
	private InternalDesktopFrameListener internalFrameListener = new InternalDesktopFrameListener();
	private ActionList actions;

	private DesktopPanel currentPanel;

	private CloseCurrentAction closeCurrentAction = new CloseCurrentAction();
	private CloseOtherAction closeOtherAction = new CloseOtherAction();
	private CloseAllAction closeAllAction = new CloseAllAction();

	private static int openFrameCount = 0;
	private static final int xOffset = 30, yOffset = 30;
	private JPanel desktopPanel = new JPanel( new BorderLayout() );

	private boolean transferring;

	private List<DesktopPanel> deferredDesktopPanels = new LinkedList<DesktopPanel>();

	public StandaloneDesktop( Workspace workspace )
	{
		super( workspace );

		buildUI();

		actions = new DefaultActionList( "Desktop" );
		actions.addAction( closeCurrentAction );
		actions.addAction( closeOtherAction );
		actions.addAction( closeAllAction );

		// Setting Mac-like color for all platforms pending 
		desktop.setBackground( UISupport.MAC_BACKGROUND_COLOR );

		enableWindowActions();
		desktop.addComponentListener( new DesktopResizeListener() );
	}

	private void enableWindowActions()
	{
		closeCurrentAction.setEnabled( currentPanel != null && internalFrameToDesktopPanelMap.size() > 0 );
		closeOtherAction.setEnabled( currentPanel != null && internalFrameToDesktopPanelMap.size() > 1 );
		closeAllAction.setEnabled( internalFrameToDesktopPanelMap.size() > 0 );
	}

	private void buildUI()
	{
		desktop = new SoapUIDesktopPane();
		JScrollPane scrollPane = new JScrollPane( desktop );
		desktopPanel.add( scrollPane, BorderLayout.CENTER );
	}

	public JComponent getDesktopComponent()
	{
		return desktopPanel;
	}

	public boolean closeDesktopPanel( DesktopPanel desktopPanel )
	{
		try
		{
			if( desktopPanel.getModelItem() != null )
			{
				return closeDesktopPanel( desktopPanel.getModelItem() );
			}
			else
			{
				JInternalFrame frame = getFrameForDesktopPanel( desktopPanel );
				if( frame != null )
				{
					frame.doDefaultCloseAction();
					return frame.isClosed();
				}
				// else
				// throw new RuntimeException( "Cannot close unkown DesktopPanel: "
				// + desktopPanel.getTitle() );

				return false;
			}
		} finally
		{
			enableWindowActions();
		}
	}

	private JInternalFrame getFrameForDesktopPanel( DesktopPanel desktopPanel )
	{
		for( JInternalFrame frame : internalFrameToDesktopPanelMap.keySet() )
		{
			if( internalFrameToDesktopPanelMap.get( frame ) == desktopPanel )
			{
				return frame;
			}
		}

		return null;
	}

	public boolean hasDesktopPanel( ModelItem modelItem )
	{
		return modelItemToInternalFrameMap.containsKey( modelItem );
	}

	public DesktopPanel showDesktopPanel( ModelItem modelItem )
	{
		PanelBuilder<ModelItem> panelBuilder = PanelBuilderRegistry.getPanelBuilder( modelItem );
		if( modelItemToInternalFrameMap.containsKey( modelItem ) )
		{
			JInternalFrame frame = modelItemToInternalFrameMap.get( modelItem );
			try
			{
				desktop.getDesktopManager().deiconifyFrame( frame );
				frame.setSelected( true );
				frame.moveToFront();
				currentPanel = internalFrameToDesktopPanelMap.get( frame );
			}
			catch( PropertyVetoException e )
			{
				SoapUI.logError( e );
			}
		}
		else if( panelBuilder != null && panelBuilder.hasDesktopPanel() )
		{
			DesktopPanel desktopPanel = panelBuilder.buildDesktopPanel( modelItem );
			if( desktopPanel == null )
				return null;

			JInternalFrame frame = createContentFrame( desktopPanel );

			desktop.add( frame );
			try
			{
				frame.setSelected( true );
			}
			catch( PropertyVetoException e )
			{
				SoapUI.logError( e );
			}

			modelItemToInternalFrameMap.put( modelItem, frame );
			internalFrameToDesktopPanelMap.put( frame, desktopPanel );

			fireDesktopPanelCreated( desktopPanel );

			currentPanel = desktopPanel;
			desktopPanel.getComponent().requestFocusInWindow();
		}
		else
			Toolkit.getDefaultToolkit().beep();

		enableWindowActions();

		return currentPanel;
	}

	private JInternalFrame createContentFrame( DesktopPanel desktopPanel )
	{
		desktopPanel.addPropertyChangeListener( desktopPanelPropertyChangeListener );

		JComponent panel = desktopPanel.getComponent();
		panel.setOpaque( true );

		String title = desktopPanel.getTitle();

		JInternalFrame frame = new JInternalFrame( title, true, true, true, true );
		frame.addInternalFrameListener( internalFrameListener );
		frame.setContentPane( panel );
		frame.setLocation( xOffset * ( openFrameCount % 10 ), yOffset * ( openFrameCount % 10 ) );
		Point location = frame.getLocation();
		Dimension frameSize = calculateDesktopPanelSize( panel, location );
		frame.setSize( frameSize );
		frame.setVisible( true );
		frame.setFrameIcon( desktopPanel.getIcon() );
		frame.setToolTipText( desktopPanel.getDescription() );
		frame.setDefaultCloseOperation( JInternalFrame.DO_NOTHING_ON_CLOSE );
		if( !SoapUI.getSettings().getBoolean( UISettings.NATIVE_LAF ) )
		{
			// This creates an empty frame on Mac OS X native L&F.
			frame.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createRaisedBevelBorder(),
					BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) ) );
		}
		openFrameCount++;
		return frame;
	}

	private Dimension calculateDesktopPanelSize( JComponent panel, Point location )
	{
		Dimension frameSize;
		Dimension preferredSize = panel.getPreferredSize();
		if( desktop.getBounds().contains( new Rectangle( location, preferredSize ) ) )
		{
			frameSize = preferredSize;
		}
		else
		{
			frameSize = new Dimension( ( int )( ( desktop.getWidth() - location.x ) * .95 ),
					( int )( ( desktop.getHeight() - location.y ) * .95 ) );
		}
		return frameSize;
	}

	public boolean closeDesktopPanel( ModelItem modelItem )
	{
		try
		{
			if( modelItemToInternalFrameMap.containsKey( modelItem ) )
			{
				JInternalFrame frame = modelItemToInternalFrameMap.get( modelItem );
				frame.doDefaultCloseAction();
				return frame.isClosed();
			}

			return false;
		} finally
		{
			enableWindowActions();
		}
	}

	private class InternalDesktopFrameListener extends InternalFrameAdapter
	{
		public void internalFrameClosing( InternalFrameEvent e )
		{
			DesktopPanel desktopPanel = internalFrameToDesktopPanelMap.get( e.getInternalFrame() );
			if( !transferring && !desktopPanel.onClose( true ) )
			{
				return;
			}

			desktopPanel.removePropertyChangeListener( desktopPanelPropertyChangeListener );

			modelItemToInternalFrameMap.remove( desktopPanel.getModelItem() );
			internalFrameToDesktopPanelMap.remove( e.getInternalFrame() );

			// replace content frame to make sure it is released
			e.getInternalFrame().setContentPane( new JPanel() );
			e.getInternalFrame().dispose();

			if( !transferring )
				fireDesktopPanelClosed( desktopPanel );

			if( currentPanel == desktopPanel )
				currentPanel = null;
		}

		public void internalFrameActivated( InternalFrameEvent e )
		{
			currentPanel = internalFrameToDesktopPanelMap.get( e.getInternalFrame() );
			if( currentPanel != null )
			{
				fireDesktopPanelSelected( currentPanel );
			}

			enableWindowActions();
		}

		public void internalFrameDeactivated( InternalFrameEvent e )
		{
			currentPanel = null;
			enableWindowActions();
		}
	}

	public class CloseCurrentAction extends AbstractAction
	{
		public CloseCurrentAction()
		{
			super( "Close Current" );
			putValue( Action.SHORT_DESCRIPTION, "Closes the current window" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu F4" ) );

		}

		public void actionPerformed( ActionEvent e )
		{
			JInternalFrame frame = desktop.getSelectedFrame();
			if( frame != null )
				closeDesktopPanel( internalFrameToDesktopPanelMap.get( frame ) );
		}
	}

	public class CloseOtherAction extends AbstractAction
	{
		public CloseOtherAction()
		{
			super( "Close Others" );
			putValue( Action.SHORT_DESCRIPTION, "Closes all windows except the current one" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu alt O" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			JInternalFrame frame = desktop.getSelectedFrame();
			if( frame == null )
				return;

			JInternalFrame[] frames = internalFrameToDesktopPanelMap.keySet().toArray(
					new JInternalFrame[internalFrameToDesktopPanelMap.size()] );
			for( JInternalFrame f : frames )
			{
				if( f != frame )
				{
					closeDesktopPanel( internalFrameToDesktopPanelMap.get( f ) );
				}
			}
		}
	}

	public class CloseAllAction extends AbstractAction
	{
		public CloseAllAction()
		{
			super( "Close All" );
			putValue( Action.SHORT_DESCRIPTION, "Closes all windows" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu alt L" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			closeAll();
		}
	}

	public ActionList getActions()
	{
		return actions;
	}

	private class DesktopPanelPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			DesktopPanel desktopPanel = ( DesktopPanel )evt.getSource();
			JInternalFrame frame = getFrameForDesktopPanel( desktopPanel );
			if( frame != null )
			{
				if( evt.getPropertyName().equals( DesktopPanel.TITLE_PROPERTY ) )
				{
					frame.setTitle( desktopPanel.getTitle() );
				}
				else if( evt.getPropertyName().equals( DesktopPanel.ICON_PROPERTY ) )
				{
					frame.setFrameIcon( desktopPanel.getIcon() );
				}
			}
		}
	}

	public DesktopPanel[] getDesktopPanels()
	{
		return internalFrameToDesktopPanelMap.values().toArray( new DesktopPanel[internalFrameToDesktopPanelMap.size()] );
	}

	public DesktopPanel getDesktopPanel( ModelItem modelItem )
	{
		for( DesktopPanel panel : internalFrameToDesktopPanelMap.values() )
		{
			if (panel.getModelItem() == modelItem)
			{
				return panel;
			}
		}
		return null;
	}

	public DesktopPanel showDesktopPanel( DesktopPanel desktopPanel )
	{
		if( desktop.getBounds().width == 0 )
		{
			deferredDesktopPanels.add( desktopPanel );
			return desktopPanel;
		}
		JInternalFrame frame = getFrameForDesktopPanel( desktopPanel );
		if( frame != null )
		{
			try
			{
				desktop.getDesktopManager().deiconifyFrame( frame );
				frame.setSelected( true );
				frame.moveToFront();
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
		else
		{
			frame = createContentFrame( desktopPanel );
			desktop.add( frame );

			if( desktopPanel.getModelItem() != null )
				modelItemToInternalFrameMap.put( desktopPanel.getModelItem(), frame );

			internalFrameToDesktopPanelMap.put( frame, desktopPanel );
			fireDesktopPanelCreated( desktopPanel );
			frame.moveToFront();
			desktopPanel.getComponent().requestFocusInWindow();
		}

		currentPanel = desktopPanel;
		enableWindowActions();

		return desktopPanel;
	}

	class SoapUIDesktopPane extends JDesktopPane
	{
		Image img;
		private int imageWidth;
		private int imageHeight;

		public SoapUIDesktopPane()
		{
			try
			{
				File file = new File( "soapui-background.gif" );
				if( !file.exists() )
					file = new File( "soapui-background.jpg" );
				if( !file.exists() )
					file = new File( "soapui-background.png" );

				if( file.exists() )
				{
					img = javax.imageio.ImageIO.read( file );
					imageWidth = img.getWidth( this );
					imageHeight = img.getHeight( this );
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e, "Could not load graphics for desktop" );
			}
		}

		public void paintComponent( Graphics g )
		{
			super.paintComponent( g );

			if( img == null )
				return;

			int x = ( this.getWidth() - imageWidth ) / 2;
			int y = ( this.getHeight() - imageHeight ) / 2;

			g.drawImage( img, x, y, imageWidth, imageHeight, this );
		}
	}

	public void transferTo( SoapUIDesktop newDesktop )
	{
		transferring = true;

		List<DesktopPanel> values = new ArrayList<DesktopPanel>( internalFrameToDesktopPanelMap.values() );
		for( DesktopPanel desktopPanel : values )
		{
			closeDesktopPanel( desktopPanel );
			newDesktop.showDesktopPanel( desktopPanel );
		}

		transferring = false;
	}

	public boolean closeAll()
	{
		while( internalFrameToDesktopPanelMap.size() > 0 )
		{
			Iterator<JInternalFrame> i = internalFrameToDesktopPanelMap.keySet().iterator();
			try
			{
				i.next().setClosed( true );
			}
			catch( PropertyVetoException e1 )
			{
				SoapUI.logError( e1 );
			}
		}

		internalFrameToDesktopPanelMap.clear();
		modelItemToInternalFrameMap.clear();

		JInternalFrame[] allFrames = desktop.getAllFrames();
		for( JInternalFrame frame : allFrames )
		{
			frame.doDefaultCloseAction();
		}

		enableWindowActions();
		return true;
	}

	public void minimize( DesktopPanel desktopPanel )
	{
		try
		{
			getFrameForDesktopPanel( desktopPanel ).setIcon( true );
		}
		catch( PropertyVetoException e )
		{
			SoapUI.logError( e );
		}
	}

	public void maximize( DesktopPanel desktopPanel )
	{
		desktop.getDesktopManager().maximizeFrame( getFrameForDesktopPanel( desktopPanel ) );
	}


	/**
	 * Helper class that ensures that desktop panels are displayed after a change from Tabbed to Standalone desktop.
	 */
	private class DesktopResizeListener implements ComponentListener
	{
		@Override
		public void componentResized( ComponentEvent e )
		{
			Iterator<DesktopPanel> iterator = deferredDesktopPanels.iterator();
			while( iterator.hasNext() )
			{
				DesktopPanel nextPanel = iterator.next();
				//Workaround: Avoid JXBrowser problems on Mac
				if( !( UISupport.isMac() && nextPanel instanceof URLDesktopPanel ) )
				{
					showDesktopPanel( nextPanel );
				}
				iterator.remove();
			}
		}

		@Override
		public void componentMoved( ComponentEvent e )
		{

		}

		@Override
		public void componentShown( ComponentEvent e )
		{
		}

		@Override
		public void componentHidden( ComponentEvent e )
		{

		}
	}
}
