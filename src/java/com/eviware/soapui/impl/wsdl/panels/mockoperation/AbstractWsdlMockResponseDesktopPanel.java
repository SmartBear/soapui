/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.support.components.RequestMessageXmlEditor;
import com.eviware.soapui.impl.support.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.OpenRequestForMockResponseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.CreateEmptyMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.CreateFaultMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.RecreateMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.WSIValidateResponseAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
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
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

/**
 * Abstract base DesktopPanel for WsdlMockResponses
 * 
 * @author Ole.Matzura
 */

public class AbstractWsdlMockResponseDesktopPanel<T extends ModelItem, T2 extends WsdlMockResponse> extends
		ModelItemDesktopPanel<T>
{
	// private final static Log log =
	// Logger.getLogger(WsdlMockOperationDesktopPanel.class);
	private JEditorStatusBarWithProgress statusBar;
	private JButton splitButton;
	private MockRunner mockRunner;
	private JButton recreateButton;
	private JButton createEmptyButton;
	private JSplitPane requestSplitPane;
	private MoveFocusAction moveFocusAction;
	private ClosePanelAction closePanelAction = new ClosePanelAction();

	private ModelItemXmlEditor<?, ?> requestEditor;
	private ModelItemXmlEditor<?, ?> responseEditor;

	public AbstractAction wsiValidateAction;

	private JTabbedPane requestTabs;
	private JPanel requestTabPanel;
	private JToggleButton tabsButton;

	public boolean responseHasFocus;

	private InternalPropertyChangeListener propertyChangeListener = new InternalPropertyChangeListener();
	private JButton createFaultButton;
	private T2 mockResponse;
	private JButton openRequestButton;

	public AbstractWsdlMockResponseDesktopPanel( T modelItem )
	{
		super( modelItem );
	}

	protected void init( T2 mockResponse )
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
				mockResponse.getOperation().getInterface().getDefinitionContext().loadIfNecessary();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	protected WsdlMockResponse getMockResponse()
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

	@SuppressWarnings( "unchecked" )
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

		// TODO Ericsson: This was removed and replaced with "true" below.
		boolean bidirectional = mockResponse.getMockOperation().getOperation().isBidirectional();

		recreateButton = createActionButton( new RecreateMockResponseAction( mockResponse ), bidirectional );
		createEmptyButton = createActionButton( new CreateEmptyMockResponseAction( mockResponse ), bidirectional );
		createFaultButton = createActionButton( new CreateFaultMockResponseAction( mockResponse ), bidirectional );

		moveFocusAction = new MoveFocusAction();
		wsiValidateAction = // new WSIValidateResponseAction( mockResponse );
		SwingActionDelegate.createDelegate( new WSIValidateResponseAction(), mockResponse, "alt W" );

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

	protected ModelItemXmlEditor<?, ?> buildResponseEditor()
	{
		return new WsdlMockResponseMessageEditor( new MockResponseXmlDocument( mockResponse ) );
	}

	protected ModelItemXmlEditor<?, ?> buildRequestEditor()
	{
		return new WsdlMockRequestMessageEditor( new MockRequestXmlDocument( mockResponse ) );
	}

	protected JComponent buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.add( openRequestButton );
		toolbar.addUnrelatedGap();
		toolbar.add( recreateButton );
		toolbar.add( createEmptyButton );
		toolbar.add( createFaultButton );

		createToolbar( toolbar );

		toolbar.add( Box.createHorizontalGlue() );
		toolbar.add( tabsButton );
		toolbar.add( splitButton );
		toolbar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( getHelpUrl() ) ) );

		return toolbar;
	}

	protected void createToolbar( JXToolBar toolbar )
	{
	}

	protected String getHelpUrl()
	{
		return HelpUrls.REQUESTEDITOR_HELP_URL;
	}

	protected void insertButtons( JToolBar toolbar )
	{
	}

	public void setEnabled( boolean enabled )
	{
		requestEditor.getSourceEditor().setEditable( enabled );
		responseEditor.getSourceEditor().setEditable( enabled );
		recreateButton.setEnabled( enabled );
		createEmptyButton.setEnabled( enabled );
		statusBar.setIndeterminate( !enabled );
	}

	private final class InternalPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlMockResponse.MOCKRESULT_PROPERTY ) )
			{
				WsdlMockResult mockResult = mockResponse.getMockResult();
				WsdlMockRequest mockRequest = mockResult == null ? null : mockResult.getMockRequest();
				requestEditor.getDocument().setXml( mockRequest == null ? "" : mockRequest.getRequestContent() );

				boolean bidirectional = mockResponse.getMockOperation().getOperation().isBidirectional();
				wsiValidateAction.setEnabled( bidirectional ); // TODO Ericsson: Had
																				// "true" here. Why?
			}
		}
	}

	public class WsdlMockRequestMessageEditor extends RequestMessageXmlEditor<WsdlMockResponse, XmlDocument>
	{
		public WsdlMockRequestMessageEditor( XmlDocument document )
		{
			super( document, mockResponse );
		}

		protected XmlSourceEditorView buildSourceEditor()
		{
			XmlSourceEditorView editor = getSourceEditor();
			JXEditTextArea inputArea = editor.getInputArea();

			inputArea.addFocusListener( new InputAreaFocusListener() );

			inputArea.getInputHandler().addKeyBinding( "AC+TAB", moveFocusAction );
			inputArea.getInputHandler().addKeyBinding( "F5", recreateButton.getAction() );
			inputArea.getInputHandler().addKeyBinding( "C+F4", closePanelAction );

			return editor;
		}
	}

	public class WsdlMockResponseMessageEditor extends ResponseMessageXmlEditor<WsdlMockResponse, XmlDocument>
	{
		public WsdlMockResponseMessageEditor( XmlDocument document )
		{
			super( document, mockResponse );

			XmlSourceEditorView editor = getSourceEditor();

			if( getModelItem().getMockOperation().isBidirectional() )
			{
				JXEditTextArea inputArea = editor.getInputArea();
				inputArea.addFocusListener( new ResultAreaFocusListener() );

				inputArea.getInputHandler().addKeyBinding( "AC+TAB", moveFocusAction );
				inputArea.getInputHandler().addKeyBinding( "C+F4", closePanelAction );

				// TODO Ericsson: This if test was changed and moved up. Ok?
				// if( !getModelItem().getMockOperation().isOneWay())
				// {
				JPopupMenu inputPopup = editor.getEditorPopup();
				inputPopup.insert( new JSeparator(), 2 );
				inputPopup.insert( wsiValidateAction, 3 );
			}
		}
	}

	protected final class InputAreaFocusListener implements FocusListener
	{
		public void focusGained( FocusEvent e )
		{
			responseHasFocus = false;

			statusBar.setTarget( requestEditor.getSourceEditor().getInputArea() );
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

			statusBar.setTarget( responseEditor.getSourceEditor().getInputArea() );
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