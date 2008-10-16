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

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import org.apache.xmlbeans.XmlObject;

import javax.swing.*;

public abstract class AbstractMockOperationDispatcher implements MockOperationDispatcher
{
   private WsdlMockOperation mockOperation;
   private XmlObject config;

   protected AbstractMockOperationDispatcher( WsdlMockOperation mockOperation, XmlObject config )
   {
      this.mockOperation = mockOperation;
      this.config = config;
   }

   public JComponent buildEditorComponent()
   {
      return new JPanel();
   }

   public void release()
   {
      mockOperation = null;
   }

   public XmlObject getConfig()
   {
      return config;
   }

   public WsdlMockOperation getMockOperation()
   {
      return mockOperation;
   }
}
