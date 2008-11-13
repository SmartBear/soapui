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

package com.eviware.soapui.ui;

import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

import javax.swing.*;
import java.awt.*;

public class URLDesktopPanel extends DefaultDesktopPanel
{
   private BrowserComponent browser;

   public URLDesktopPanel( String title, String description, final String url )
   {
      super( title, description, new JPanel( new BorderLayout() ) );

      JPanel panel = (JPanel) getComponent();

      browser = new BrowserComponent();
      panel.add( browser.getComponent(), BorderLayout.CENTER );

      navigate( url, null, true );
   }

   public void navigate( final String url, final String errorUrl, boolean async )
   {
      if( async )
      {
         new Thread( new Runnable()
         {
            public void run()
            {
               browser.navigate( url, errorUrl );
            }
         } ).start();
      }
      else
      {
         browser.navigate( url, errorUrl );
      }
   }

   public boolean onClose( boolean canCancel )
   {
      browser.release();
      return super.onClose( canCancel );
   }
}
