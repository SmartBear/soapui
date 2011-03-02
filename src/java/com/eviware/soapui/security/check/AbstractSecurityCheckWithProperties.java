package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;

/**
 * 
 * These are for Security Checks that mutate parameters.
 * 
 * @author robert
 *
 */
public abstract class AbstractSecurityCheckWithProperties extends AbstractSecurityCheck implements XPathReferenceContainer
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
		
		if( getParameterHolder() != null && getConfig().getChekedPameters() != null)
		{
			getParameterHolder().updateConfig( config.getChekedPameters() );
		}
	}
}
