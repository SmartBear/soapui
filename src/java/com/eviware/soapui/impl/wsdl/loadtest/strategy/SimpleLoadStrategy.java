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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * LoadStrategy allowing maximum runs and request delays
 * 
 * @author Ole.Matzura
 */

public class SimpleLoadStrategy extends AbstractLoadStrategy
{
	private static final int DEFAULT_TEST_DELAY = 1000;
	private static final float DEFAULT_RANDOM_FACTOR = 0.5F;
	public static final String STRATEGY_TYPE = "Simple";

	private int testDelay = DEFAULT_TEST_DELAY;
	private float randomFactor = DEFAULT_RANDOM_FACTOR;

	private JPanel configPanel;
	private JTextField testDelayField;
	private JTextField randomFactorField;

	public SimpleLoadStrategy( XmlObject config, WsdlLoadTest loadTest )
	{
		super( STRATEGY_TYPE, loadTest );

		if( config != null )
		{
			XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( config );
			testDelay = reader.readInt( "testDelay", DEFAULT_TEST_DELAY );
			randomFactor = reader.readFloat( "randomFactor", DEFAULT_RANDOM_FACTOR );
		}
	}

	public XmlObject getConfig()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "testDelay", testDelay );
		builder.add( "randomFactor", randomFactor );
		return builder.finish();
	}

	public void beforeTestCase( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
			TestCaseRunContext runContext )
	{
		int delay = calculateDelay( testDelay );
		if( delay == 0 )
			return;
		try
		{
			Thread.sleep( delay );
		}
		catch( InterruptedException e )
		{
			SoapUI.logError( e );
		}
	}

	public int calculateDelay( int delay )
	{
		if( delay == 0 || randomFactor == 0 )
			return delay;

		int fixDelay = ( int )( ( float )delay * ( 1 - randomFactor ) );
		int randDelay = ( int )( randomFactor == 0 ? 0 : ( float )( delay - fixDelay ) * Math.random() );
		return fixDelay + randDelay;
	}

	public JComponent getConfigurationPanel()
	{
		if( configPanel == null )
		{
			ButtonBarBuilder builder = new ButtonBarBuilder();

			testDelayField = new JTextField( 5 );
			UISupport.setPreferredHeight( testDelayField, 18 );
			testDelayField.setHorizontalAlignment( JTextField.RIGHT );
			testDelayField.setText( String.valueOf( testDelay ) );
			testDelayField.setToolTipText( "Sets the delay between each test run in milliseconds" );
			testDelayField.getDocument().addDocumentListener( new ConfigDocumentListener() );

			builder.addFixed( new JLabel( "Test Delay" ) );
			builder.addRelatedGap();

			builder.addFixed( testDelayField );
			builder.addRelatedGap();

			randomFactorField = new JTextField( 4 );
			UISupport.setPreferredHeight( randomFactorField, 18 );
			randomFactorField.setHorizontalAlignment( JTextField.RIGHT );
			randomFactorField.setText( String.valueOf( randomFactor ) );
			randomFactorField
					.setToolTipText( "Specifies the relative amount of randomization for delay (0 = no random, 1 = all random)" );
			randomFactorField.getDocument().addDocumentListener( new ConfigDocumentListener() );

			builder.addFixed( new JLabel( "Random" ) );
			builder.addRelatedGap();
			builder.addFixed( randomFactorField );

			configPanel = builder.getPanel();
		}

		return configPanel;
	}

	private final class ConfigDocumentListener extends DocumentListenerAdapter
	{
		public void update( Document document )
		{
			try
			{
				if( document == testDelayField.getDocument() )
					testDelay = Integer.parseInt( testDelayField.getText() );
				if( document == randomFactorField.getDocument() )
					randomFactor = Float.parseFloat( randomFactorField.getText().replace( ',', '.' ) );

				notifyConfigurationChanged();
			}
			catch( NumberFormatException e )
			{
			}
		}
	}

	public int getTestDelay()
	{
		return testDelay;
	}

	public float getRandomFactor()
	{
		return randomFactor;
	}

	/**
	 * Factory for SimpleLoadStrategy class
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
			return new SimpleLoadStrategy( config, loadTest );
		}

		public LoadStrategy create( WsdlLoadTest loadTest )
		{
			return new SimpleLoadStrategy( null, loadTest );
		}
	}
}
