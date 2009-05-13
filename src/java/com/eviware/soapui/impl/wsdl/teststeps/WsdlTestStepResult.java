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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

/**
 * Default implementation of TestStepResult interface
 * 
 * @author Ole.Matzura
 */

public class WsdlTestStepResult implements TestStepResult
{
	private static final String[] EMPTY_MESSAGES = new String[0];
	private final WsdlTestStep testStep;
	private List<String> messages = new ArrayList<String>();
	private Throwable error;
	private TestStepStatus status = TestStepStatus.UNKNOWN;
	private long timeTaken;
	private long timeStamp;
	private long size;
	private DefaultActionList actionList;
	private long startTime;
	private boolean discarded;

	private static DefaultActionList discardedActionList = new DefaultActionList( null );

	static
	{
		discardedActionList.setDefaultAction( new AbstractAction()
		{
			public void actionPerformed( ActionEvent arg0 )
			{
				UISupport.showErrorMessage( "Result has been discarded" );
			}
		} );
	}

	public WsdlTestStepResult( WsdlTestStep testStep )
	{
		this.testStep = testStep;
		timeStamp = System.currentTimeMillis();
	}

	public TestStepStatus getStatus()
	{
		return status;
	}

	public void setStatus( TestStepStatus status )
	{
		this.status = status;
	}

	public TestStep getTestStep()
	{
		return testStep;
	}

	public ActionList getActions()
	{
		if( isDiscarded() )
			return discardedActionList;

		if( actionList == null )
		{
			actionList = new DefaultActionList( testStep.getName() );
			actionList.setDefaultAction( new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					if( getMessages().length > 0 )
					{
						StringBuffer buf = new StringBuffer();
						if( getError() != null )
							buf.append( getError().toString() ).append( "\r\n" );

						for( String s : getMessages() )
							buf.append( s ).append( "\r\n" );

						UISupport.showExtendedInfo( "TestStep Result", "Step [" + testStep.getName() + "] ran with status ["
								+ getStatus() + "]", buf.toString(), null );
					}
					else if( getError() != null )
					{
						UISupport.showExtendedInfo( "TestStep Result", "Step [" + testStep.getName() + "] ran with status ["
								+ getStatus() + "]", getError().toString(), null );
					}
					else
					{
						UISupport.showInfoMessage( "Step [" + testStep.getName() + "] ran with status [" + getStatus() + "]",
								"TestStep Result" );
					}
				}
			} );
		}

		return actionList;
	}

	public void addAction( Action action, boolean isDefault )
	{
		if( isDiscarded() )
			return;

		if( actionList == null )
		{
			actionList = new DefaultActionList( testStep.getName() );
		}

		actionList.addAction( action );
		if( isDefault )
			actionList.setDefaultAction( action );
	}

	public Throwable getError()
	{
		return error;
	}

	public void setError( Throwable error )
	{
		this.error = error;
	}

	public String[] getMessages()
	{
		return messages == null ? EMPTY_MESSAGES : messages.toArray( new String[messages.size()] );
	}

	public void addMessage( String message )
	{
		if( messages != null )
			messages.add( message );
	}

	public long getTimeTaken()
	{
		return timeTaken;
	}

	public void setTimeTaken( long timeTaken )
	{
		this.timeTaken = timeTaken;
	}

	public long getTimeStamp()
	{
		return timeStamp;
	}

	public void setTimeStamp( long timeStamp )
	{
		this.timeStamp = timeStamp;
	}

	public void setSize( long size )
	{
		this.size = size;
	}

	public long getSize()
	{
		return size;
	}

	public void writeTo( PrintWriter writer )
	{
		writer.println( "Status: " + getStatus() );
		writer.println( "Time Taken: " + getTimeTaken() );
		writer.println( "Size: " + getSize() );
		writer.println( "Timestamp: " + new Date( getTimeStamp() ).toString() );
		writer.println( "TestStep: " + getTestStep().getName() );
		if( error != null )
			writer.println( "Error:" + error.toString() );

		if( messages != null )
		{
			writer.println( "\r\n----------------- Messages ------------------------------" );
			for( String message : messages )
				if( message != null )
					writer.println( message );
		}

		if( isDiscarded() )
			writer.println( "Result has been Discarded!" );
	}

	public void startTimer()
	{
		startTime = System.nanoTime();
	}

	public void stopTimer()
	{
		timeTaken = ( ( System.nanoTime() - startTime ) / 1000000 );
	}

	public void discard()
	{
		discarded = true;

		messages = null;
		error = null;
		actionList = null;
	}

	public boolean isDiscarded()
	{
		return discarded;
	}

	public void addMessages( String[] messages )
	{
		if( this.messages != null )
			this.messages.addAll( Arrays.asList( messages ) );
	}
}
