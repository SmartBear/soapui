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

package com.eviware.soapui.support.editor;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor-framework for Documents
 * 
 * @author ole.matzura
 */

@SuppressWarnings( "serial" )
public class Editor<T extends EditorDocument> extends JPanel implements PropertyChangeListener,
		EditorLocationListener<T>
{
	private JTabbedPane inputTabs;
	private List<EditorView<T>> views = new ArrayList<EditorView<T>>();
	private EditorView<T> currentView;
	private T document;
	private JInspectorPanel inspectorPanel;
	private InputTabsChangeListener inputTabsChangeListener;

	public Editor( T document )
	{
		super( new BorderLayout() );
		this.document = document;

		setBackground( Color.LIGHT_GRAY );
		inputTabs = new JTabbedPane( JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT );

		prettifyTabbedPaneUI();

		inputTabs.setFont( inputTabs.getFont().deriveFont( 8 ) );
		inputTabsChangeListener = new InputTabsChangeListener();
		inputTabs.addChangeListener( inputTabsChangeListener );

		inspectorPanel = JInspectorPanelFactory.build( inputTabs );
		add( inspectorPanel.getComponent(), BorderLayout.CENTER );
	}

	private void prettifyTabbedPaneUI()
	{
		if( !UISupport.isMac()  )
		{
			// For some reason the tabs get very wide in some L&Fs. Workaround is to replace the UI.
			if(inputTabs.getUI().getClass().getSimpleName().equals( "WindowsTabbedPaneUI" )){
				inputTabs.setUI( new VerticalWindowsTabbedPaneUI() );
			} else {
				inputTabs.setUI( new VerticalMetalTabbedPaneUI() );
			}
		}
	}

	public void addEditorView( EditorView<T> editorView )
	{
		views.add( editorView );

		if( UISupport.isMac() )
		{
			inputTabs.addTab( editorView.getTitle(), editorView.getComponent() );
		}
		else
		{
			inputTabs.addTab( null, new VTextIcon( inputTabs, editorView.getTitle(), VTextIcon.ROTATE_LEFT ),
					editorView.getComponent() );
		}
		editorView.addPropertyChangeListener( this );
		editorView.addLocationListener( this );

		editorView.setDocument( document );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( EditorView.TITLE_PROPERTY ) )
		{
			int ix = views.indexOf( evt.getSource() );
			if( ix == -1 )
				return;

			inputTabs.setTitleAt( ix, ( String )evt.getNewValue() );
		}
	}

	public void selectView( int viewIndex )
	{
		inputTabs.setSelectedIndex( viewIndex );
	}

	public void selectView( String viewId )
	{
		for( int c = 0; c < views.size(); c++ )
		{
			if( views.get( c ).getViewId().equals( viewId ) )
			{
				inputTabs.setSelectedIndex( c );
				return;
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	public void locationChanged( EditorLocation<T> location )
	{
		if( location != null )
		{
			for( Inspector inspector : inspectorPanel.getInspectors() )
			{
				( ( EditorInspector<T> )inspector ).locationChanged( location );
			}
		}
	}

	public void requestFocus()
	{
		if( currentView != null )
			currentView.getComponent().requestFocus();
	}

	public T getDocument()
	{
		return document;
	}

	public boolean hasFocus()
	{
		return currentView == null ? false : currentView.getComponent().hasFocus();
	}

	public final void setDocument( T document )
	{
		if( this.document != null )
			this.document.release();

		this.document = document;

		for( EditorView<T> view : views )
		{
			view.setDocument( document );
		}
	}

	public final EditorView<T> getCurrentView()
	{
		return currentView;
	}

	public final JTabbedPane getInputTabs()
	{
		return inputTabs;
	}

	public final List<EditorView<T>> getViews()
	{
		return views;
	}

	public EditorView<T> getView( String viewId )
	{
		for( EditorView<T> view : views )
		{
			if( view.getViewId().equals( viewId ) )
				return view;
		}

		return null;
	}

	public Inspector getInspector( String inspectorId )
	{
		return inspectorPanel.getInspector( inspectorId );
	}

	public void setEditable( boolean enabled )
	{
		for( EditorView<T> view : views )
		{
			view.setEditable( enabled );
		}
	}

	public void addInspector( EditorInspector<T> inspector )
	{
		inspectorPanel.addInspector( inspector );
		inspectorPanel
				.setInspectorVisible( inspector, currentView == null ? true : inspector.isEnabledFor( currentView ) );
	}

	private final class InputTabsChangeListener implements ChangeListener
	{
		private int lastDividerLocation;

		@SuppressWarnings( "unchecked" )
		public void stateChanged( ChangeEvent e )
		{
			int currentViewIndex = views.indexOf( currentView );

			if( currentView != null )
			{
				if( inputTabs.getSelectedIndex() == currentViewIndex )
					return;

				if( !currentView.deactivate() )
				{
					inputTabs.setSelectedIndex( currentViewIndex );
					return;
				}
			}

			EditorView<T> previousView = currentView;
			int selectedIndex = inputTabs.getSelectedIndex();
			if( selectedIndex == -1 )
			{
				currentView = null;
				return;
			}

			currentView = views.get( selectedIndex );

			if( currentView != null
					&& !currentView.activate( previousView == null ? null : previousView.getEditorLocation() ) )
			{
				inputTabs.setSelectedIndex( currentViewIndex );
				if( currentViewIndex == -1 )
					return;
			}

			EditorInspector<T> currentInspector = ( EditorInspector<T> )inspectorPanel.getCurrentInspector();

			if( currentInspector != null )
			{
				lastDividerLocation = inspectorPanel.getDividerLocation();
			}

			for( Inspector inspector : inspectorPanel.getInspectors() )
			{
				inspectorPanel.setInspectorVisible( inspector,
						( ( EditorInspector<T> )inspector ).isEnabledFor( currentView ) );
			}

			if( currentInspector != null && ( ( EditorInspector<T> )currentInspector ).isEnabledFor( currentView ) )
			{
				if( lastDividerLocation == 0 )
					inspectorPanel.setResetDividerLocation();
				else
					inspectorPanel.setDividerLocation( lastDividerLocation );
			}
			else
				currentInspector = null;

			SwingUtilities.invokeLater( new Runnable()
			{

				public void run()
				{
					if( currentView != null )
						currentView.getComponent().requestFocus();
				}
			} );
		}
	}

	public void release()
	{
		for( EditorView<T> view : views )
		{
			view.release();
			view.removePropertyChangeListener( this );
		}

		views.clear();

		inputTabs.removeChangeListener( inputTabsChangeListener );
		inputTabs.removeAll();

		inspectorPanel.release();
		document.release();
	}
}
