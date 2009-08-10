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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

import com.eviware.soapui.config.PropertyTransferConfig;
import com.eviware.soapui.config.PropertyTransfersStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowTransferValuesResultsAction;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.resolver.ResolveContext;

/**
 * WsdlTestStep for transferring values from a WsdlTestRequest response to a
 * WsdlTestRequest request using XPath expressions
 * 
 * @author Ole.Matzura
 */

public class PropertyTransfersTestStep extends WsdlTestStepWithProperties implements XPathReferenceContainer
{
	private PropertyTransfersStepConfig transferStepConfig;
	private boolean canceled;
	private List<PropertyTransfer> transfers = new ArrayList<PropertyTransfer>();
	private ImageIcon failedIcon;
	private ImageIcon okIcon;

	public PropertyTransfersTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, true, forLoadTest );

		if( !forLoadTest )
		{
			okIcon = UISupport.createImageIcon( "/value_transfer.gif" );
			failedIcon = UISupport.createImageIcon( "/value_transfer_failed.gif" );
			setIcon( okIcon );
		}
	}

	@Override
	public void afterLoad()
	{
		TestStepConfig config = getConfig();

		if( config.getConfig() != null )
		{
			transferStepConfig = ( PropertyTransfersStepConfig )config.getConfig().changeType(
					PropertyTransfersStepConfig.type );
			for( int c = 0; c < transferStepConfig.sizeOfTransfersArray(); c++ )
			{
				transfers.add( new PropertyTransfer( this, transferStepConfig.getTransfersArray( c ) ) );
			}
		}
		else
		{
			transferStepConfig = ( PropertyTransfersStepConfig )config.addNewConfig().changeType(
					PropertyTransfersStepConfig.type );
		}

		super.afterLoad();
	}

	public PropertyTransfersStepConfig getTransferConfig()
	{
		return transferStepConfig;
	}

	@Override
	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );

		transferStepConfig = ( PropertyTransfersStepConfig )config.getConfig().changeType(
				PropertyTransfersStepConfig.type );
		for( int c = 0; c < transferStepConfig.sizeOfTransfersArray(); c++ )
		{
			transfers.get( c ).setConfigOnMove( transferStepConfig.getTransfersArray( c ) );
		}
	}

	public TestStepResult run( TestCaseRunner runner, TestCaseRunContext context )
	{
		return run( runner, context, null );
	}

	public TestStepResult run( TestCaseRunner runner, TestCaseRunContext context, PropertyTransfer transfer )
	{
		PropertyTransferResult result = new PropertyTransferResult();
		canceled = false;

		long startTime = System.currentTimeMillis();

		for( int c = 0; c < transfers.size(); c++ )
		{
			PropertyTransfer valueTransfer = transfers.get( c );
			if( ( transfer != null && transfer != valueTransfer ) || valueTransfer.isDisabled() )
				continue;

			try
			{
				if( canceled )
				{
					result.setStatus( TestStepStatus.CANCELED );
					result.setTimeTaken( System.currentTimeMillis() - startTime );
					return result;
				}

				String[] values = valueTransfer.transferProperties( context );
				if( values != null && values.length > 0 )
				{
					String name = valueTransfer.getName();
					result.addMessage( "Performed transfer [" + name + "]" );
					result.addTransferResult( valueTransfer, values );
				}
			}
			catch( PropertyTransferException e )
			{
				result.addMessage( "Error performing transfer [" + valueTransfer.getName() + "] - " + e.getMessage() );
				result.addTransferResult( valueTransfer, new String[] { e.getMessage() } );

				if( transfers.get( c ).getFailOnError() )
				{
					result.setError( e );
					result.setStatus( TestStepStatus.FAILED );
					result.setTimeTaken( System.currentTimeMillis() - startTime );

					if( failedIcon != null )
						setIcon( failedIcon );

					return result;
				}
			}
		}

		if( okIcon != null )
			setIcon( okIcon );

		result.setStatus( TestStepStatus.OK );
		result.setTimeTaken( System.currentTimeMillis() - startTime );

		return result;
	}

	@Override
	public boolean cancel()
	{
		canceled = true;
		return canceled;
	}

	public int getTransferCount()
	{
		return transferStepConfig.sizeOfTransfersArray();
	}

	public PropertyTransfer getTransferAt( int index )
	{
		return transfers.get( index );
	}

	public PropertyTransfer addTransfer( String name )
	{
		PropertyTransfer transfer = new PropertyTransfer( this, transferStepConfig.addNewTransfers() );
		transfer.setName( name );
		transfer.setFailOnError( true );
		transfers.add( transfer );
		return transfer;
	}

	public void removeTransferAt( int index )
	{
		transfers.remove( index ).release();
		transferStepConfig.removeTransfers( index );
	}

	public TestStepResult createFailedResult( String message )
	{
		PropertyTransferResult result = new PropertyTransferResult();
		result.setStatus( TestStepStatus.FAILED );
		result.addMessage( message );

		return result;
	}

	@Override
	public void release()
	{
		super.release();

		for( PropertyTransfer transfer : transfers )
		{
			transfer.release();
		}
	}

	public class PropertyTransferResult extends WsdlTestStepResult
	{
		private List<PropertyTransferConfig> transfers = new ArrayList<PropertyTransferConfig>();
		private List<String[]> values = new ArrayList<String[]>();
		private boolean addedAction;

		public PropertyTransferResult()
		{
			super( PropertyTransfersTestStep.this );
		}

		public void addTransferResult( PropertyTransfer transfer, String[] values )
		{
			// save a copy, so we dont mirror changes
			transfers.add( ( PropertyTransferConfig )transfer.getConfig().copy() );
			this.values.add( values );
		}

		@Override
		public ActionList getActions()
		{
			if( !addedAction )
			{
				addAction( new ShowTransferValuesResultsAction( this ), true );
				addedAction = true;
			}

			return super.getActions();
		}

		public int getTransferCount()
		{
			return transfers == null ? 0 : transfers.size();
		}

		public PropertyTransferConfig getTransferAt( int index )
		{
			return transfers == null ? null : transfers.get( index );
		}

		public String[] getTransferredValuesAt( int index )
		{
			return values == null ? null : values.get( index );
		}

		@Override
		public void discard()
		{
			super.discard();

			transfers = null;
			values = null;
		}

		@Override
		public void writeTo( PrintWriter writer )
		{
			super.writeTo( writer );

			if( !isDiscarded() )
			{
				writer.println( "----------------------------------------------------" );
				for( int c = 0; c < transfers.size(); c++ )
				{
					PropertyTransferConfig transfer = transfers.get( c );
					writer.println( transfer.getName() + " transferred [" + Arrays.toString( values.get( c ) ) + "] from ["
							+ transfer.getSourceStep() + "." + transfer.getSourceType() + "] to [" + transfer.getTargetStep()
							+ "." + transfer.getTargetType() + "]" );
					if( transfer.getSourcePath() != null )
					{
						writer.println( "------------ source path -------------" );
						writer.println( transfer.getSourcePath() );
					}
					if( transfer.getTargetPath() != null )
					{
						writer.println( "------------ target path -------------" );
						writer.println( transfer.getTargetPath() );
					}
				}
			}
		}
	}

	public PropertyTransfer getTransferByName( String name )
	{
		for( int c = 0; c < getTransferCount(); c++ )
		{
			PropertyTransfer transfer = getTransferAt( c );
			if( transfer.getName().equals( name ) )
				return transfer;
		}

		return null;
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		for( PropertyTransfer transfer : transfers )
		{
			result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, transfer, "sourcePath" ) );
			result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, transfer, "targetPath" ) );
		}

		return result.toArray( new PropertyExpansion[result.size()] );
	}

	@Override
	public boolean hasProperties()
	{
		return false;
	}

	public XPathReference[] getXPathReferences()
	{
		List<XPathReference> result = new ArrayList<XPathReference>();

		for( PropertyTransfer transfer : transfers )
		{
			if( StringUtils.hasContent( transfer.getSourcePath() ) )
				result.add( new XPathReferenceImpl( "Source path for " + transfer.getName() + " PropertyTransfer in "
						+ getName(), transfer.getSourceProperty(), transfer, "sourcePath" ) );

			if( StringUtils.hasContent( transfer.getTargetPath() ) )
				result.add( new XPathReferenceImpl( "Target path for " + transfer.getName() + " PropertyTransfer in "
						+ getName(), transfer.getTargetProperty(), transfer, "targetPath" ) );
		}

		return result.toArray( new XPathReference[result.size()] );
	}

	@Override
	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );

		for( PropertyTransfer pTransfer : transfers )
		{
			pTransfer.resolve( context, this );

		}
	}
}
