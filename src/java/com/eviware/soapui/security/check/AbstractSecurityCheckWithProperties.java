package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;

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

	private SecurityCheckedParameterHolder parameterHolder;

	public AbstractSecurityCheckWithProperties( TestStep testStep, SecurityCheckConfig config, ModelItem parent,
			String icon )
	{
		super( testStep, config, parent, icon );

		setParameterHolder( new SecurityCheckedParameterHolder( this, config.getChekedPameters() ) );
	}

	public SecurityCheckedParameterHolder getParameterHolder()
	{
		return this.parameterHolder;
	}

	protected void setParameterHolder( SecurityCheckedParameterHolder parameterHolder )
	{
		this.parameterHolder = parameterHolder;
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
					result.add( new XPathReferenceImpl( "SecurityCheck Parameter " + param.getName(),
							( ( WsdlTestRequestStep )t ).getOperation(), true, param, "xPath" ) );
			}
		}

		return result.toArray( new XPathReference[result.size()] );
	}

	@Override
	public void updateSecurityConfig( SecurityCheckConfig config )
	{
		super.updateSecurityConfig( config );

		if( getParameterHolder() != null && getConfig().getChekedPameters() != null )
		{
			getParameterHolder().updateConfig( config.getChekedPameters() );
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
				param.setXPath( source.getXPath() );
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
			return getParameterHolder().addParameter( newLabel, source.getName(), source.getXPath(), source.isChecked() );
		}
	}

}
