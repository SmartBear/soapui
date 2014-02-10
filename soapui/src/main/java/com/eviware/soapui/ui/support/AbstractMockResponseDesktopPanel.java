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

package com.eviware.soapui.ui.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.RequestMessageXmlEditor;
import com.eviware.soapui.impl.support.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.OpenRequestForMockResponseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.MockRequestXmlDocument;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.MockResponseXmlDocument;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.RecreateMockResponseAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.actions.ChangeSplitPaneOrientationAction;
import com.eviware.soapui.support.components.JEditorStatusBarWithProgress;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.swing.SoapUISplitPaneUI;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Abstract base DesktopPanel for MockResponses
 * 
 * @author Ole.Matzura
 */

public class AbstractMockResponseDesktopPanel<ModelItemType extends ModelItem, MockResponseType extends MockResponse> extends
		ModelItemDesktopPanel<ModelItemType>
{
	private JEditorStatusBarWithProgress statusBar;
	private JButton splitButton;
	private MockRunner mockRunner;
	private JButton recreateButton;
	private JSplitPane requestSplitPane;
	private MoveFocusAction moveFocusAction;
	private ClosePanelAction closePanelAction = new ClosePanelAction();

	private ModelItemXmlEditor<?, ?> requestEditor;
	private ModelItemXmlEditor<?, ?> responseEditor;

	private JTabbedPane requestTabs;
	private JPanel requestTabPanel;
	private JToggleButton tabsButton;

	public boolean responseHasFocus;

	private InternalPropertyChangeListener propertyChangeListener = new InternalPropertyChangeListener();
	private MockResponseType mockResponse;
	private JButton openRequestButton;

	public AbstractMockResponseDesktopPanel( ModelItemType modelItem )
	{
		super( modelItem );
	}

	protected void init( MockResponseType mockResponse )
	{
		this.mockResponse = mockResponse;

		add( buildContent(), BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );
		add( buildStatusLabel(), BorderLayout.SOUTH );

		setPreferredSize( new Dimension( 600, 500 ) );

		mockResponse.addPropertyChangeListener( propertyChangeListener );

		addFocusListener( new FocusAdapter()
		{

			@Override
			public void focusGained( FocusEvent e )
			{
				if( requestTabs.getSelectedIndex() == 1 || responseHasFocus )
					responseEditor.requestFocus();
				else
					requestEditor.requestFocus();
			}
		} );

		try
		{
			// required to avoid deadlock in UI when opening attachments inspector
			if( mockResponse.getAttachmentCount() > 0 )
			{
				mockResponse.getMockOperation().getOperation().getInterface().getDefinitionContext().loadIfNecessary();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected MockResponseType getMockResponse()
	{
		return mockResponse;
	}

	public final ModelItemXmlEditor<?, ?> getRequestEditor()
	{
		return requestEditor;
	}

	public final ModelItemXmlEditor<?, ?> getResponseEditor()
	{
		return responseEditor;
	}

	public MockRunner getSubmit()
	{
		return mockRunner;
	}

	protected JComponent buildStatusLabel()
	{
		statusBar = new JEditorStatusBarWithProgress();
		statusBar.setBorder( BorderFactory.createEmptyBorder( 1, 0, 0, 0 ) );

		return statusBar;
	}

	public JEditorStatusBarWithProgress getStatusBar()
	{
		return statusBar;
	}

	protected JComponent buildContent()
	{
		requestSplitPane = UISupport.createHorizontalSplit();
		requestSplitPane.setResizeWeight( 0.5 );
		requestSplitPane.setBorder( null );

		splitButton = createActionButton( new ChangeSplitPaneOrientationAction( requestSplitPane ), true );

		tabsButton = new JToggleButton( new ChangeToTabsAction() );
		tabsButton.setPreferredSize( UISupport.TOOLBAR_BUTTON_DIMENSION );

		openRequestButton = createActionButton( SwingActionDelegate.createDelegate(
				OpenRequestForMockResponseAction.SOAPUI_ACTION_ID, mockResponse, null, "/open_request.gif" ), true );

		recreateButton = createActionButton( new RecreateMockResponseAction( mockResponse ), isBidirectional() );

		moveFocusAction = new MoveFocusAction();

		requestEditor = buildRequestEditor();
		responseEditor = buildResponseEditor();

		requestTabs = new JTabbedPane();
		requestTabPanel = UISupport.createTabPanel( requestTabs, true );

		JComponent component = null;

		if( mockResponse.getSettings().getBoolean( UISettings.START_WITH_REQUEST_TABS ) )
		{
			requestTabs.addTab( "Last Request", requestEditor );
			requestTabs.addTab( "Mock Response", responseEditor );
			splitButton.setEnabled( false );
			tabsButton.setSelected( true );
			component = requestTabPanel;

			requestTabs.setSelectedIndex( 1 );
		}
		else
		{
			requestSplitPane.setTopComponent( requestEditor );
			requestSplitPane.setBottomComponent( responseEditor );
			requestSplitPane.setDividerLocation( 0.5 );
			component = requestSplitPane;
		}

		return component;
	}

	/**
	 * Override this method if you are not bidirectional.
	 *
	 * @return true
	 */
	protected boolean isBidirectional()
	{
		return true;
	}

	protected ModelItemXmlEditor<?, ?> buildResponseEditor()
	{
		return new MockResponseMessageEditor( new MockResponseXmlDocument( mockResponse ) );
	}

	protected ModelItemXmlEditor<?, ?> buildRequestEditor()
	{
		return new MockRequestMessageEditor( new MockRequestXmlDocument( mockResponse ) );
	}

	protected JComponent buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.add( openRequestButton );
		toolbar.addUnrelatedGap();
		toolbar.add( recreateButton );

		createToolbar( toolbar );

		toolbar.add( Box.createHorizontalGlue() );
		toolbar.add( tabsButton );
		toolbar.add( splitButton );
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( getHelpUrl() ) ) );

		return toolbar;
	}

	/**
	 * Override this method if you want to but your own things in the toolbar.
	 *
	 * @param toolbar this is the actual toolbar for you to manipulate. Don't remove stuff on it please....
	 */
	protected void createToolbar( JXToolBar toolbar )
	{
	}

	protected String getHelpUrl()
	{
		return HelpUrls.REQUESTEDITOR_HELP_URL;
	}

	public void setEnabled( boolean enabled )
	{
		requestEditor.getSourceEditor().setEditable( enabled );
		responseEditor.getSourceEditor().setEditable( enabled );
		recreateButton.setEnabled( enabled );
		statusBar.setIndeterminate( !enabled );
	}

	private final class InternalPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlMockResponse.MOCKRESULT_PROPERTY ) )
			{
				MockResult mockResult = mockResponse.getMockResult();
				MockRequest mockRequest = mockResult == null ? null : mockResult.getMockRequest();
				requestEditor.getDocument().setXml( mockRequest == null ? "" : mockRequest.getRequestContent() );
			}
		}
	}

	public class MockRequestMessageEditor extends RequestMessageXmlEditor<MockResponse, XmlDocument>
	{
		public MockRequestMessageEditor( XmlDocument document )
		{
			super( document, mockResponse );
		}

		protected XmlSourceEditorView<?> buildSourceEditor()
		{
			XmlSourceEditorView<?> editor = getSourceEditor();
			RSyntaxTextArea inputArea = editor.getInputArea();

			inputArea.addFocusListener( new InputAreaFocusListener() );

			if( UISupport.isMac() )
			{
				inputArea.getInputMap().put( KeyStroke.getKeyStroke( "control meta TAB" ), moveFocusAction );
				inputArea.getInputMap().put( KeyStroke.getKeyStroke( "F5" ), recreateButton.getAction() );
				inputArea.getInputMap().put( KeyStroke.getKeyStroke( "ctrl F4" ), closePanelAction );
			}
			else
			{
				inputArea.getInputMap().put( KeyStroke.getKeyStroke( "control alt TAB" ), moveFocusAction );
				inputArea.getInputMap().put( KeyStroke.getKeyStroke( "F5" ), recreateButton.getAction() );
				inputArea.getInputMap().put( KeyStroke.getKeyStroke( "ctrl F4" ), closePanelAction );
			}

			return editor;
		}
	}

	public class MockResponseMessageEditor extends ResponseMessageXmlEditor<MockResponse, XmlDocument>
	{
		public MockResponseMessageEditor( XmlDocument document )
		{
			super( document, mockResponse );

			if( isBidirectional() )
			{
				XmlSourceEditorView<?> editor = getSourceEditor();

				RSyntaxTextArea inputArea = editor.getInputArea();
				inputArea.addFocusListener( new ResultAreaFocusListener() );

				if( UISupport.isMac() )
				{
					inputArea.getInputMap().put( KeyStroke.getKeyStroke( "control meta TAB" ), moveFocusAction );
					inputArea.getInputMap().put( KeyStroke.getKeyStroke( "ctrl F4" ), closePanelAction );
				}
				else
				{
					inputArea.getInputMap().put( KeyStroke.getKeyStroke( "control alt TAB" ), moveFocusAction );
					inputArea.getInputMap().put( KeyStroke.getKeyStroke( "ctrl F4" ), closePanelAction );
				}

				JPopupMenu inputPopup = editor.getEditorPopup();
				inputPopup.insert( new JSeparator(), 2 );
			}
		}
	}

	protected final class InputAreaFocusListener implements FocusListener
	{
		public void focusGained( FocusEvent e )
		{
			responseHasFocus = false;

			if( !splitButton.isEnabled() )
			{
				requestTabs.setSelectedIndex( 0 );
				return;
			}

			if( getModelItem().getSettings().getBoolean( UISettings.NO_RESIZE_REQUEST_EDITOR ) )
				return;

			// dont resize if split has been dragged
			if( ( ( SoapUISplitPaneUI )requestSplitPane.getUI() ).hasBeenDragged() )
				return;

			int pos = requestSplitPane.getDividerLocation();
			if( pos >= 600 )
				return;
			if( requestSplitPane.getMaximumDividerLocation() > 700 )
				requestSplitPane.setDividerLocation( 600 );
			else
				requestSplitPane.setDividerLocation( 0.8 );
		}

		public void focusLost( FocusEvent e )
		{
		}
	}

	protected final class ResultAreaFocusListener implements FocusListener
	{
		public void focusGained( FocusEvent e )
		{
			responseHasFocus = true;

			if( !splitButton.isEnabled() )
			{
				requestTabs.setSelectedIndex( 1 );
				return;
			}

			if( getModelItem().getSettings().getBoolean( UISettings.NO_RESIZE_REQUEST_EDITOR ) )
				return;

			// dont resize if split has been dragged or result is empty
			if( ( ( SoapUISplitPaneUI )requestSplitPane.getUI() ).hasBeenDragged() )
				return;

			int pos = requestSplitPane.getDividerLocation();
			int maximumDividerLocation = requestSplitPane.getMaximumDividerLocation();
			if( pos + 600 < maximumDividerLocation )
				return;

			if( maximumDividerLocation > 700 )
				requestSplitPane.setDividerLocation( maximumDividerLocation - 600 );
			else
				requestSplitPane.setDividerLocation( 0.2 );
		}

		public void focusLost( FocusEvent e )
		{
		}
	}

	private class ClosePanelAction extends AbstractAction
	{
		public void actionPerformed( ActionEvent e )
		{
			SoapUI.getDesktop().closeDesktopPanel( getModelItem() );
		}
	}

	private class MoveFocusAction extends AbstractAction
	{
		public void actionPerformed( ActionEvent e )
		{
			if( requestEditor.hasFocus() )
			{
				responseEditor.requestFocus();
			}
			else
			{
				requestEditor.requestFocus();
			}
		}
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == getModelItem() || modelItem == mockResponse.getMockOperation()
				|| modelItem == mockResponse.getMockOperation().getMockService()
				|| modelItem == mockResponse.getMockOperation().getMockService().getProject();
	}

	private final class ChangeToTabsAction extends AbstractAction
	{
		public ChangeToTabsAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/toggle_tabs.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Toggles to tab-based layout" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( splitButton.isEnabled() )
			{
				splitButton.setEnabled( false );
				removeContent( requestSplitPane );
				setContent( requestTabPanel );
				requestTabs.addTab( "Last Request", requestEditor );
				requestTabs.addTab( "Mock Response", responseEditor );
			}
			else
			{
				int selectedIndex = requestTabs.getSelectedIndex();

				splitButton.setEnabled( true );
				removeContent( requestTabPanel );
				setContent( requestSplitPane );
				requestSplitPane.setTopComponent( requestEditor );
				requestSplitPane.setBottomComponent( responseEditor );
				requestSplitPane.setDividerLocation( 0.5 );

				if( selectedIndex == 0 )
					requestEditor.requestFocus();
				else
					responseEditor.requestFocus();
			}

			revalidate();
		}
	}

	public void setContent( JComponent content )
	{
		add( content, BorderLayout.CENTER );
	}

	public void removeContent( JComponent content )
	{
		remove( content );
	}

	public boolean onClose( boolean canCancel )
	{
		mockResponse.removePropertyChangeListener( propertyChangeListener );

		requestEditor.release();
		responseEditor.release();

		responseEditor.getParent().remove( responseEditor );
		responseEditor = null;
		requestEditor.getParent().remove( requestEditor );
		requestEditor = null;

		return release();
	}
}
