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

	public RestMockResponseDesktopPanel( MockResponse mockResponse )
	{
		super( ( RestMockResponse )mockResponse );

		init( mockResponse );
	}

	public JComponent addTopEditorPanel( )
	{
		JPanel topEditorPanel = new JPanel( );
		topEditorPanel.setLayout( new BoxLayout( topEditorPanel, BoxLayout.Y_AXIS ) );

		topEditorPanel.add( createHeaderInspector() );

		return topEditorPanel;
	}

	private JComponent createHeaderInspector()
	{
		MockResponseHeadersModel model = new MockResponseHeadersModel( getModelItem() );
		HttpHeadersInspector inspector = new HttpHeadersInspector( model );

		boolean shouldShowOnlineHelpIcon = false;
		JComponent component = inspector.getComponent( shouldShowOnlineHelpIcon );
		return component;
	}

}
