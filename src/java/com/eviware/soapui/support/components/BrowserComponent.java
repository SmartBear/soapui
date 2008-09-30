package com.eviware.soapui.support.components;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.DefaultHyperlinkListener;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.URL;

public class BrowserComponent
{
   private JEditorPane editorPane;
   private JScrollPane scrollPane;
//   private WebBrowser browser;
//   private static WebBrowserFactory webBrowserFactory;
//
//   static
//   {
//      Xpcom.initialize( Xpcom.AWT );    
//   }

   public BrowserComponent()
   {
      editorPane = new JEditorPane();
      editorPane.setEditorKit( new HTMLEditorKit() );
      editorPane.setEditable( false );
      editorPane.addHyperlinkListener( new DefaultHyperlinkListener( editorPane ) );
   }

   public Component getComponent()
   {
//      if( browser == null )
//      {
//         initBrowser();
//      }
//
//      return browser.getComponent();
      if( scrollPane == null )
      {
         scrollPane = new JScrollPane( editorPane );
         UISupport.addPreviewCorner( scrollPane, false );
      }
      return scrollPane;
   }

   private void initBrowser()
   {
//      if( webBrowserFactory == null )
//         webBrowserFactory = WebBrowserFactory.getInstance();
//
//      browser = webBrowserFactory.createBrowser();
//      browser.addStatusChangeListener( new StatusChangeListener() {
//         public void statusChanged( StatusChangeEvent event )
//         {
////            System.out.println( "Status change: " + event );
//         }
//      } );
//
//      browser.deactivate();
   }

   public void release()
   {
//      if( browser != null )
//         browser.dispose();
//      
//      browser = null;
   }

   public void setContent( String contentAsString, String contentType, String contextUri )
   {
      editorPane.setContentType( contentType );
      try
      {
         editorPane.read( new ByteArrayInputStream( contentAsString.getBytes() ), editorPane.getEditorKit().createDefaultDocument() );
      }
      catch( IOException e )
      {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }

//      if( browser == null )
//      {
//         initBrowser();
//      }
//      browser.setContentWithContext( contentAsString, contentType, contextUri );
   }

   public void setContent( String content, String contentType )
   {
      setContent( content, contentType, null );

//      if( browser == null )
//      {
//         initBrowser();
//      }
//      browser.setContent( content, contentType );
   }

   public boolean navigate( String url )
   {
      try
      {
         editorPane.setPage( new URL( url ) );
         return true;
      }
      catch( NoRouteToHostException e )
      {
         SoapUI.log.warn( "Failed to access [" + url + "]" );
      }
      catch( Exception e )
      {
         SoapUI.logError( e );
      }

      return false;

//      if( browser == null )
//      {
//         initBrowser();
//      }
//
//      browser.navigate( url );
   }

   public String getContent()
   {
//      return browser == null ? null : XmlUtils.serialize( browser.getDocument() );
      return editorPane.getText();
   }
}
