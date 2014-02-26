package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import com.eviware.soapui.impl.support.components.ModelItemXmlEditor;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.OpenRequestForMockResponseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.CreateEmptyWsdlMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.CreateFaultWsdlMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.RecreateMockResponseAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.actions.WSIValidateResponseAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.ui.support.AbstractMockResponseDesktopPanel;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AbstractWsdlMockResponseDesktopPanel<ModelItemType extends ModelItem>
		extends AbstractMockResponseDesktopPanel<ModelItemType, WsdlMockResponse>
{
	private JButton createEmptyButton;
	private JButton createFaultButton;
	private AbstractAction wsiValidateAction;

	private InternalPropertyChangeListener propertyChangeListener = new InternalPropertyChangeListener();

	private JButton openRequestButton;
	private JButton recreateButton;

	public AbstractWsdlMockResponseDesktopPanel( ModelItemType modelItem )
	{
		super( modelItem );
		modelItem.addPropertyChangeListener( propertyChangeListener );
	}

	protected JComponent buildContent()
	{
		MockResponse mockResponse = getMockResponse();

		createEmptyButton = createActionButton( new CreateEmptyWsdlMockResponseAction( mockResponse ), isBidirectional() );
		createFaultButton = createActionButton( new CreateFaultWsdlMockResponseAction( mockResponse ), isBidirectional() );
		wsiValidateAction = SwingActionDelegate.createDelegate( new WSIValidateResponseAction(), mockResponse, "alt W" );

		openRequestButton = createActionButton( SwingActionDelegate.createDelegate(
				OpenRequestForMockResponseAction.SOAPUI_ACTION_ID, mockResponse, null, "/open_request.gif" ), true );

		recreateButton = createActionButton( new RecreateMockResponseAction( mockResponse ), isBidirectional() );

		return super.buildContent();
	}

	protected void createToolbar( JXToolBar toolbar )
	{
		toolbar.add( openRequestButton );
		toolbar.addUnrelatedGap();
		toolbar.add( recreateButton );

		toolbar.add( createEmptyButton );
		toolbar.add( createFaultButton );
	}

	public void setEnabled( boolean enabled )
	{
		recreateButton.setEnabled( enabled );
		createEmptyButton.setEnabled( enabled );
		super.setEnabled( enabled );
	}

	protected boolean isBidirectional()
	{
		return getMockResponse().getMockOperation().getOperation().isBidirectional();
	}

	private final class InternalPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlMockResponse.MOCKRESULT_PROPERTY ) )
			{
				wsiValidateAction.setEnabled( isBidirectional() );
			}
		}
	}

	public boolean onClose( boolean canCancel )
	{
		getMockResponse().removePropertyChangeListener( propertyChangeListener );
		return super.onClose( canCancel );
	}

	public class WsdlMockResponseMessageEditor extends MockResponseMessageEditor
	{
		public WsdlMockResponseMessageEditor( XmlDocument document )
		{
			super( document );

			if( isBidirectional() )
			{
				XmlSourceEditorView<?> editor = getSourceEditor();
				JPopupMenu inputPopup = editor.getEditorPopup();
				inputPopup.insert( wsiValidateAction, 3 );
			}
		}
	}

	protected MockResponseMessageEditor buildResponseEditor()
	{
		return new WsdlMockResponseMessageEditor( new MockResponseXmlDocument( getMockResponse() ) );
	}


}
