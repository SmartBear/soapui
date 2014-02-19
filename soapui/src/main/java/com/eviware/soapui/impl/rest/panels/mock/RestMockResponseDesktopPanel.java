package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspector;
import com.eviware.soapui.support.editor.inspectors.httpheaders.MockResponseHeadersModel;
import com.eviware.soapui.ui.support.AbstractMockResponseDesktopPanel;
import com.eviware.soapui.model.mock.MockResponse;

import javax.swing.*;
import java.awt.*;

public class RestMockResponseDesktopPanel extends
		AbstractMockResponseDesktopPanel<RestMockResponse, MockResponse>
{
	private final int MAX_HEIGHT_TOP_PANEL = 12;

	public RestMockResponseDesktopPanel( MockResponse mockResponse )
	{
		super( ( RestMockResponse )mockResponse );

		init( mockResponse );
	}

	public JComponent addTopEditorPanel( )
	{

		HttpHeadersInspector inspector = new HttpHeadersInspector( new MockResponseHeadersModel(
				(RestMockResponse)getModelItem() ) );

		JComponent component = inspector.getComponent();
		component.setMaximumSize( new Dimension( Short.MAX_VALUE, MAX_HEIGHT_TOP_PANEL ) );
		return component;
	}

}
