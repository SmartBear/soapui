/*
 * soapUI, copyright (C) 2004-2008 eviware.com
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

import com.eviware.soapui.config.MockOperationQueryDispatchConfig;
import com.eviware.soapui.impl.wsdl.mock.*;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.AbstractPropertyChangeNotifier;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jgoodies.binding.PresentationModel;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryMatchMockOperationDispatcher extends AbstractMockOperationDispatcher implements PropertyChangeListener
{
   private MockOperationQueryDispatchConfig conf;
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
         conf = MockOperationQueryDispatchConfig.Factory.parse( getConfig().xmlText() );

         for( MockOperationQueryDispatchConfig.Query query : conf.getQueryList() )
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
            queryDetailFormPresentationModel.setBean( (Query) itemList.getSelectedValue() );
            detailForm.setEnabled( queryDetailFormPresentationModel.getBean() != null );

            renameButton.setEnabled( queryDetailFormPresentationModel.getBean() != null );
            deleteButton.setEnabled( queryDetailFormPresentationModel.getBean() != null );
            copyButton.setEnabled( queryDetailFormPresentationModel.getBean() != null );
            extractFromCurrentButton.setEnabled( queryDetailFormPresentationModel.getBean() != null &&
                    getMockOperation().getLastMockResult() != null );
            copyButton.setEnabled( queryDetailFormPresentationModel.getBean() != null );
            runButton.setEnabled( getQueryCount() > 0 );
         }
      } );

      panel.add( buildItemsToolbar(), BorderLayout.NORTH );
      panel.add( new JScrollPane( itemList ), BorderLayout.CENTER );

      return panel;
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

      detailForm.setDefaultTextAreaRows( 8 );
      detailForm.setDefaultTextAreaColumns( 50 );

      detailForm.append( buildXPathActions() );
      detailForm.appendTextArea( "path", "XPath", "The XPath to query in the request" );
      detailForm.appendTextArea( "value", "Expected Value", "The value to match" );
      JComboBox comboBox = detailForm.appendComboBox( "response", "Dispatch to", new MockResponsesComboBoxModel(), "The MockResponse to dispatch to" );
      UISupport.setFixedSize( comboBox, 150, 20 );
      detailForm.appendCheckBox( "disabled", "Disabled", "Disables this Query" );

      detailForm.setEnabled( false );

      return new JScrollPane( detailForm.getPanel() );
   }

   private JComponent buildXPathActions()
   {
      JXToolBar toolBar = UISupport.createSmallToolbar();

      toolBar.addFixed( declareNsButton );
      toolBar.addFixed( extractFromCurrentButton );

      return toolBar;
   }

   public WsdlMockResponse selectMockResponse( WsdlMockRequest request, WsdlMockResult result ) throws DispatchException
   {
      try
      {
         XmlObject xmlObject = request.getRequestXmlObject();
         Map<String, XmlObject[]> nodesCache = new HashMap<String, XmlObject[]>();

         for( Query query : getQueries() )
         {
            if( query.isDisabled() )
               continue;

            String path = PropertyExpansionUtils.expandProperties( request.getContext(), query.getPath() );

            XmlObject[] nodes = nodesCache.containsKey( path ) ?
                    nodesCache.get( path ) : xmlObject.selectPath( path );

            if( nodes != null && nodes.length > 0 )
            {
               String value = PropertyExpansionUtils.expandProperties( request.getContext(), query.getValue() );

               if( value.equals( XmlUtils.getNodeValue( nodes[0].getDomNode() ) ) )
               {
                  return getMockOperation().getMockResponseByName( query.getResponse() );
               }
            }

            nodesCache.put( path, nodes );
         }

         return null;
      }
      catch( XmlException e )
      {
         throw new DispatchException( e );
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
      private MockOperationQueryDispatchConfig.Query config;

      protected Query( MockOperationQueryDispatchConfig.Query config )
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

      public String getPath()
      {
         return config.getPath();
      }

      public void setPath( String s )
      {
         String old = config.getPath();
         config.setPath( s );
         saveConfig();
         firePropertyChange( "path", old, s );
      }

      public String getValue()
      {
         return config.getValue();
      }

      public void setValue( String s )
      {
         String old = config.getValue();
         config.setValue( s );
         saveConfig();
         firePropertyChange( "value", old, s );
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

   private Query getSelectedQuery()
   {
      return queryDetailFormPresentationModel == null ? null :
              queryDetailFormPresentationModel.getBean();
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
         if( name == null || name.trim().length() == 0 ) return;

         addQuery( name );
         itemList.setSelectedIndex( getQueryCount() - 1 );
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
         if( name == null || name.trim().length() == 0 ) return;

         QueryMatchMockOperationDispatcher.Query query = addQuery( name );
         query.setValue( selectedQuery.getValue() );
         query.setPath( selectedQuery.getPath() );
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
         putValue( Action.SHORT_DESCRIPTION, "Declare available response/request namespaces in source/target expressions" );
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
               if( !UISupport.confirm( "Missing last result, declare from default request instead?", "Declare Namespaces" ) )
               {
                  return;
               }

               content = getMockOperation().getOperation().createRequest( true );
            }
            else
            {
               content = lastResult.getMockRequest().getRequestContent();
            }

            String path = selectedQuery.getPath();
            if( path == null )
               path = "";

            selectedQuery.setPath( XmlUtils.declareXPathNamespaces( content ) + path );
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
         putValue( Action.SHORT_DESCRIPTION, "Runs selected PropertyTransfer" );
      }

      public void actionPerformed( ActionEvent e )
      {
         WsdlMockResult result = getMockOperation().getLastMockResult();
         if( result != null )
         {
            try
            {
               UISupport.showInfoMessage( "Selected " + selectMockResponse( result.getMockRequest(), result ) );
            }
            catch( DispatchException e1 )
            {
               UISupport.showErrorMessage( e1 );
            }
         }
         else
         {
            UISupport.showErrorMessage( "Missing MockRequest to select from" );
         }
      }
   }

   private final class ExtractFromCurrentAction extends AbstractAction
   {
      public ExtractFromCurrentAction()
      {
         super( "Select" );
         putValue( Action.SHORT_DESCRIPTION, "Extracts the current value into the Value field" );
      }

      public void actionPerformed( ActionEvent e )
      {
         QueryMatchMockOperationDispatcher.Query selectedQuery = getSelectedQuery();
         if( selectedQuery == null )
            return;

         WsdlMockResult result = getMockOperation().getLastMockResult();
         if( result != null )
         {
            try
            {
               XmlObject xmlObject = XmlObject.Factory.parse( result.getMockRequest().getRequestContent() );
               selectedQuery.setValue( xmlObject.xmlText() );
            }
            catch( XmlException e1 )
            {
               e1.printStackTrace();
            }
         }
         else
         {
            UISupport.showErrorMessage( "Missing MockRequest to select from" );
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
      public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
      {
         JLabel component = (JLabel) super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

         Query query = (Query) value;
         component.setText( query.getName() );
         component.setForeground( ( (Query) value ).isDisabled() ? Color.GRAY : defaultForeground );

         return component;
      }
   }
}