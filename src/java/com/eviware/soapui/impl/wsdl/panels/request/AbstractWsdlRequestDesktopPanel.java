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

package com.eviware.soapui.impl.wsdl.panels.request;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToMockServiceAction;
import com.eviware.soapui.impl.wsdl.actions.request.CloneRequestAction;
import com.eviware.soapui.impl.wsdl.actions.request.CreateEmptyRequestAction;
import com.eviware.soapui.impl.wsdl.actions.request.RecreateRequestAction;
import com.eviware.soapui.impl.wsdl.actions.support.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.request.actions.WSIValidateRequestAction;
import com.eviware.soapui.impl.wsdl.panels.request.components.RequestMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.panels.request.components.RequestXmlDocument;
import com.eviware.soapui.impl.wsdl.panels.request.components.ResponseMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.panels.request.components.ResponseXmlDocument;
import com.eviware.soapui.impl.wsdl.panels.request.components.SoapMessageXmlEditor;
import com.eviware.soapui.impl.wsdl.panels.support.EndpointsComboBoxModel;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.iface.Submit.Status;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.actions.ChangeSplitPaneOrientationAction;
import com.eviware.soapui.support.components.JEditorStatusBarWithProgress;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.support.DefaultXmlDocument;
import com.eviware.soapui.support.swing.SoapUISplitPaneUI;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

/**
 * Abstract DesktopPanel for WsdlRequests
 * 
 * @author Ole.Matzura
 */

public class AbstractWsdlRequestDesktopPanel<T extends ModelItem, T2 extends WsdlRequest> extends ModelItemDesktopPanel<T>
{
	private final static Logger log = Logger.getLogger(AbstractWsdlRequestDesktopPanel.class);

	private JComboBox endpointCombo;
	private JButton submitButton;
	private JButton cancelButton;
	private EndpointsComboBoxModel endpointsModel;
	private JEditorStatusBarWithProgress statusBar;
	private JButton splitButton;
	private Submit submit;
	private JButton recreateButton;
	private JButton cloneButton;
	private JButton createEmptyButton;
	private InternalSubmitListener internalSubmitListener;
	private JSplitPane requestSplitPane;
	private MoveFocusAction moveFocusAction;
	private ClosePanelAction closePanelAction = new ClosePanelAction();
	private T2 request;

	private SoapMessageXmlEditor<?> requestEditor;
	private SoapMessageXmlEditor<?> responseEditor;

	private JTabbedPane requestTabs;
	private JPanel requestTabPanel;
	private JToggleButton tabsButton;

	public boolean responseHasFocus;

	private JButton addToMockServiceButton;
	
	private AbstractAction wsiValidateAction;
	private SubmitAction submitAction;

	public AbstractWsdlRequestDesktopPanel(T modelItem)
	{
		super(modelItem);
	}

	protected void init(T2 request)
	{
		this.request = request;
		
		this.endpointsModel = new EndpointsComboBoxModel(request);
		internalSubmitListener = createSubmitListener();

		request.addSubmitListener(internalSubmitListener);

		add(buildContent(), BorderLayout.CENTER);
		add(buildToolbar(), BorderLayout.NORTH);
		add(buildStatusLabel(), BorderLayout.SOUTH);

		setPreferredSize(new Dimension(600, 500));
		
		addFocusListener( new FocusAdapter() {

			@Override
			public void focusGained( FocusEvent e )
			{
				if( requestTabs.getSelectedIndex() == 1 || responseHasFocus )
					responseEditor.requestFocusInWindow();
				else
					requestEditor.requestFocusInWindow();
			}} );
	}

	protected InternalSubmitListener createSubmitListener()
	{
		return new InternalSubmitListener();
	}
	
	public final T2 getRequest()
	{
		return request;
	}
	
	public final SoapMessageXmlEditor<?> getRequestEditor()
	{
		return requestEditor;
	}

	public final SoapMessageXmlEditor<?> getResponseEditor()
	{
		return responseEditor;
	}
	
	public Submit getSubmit()
	{
		return submit;
	}

	protected JComponent buildStatusLabel()
	{
		statusBar = new JEditorStatusBarWithProgress();
		statusBar.setBorder( BorderFactory.createEmptyBorder( 1, 0, 0, 0 ));
		
		return statusBar;
	}
	
