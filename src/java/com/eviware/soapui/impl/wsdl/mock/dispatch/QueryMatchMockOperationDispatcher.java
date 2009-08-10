/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockOperationQueryMatchDispatchConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.AbstractPropertyChangeNotifier;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import com.jgoodies.binding.PresentationModel;

public class QueryMatchMockOperationDispatcher extends AbstractMockOperationDispatcher implements
		PropertyChangeListener
{
	private MockOperationQueryMatchDispatchConfig conf;
	private List<Query> queries = new ArrayList<Query>();
	private PresentationModel<Query> queryDetailFormPresentationModel;
	private QueryItemListModel queryItemListModel;
	private JList itemList;
	private JButton deleteButton;
	private JButton copyButton;
	private JButton renameButton;
	private SimpleBindingForm detailForm;
	private JButton runButton;
	private JButton declareNsButton = new JButton( new DeclareNamespacesAction() );
	private JButton extractFromCurrentButton = new JButton( new ExtractFromCurrentAction() );

	public QueryMatchMockOperationDispatcher( WsdlMockOperation mockOperation )
	{
		super( mockOperation );

		try
		{
			conf = MockOperationQueryMatchDispatchConfig.Factory.parse( getConfig().xmlText() );

			for( MockOperationQueryMatchDispatchConfig.Query query : conf.getQueryList() )
			{
				queries.add( new Query( query ) );
			}
		}
		catch( XmlException e )
		{
			e.printStackTrace();
		}

		mockOperation.addPropertyChangeListener( "mockResponses", this );
	}

	@Override
	public void release()
	{
		getMockOperation().removePropertyChangeListener( "mockResponses", this );
		super.release();
	}

	@Override
	public JComponent buildEditorComponent()
	{
		JSplitPane splitPane = UISupport.createHorizontalSplit( buildQueryListComponent(), buildQueryDetailComponent() );
		splitPane.setDividerLocation( 300 );
		setEnabled();
		return splitPane;
	}

	protected Component buildQueryListComponent()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		queryItemListModel = new QueryItemListModel();
		itemList = new JList( queryItemListModel );
		itemList.setCellRenderer( new QueryItemListCellRenderer() );
		itemList.addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged( ListSelectionEvent e )
			{
				queryDetailFormPresentationModel.setBean( ( Query )itemList.getSelectedValue() );
				setEnabled();
			}
		} );

		panel.add( buildItemsToolbar(), BorderLayout.NORTH );
		panel.add( new JScrollPane( itemList ), BorderLayout.CENTER );

		return panel;
	}

	protected void setEnabled()
	{
		QueryMatchMockOperationDispatcher.Query bean = queryDetailFormPresentationModel.getBean();

		detailForm.setEnabled( bean != null );
		renameButton.setEnabled( bean != null );
		deleteButton.setEnabled( bean != null );
		copyButton.setEnabled( bean != null );
		extractFromCurrentButton.setEnabled( bean != null );
		copyButton.setEnabled( bean != null );
		declareNsButton.setEnabled( bean != null );
		runButton.setEnabled( getQueryCount() > 0 );
	}

	private JXToolBar buildItemsToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		runButton = UISupport.createToolbarButton( new RunAction() );
		toolbar.addFixed( runButton );
		toolbar.addSeparator();

		toolbar.addFixed( UISupport.createToolbarButton( new AddAction() ) );
		deleteButton = UISupport.createToolbarButton( new DeleteAction() );
		deleteButton.setEnabled( false );
		toolbar.addFixed( deleteButton );
		toolbar.addSeparator();
		copyButton = UISupport.createToolbarButton( new CopyAction() );
		copyButton.setEnabled( false );
		toolbar.addFixed( copyButton );
		renameButton = UISupport.createToolbarButton( new RenameAction() );
		renameButton.setEnabled( false );
		toolbar.addFixed( renameButton );

		toolbar.addSeparator();

		return toolbar;
	}

	protected Component buildQueryDetailComponent()
	{
		queryDetailFormPresentationModel = new PresentationModel<Query>( null );
		detailForm = new SimpleBindingForm( queryDetailFormPresentationModel );

		detailForm.setDefaultTextAreaRows( 5 );
		detailForm.setDefaultTextAreaColumns( 50 );

		detailForm.append( buildQueryToolbar() );
		detailForm.appendTextArea( "query", "XPath", "The XPath to query in the request" );
		detailForm.appendTextArea( "match", "Expected Value", "The value to match" );
		JComboBox comboBox = detailForm.appendComboBox( "response", "Dispatch to", new MockResponsesComboBoxModel(),
				"The MockResponse to dispatch to" );
		UISupport.setFixedSize( comboBox, 150, 20 );
		detailForm.appendCheckBox( "disabled", "Disabled", "Disables this Query" );

		return new JScrollPane( detailForm.getPanel() );
	}

	protected JXToolBar buildQueryToolbar()
	{
		JXToolBar toolBar = UISupport.createSmallToolbar();

		addlQueryToolbarActions( toolBar );

		toolBar.addGlue();
		toolBar.addFixed( ModelItemDesktopPanel.createActionButton( new ShowOnlineHelpAction(
				HelpUrls.MOCKOPERATION_QUERYMATCHDISPATCH_HELP_URL ), true ) );

		return toolBar;
	}

	protected void addlQueryToolbarActions( JXToolBar toolBar )
	{
		toolBar.addFixed( declareNsButton );
		toolBar.addFixed( extractFromCurrentButton );
	}

	public WsdlMockResponse selectMockResponse( WsdlMockRequest request, WsdlMockResult result )
			throws DispatchException
	{
		Map<String, XmlCursor> cursorCache = new HashMap<String, XmlCursor>();

		try
		{
			XmlObject xmlObject = request.getRequestXmlObject();

			for( Query query : getQueries() )
			{
				if( query.isDisabled() )
					continue;

				String path = PropertyExpander.expandProperties( request.getContext(), query.getQuery() );
				if( StringUtils.hasContent( path ) )
				{
					XmlCursor cursor = cursorCache.get( path );
					if( cursor == null && !cursorCache.containsKey( path ) )
					{
						cursor = xmlObject.newCursor();
						cursor.selectPath( path );
						if( !cursor.toNextSelection() )
						{
							cursor.dispose();
							cursor = null;
						}
					}

					if( cursor != null )
					{
						String value = PropertyExpander.expandProperties( request.getContext(), query.getMatch() );

						if( value.equals( XmlUtils.getValueForMatch( cursor ) ) )
						{
							return getMockOperation().getMockResponseByName( query.getResponse() );
						}
					}

					cursorCache.put( path, cursor );
				}
			}

			return null;
		}
		catch( Throwable e )
		{
			throw new DispatchException( e );
		}
		finally
		{
			for( XmlCursor cursor : cursorCache.values() )
			{
				if( cursor != null )
				{
					cursor.dispose();
				}
			}
		}
	}

	public Query addQuery( String name )
	{
		Query query = new Query( conf.addNewQuery() );
		query.setName( name );
		queries.add( query );

		getPropertyChangeSupport().firePropertyChange( "queries", null, query );

		if( queryItemListModel != null )
			queryItemListModel.fireAdded();

		return query;
	}

	public void deleteQuery( Query query )
	{
		int ix = queries.indexOf( query );
		queries.remove( ix );
		getPropertyChangeSupport().firePropertyChange( "queries", query, null );

		if( queryItemListModel != null )
			queryItemListModel.fireRemoved( ix );

		conf.removeQuery( ix );
		saveConfig();
	}

	public Query[] getQueries()
	{
		return queries.toArray( new Query[queries.size()] );
	}

	public int getQueryCount()
	{
		return queries.size();
	}

	public Query getQueryAt( int index )
	{
		return queries.get( index );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( queryItemListModel != null )
			queryItemListModel.refresh();
	}

	public static class Factory implements MockOperationDispatchFactory
	{
		public MockOperationDispatcher build( WsdlMockOperation mockOperation )
		{
			return new QueryMatchMockOperationDispatcher( mockOperation );
		}
	}

	public class Query extends AbstractPropertyChangeNotifier
	{
		private MockOperationQueryMatchDispatchConfig.Query config;

		protected Query( MockOperationQueryMatchDispatchConfig.Query config )
		{
			this.config = config;
		}

		public String getName()
		{
			return config.getName();
		}

		public void setName( String s )
		{
			String old = config.getName();
			config.setName( s );
			saveConfig();
			firePropertyChange( "name", old, s );
		}

		public boolean isDisabled()
		{
			return config.getDisabled();
		}

		public void setDisabled( boolean disabled )
		{
			boolean old = config.getDisabled();
			if( old == disabled )
				return;
			config.setDisabled( disabled );
			saveConfig();
			firePropertyChange( "disabled", old, disabled );
			queryItemListModel.refresh();
		}

		public String getQuery()
		{
			return config.getQuery();
		}

		public void setQuery( String s )
		{
			String old = config.getQuery();
			config.setQuery( s );
			saveConfig();
			firePropertyChange( "query", old, s );
		}

		public String getMatch()
		{
			return config.getMatch();
		}

		public void setMatch( String s )
		{
			String old = config.getMatch();
			config.setMatch( s );
			saveConfig();
			firePropertyChange( "match", old, s );
		}

		public String getResponse()
		{
			return config.getResponse();
		}

		public void setResponse( String s )
		{
			String old = config.getResponse();
			config.setResponse( s );
			saveConfig();
			firePropertyChange( "response", old, s );
		}
	}

	private void saveConfig()
	{
		saveConfig( conf );
	}

	private class QueryItemListModel extends AbstractListModel
	{
		public int getSize()
		{
			return getQueryCount();
		}

		public Object getElementAt( int index )
		{
			return getQueryAt( index );
		}

		public void refresh()
		{
			fireContentsChanged( this, 0, getQueryCount() );
		}

		public void fireAdded()
		{
			fireIntervalAdded( this, getQueryCount(), getQueryCount() );
		}

		public void fireRemoved( int index )
		{
			fireIntervalRemoved( this, index, index );
		}
	}

	private class MockResponsesComboBoxModel extends AbstractListModel implements ComboBoxModel
	{
		public int getSize()
		{
			return getMockOperation().getMockResponseCount();
		}

		public Object getElementAt( int index )
		{
			return getMockOperation().getMockResponseAt( index ).getName();
		}

		public void setSelectedItem( Object anItem )
		{
			Query query = getSelectedQuery();
			if( query != null )
				query.setResponse( String.valueOf( anItem ) );
		}

		public Object getSelectedItem()
		{
			Query query = getSelectedQuery();
			return query != null ? query.getResponse() : null;
		}
	}

	protected Query getSelectedQuery()
	{
		return queryDetailFormPresentationModel == null ? null : queryDetailFormPresentationModel.getBean();
	}

	private final class AddAction extends AbstractAction
	{
		public AddAction()
		{
			putValue( Action.SHORT_DESCRIPTION, "Adds a new Match" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			String name = UISupport.prompt( "Specify name for Match", "Add Query Match", "" );
			if( name == null || name.trim().length() == 0 )
				return;

			addQuery( name );
		}
	}

	private final class CopyAction extends AbstractAction
	{
		public CopyAction()
		{
			putValue( Action.SHORT_DESCRIPTION, "Copies the selected Match" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clone_request.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			QueryMatchMockOperationDispatcher.Query selectedQuery = getSelectedQuery();
			if( selectedQuery == null )
				return;

			String name = UISupport.prompt( "Specify name for copied Query", "Copy Query", selectedQuery.getName() );
			if( name == null || name.trim().length() == 0 )
				return;

			QueryMatchMockOperationDispatcher.Query query = addQuery( name );
			query.setMatch( selectedQuery.getMatch() );
			query.setQuery( selectedQuery.getQuery() );
			query.setResponse( selectedQuery.getResponse() );

			itemList.setSelectedIndex( getQueryCount() - 1 );
		}
	}

	private final class DeleteAction extends AbstractAction
	{
		public DeleteAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Deletes the selected Property Transfer" );
		}

		public void actionPerformed( ActionEvent e )
		{
			QueryMatchMockOperationDispatcher.Query selectedQuery = getSelectedQuery();
			if( selectedQuery == null )
				return;

			if( UISupport.confirm( "Delete selected Query", "Delete Query" ) )
			{
				deleteQuery( selectedQuery );
				if( getQueryCount() > 0 )
					itemList.setSelectedIndex( 0 );
			}
		}
	}

	private final class RenameAction extends AbstractAction
	{
		public RenameAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/rename.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Renames the selected Property Transfer" );
		}

		public void actionPerformed( ActionEvent e )
		{
			QueryMatchMockOperationDispatcher.Query selectedQuery = getSelectedQuery();
			if( selectedQuery == null )
				return;

			String newName = UISupport.prompt( "Specify new name for Query", "Rename Query", selectedQuery.getName() );

			if( newName != null && !selectedQuery.getName().equals( newName ) )
			{
				selectedQuery.setName( newName );
				queryItemListModel.refresh();
			}
		}
	}

	private final class DeclareNamespacesAction extends AbstractAction
	{
		public DeclareNamespacesAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/declareNs.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Declare request namespaces in current query" );
		}

		public void actionPerformed( ActionEvent e )
		{
			QueryMatchMockOperationDispatcher.Query selectedQuery = getSelectedQuery();
			if( selectedQuery == null )
				return;

			try
			{
				WsdlMockResult lastResult = getMockOperation().getLastMockResult();
				String content = null;
				if( lastResult == null )
				{
					if( !UISupport.confirm( "Missing last result, declare from default request instead?",
							"Declare Namespaces" ) )
					{
						return;
					}

					content = getMockOperation().getOperation().createRequest( true );
				}
				else
				{
					content = lastResult.getMockRequest().getRequestContent();
				}

				String path = selectedQuery.getQuery();
				if( path == null )
					path = "";

				selectedQuery.setQuery( XmlUtils.declareXPathNamespaces( content ) + path );
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
	}

	private final class RunAction extends AbstractAction
	{
		public RunAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs Queries on last request" );
		}

		public void actionPerformed( ActionEvent e )
		{
			WsdlMockResult result = getMockOperation().getLastMockResult();
			if( result != null )
			{
				try
				{
					UISupport.showInfoMessage( "Selected ["
							+ selectMockResponse( result.getMockRequest(), result ).getName() + "]" );
				}
				catch( DispatchException e1 )
				{
					UISupport.showErrorMessage( e1 );
				}
			}
			else
			{
				UISupport.showErrorMessage( "Missing request to query" );
			}
		}
	}

	private final class ExtractFromCurrentAction extends AbstractAction
	{
		public ExtractFromCurrentAction()
		{
			super( "Extract" );
			putValue( Action.SHORT_DESCRIPTION, "Extracts the current value into the Value field" );
		}

		public void actionPerformed( ActionEvent e )
		{
			QueryMatchMockOperationDispatcher.Query selectedQuery = getSelectedQuery();
			if( selectedQuery == null )
				return;

			WsdlMockResult result = getMockOperation().getLastMockResult();
			String content;

			if( result != null && StringUtils.hasContent( result.getMockRequest().getRequestContent() ) )
			{
				content = result.getMockRequest().getRequestContent();
			}
			else
			{
				if( !UISupport.confirm( "Missing last result, extract from default request instead?", "Extract Match" ) )
				{
					return;
				}

				content = getMockOperation().getOperation().createRequest( true );
			}

			XmlCursor cursor = null;

			try
			{
				XmlObject xmlObject = XmlObject.Factory.parse( content );
				cursor = xmlObject.newCursor();
				cursor.selectPath( selectedQuery.getQuery() );
				if( !cursor.toNextSelection() )
				{
					UISupport.showErrorMessage( "Missing match in request" );
				}
				else
				{
					selectedQuery.setMatch( XmlUtils.getValueForMatch( cursor ) );
				}
			}
			catch( Throwable e1 )
			{
				SoapUI.logError( e1 );
			}
			finally
			{
				if( cursor != null )
				{
					cursor.dispose();
				}
			}
		}
	}

	private class QueryItemListCellRenderer extends DefaultListCellRenderer
	{
		private Color defaultForeground;

		private QueryItemListCellRenderer()
		{
			this.defaultForeground = getForeground();
		}

		@Override
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			JLabel component = ( JLabel )super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			Query query = ( Query )value;
			component.setText( query.getName() );
			component.setForeground( ( ( Query )value ).isDisabled() ? Color.GRAY : defaultForeground );

			return component;
		}
	}
}