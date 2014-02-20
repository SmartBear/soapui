package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspector;
import com.eviware.soapui.support.editor.inspectors.httpheaders.MockResponseHeadersModel;
import com.eviware.soapui.ui.support.AbstractMockResponseDesktopPanel;
import com.eviware.soapui.model.mock.MockResponse;
import org.apache.commons.httpclient.HttpStatus;
import sun.print.resources.serviceui_sv;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.Vector;

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

		topEditorPanel.add( new JLabel( "Headers:" ) );
		topEditorPanel.add( createHeaderInspector() );
		topEditorPanel.add( Box.createVerticalStrut( 5 ) );
		topEditorPanel.add( createHttpStatusPanel() );

		return topEditorPanel;
	}

	private JComponent createHttpStatusPanel()
	{
		JPanel httpStatusPanel = new JPanel(  );

		httpStatusPanel.add( new JLabel( "Http Status Code: " ) );
		httpStatusPanel.add( createStatusCodeCombo() );

		return httpStatusPanel;
	}

	private JComboBox createStatusCodeCombo()
	{
		ComboBoxModel httpStatusCodeComboBoxModel = new HttpStatusCodeComboBoxModel();

		final JComboBox statusCodeCombo = new JComboBox( httpStatusCodeComboBoxModel );

		statusCodeCombo.setSelectedItem( getModelItem().getResponseHttpStatus() );
		statusCodeCombo.setToolTipText( "Set desired HTTP status code" );
		statusCodeCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				getModelItem().setResponseHttpStatus( ( Integer )statusCodeCombo.getSelectedItem() );
			}
		} );
		return statusCodeCombo;
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

class HttpStatusCodeComboBoxModel extends DefaultComboBoxModel
{
	private static Vector<Integer> LIST_OF_CODES = new Vector<Integer>(  );

	static
	{
		final String statusCodePrefix = "SC_";

		for( Field statusCodeField : HttpStatus.class.getDeclaredFields() )
		{
			try
			{
				if( statusCodeField.getName().startsWith( statusCodePrefix ) )
				{
					LIST_OF_CODES.add( statusCodeField.getInt( null ) );
				}
			}
			catch( IllegalAccessException e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public HttpStatusCodeComboBoxModel()
	{
		super( LIST_OF_CODES );
	}
}