	public JEditorStatusBarWithProgress getStatusBar()
	{
		return statusBar;
	}

	@SuppressWarnings("unchecked")
	protected JComponent buildContent()
	{
		requestSplitPane = UISupport.createHorizontalSplit();
		requestSplitPane.setResizeWeight(0.5);
		requestSplitPane.setBorder(null);
		
		submitAction = new SubmitAction();
		submitButton = createActionButton(submitAction, true);
		cancelButton = createActionButton(new CancelAction(), false);
		splitButton = createActionButton(new ChangeSplitPaneOrientationAction(requestSplitPane), true);
		
		tabsButton = new JToggleButton( new ChangeToTabsAction() );
		tabsButton.setPreferredSize( UISupport.TOOLBAR_BUTTON_DIMENSION );
		
		recreateButton = createActionButton(new RecreateRequestAction(request), true);
		addToMockServiceButton = createActionButton( 
					SwingActionDelegate.createDelegate( AddRequestToMockServiceAction.SOAPUI_ACTION_ID, request, null, "/addToMockService.gif" ), true );
		
		cloneButton = createActionButton(
					SwingActionDelegate.createDelegate( CloneRequestAction.SOAPUI_ACTION_ID, request, null, "/clone_request.gif" ), true );
		
		createEmptyButton = createActionButton(new CreateEmptyRequestAction(request), true);

		submitButton.setEnabled(request.getEndpoint() != null && request.getEndpoint().trim().length() > 0);
		wsiValidateAction = 
			SwingActionDelegate.createDelegate( new WSIValidateRequestAction(), request, "alt W" ); 
		wsiValidateAction.setEnabled( request.getResponse() != null );
		
		moveFocusAction = new MoveFocusAction();

		requestEditor = buildRequestEditor();
		responseEditor = buildResponseEditor();
		
		requestTabs = new JTabbedPane();
		requestTabs.addChangeListener( new ChangeListener() {

			public void stateChanged( ChangeEvent e )
			{
				SwingUtilities.invokeLater( new Runnable() {

					public void run()
					{
						int ix = requestTabs.getSelectedIndex();
						if( ix == 0 )
							requestEditor.requestFocus(); 
						else if( ix == 1 )
							responseEditor.requestFocus(); 
					}} );
			}} );
		
	   requestTabPanel = UISupport.createTabPanel( requestTabs, true );
	   
		if( request.getSettings().getBoolean( UISettings.START_WITH_REQUEST_TABS ))
	   {
	   	requestTabs.addTab( "Request", requestEditor );
	   	requestTabs.addTab( "Response", responseEditor );
	   	splitButton.setEnabled( false );
	   	tabsButton.setSelected( true );
	   	
	   	return requestTabPanel;
	   }
	   else
	   {
	   	requestSplitPane.setTopComponent(requestEditor);
	   	requestSplitPane.setBottomComponent(responseEditor);
	   	requestSplitPane.setDividerLocation(0.5);
	   	return requestSplitPane;
	   }
	}

	public SubmitAction getSubmitAction()
	{
		return submitAction;
	}

	protected SoapMessageXmlEditor<?> buildResponseEditor()
	{
		return new WsdlResponseMessageEditor( new ResponseXmlDocument( request ));
	}

	protected SoapMessageXmlEditor<?> buildRequestEditor()
	{
		return new WsdlRequestMessageEditor( new RequestXmlDocument( request ));
	}
	
	public static class OneWayResponseMessageEditor extends SoapMessageXmlEditor<ModelItem>
	{
		public OneWayResponseMessageEditor( ModelItem modelItem )
		{
			super( new DefaultXmlDocument(), modelItem );
		}
	}

