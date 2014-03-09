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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import javax.swing.JComponent;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.model.Releasable;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;

public interface MockOperationDispatcher extends Releasable
{
	public MockResponse selectMockResponse( MockRequest request, MockResult result )
			throws DispatchException;

	public JComponent getEditorComponent();

	public void releaseEditorComponent();
}
