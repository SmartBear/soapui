package com.eviware.soapui.impl.support;


import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.model.environment.Endpoint;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Test;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EndpointsComboBoxModelConcurrencyTest
{

	@Test
	public void forceConcurrencyProblem() throws SoapUIException, InterruptedException
	{
		final RestRequest restRequest = ModelItemFactory.makeRestRequest();
		final EndpointsComboBoxModel comboBoxModel = new EndpointsComboBoxModel( restRequest );

		final List<ListDataListener> listeners = new ArrayList<ListDataListener>(  );
		for(int i = 0; i < 100; i++)
		{
			ListDataListener listDataListener = createListDataListener();
			listeners.add( listDataListener );
			comboBoxModel.addListDataListener( listDataListener );
		}


		Runnable disruptorRunner = new Runnable()
		{
			@Override
			public void run()
			{
				for( int i = 0; i < 1000; i++)
				{
					try
					{
						int randomNumber = Math.abs( new Random( System.currentTimeMillis() ).nextInt()%100 );
						ListDataListener listener = listeners.get( randomNumber );
						System.err.println( " Adding listers # " + i );
						comboBoxModel.removeListDataListener( listener );
						comboBoxModel.addListDataListener( listener );
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			}
		};




		final EndpointSupport endpointSupport = new EndpointSupport();

		Runnable addEndpointRunner = new Runnable()
		{
			public void run()
			{
				for( int i = 0; i < 1000; i++ )
				{

					try
					{
						System.err.println( " Adding Endpoint # " + i );
						endpointSupport.setEndpoint( ( AbstractHttpRequest )restRequest, "http://localhost:800" + i );
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}

			}
		};

		Thread disruptorThread = new Thread( disruptorRunner );
		Thread endpointThread = new Thread( addEndpointRunner );

		disruptorThread.start();
		endpointThread.start();

		disruptorThread.join();
		endpointThread.join();
	}

	private ListDataListener createListDataListener()
	{
		return new ListDataListener()
		{
			@Override
			public void intervalAdded( ListDataEvent e )
			{

			}

			@Override
			public void intervalRemoved( ListDataEvent e )
			{

			}

			@Override
			public void contentsChanged( ListDataEvent e )
			{

			}
		};
	}

}