	protected JComponent buildToolbar()
	{
		endpointCombo = new JComboBox(endpointsModel);
		endpointCombo.setToolTipText(endpointsModel.getSelectedItem().toString());

		endpointCombo.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				Object item = endpointCombo.getSelectedItem();
				if (item == null)
				{
					endpointCombo.setToolTipText("- no endpoint set for request -");
				}
				else
				{
					String selectedItem = item.toString();
					endpointCombo.setToolTipText(selectedItem);
				}

				submitButton.setEnabled(submit == null && request.getEndpoint() != null
						&& request.getEndpoint().trim().length() > 0);
			}
		});

		JXToolBar toolbar =  UISupport.createToolbar(); 
		
		toolbar.add( submitButton );
		insertButtons( toolbar );
		
		toolbar.add( addToMockServiceButton);
		
		toolbar.add(recreateButton);
		toolbar.add(createEmptyButton);
		toolbar.add(cloneButton);
		toolbar.add(cancelButton);
		toolbar.addSeparator();
		toolbar.add(endpointCombo);
		
		toolbar.add( Box.createHorizontalGlue() );
		toolbar.add( tabsButton );
		toolbar.add(splitButton);
		toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(getHelpUrl())));
		
		return toolbar;
	}

	protected String getHelpUrl()
	{
		return HelpUrls.REQUESTEDITOR_HELP_URL;
	}

	protected void insertButtons(JToolBar toolbar)
	{
	}

	public void setEnabled(boolean enabled)
	{
		endpointCombo.setEnabled(enabled);
		requestEditor.setEditable(enabled);
		responseEditor.setEditable(enabled);

		submitButton.setEnabled(enabled && request.getEndpoint() != null
				&& request.getEndpoint().trim().length() > 0);
		recreateButton.setEnabled(enabled);
		createEmptyButton.setEnabled(enabled);
		cloneButton.setEnabled(enabled);
		
		statusBar.setIndeterminate( !enabled );
	}

	public class WsdlRequestMessageEditor extends RequestMessageXmlEditor<WsdlRequest>
	{
		private InputAreaFocusListener inputAreaFocusListener;
		private JXEditTextArea inputArea;

		public WsdlRequestMessageEditor(XmlDocument document)
		{
			super(document, request );
			
			XmlSourceEditorView editor = getSourceEditor();
			inputArea = editor.getInputArea();
			inputArea.getInputHandler().addKeyBinding("A+ENTER", submitButton.getAction());
			inputArea.getInputHandler().addKeyBinding("A+X", cancelButton.getAction());
			inputArea.getInputHandler().addKeyBinding("AC+TAB", moveFocusAction);
			inputArea.getInputHandler().addKeyBinding("F5", recreateButton.getAction());
			inputArea.getInputHandler().addKeyBinding("C+F4", closePanelAction);
			
			inputAreaFocusListener = new InputAreaFocusListener();
			inputArea.addFocusListener( inputAreaFocusListener);
		}

		@Override
		public void release()
		{
			super.release();
			inputArea.removeFocusListener( inputAreaFocusListener );
		}
	}

	public class WsdlResponseMessageEditor extends ResponseMessageXmlEditor<WsdlRequest>
	{
		private JXEditTextArea inputArea;
		private ResultAreaFocusListener resultAreaFocusListener;

		public WsdlResponseMessageEditor(XmlDocument document)
		{
			super(document, request );
		
			XmlSourceEditorView editor = getSourceEditor();
			
			inputArea = editor.getInputArea();
			resultAreaFocusListener = new ResultAreaFocusListener();
			inputArea.addFocusListener(resultAreaFocusListener);
			
			inputArea.getInputHandler().addKeyBinding("A+ENTER", submitButton.getAction());
			inputArea.getInputHandler().addKeyBinding("A+X", cancelButton.getAction());
			inputArea.getInputHandler().addKeyBinding("AC+TAB", moveFocusAction);
			inputArea.getInputHandler().addKeyBinding("C+F4", closePanelAction);
			
			JPopupMenu inputPopup = editor.getEditorPopup();
			inputPopup.insert( new JSeparator(), 2 );
			inputPopup.insert( wsiValidateAction, 3 );
		}

		@Override
		public void release()
		{
			super.release();
			
			inputArea.removeFocusListener( resultAreaFocusListener );
		}
	}
	
	protected final class InputAreaFocusListener implements FocusListener
	{
		public void focusGained(FocusEvent e)
		{
			responseHasFocus = false;
			
			statusBar.setTarget( requestEditor.getSourceEditor().getInputArea() );
			if( !splitButton.isEnabled() )
			{
				requestTabs.setSelectedIndex( 0 );
				return;
			}
			
			if (getModelItem().getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR))
				return;

			// dont resize if split has been dragged
			if (((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged())
				return;

			int pos = requestSplitPane.getDividerLocation();
			if (pos >= 600)
				return;
			if (requestSplitPane.getMaximumDividerLocation() > 700)
				requestSplitPane.setDividerLocation(600);
			else
				requestSplitPane.setDividerLocation(0.8);
		}

		public void focusLost(FocusEvent e)
		{
		}
	}

	protected final class ResultAreaFocusListener implements FocusListener
	{
		public void focusGained(FocusEvent e)
		{
			responseHasFocus = true;
			
			statusBar.setTarget( responseEditor.getSourceEditor().getInputArea() );
			if( !splitButton.isEnabled() )
			{
				requestTabs.setSelectedIndex( 1 );
				return;
			}
			
			if (request.getSettings().getBoolean(UISettings.NO_RESIZE_REQUEST_EDITOR))
				return;

			// dont resize if split has been dragged or result is empty
			if (((SoapUISplitPaneUI) requestSplitPane.getUI()).hasBeenDragged() || request.getResponse() == null)
				return;

			int pos = requestSplitPane.getDividerLocation();
			int maximumDividerLocation = requestSplitPane.getMaximumDividerLocation();
			if (pos + 600 < maximumDividerLocation)
				return;

			if (maximumDividerLocation > 700)
				requestSplitPane.setDividerLocation(maximumDividerLocation - 600);
			else
				requestSplitPane.setDividerLocation(0.2);
		}

		public void focusLost(FocusEvent e)
		{
		}
	}

	public class SubmitAction extends AbstractAction
	{
		public SubmitAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/submit_request.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Submit request to specified endpoint URL");
			putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "alt ENTER" ));
		}

		public void actionPerformed(ActionEvent e)
		{
			if (submit != null && submit.getStatus() == Submit.Status.RUNNING)
			{
				if (UISupport.confirm("Cancel current request?", "Submit Request"))
				{
					submit.cancel();
				}
				else
					return;
			}

			try
			{
				submit = doSubmit();
			}
			catch (SubmitException e1)
			{
				SoapUI.logError( e1 );
			}
		}
	}
	
	protected Submit doSubmit() throws SubmitException
	{
		return request.submit(new WsdlSubmitContext(getModelItem()), true);
	}

	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super();
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/cancel_request.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Aborts ongoing request");
			putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "alt X" ));
		}

		public void actionPerformed(ActionEvent e)
		{
			if (submit == null)
				return;

			cancelButton.setEnabled(false);
			submit.cancel();
			setEnabled(true);
			submit = null;
		}
	}

	private class ClosePanelAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			SoapUI.getDesktop().closeDesktopPanel(getModelItem());
		}
	}

	private class MoveFocusAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if (requestEditor.hasFocus())
			{
				responseEditor.requestFocus();
			}
			else
			{
				requestEditor.requestFocus();
			}
		}
	}

	protected class InternalSubmitListener implements SubmitListener
	{
		protected InternalSubmitListener()
		{
		}

		public boolean beforeSubmit(Submit submit, SubmitContext context)
		{
			if (submit.getRequest() != request )
				return true;
			
			if( getModelItem().getSettings().getBoolean( UISettings.AUTO_VALIDATE_REQUEST ))
			{
				boolean result = requestEditor.saveDocument( true );
				if( !result &&  getModelItem().getSettings().getBoolean( UISettings.ABORT_ON_INVALID_REQUEST ))
				{
					statusBar.setInfo( "Cancelled request due to invalid content" );
					return false;
				}
			}
			else requestEditor.saveDocument( false );

			setEnabled( false );
			cancelButton.setEnabled( AbstractWsdlRequestDesktopPanel.this.submit != null );
			wsiValidateAction.setEnabled( false );
			return true;
		}

		public void afterSubmit(Submit submit, SubmitContext context)
		{
			if (submit.getRequest() != request )
				return;
			
			Status status = submit.getStatus();
			WsdlResponse response = (WsdlResponse) submit.getResponse();
			if (status != Status.CANCELED )
			{
				request.setResponse(response, context);
			}
			
			cancelButton.setEnabled(false);
			wsiValidateAction.setEnabled( request.getResponse() != null );
			setEnabled(true);

			String message = null;
			String infoMessage = null;
			String requestName = request.getOperation().getInterface().getName() + "."
					+ request.getOperation().getName() + ":" + request.getName();

			if (status == Status.CANCELED)
			{
				message = "CANCELED";
				infoMessage = "[" + requestName + "] - CANCELED";
			}
			else
			{
				if (status == Status.ERROR || response == null)
				{
					message = "Error getting response; " + submit.getError();
					infoMessage = "Error getting response for [" + requestName + "]; " + submit.getError();
				}
				else
				{
					message = "response time: " + response.getTimeTaken() + "ms (" + response.getContentLength() + " bytes)";
					infoMessage = "Got response for [" + requestName + "] in " + response.getTimeTaken() + "ms ("
							+ response.getContentLength() + " bytes)";

					if( !splitButton.isEnabled() )
						requestTabs.setSelectedIndex( 1 );
					
					responseEditor.requestFocus();
				}
				
//				responseHeadersModel.setData( submit.getResponse() == null ? null : submit.getResponse().getResponseHeaders());
				
//				updateSSLInfo( response == null ? null : response.getSSLInfo() );
				wsiValidateAction.setEnabled( true );
			}

			logMessages(message, infoMessage);
			
			if( getModelItem().getSettings().getBoolean( UISettings.AUTO_VALIDATE_RESPONSE ))
				responseEditor.getSourceEditor().validate();

			AbstractWsdlRequestDesktopPanel.this.submit = null;
		}

		protected void logMessages(String message, String infoMessage)
		{
			log.info(infoMessage);
			statusBar.setInfo(message);
		}
	}

	public boolean onClose(boolean canCancel)
	{
		if (canCancel)
		{
			if (submit != null && submit.getStatus() == Submit.Status.RUNNING)
			{
				Boolean retVal = UISupport.confirmOrCancel("Cancel request before closing?", "Closing window");
				if (retVal == null)
					return false;

				if (retVal.booleanValue() && submit.getStatus() == Submit.Status.RUNNING)
					submit.cancel();
			}
		}
		else if (submit != null && submit.getStatus() == Submit.Status.RUNNING)
		{
			submit.cancel();
		}

		request.removeSubmitListener( internalSubmitListener );
		endpointsModel.release();

		requestEditor.saveDocument( false );
		requestEditor.release();
		responseEditor.release();
		
		responseEditor.getParent().remove( responseEditor );
		requestEditor.getParent().remove( requestEditor );
		requestSplitPane.removeAll();
		
		return super.release();
	}

	public boolean dependsOn(ModelItem modelItem)
	{
		return modelItem == request || modelItem == request.getOperation()
				|| modelItem == request.getOperation().getInterface()
				|| modelItem == request.getOperation().getInterface().getProject();
	}

	private final class ChangeToTabsAction extends AbstractAction
	{
		public ChangeToTabsAction()
		{
			putValue(Action.SMALL_ICON, UISupport.createImageIcon("/toggle_tabs.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Toggles to tab-based layout");
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if( splitButton.isEnabled() )
			{
				splitButton.setEnabled( false );
				removeContent( requestSplitPane );
				setContent( requestTabPanel );
				requestTabs.addTab( "Request", requestEditor );
				requestTabs.addTab( "Response", responseEditor );
				
				if( responseHasFocus )
				{
					requestTabs.setSelectedIndex( 1 );
					requestEditor.requestFocus();
				}
			}
			else
			{
				int selectedIndex = requestTabs.getSelectedIndex();
				
				splitButton.setEnabled( true );
				removeContent( requestTabPanel );
				setContent( requestSplitPane );
				requestSplitPane.setTopComponent(requestEditor);
		   	requestSplitPane.setBottomComponent(responseEditor);
		   	requestSplitPane.setDividerLocation(0.5);
		   	
		   	if( selectedIndex == 0 )
		   		requestEditor.requestFocus();
		   	else
		   		responseEditor.requestFocus();
			}
			
			revalidate();
		}
	}
	
	public void setContent(JComponent content)
	{
		add( content, BorderLayout.CENTER );
	}

	public void removeContent(JComponent content)
	{
		remove( content );
	}
}