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

import javax.swing.ImageIcon;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

/**
 * Base class for WsdlAssertions
 * 
 * @author Ole.Matzura
 */

public abstract class WsdlMessageAssertion extends AbstractModelItem implements PropertyExpansionContainer,
		TestAssertion
{
	private TestAssertionConfig assertionConfig;
	private final Assertable assertable;
	private AssertionStatus assertionStatus = AssertionStatus.UNKNOWN;
	private com.eviware.soapui.model.testsuite.AssertionError[] assertionErrors;
	private ImageIcon validIcon;
	private ImageIcon failedIcon;
	private ImageIcon unknownIcon;

	private final boolean cloneable;
	private final boolean configurable;
	private final boolean allowMultiple;
	private final boolean requiresResponseContent;

	protected WsdlMessageAssertion( TestAssertionConfig assertionConfig, Assertable modelItem, boolean cloneable,
			boolean configurable, boolean multiple, boolean requiresResponseContent )
	{
		this.assertionConfig = assertionConfig;
		this.assertable = modelItem;
		this.cloneable = cloneable;
		this.configurable = configurable;
		this.allowMultiple = multiple;
		this.requiresResponseContent = requiresResponseContent;

		validIcon = UISupport.createImageIcon( "/valid_assertion.gif" );
		failedIcon = UISupport.createImageIcon( "/failed_assertion.gif" );
		unknownIcon = UISupport.createImageIcon( "/unknown_assertion.gif" );
	}

	public XmlObject getConfiguration()
	{
		if( null == assertionConfig.getConfiguration() )
		{
			assertionConfig.addNewConfiguration();
		}

		return assertionConfig.getConfiguration();
	}

	public void setConfiguration( XmlObject configuration )
	{
		XmlObject oldConfig = assertionConfig.getConfiguration();
		assertionConfig.setConfiguration( configuration );
		notifyPropertyChanged( TestAssertion.CONFIGURATION_PROPERTY, oldConfig, configuration );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#getName()
	 */
	public String getName()
	{
		return assertionConfig.isSetName() ? assertionConfig.getName() : TestAssertionRegistry.getInstance()
				.getAssertionNameForType( assertionConfig.getType() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#getStatus()
	 */
	public AssertionStatus getStatus()
	{
		return isDisabled() ? AssertionStatus.UNKNOWN : assertionStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#getErrors()
	 */
	public AssertionError[] getErrors()
	{
		return isDisabled() ? null : assertionErrors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#isAllowMultiple()
	 */
	public boolean isAllowMultiple()
	{
		return allowMultiple;
	}

	public AssertionStatus assertResponse( MessageExchange messageExchange, SubmitContext context )
	{
		AssertionStatus oldStatus = assertionStatus;
		AssertionError[] oldErrors = getErrors();
		ImageIcon oldIcon = getIcon();

		if( isDisabled() )
		{
			assertionStatus = AssertionStatus.UNKNOWN;
			assertionErrors = null;
		}
		else if( !messageExchange.hasResponse() && requiresResponseContent )
		{
			assertionStatus = AssertionStatus.FAILED;
			assertionErrors = new com.eviware.soapui.model.testsuite.AssertionError[] { new com.eviware.soapui.model.testsuite.AssertionError(
					"null/empty response" ) };
		}
		else
		{
			try
			{
				internalAssertResponse( messageExchange, context );
				assertionStatus = AssertionStatus.VALID;
				assertionErrors = null;
			}
			catch( AssertionException e )
			{
				assertionStatus = AssertionStatus.FAILED;
				assertionErrors = e.getErrors();
			}
			catch( Throwable e )
			{
				assertionStatus = AssertionStatus.FAILED;
				assertionErrors = new com.eviware.soapui.model.testsuite.AssertionError[] { new com.eviware.soapui.model.testsuite.AssertionError(
						e.getMessage() ) };
			}
		}

		notifyPropertyChanged( STATUS_PROPERTY, oldStatus, assertionStatus );
		notifyPropertyChanged( ERRORS_PROPERTY, oldErrors, assertionErrors );
		notifyPropertyChanged( ICON_PROPERTY, oldIcon, getIcon() );

		return assertionStatus;
	}

	protected abstract String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException;

	public AssertionStatus assertRequest( MessageExchange messageExchange, SubmitContext context )
	{
		AssertionStatus oldStatus = assertionStatus;
		ImageIcon oldIcon = getIcon();

		if( !messageExchange.hasRequest( true ) )
		{
			assertionStatus = AssertionStatus.FAILED;
			assertionErrors = new com.eviware.soapui.model.testsuite.AssertionError[] { new com.eviware.soapui.model.testsuite.AssertionError(
					"null/empty request" ) };
		}
		else
		{
			try
			{
				internalAssertRequest( messageExchange, context );
				assertionStatus = AssertionStatus.VALID;
				assertionErrors = null;
			}
			catch( AssertionException e )
			{
				assertionStatus = AssertionStatus.FAILED;
				assertionErrors = e.getErrors();
			}
			catch( Throwable e )
			{
				e.printStackTrace();
				assertionStatus = AssertionStatus.FAILED;
				assertionErrors = new com.eviware.soapui.model.testsuite.AssertionError[] { new com.eviware.soapui.model.testsuite.AssertionError(
						e.getMessage() ) };
			}
		}

		notifyPropertyChanged( STATUS_PROPERTY, oldStatus, assertionStatus );
		notifyPropertyChanged( ICON_PROPERTY, oldIcon, getIcon() );

		return assertionStatus;
	}

	protected abstract String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#isConfigurable()
	 */
	public boolean isConfigurable()
	{
		return configurable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#isClonable()
	 */
	public boolean isClonable()
	{
		return cloneable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#configure()
	 */
	public boolean configure()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#getDescription()
	 */
	public String getDescription()
	{
		return getConfig().getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#getIcon()
	 */
	public ImageIcon getIcon()
	{
		switch( getStatus() )
		{
		case FAILED :
			return failedIcon;
		case UNKNOWN :
			return unknownIcon;
		case VALID :
			return validIcon;
		}

		return null;
	}

	public void updateConfig( TestAssertionConfig config )
	{
		this.assertionConfig = config;
	}

	public TestAssertionConfig getConfig()
	{
		return assertionConfig;
	}

	public Settings getSettings()
	{
		return assertable.getModelItem().getSettings();
	}

	public void release()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#getAssertable()
	 */
	public Assertable getAssertable()
	{
		return assertable;
	}

	public String getId()
	{
		if( !assertionConfig.isSetId() )
			assertionConfig.setId( ModelSupport.generateModelItemID() );

		return assertionConfig.getId();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		return null;
	}

	public void setName( String name )
	{
		String oldLabel = getLabel();

		String old = getName();
		assertionConfig.setName( name );
		notifyPropertyChanged( NAME_PROPERTY, old, name );

		String label = getLabel();
		if( !oldLabel.equals( label ) )
		{
			notifyPropertyChanged( LABEL_PROPERTY, oldLabel, label );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#getLabel()
	 */
	public String getLabel()
	{
		String name = getName();
		if( isDisabled() )
			return name + " (disabled)";
		else
			return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.impl.wsdl.teststeps.TestAssertion#isDisabled()
	 */
	public boolean isDisabled()
	{
		return getConfig().getDisabled();
	}

	public void setDisabled( boolean disabled )
	{
		String oldLabel = getLabel();

		boolean oldDisabled = isDisabled();
		if( oldDisabled == disabled )
			return;

		if( disabled )
		{
			getConfig().setDisabled( disabled );
		}
		else if( getConfig().isSetDisabled() )
		{
			getConfig().unsetDisabled();
		}

		String label = getLabel();
		if( !oldLabel.equals( label ) )
		{
			notifyPropertyChanged( LABEL_PROPERTY, oldLabel, label );
		}

		notifyPropertyChanged( DISABLED_PROPERTY, oldDisabled, disabled );
	}

	public ModelItem getParent()
	{
		return assertable.getModelItem();
	}

	public boolean isValid()
	{
		return getStatus() == AssertionStatus.VALID;
	}

	public boolean isFailed()
	{
		return getStatus() == AssertionStatus.FAILED;
	}

	public void prepare( TestCaseRunner testRunner, TestCaseRunContext testRunContext ) throws Exception
	{
		assertionStatus = AssertionStatus.UNKNOWN;
	}

	public void resolve( ResolveContext<?> context )
	{
	}
}
