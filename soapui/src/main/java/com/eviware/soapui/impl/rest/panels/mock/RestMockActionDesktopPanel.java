package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.config.MockOperationDispatchStyleConfig;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.panels.request.TextPanelWithTopLabel;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.NewMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.OpenRequestForMockOperationAction;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatchRegistry;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.ExtendedComboBoxModel;
import com.eviware.soapui.support.swing.ModelItemListKeyListener;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;
import com.eviware.soapui.ui.support.AbstractMockOperationDesktopPanel;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class RestMockActionDesktopPanel extends AbstractMockOperationDesktopPanel<RestMockAction>
{
	public RestMockActionDesktopPanel( RestMockAction mockOperation )
	{
		super( mockOperation );
	}

	@Override
	protected String getAddToMockOperationIconPath()
	{
		return "/addToRestMockAction.gif";
	}

	@Override
	protected Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.setLayout( new BorderLayout() );

		Box methodBox = Box.createHorizontalBox();
		methodBox.add( createMethodComboBox() );
		methodBox.add( Box.createHorizontalStrut( 10 ) );
		toolbar.add( methodBox, BorderLayout.WEST );

		toolbar.add( createResourcePathTextField(), BorderLayout.CENTER );

		return toolbar;
	}

	private JComponent createResourcePathTextField()
	{
		final JTextField resourcePathEditor = new JTextField(  );
		resourcePathEditor.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyReleased( KeyEvent e )
			{
				getModelItem().setResourcePath( resourcePathEditor.getText() );
			}
		} );
		return new TextPanelWithTopLabel( "Resource Path", getModelItem().getResourcePath(), resourcePathEditor );
	}

	private JComponent createMethodComboBox()
	{
		JPanel comboPanel = new JPanel( new BorderLayout(  ) );

		comboPanel.add( new JLabel( "Method" ), BorderLayout.NORTH );

		final JComboBox methodCombo = new JComboBox( HttpMethod.getMethods() );

		methodCombo.setSelectedItem( getModelItem().getMethod() );
		methodCombo.setToolTipText( "Set desired HTTP method" );
		methodCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				getModelItem().setMethod( ( HttpMethod )methodCombo.getSelectedItem() );
			}
		} );

		comboPanel.add( methodCombo, BorderLayout.SOUTH );

		return comboPanel;
	}

	protected String[] getAvailableDispatchTypes()
	{
		return new String[]{
			MockOperationDispatchStyleConfig.SEQUENCE.toString(),
			MockOperationDispatchStyleConfig.SCRIPT.toString()
		};
	}

}

