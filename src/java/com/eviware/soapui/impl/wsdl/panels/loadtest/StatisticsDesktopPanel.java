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

package com.eviware.soapui.impl.wsdl.panels.loadtest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.data.actions.ExportStatisticsHistoryAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

/**
 * DesktopPanel for Statistics Graphs
 * 
 * @author Ole.Matzura
 */

public class StatisticsDesktopPanel extends DefaultDesktopPanel
{
	private JPanel panel;
	private final WsdlLoadTest loadTest;
	private JStatisticsGraph statisticsGraph;
	private JButton exportButton;
	private SelectStepComboBoxModel selectStepComboBoxModel;
	private InternalPropertyChangeListener propertyChangeListener = new InternalPropertyChangeListener();
	private JComboBox resolutionCombo;

	public StatisticsDesktopPanel( WsdlLoadTest loadTest )
	{
		super( "Statistics for [" + loadTest.getName() + "]", null, null );
		this.loadTest = loadTest;

		loadTest.addPropertyChangeListener( propertyChangeListener );

		buildUI();
	}

	private void buildUI()
	{
		statisticsGraph = new JStatisticsGraph( loadTest );

		JScrollPane scrollPane = new JScrollPane( statisticsGraph );
		scrollPane.getViewport().setBackground( Color.WHITE );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );

		panel = UISupport.buildPanelWithToolbarAndStatusBar( buildToolbar(), scrollPane, statisticsGraph.getLegend() );
		panel.setPreferredSize( new Dimension( 600, 400 ) );
	}

	private JComponent buildToolbar()
	{
		exportButton = UISupport.createToolbarButton( new ExportStatisticsHistoryAction( statisticsGraph ) );

		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.addSpace( 5 );
		toolbar.addLabeledFixed( "Select Step:", buildSelectStepCombo() );
		toolbar.addUnrelatedGap();
		toolbar.addLabeledFixed( "Resolution:", buildResolutionCombo() );
		toolbar.addGlue();
		toolbar.addFixed( exportButton );
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.STATISTICSGRAPH_HELP_URL ) ) );

		return toolbar;
	}

	private JComponent buildResolutionCombo()
	{
		resolutionCombo = new JComboBox( new String[] { "data", "250", "500", "1000" } );
		resolutionCombo.setEditable( true );
		resolutionCombo.setToolTipText( "Sets update interval of graph in milliseconds" );
		long resolution = statisticsGraph.getResolution();
		resolutionCombo.setSelectedItem( resolution == 0 ? "data" : String.valueOf( resolution ) );
		resolutionCombo.addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent e )
			{
				try
				{
					String value = resolutionCombo.getSelectedItem().toString();
					long resolution = value.equals( "data" ) ? 0 : Long.parseLong( value );
					if( resolution != statisticsGraph.getResolution() )
					{
						statisticsGraph.setResolution( resolution );
					}
				}
				catch( Exception ex )
				{
					long resolution = statisticsGraph.getResolution();
					resolutionCombo.setSelectedItem( resolution == 0 ? "data" : String.valueOf( resolution ) );
				}
			}
		} );
		return resolutionCombo;
	}

	private JComponent buildSelectStepCombo()
	{
		selectStepComboBoxModel = new SelectStepComboBoxModel();
		JComboBox selectStepCombo = new JComboBox( selectStepComboBoxModel );
		selectStepCombo.setRenderer( new TestStepCellRenderer() );
		return selectStepCombo;
	}

	public JComponent getComponent()
	{
		return panel;
	}

	private final class InternalPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlLoadTest.NAME_PROPERTY ) )
			{
				setTitle( "Statistics for [" + loadTest.getName() + "]" );
			}
		}
	}

	private class SelectStepComboBoxModel extends AbstractListModel implements ComboBoxModel
	{
		private TestStep selectedStep;
		private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();

		public SelectStepComboBoxModel()
		{
			loadTest.getTestCase().getTestSuite().addTestSuiteListener( testSuiteListener );
		}

		public void setSelectedItem( Object anItem )
		{
			if( anItem == selectedStep )
				return;

			if( anItem == null || anItem.equals( "Total" ) )
				selectedStep = null;

			if( anItem instanceof TestStep )
			{
				selectedStep = ( TestStep )anItem;
			}

			statisticsGraph.setTestStep( selectedStep );
		}

		public Object getSelectedItem()
		{
			return selectedStep == null ? "Total" : selectedStep;
		}

		public int getSize()
		{
			return loadTest.getTestCase().getTestStepCount() + 1;
		}

		public Object getElementAt( int index )
		{
			return index == getSize() - 1 ? "Total" : loadTest.getTestCase().getTestStepAt( index );
		}

		private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
		{
			public void testStepAdded( TestStep testStep, int index )
			{
				if( testStep.getTestCase() == loadTest.getTestCase() )
				{
					fireIntervalAdded( SelectStepComboBoxModel.this, index, index );
				}
			}

			public void testStepRemoved( TestStep testStep, int index )
			{
				if( testStep.getTestCase() == loadTest.getTestCase() )
				{
					if( selectedStep == testStep )
					{
						setSelectedItem( null );
						fireContentsChanged( SelectStepComboBoxModel.this, -1, -1 );
					}

					fireIntervalRemoved( SelectStepComboBoxModel.this, index, index );
				}
			}
		}

		public void release()
		{
			loadTest.getTestCase().getTestSuite().removeTestSuiteListener( testSuiteListener );
		}
	}

	private final static class TestStepCellRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			JLabel label = ( JLabel )super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

			if( value instanceof TestStep )
				label.setText( ( ( TestStep )value ).getName() );

			return label;
		}
	}

	public boolean onClose( boolean canCancel )
	{
		selectStepComboBoxModel.release();
		loadTest.removePropertyChangeListener( propertyChangeListener );
		statisticsGraph.release();

		return super.onClose( canCancel );
	}
}
