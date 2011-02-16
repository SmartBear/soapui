package com.eviware.soapui.security.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

/**
 * Holder for SecurityCheckPameters, which are request parameters on which security check is 
 * applied. 
 * @author robert
 *
 */
public class SecurityCheckedParameterHolder
{

	private AbstractSecurityCheck securityCheck;
	private CheckedParametersListConfig paramsConfig;

	private List<SecurityCheckedParameter> params = new ArrayList<SecurityCheckedParameter>();
	private Map<String, SecurityCheckedParameter> paramsMap = new HashMap<String, SecurityCheckedParameter>();

	private Set<SecurityCheckParameterListener> listeners = new HashSet<SecurityCheckParameterListener>();

	public SecurityCheckedParameterHolder( AbstractSecurityCheck securityCheck,
			CheckedParametersListConfig chekedPameters )
	{
		this.securityCheck = securityCheck;
		this.paramsConfig = chekedPameters;

		for( CheckedParameterConfig param : paramsConfig.getParametersList() )
		{
			addParameter( param );
		}
	}

	public SecurityCheckedParameter addParameter( CheckedParameterConfig param )
	{
		SecurityCheckedParameter result = new SecurityCheckedParameter( param );
		params.add( result );
		paramsMap.put( result.getName().toUpperCase(), result );

		fireParameterAdded( result );

		return result;
	}

	public void removeParameter( SecurityCheckedParameter parameter )
	{

		paramsConfig.removeParameters( params.indexOf( parameter ) );

		params.remove( parameter );
		paramsMap.remove( parameter.getName().toUpperCase() );

		fireParameterRemoved( parameter );
	}

	public void fireParameterAdded( SecurityCheckedParameter parameter )
	{
		for( SecurityCheckParameterListener listener : listeners )
			listener.parameterAdded( parameter );
	}

	public void fireParameterRemoved( SecurityCheckedParameter parameter )
	{
		for( SecurityCheckParameterListener listener : listeners )
			listener.parameterRemoved( parameter );
	}

	public List<SecurityCheckedParameter> getPropertyList()
	{
		return params;
	}
	
	

}
