/*
 *  soapUI Pro, copyright (C) 2007-2009 eviware software ab 
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class JdbcRequestTestStepDesktopPanel extends ModelItemDesktopPanel<JdbcRequestTestStep>
{
	private JPanel configPanel;
	private JXTable logTable;
//	private JList propertyList;
	private JButton runButton;
//	private JButton removeButton;
	private JLabel statusLabel;
   private JInspectorPanel inspectorPanel;
   private JdbcRequestTestStep jdbcRequestTestStep;
   
   public JdbcRequestTestStepDesktopPanel( JdbcRequestTestStep modelItem )
	{
		super( modelItem );
		jdbcRequestTestStep = modelItem;
		
		buildUI();
		
//		JdbcRequestTestStep jdbcRequest = modelItem.getJdbcRequest();
//		runButton.setEnabled( jdbcRequest != null && modelItem.getPropertyCount() > 0 );
////		removeButton.setEnabled( propertyList.getSelectedIndex() != -1 );
//		
//		if( jdbcRequest != null )
//		{
//			comboBox.setSelectedItem( jdbcRequest.getType() );
//			configPanel.add( jdbcRequest.getComponent(), BorderLayout.CENTER );
//		}
//		else
//		{
//			comboBox.setSelectedItem( null );
//		}
		
	}

	private void buildUI()
	{
      inspectorPanel = JInspectorPanelFactory.build( buildContent());
//		JComponentInspector<JComponent> insp = inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildPreview(), "Data Log", 
//					"Read values", true ) );
		inspectorPanel.setDefaultDividerLocation( 0.7F  );
//		inspectorPanel.activate( insp );
		
		add( inspectorPanel.getComponent(), BorderLayout.CENTER);
		add( buildStatusBar(), BorderLayout.SOUTH );
		setPreferredSize( new Dimension( 600, 450 ));
	}

	private Component buildStatusBar()
	{
		JPanel statusBar = new JPanel( new BorderLayout() );
		statusBar.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ));
		statusLabel = new JLabel(" ");
		statusBar.add( statusLabel, BorderLayout.WEST );
		return statusBar;
	}

	private JComponent buildContent()
	{
		JSplitPane split = UISupport.createHorizontalSplit( buildConfigPanel(), buildResultPanel() );
		split.setDividerLocation( 180 );
		return split;
	}
	
	private JComponent buildConfigPanel()
	{
		configPanel = UISupport.addTitledBorder( new JPanel( new BorderLayout() ), "Configuration" );
		configPanel.add( jdbcRequestTestStep.getComponent(), BorderLayout.CENTER );
		return configPanel;

	}
	private JComponent buildResultPanel()
	{
		
//		JdbcResponseEditor jdbcResponseEditor = new JdbcResponseEditor();
//		panel.add( toolbar, BorderLayout.NORTH );
//		panel.add( configPanel, BorderLayout.CENTER );
		
		return new JPanel();
	}
	
	public boolean dependsOn(ModelItem modelItem)
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getTestCase() ||
				modelItem == getModelItem().getTestCase().getTestSuite() ||
				modelItem == getModelItem().getTestCase().getTestSuite().getProject();
	}

	public boolean onClose( boolean canCancel )
	{
		configPanel.removeAll();
		inspectorPanel.release();

		return release();
	}

	
	
}
