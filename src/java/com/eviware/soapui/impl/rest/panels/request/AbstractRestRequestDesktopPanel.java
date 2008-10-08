/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest.RequestMethod;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.panels.AbstractHttpRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class AbstractRestRequestDesktopPanel<T extends ModelItem, T2 extends RestRequest>
        extends AbstractHttpRequestDesktopPanel<T, T2>
{
   private boolean updatingRequest;
   private JComboBox methodCombo;
   private JUndoableTextField pathTextField;
   private JComboBox acceptCombo;
   private JLabel pathLabel;
   private boolean updating;
   // private JButton recreatePathButton;

   public AbstractRestRequestDesktopPanel( T modelItem, T2 requestItem )
   {
      super( modelItem, requestItem );

      if( requestItem.getResource() != null )
         requestItem.getResource().addPropertyChangeListener( this );
   }

   public void propertyChange( PropertyChangeEvent evt )
   {
      if( evt.getPropertyName().equals( "method" ) && !updatingRequest )
      {
         methodCombo.setSelectedItem( evt.getNewValue() );
      }
      else if( evt.getPropertyName().equals( "accept" ) && !updatingRequest )
      {
         acceptCombo.setSelectedItem( evt.getNewValue() );
      }
      else if( evt.getPropertyName().equals( "responseMediaTypes" ) && !updatingRequest )
      {
         Object item = acceptCombo.getSelectedItem();
         acceptCombo.setModel( new DefaultComboBoxModel( ( Object[] ) evt.getNewValue() ) );
         acceptCombo.setSelectedItem( item );
      }
      else if( evt.getPropertyName().equals( "path" ) &&
              ( getRequest().getResource() == null || getRequest().getResource() == evt.getSource() ) )
      {
         if( pathLabel != null )
         {
            pathLabel.setText( getRequest().getResource().getFullPath() );
         }

         if( !updating )
         {
            updating = true;
            pathTextField.setText( ( String ) evt.getNewValue() );
            updating = false;
         }
      }

      super.propertyChange( evt );
   }

   @Override
   protected ModelItemXmlEditor<?, ?> buildRequestEditor()
   {
      return new RestRequestMessageEditor( getRequest() );
   }

   @Override
   protected ModelItemXmlEditor<?, ?> buildResponseEditor()
   {
      return new RestResponseMessageEditor( getRequest() );
   }

   @Override
   protected Submit doSubmit() throws SubmitException
   {
      return getRequest().submit( new WsdlSubmitContext( getModelItem() ), true );
   }

   

   @Override
   protected String getHelpUrl()
   {
      return null;
   }

   @Override
   protected void insertButtons( JXToolBar toolbar )
   {
      if( getRequest().getResource() == null )
      {
         addToolbarComponents( toolbar );
      }
   }

   protected JComponent buildEndpointComponent()
   {
      return getRequest().getResource() == null ? null : super.buildEndpointComponent();
   }

   @Override
   protected JComponent buildToolbar()
   {
      if( getRequest().getResource() != null )
      {
         JPanel panel = new JPanel( new BorderLayout() );
         panel.add( super.buildToolbar(), BorderLayout.NORTH );

         JXToolBar toolbar = UISupport.createToolbar();
         addToolbarComponents( toolbar );

         panel.add( toolbar, BorderLayout.SOUTH );
         return panel;
      }
      else
      {
         return super.buildToolbar();
      }
   }

   protected void addToolbarComponents( JXToolBar toolbar )
   {
      toolbar.addSeparator();
      methodCombo = new JComboBox( new Object[]{RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
              RequestMethod.DELETE, RequestMethod.HEAD} );

      methodCombo.setSelectedItem( getRequest().getMethod() );
      methodCombo.setToolTipText( "Set desired HTTP method" );
      methodCombo.addItemListener( new ItemListener()
      {

         public void itemStateChanged( ItemEvent e )
         {
            updatingRequest = true;
            getRequest().setMethod( ( RequestMethod ) methodCombo.getSelectedItem() );
            updatingRequest = false;
         }
      } );

      toolbar.addLabeledFixed( "Method", methodCombo );
      toolbar.addSeparator();

      if( getRequest().getResource() != null )
      {
         acceptCombo = new JComboBox( getRequest().getResponseMediaTypes() );
         acceptCombo.setEditable( true );
         acceptCombo.setToolTipText( "Sets accepted encoding(s) for response" );
         acceptCombo.setSelectedItem( getRequest().getAccept() );
         acceptCombo.addItemListener( new ItemListener()
         {
            public void itemStateChanged( ItemEvent e )
            {
               updatingRequest = true;
               getRequest().setAccept( String.valueOf( acceptCombo.getSelectedItem() ) );
               updatingRequest = false;
            }
         } );

         toolbar.addLabeledFixed( "Accept", acceptCombo );
         toolbar.addSeparator();

         pathTextField = new JUndoableTextField();
         pathTextField.setPreferredSize( new Dimension( 200, 20 ) );
         pathTextField.setText( getRequest().getResource().getPath() );
         pathTextField.getDocument().addDocumentListener( new DocumentListenerAdapter()
         {
            @Override
            public void update( Document document )
            {
               if( updating )
                  return;

               updating = true;
               getRequest().getResource().setPath( pathTextField.getText() );
               updating = false;
            }
         } );

         toolbar.addLabeledFixed( "Resource Path:", pathTextField );

         pathLabel = new JLabel( getRequest().getResource().getFullPath() );
         pathLabel.setPreferredSize( new Dimension( 200, 20 ) );

         toolbar.addSeparator();
         toolbar.addLabeledFixed( "Full Path:", pathLabel );
      }
      else
      {
         pathTextField = new JUndoableTextField();
         pathTextField.setPreferredSize( new Dimension( 300, 20 ) );
         pathTextField.setText( getRequest().getPath() );
         pathTextField.getDocument().addDocumentListener( new DocumentListenerAdapter()
         {
            @Override
            public void update( Document document )
            {
               getRequest().setPath( pathTextField.getText() );
            }
         } );

         toolbar.addLabeledFixed( "Request URL:", pathTextField );
      }

      toolbar.addSeparator();
   }

   protected boolean release()
   {
      if( getRequest().getResource() != null )
      {
         getRequest().getResource().removePropertyChangeListener( this );
      }

      return super.release();
   }

   public class RestRequestMessageEditor extends
           AbstractHttpRequestDesktopPanel<?, ?>.AbstractHttpRequestMessageEditor<RestRequestDocument>
   {
      public RestRequestMessageEditor( RestRequest modelItem )
      {
         super( new RestRequestDocument( modelItem ) );
      }
   }

   public class RestResponseMessageEditor extends
           AbstractHttpRequestDesktopPanel<?, ?>.AbstractHttpResponseMessageEditor<RestResponseDocument>
   {
      public RestResponseMessageEditor( RestRequest modelItem )
      {
         super( new RestResponseDocument( modelItem ) );
      }
   }

   public class RestRequestDocument extends AbstractXmlDocument implements PropertyChangeListener
   {
      private final RestRequest modelItem;
      private boolean updating;

      public RestRequestDocument( RestRequest modelItem )
      {
         this.modelItem = modelItem;

         modelItem.addPropertyChangeListener( this );
      }

      public RestRequest getRequest()
      {
         return modelItem;
      }

      public String getXml()
      {
         return getRequest().getRequestContent();
      }

      public void setXml( String xml )
      {
         if( !updating )
         {
            updating = true;
            getRequest().setRequestContent( xml );
            updating = false;
         }
      }

      public void propertyChange( PropertyChangeEvent evt )
      {
         if( evt.getPropertyName().equals( RestRequest.REQUEST_PROPERTY ) && !updating )
         {
            updating = true;
            fireXmlChanged( ( String ) evt.getOldValue(), ( String ) evt.getNewValue() );
            updating = false;
         }
      }
   }

   public class RestResponseDocument extends AbstractXmlDocument implements PropertyChangeListener
   {
      private final RestRequest modelItem;

      public RestResponseDocument( RestRequest modelItem )
      {
         this.modelItem = modelItem;

         modelItem.addPropertyChangeListener( RestRequest.RESPONSE_PROPERTY, this );
      }

      public RestRequest getRequest()
      {
         return modelItem;
      }

      public String getXml()
      {
         return modelItem.getResponseContentAsXml();
      }

      public void setXml( String xml )
      {
         HttpResponse response = getRequest().getResponse();
         if( response != null )
            response.setResponseContent( xml );
      }

      public void propertyChange( PropertyChangeEvent evt )
      {
         fireXmlChanged( evt.getOldValue() == null ? null : ( ( HttpResponse ) evt.getOldValue() ).getContentAsString(),
                 getXml() );
      }
   }
}
