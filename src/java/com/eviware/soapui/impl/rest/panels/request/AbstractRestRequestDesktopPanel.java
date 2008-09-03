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

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRepresentation.Type;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest.RequestMethod;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.panels.AbstractHttpRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractRestRequestDesktopPanel<T extends ModelItem, T2 extends RestRequest>
        extends AbstractHttpRequestDesktopPanel<T, T2>
{
   private boolean updatingRequest;
   private JComboBox methodCombo;
   private JUndoableTextField pathField;
   private JButton recreatePathButton;

   public AbstractRestRequestDesktopPanel( T modelItem, T2 requestItem )
   {
      super( modelItem, requestItem );
   }

   public void propertyChange( PropertyChangeEvent evt )
   {
      if( evt.getPropertyName().equals( "method" ) && !updatingRequest )
      {
         methodCombo.setSelectedItem( evt.getNewValue() );
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
   public void afterSubmit( Submit submit, SubmitContext context )
   {
      super.afterSubmit( submit, context );

      HttpResponse response = getRequest().getResponse();
      if( response != null )
      {
         if( HttpUtils.isErrorStatus( response.getStatusCode() ) )
         {
            extractRepresentation( response, RestRepresentation.Type.FAULT );
         }
         else
         {
            extractRepresentation( response, RestRepresentation.Type.RESPONSE );
         }
      }
   }

   @SuppressWarnings( "unchecked" )
   private void extractRepresentation( HttpResponse response, Type type )
   {
      RestRepresentation[] representations = getRequest().getRepresentations( type );
      int c = 0;
      for( ; c < representations.length; c++ )
      {
         if( representations[c].getMediaType().equals( response.getContentType() ) )
         {
            List status = representations[c].getStatus();
            if( status == null || !status.contains( response.getStatusCode() ) )
            {
               status = status == null ? new ArrayList<Integer>() : new ArrayList<Integer>( status );
               status.add( response.getStatusCode() );
               representations[c].setStatus( status );
            }
            break;
         }
      }

      if( c == representations.length )
      {
         RestRepresentation representation = getRequest().addNewRepresentation( type );
         representation.setMediaType( response.getContentType() );
         representation.setStatus( Arrays.asList( response.getStatusCode() ) );
      }
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
      recreatePathButton = createActionButton( new RecreatePathAction(), true );

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
      methodCombo = new JComboBox( new Object[] { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
              RequestMethod.DELETE, RequestMethod.HEAD } );

      methodCombo.setSelectedItem( getRequest().getMethod() );
      methodCombo.addItemListener( new ItemListener()
      {

         public void itemStateChanged( ItemEvent e )
         {
            updatingRequest = true;
            getRequest().setMethod( (RequestMethod) methodCombo.getSelectedItem() );
            updatingRequest = false;
         }
      } );

      toolbar.addLabeledFixed( "Method", methodCombo );
      toolbar.addSeparator();

      pathField = new JUndoableTextField();
      pathField.setPreferredSize( new Dimension( 250, 20 ) );
      pathField.setText( getRequest().getPath() );
      pathField.getDocument().addDocumentListener( new DocumentListenerAdapter()
      {

         @Override
         public void update( Document document )
         {
            getRequest().setPath( pathField.getText() );
         }
      } );

      if( getRequest().getResource() != null )
      {
         toolbar.addLabeledFixed( "Resource Path:", pathField );
         toolbar.add( recreatePathButton );
      }
      else
      {
         toolbar.addLabeledFixed( "Request URL:", pathField );
      }

      toolbar.addSeparator();
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

   public class RestRequestDocument extends AbstractXmlDocument
   {
      private final RestRequest modelItem;

      public RestRequestDocument( RestRequest modelItem )
      {
         this.modelItem = modelItem;
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
         getRequest().setRequestContent( xml );
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
         fireXmlChanged( evt.getOldValue() == null ? null : ((HttpResponse) evt.getOldValue()).getContentAsString(),
                 getXml() );
      }
   }

   private class RecreatePathAction extends AbstractAction
   {
      public RecreatePathAction()
      {
         super();
         putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/recreate_request.gif" ) );
      }

      public void actionPerformed( ActionEvent e )
      {
         getRequest().setPath( getRequest().getResource().getFullPath() );
      }
   }
}
