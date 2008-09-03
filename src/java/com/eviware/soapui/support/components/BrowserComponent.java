package com.eviware.soapui.support.components;

import com.eviware.soapui.support.xml.XmlUtils;
//import com.teamdev.jxbrowser.WebBrowser;
//import com.teamdev.jxbrowser.WebBrowserFactory;
//import com.teamdev.jxbrowser.event.StatusChangeEvent;
//import com.teamdev.jxbrowser.event.StatusChangeListener;
//import com.teamdev.xpcom.Xpcom;

import java.awt.*;

public class BrowserComponent
{
//   private WebBrowser browser;
//   private static WebBrowserFactory webBrowserFactory;
//
//   static
//   {
//      Xpcom.initialize( Xpcom.AWT );    
//   }

   public BrowserComponent()
   {

   }

   public Component getComponent()
   {
//      if( browser == null )
//      {
//         initBrowser();
//      }
//
//      return browser.getComponent();
   	return null;
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
//      if( browser == null )
//      {
//         initBrowser();
//      }
//      browser.setContentWithContext( contentAsString, contentType, contextUri );
   }

   public void setContent( String content, String contentType )
   {
//      if( browser == null )
//      {
//         initBrowser();
//      }
//      browser.setContent( content, contentType );
   }

   public void navigate( String url )
   {
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
   	return "";
   }
}
