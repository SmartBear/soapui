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

package com.eviware.soapui.impl.wsdl.loadtest.strategy;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Burst LoadStrategy that pauses for a certain amount of time
 * 
 * @author Ole.Matzura
 */

public class BurstLoadStrategy extends AbstractLoadStrategy
{
	private static final String BURST_DURATION_ELEMENT = "burstDuration";
	private static final String BURST_DELAY_ELEMENT = "burstDelay";
	private static final int DEFAULT_BURST_DURATION = 10000;
	private static final int DEFAULT_BURST_DELAY = 60000;
	public static final String STRATEGY_TYPE = "Burst";
	private JPanel configPanel;

	private int burstDelay = DEFAULT_BURST_DELAY;
	private int burstDuration = DEFAULT_BURST_DURATION;
	private long startTime;
	private JTextField delayField;
	private JTextField durationField;
	private JLabel infoLabel;
	private long threadCount;

	public BurstLoadStrategy( WsdlLoadTest loadTest )
	{
		super( STRATEGY_TYPE, loadTest );

		burstDelay = DEFAULT_BURST_DELAY;
		burstDuration = DEFAULT_BURST_DURATION;
	}

	public BurstLoadStrategy( XmlObject config, WsdlLoadTest loadTest )
	{
		super( STRATEGY_TYPE, loadTest );

		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( config );
		burstDelay = reader.readInt( BURST_DELAY_ELEMENT, DEFAULT_BURST_DELAY );
		burstDuration = reader.readInt( BURST_DURATION_ELEMENT, DEFAULT_BURST_DURATION );
	}

	public void beforeLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		super.beforeLoadTest( loadTestRunner, context );
		startTime = System.currentTimeMillis();
		if( infoLabel != null )
			infoLabel.setText( "starting.." );

		WsdlLoadTest wsdlLoadTest = ( WsdlLoadTest )loadTestRunner.getLoadTest();
		threadCount = wsdlLoadTest.getThreadCount();
		wsdlLoadTest.setThreadCount( 0 );
	}

	public void recalculate( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		// get time passed since start of test
		long timePassed = System.currentTimeMillis() - startTime;

		if( loadTestRunner.getStatus() == Status.RUNNING )
		{
			WsdlLoadTest wsdlLoadTest = ( WsdlLoadTest )loadTestRunner.getLoadTest();
			String label = null;

			long mod = timePassed % ( burstDelay + burstDuration );
			if( mod < burstDelay )
			{
				wsdlLoadTest.setThreadCount( 0 );
				label = ( burstDelay - mod ) / 1000 + "s delay left";
			}
			else
			{
				wsdlLoadTest.setThreadCount( threadCount );
				label = ( ( burstDelay + burstDuration ) - mod ) / 1000 + "s burst left";
			}

			if( infoLabel != null && !infoLabel.getText().equals( label ) )
				infoLabel.setText( label );
		}
	}

	public void afterLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		if( infoLabel != null )
			infoLabel.setText( "" );
	}

	public JComponent getConfigurationPanel()
	{
		if( configPanel == null )
		{
			ButtonBarBuilder builder = new ButtonBarBuilder();

			infoLabel = new JLabel();
			delayField = new JTextField( 4 );
			UISupport.setPreferredHeight( delayField, 18 );
			delayField.setHorizontalAlignment( JTextField.RIGHT );
			delayField.setText( String.valueOf( burstDelay / 1000 ) );
			delayField.setToolTipText( "Sets the delay before each burst run in seconds" );
			delayField.getDocument().addDocumentListener( new DocumentListenerAdapter()
			{

				public void update( Document doc )
				{
					try
					{
						burstDelay = Integer.parseInt( delayField.getText() ) * 1000;
						notifyConfigurationChanged();
					}
					catch( NumberFormatException e )
					{
					}
				}
			} );

			builder.addFixed( new JLabel( "Burst Delay" ) );
			builder.addRelatedGap();

			builder.addFixed( delayField );
			builder.addRelatedGap();

			durationField = new JTextField( 4 );
			UISupport.setPreferredHeight( durationField, 18 );
			durationField.setHorizontalAlignment( JTextField.RIGHT );
			durationField.setText( String.valueOf( burstDuration / 1000 ) );
			durationField.setToolTipText( "Specifies the duration of a burst run in seconds" );
			durationField.getDocument().addDocumentListener( new DocumentListenerAdapter()
			{

				public void update( Document doc )
				{
					try
					{
						burstDuration = Integer.parseInt( durationField.getText() ) * 1000;
						notifyConfigurationChanged();
					}
					catch( NumberFormatException e )
					{
					}
				}
			} );

			builder.addFixed( new JLabel( "Burst Duration" ) );
			builder.addRelatedGap();
			builder.addFixed( durationField );
			builder.addRelatedGap();
			builder.addFixed( infoLabel );

			configPanel = builder.getPanel();
		}

		return configPanel;
	}

	public XmlObject getConfig()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( BURST_DELAY_ELEMENT, burstDelay );
		builder.add( BURST_DURATION_ELEMENT, burstDuration );
		return builder.finish();
	}

	@Override
	public boolean allowThreadCountChangeDuringRun()
	{
		return false;
	}

	/**
	 * Factory for BurstLoadStrategy class
	 * 
	 * @author Ole.Matzura
	 */

	public static class Factory implements LoadStrategyFactory
	{
		public String getType()
		{
			return STRATEGY_TYPE;
		}

		public LoadStrategy build( XmlObject config, WsdlLoadTest loadTest )
		{
			return new BurstLoadStrategy( config, loadTest );
		}

		public LoadStrategy create( WsdlLoadTest loadTest )
		{
			return new BurstLoadStrategy( loadTest );
		}
	}
}
