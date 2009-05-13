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
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics.Statistic;
import com.eviware.soapui.impl.wsdl.loadtest.data.actions.ExportSamplesHistoryAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

/**
 * DesktopPanel for StatisticsHistory Graphs
 * 
 * @author Ole.Matzura
 */

public class StatisticsHistoryDesktopPanel extends DefaultDesktopPanel
{
	private JPanel panel;
	private final WsdlLoadTest loadTest;
	private JStatisticsHistoryGraph historyGraph;
	private JButton exportButton;
	private JComboBox selectStatisticCombo;
	private StatisticsHistoryDesktopPanel.InternalPropertyChangeListener propertyChangeListener;
	private JComboBox resolutionCombo;

	public StatisticsHistoryDesktopPanel( WsdlLoadTest loadTest )
	{
		super( "Statistics History for [" + loadTest.getName() + "]", null, null );
		this.loadTest = loadTest;

		propertyChangeListener = new InternalPropertyChangeListener();
		loadTest.addPropertyChangeListener( WsdlLoadTest.NAME_PROPERTY, propertyChangeListener );

		buildUI();
	}

	private void buildUI()
	{
		historyGraph = new JStatisticsHistoryGraph( loadTest );

		JScrollPane scrollPane = new JScrollPane( historyGraph );
		scrollPane.getViewport().setBackground( Color.WHITE );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );

		panel = UISupport.buildPanelWithToolbarAndStatusBar( buildToolbar(), scrollPane, historyGraph.getLegend() );
		panel.setPreferredSize( new Dimension( 600, 400 ) );
	}

	private JComponent buildToolbar()
	{
		exportButton = UISupport.createToolbarButton( new ExportSamplesHistoryAction( historyGraph ) );

		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.addSpace( 5 );
		toolbar.addLabeledFixed( "Select Statistic:", buildSelectStatisticCombo() );
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
		long resolution = historyGraph.getResolution();
		resolutionCombo.setSelectedItem( resolution == 0 ? "data" : String.valueOf( resolution ) );
		resolutionCombo.addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent e )
			{
				try
				{
					String value = resolutionCombo.getSelectedItem().toString();
					long resolution = value.equals( "data" ) ? 0 : Long.parseLong( value );
					if( resolution != historyGraph.getResolution() )
					{
						historyGraph.setResolution( resolution );
					}
				}
				catch( Exception ex )
				{
					long resolution = historyGraph.getResolution();
					resolutionCombo.setSelectedItem( resolution == 0 ? "data" : String.valueOf( resolution ) );
				}
			}
		} );
		return resolutionCombo;
	}

	private JComponent buildSelectStatisticCombo()
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement( Statistic.AVERAGE );
		model.addElement( Statistic.TPS );
		model.addElement( Statistic.ERRORS );
		model.addElement( Statistic.BPS );

		selectStatisticCombo = new JComboBox( model );
		selectStatisticCombo.addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent e )
			{
				historyGraph.setStatistic( Statistic.valueOf( selectStatisticCombo.getSelectedItem().toString() ) );
			}
		} );

		return selectStatisticCombo;
	}

	public JComponent getComponent()
	{
		return panel;
	}

	public boolean onClose( boolean canCancel )
	{
		loadTest.removePropertyChangeListener( WsdlLoadTest.NAME_PROPERTY, propertyChangeListener );
		historyGraph.release();

		return super.onClose( canCancel );
	}

	private final class InternalPropertyChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			setTitle( "Statistics History for [" + loadTest.getName() + "]" );
		}
	}
}
