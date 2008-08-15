package com.eviware.soapui.support.editor.inspectors.wsa;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.jgoodies.binding.PresentationModel;

public abstract class AbstractWsaInspector extends AbstractXmlInspector
{

	private JPanel mainPanel;
	private SimpleBindingForm form;
	private final WsaContainer wsaContainer;

	protected AbstractWsaInspector( WsaContainer wsaContainer)
	{
		super( "WSA", "WS-Addressing related settings", true, WsaInspectorFactory.INSPECTOR_ID );
		this.wsaContainer = wsaContainer;
	}
//	public AbstractWsaInspector(String title, String description, boolean enabled, String inspectorId)
//	{
//		super(title, description, enabled, inspectorId);
//	}
//
	public JComponent getComponent()
	{
		if( mainPanel == null )
		{
			mainPanel = new JPanel( new BorderLayout() );
	
			form = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( wsaContainer.getWsaConfig() ) );
	
//			if( request instanceof WsdlRequest )
//			{
				
				buildContent();
//			}
			
			form.addSpace( 5 );
			
			mainPanel.add( new JScrollPane( form.getPanel() ), BorderLayout.CENTER );
		}
	
		return mainPanel;
	}

	private void buildContent()
	{
		form.addSpace( 5 );
		form.appendCheckBox("wsaEnabled", "Enable WS-A addressing", "Enable/Disable WS-A addressing");
		form.addSpace( 5 );
		//add mustUnderstand drop down list
		form.appendComboBox( "mustUnderstand", "Must understand", new String[] {MustUnderstandTypeConfig.NONE.toString(), 
				MustUnderstandTypeConfig.TRUE.toString(), MustUnderstandTypeConfig.FALSE.toString()},
			"The  property for controlling use of the mustUnderstand attribute" );
		
		form.appendComboBox( "version", "WS-A Version", new String[] {WsaVersionTypeConfig.X_200508.toString(), WsaVersionTypeConfig.X_200408.toString()},
			"The  property for managing WS-A version" );
		
		form.appendTextField( "action", "Action", "The action related to a message, will be generated if left empty" );
		form.appendTextField( "from", "From", "The source endpoint reference, will be generated if left empty" );
		form.appendTextField( "replyTo", "Reply to", "The reply endpoint reference, will be generated if left empty" );
		form.appendTextField( "faultTo", "Fault to", "The fault endpoint reference" );
		form.appendTextField( "messageID", "MessageID", " The ID of a message that can be used to uniquely identify a message, will be generated if left empty" );
		form.appendTextField( "to", "To", "The destination endpoint reference, will be generated if left empty" );
	}

	@Override
	public boolean isEnabledFor(EditorView<XmlDocument> view)
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}

}