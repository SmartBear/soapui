package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.support.FailedSecurityMessageExchange;
import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * 
 * These are for Security Checks that mutate parameters.
 * 
 * @author robert
 * 
 */
public abstract class AbstractSecurityCheckWithProperties extends AbstractSecurityCheck implements
		XPathReferenceContainer
{

	public static final String SECURITY_CHANGED_PARAMETERS = "SecurityChangedParameters";
	private SecurityCheckedParameterHolder parameterHolder;

	public AbstractSecurityCheckWithProperties( TestStep testStep, SecurityCheckConfig config, ModelItem parent,
			String icon )
	{
		super( testStep, config, parent, icon );

		setParameterHolder( new SecurityCheckedParameterHolder( this, config.getCheckedPameters() ) );
	}

	public SecurityCheckedParameterHolder getParameterHolder()
	{
		return this.parameterHolder;
	}

	protected void setParameterHolder( SecurityCheckedParameterHolder parameterHolder )
	{
		this.parameterHolder = parameterHolder;
	}

	@Override
	public void copyConfig( SecurityCheckConfig config )
	{
		super.copyConfig( config );
		getConfig().setCheckedPameters( config.getCheckedPameters() );
		setParameterHolder( new SecurityCheckedParameterHolder( this, config.getCheckedPameters() ) );
	}

	public XPathReference[] getXPathReferences()
	{
		List<XPathReference> result = new ArrayList<XPathReference>();

		for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
		{
			TestStep t = getTestStep();
			if( t instanceof WsdlTestRequestStep )
			{
				if( param != null )
					result.add( new XPathReferenceImpl( "SecurityCheck Parameter " + param.getLabel() + " in \""
							+ getTestStep().getName() + "\"", ( ( WsdlTestRequestStep )t ).getOperation(), true, param,
							"xpath" ) );
			}
		}

		return result.toArray( new XPathReference[result.size()] );
	}

	@Override
	public void updateSecurityConfig( SecurityCheckConfig config )
	{
		super.updateSecurityConfig( config );

		if( getParameterHolder() != null && getConfig().getCheckedPameters() != null )
		{
			getParameterHolder().updateConfig( config.getCheckedPameters() );
		}
	}

	public SecurityCheckedParameter getParameterAt( int i )
	{
		return getParameterHolder().getParameterList().get( i );
	}

	public SecurityCheckedParameter getParameterByLabel( String label )
	{
		return parameterHolder.getParametarByLabel( label );
	}

	public boolean importParameter( SecurityCheckedParameter source, boolean overwrite, String newLabel )
	{
		// TODO double check if this needs to return newly added parameter
		// also maybe add label checking to holder.addParam...
		// and use overwrite also
		SecurityCheckedParameterImpl param = ( SecurityCheckedParameterImpl )getParameterHolder().getParametarByLabel(
				newLabel );
		if( param != null )
		{
			if( overwrite )
			{
				param.setName( source.getName() );
				param.setXpath( source.getXpath() );
				param.setChecked( source.isChecked() );
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return getParameterHolder().addParameter( newLabel, source.getName(), source.getXpath(), source.isChecked() );
		}
	}

	/**
	 * @param message
	 * @param testStep
	 */
	protected void reportSecurityCheckException( String message )
	{
		getSecurityCheckRequestResult().setMessageExchange( new FailedSecurityMessageExchange() );
		getSecurityCheckRequestResult().setStatus( ResultStatus.FAILED );
		getSecurityCheckRequestResult().addMessage( message );
	}

	protected void createMessageExchange( StringToStringMap updatedParams, MessageExchange message )
	{
		message.getProperties().put( SECURITY_CHANGED_PARAMETERS, updatedParams.toXml() );
		getSecurityCheckRequestResult().setMessageExchange( message );
	}

}
