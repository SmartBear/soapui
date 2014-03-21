package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.config.MockOperationDispatchStyleConfig;
import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.panels.request.TextPanelWithTopLabel;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.AbstractMockOperationDesktopPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

		toolbar.add( createActionButton( new ShowOnlineHelpAction( getModelItem().getHelpUrl() ), true ), BorderLayout.EAST );

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

