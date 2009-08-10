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

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.ComponentBag;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Simple LoadStrategy that just runs until canceled without any delays
 * 
 * @author Ole.Matzura
 */

public class VarianceLoadStrategy extends AbstractLoadStrategy
{
	private final static Logger log = Logger.getLogger( VarianceLoadStrategy.class );

	public static final String STRATEGY_TYPE = "Variance";
	private static final String INTERVAL_ELEMENT = "interval";
	private static final String VARIANCE_ELEMENT = "variance";
	private static final int DEFAULT_INTERVAL = 60000;
	private static final float DEFAULT_VARIANCE = 0.5F;

	private JPanel configPanel;

	private long interval = DEFAULT_INTERVAL;
	private float variance = DEFAULT_VARIANCE;
	private JTextField intervalField;
	private JTextField varianceField;
	private JLabel infoLabel;
	private long baseThreadCount;
	private long startTime;
	private ComponentBag stateDependantComponents = new ComponentBag();

	public VarianceLoadStrategy( WsdlLoadTest loadTest )
	{
		super( STRATEGY_TYPE, loadTest );

		interval = DEFAULT_INTERVAL;
		variance = DEFAULT_VARIANCE;
	}

	public VarianceLoadStrategy( XmlObject config, WsdlLoadTest loadTest )
	{
		super( STRATEGY_TYPE, loadTest );

		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( config );
		interval = reader.readLong( INTERVAL_ELEMENT, DEFAULT_INTERVAL );
		variance = reader.readFloat( VARIANCE_ELEMENT, DEFAULT_VARIANCE );
	}

	public JComponent getConfigurationPanel()
	{
		if( configPanel == null )
		{
			ButtonBarBuilder builder = new ButtonBarBuilder();

			intervalField = new JTextField( 4 );
			UISupport.setPreferredHeight( intervalField, 18 );
			intervalField.setHorizontalAlignment( JTextField.RIGHT );
			intervalField.setText( String.valueOf( interval / 1000 ) );
			intervalField.setToolTipText( "Sets the interval between variances in seconds" );
			intervalField.getDocument().addDocumentListener( new DocumentListenerAdapter()
			{

				public void update( Document doc )
				{
					try
					{
						interval = Long.parseLong( intervalField.getText() ) * 1000;
						notifyConfigurationChanged();
					}
					catch( NumberFormatException e )
					{
					}
				}
			} );

			builder.addFixed( new JLabel( "Interval" ) );
			builder.addRelatedGap();

			builder.addFixed( intervalField );
			builder.addRelatedGap();

			varianceField = new JTextField( 3 );
			UISupport.setPreferredHeight( varianceField, 18 );
			varianceField.setHorizontalAlignment( JTextField.RIGHT );
			varianceField.setText( String.valueOf( variance ) );
			varianceField.setToolTipText( "Specifies the relative magnitude of a variance" );
			varianceField.getDocument().addDocumentListener( new DocumentListenerAdapter()
			{

				public void update( Document doc )
				{
					try
					{
						variance = Float.parseFloat( varianceField.getText() );
						notifyConfigurationChanged();
					}
					catch( NumberFormatException e )
					{
					}
				}
			} );

			builder.addFixed( new JLabel( "Variance" ) );
			builder.addRelatedGap();
			builder.addFixed( varianceField );
			builder.addRelatedGap();

			infoLabel = new JLabel();
			builder.addFixed( infoLabel );

			configPanel = builder.getPanel();

			stateDependantComponents.add( intervalField );
			stateDependantComponents.add( varianceField );
		}

		return configPanel;
	}

	public XmlObject getConfig()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( INTERVAL_ELEMENT, interval );
		builder.add( VARIANCE_ELEMENT, variance );
		return builder.finish();
	}

	/**
	 * Factory for VarianceLoadStrategy class
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
			return new VarianceLoadStrategy( config, loadTest );
		}

		public LoadStrategy create( WsdlLoadTest loadTest )
		{
			return new VarianceLoadStrategy( loadTest );
		}
	}

	public void beforeLoadTest( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		super.beforeLoadTest( loadTestRunner, context );
		baseThreadCount = ( ( WsdlLoadTest )loadTestRunner.getLoadTest() ).getThreadCount();
		startTime = System.currentTimeMillis();
		stateDependantComponents.setEnabled( false );
	}

	public void recalculate( LoadTestRunner loadTestRunner, LoadTestRunContext context )
	{
		double timePassed = ( System.currentTimeMillis() - startTime ) % interval;
		float threadCount = baseThreadCount;

		// initial increase?
		double quarter = ( double )interval / 4;

		if( timePassed < quarter )
		{
			threadCount += ( int )Math.round( ( ( timePassed / quarter ) * variance * threadCount ) );
		}
		// decrease?
		else if( timePassed < quarter * 2 )
		{
			threadCount += ( int )Math.round( ( ( 1 - ( ( timePassed % quarter ) / quarter ) ) * variance * threadCount ) );
		}
		else if( timePassed < quarter * 3 )
		{
			threadCount -= ( int )Math.round( ( ( ( timePassed % quarter ) / quarter ) * variance * threadCount ) );
		}
		// final increase
		else
		{
			threadCount -= ( int )Math.round( ( ( 1 - ( ( timePassed % quarter ) / quarter ) ) * variance * threadCount ) );
		}

		if( threadCount < 1 )
			threadCount = 1;

		WsdlLoadTest wsdlLoadTest = ( ( WsdlLoadTest )loadTestRunner.getLoadTest() );
		if( wsdlLoadTest.getThreadCount() != ( int )threadCount )
		{
			log.debug( "Changing threadcount to " + threadCount );
			wsdlLoadTest.setThreadCount( ( int )threadCount );
		}
	}

	public void afterLoadTest( LoadTestRunner testRunner, LoadTestRunContext context )
	{
		WsdlLoadTest wsdlLoadTest = ( WsdlLoadTest )testRunner.getLoadTest();
		wsdlLoadTest.setThreadCount( baseThreadCount );
		stateDependantComponents.setEnabled( true );
	}

	public boolean allowThreadCountChangeDuringRun()
	{
		return false;
	}
}
