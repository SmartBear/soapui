package com.eviware.soapui.security.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.model.security.SecurityCheckParameterHolderListener;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

/**
 * Holder for SecurityCheckPameters, which are request parameters on which
 * security check is applied.
 * 
 * @author robert
 * 
 */
public class SecurityCheckedParameterHolder extends SecurityCheckParameterListenerAdapter implements TestPropertyListener
{

	private AbstractSecurityCheck securityCheck;
	private CheckedParametersListConfig paramsConfig;

	private List<SecurityCheckedParameter> params = new ArrayList<SecurityCheckedParameter>();
	private Map<String, SecurityCheckedParameter> paramsMap = new HashMap<String, SecurityCheckedParameter>();

	private Set<SecurityCheckParameterHolderListener> listeners = new HashSet<SecurityCheckParameterHolderListener>();

	public SecurityCheckedParameterHolder( AbstractSecurityCheck securityCheck,
			CheckedParametersListConfig chekedPameters )
	{
		this.securityCheck = securityCheck;
		this.paramsConfig = chekedPameters;

		for( CheckedParameterConfig param : paramsConfig.getParametersList() )
		{
			addParameter( param );
		}
		
		securityCheck.getTestStep().addTestPropertyListener( this );
	}

	SecurityCheckedParameter addParameter( CheckedParameterConfig param )
	{
		SecurityCheckedParameter result = new SecurityCheckedParameterImpl( param );
		params.add( result );
		paramsMap.put( result.getLabel().toUpperCase(), result );

		fireParameterAdded( result );

		return result;
	}

	public SecurityCheckedParameter addParameter( String label )
	{
		if( paramsMap.get( label ) != null )
			return paramsMap.get( label );

		CheckedParameterConfig newParameterConfig = paramsConfig.addNewParameters();
		SecurityCheckedParameterImpl newParameter = new SecurityCheckedParameterImpl( newParameterConfig );
		newParameter.setLabel( label );
		params.add( newParameter );
		paramsMap.put( newParameter.getLabel().toUpperCase(), newParameter );

		fireParameterAdded( newParameter );

		return newParameter;
	}
	
	public boolean addParameter( String label, String name, String xpath, boolean used )
	{
		if( paramsMap.get( label.toUpperCase() ) != null )
			return false;

		CheckedParameterConfig newParameterConfig = paramsConfig.addNewParameters();
		SecurityCheckedParameterImpl newParameter = new SecurityCheckedParameterImpl( newParameterConfig );
		newParameter.setLabel( label );
		newParameter.setName( name );
		newParameter.setXPath( xpath );
		newParameter.setChecked( true );
		params.add( newParameter );
		paramsMap.put( newParameter.getLabel().toUpperCase(), newParameter );

		fireParameterAdded( newParameter );

		return true;
	}

	public void removeParameter( SecurityCheckedParameter parameter )
	{

		int index = params.indexOf( parameter );
		params.remove( parameter );
		paramsMap.remove( parameter.getLabel().toUpperCase() );

		paramsConfig.removeParameters( index );

		fireParameterRemoved( parameter );
	}

	public void fireParameterAdded( SecurityCheckedParameter parameter )
	{
		for( SecurityCheckParameterHolderListener listener : listeners )
			listener.parameterAdded( parameter );
	}

	public void fireParameterRemoved( SecurityCheckedParameter parameter )
	{
		for( SecurityCheckParameterHolderListener listener : listeners )
			listener.parameterRemoved( parameter );
	}

	public List<SecurityCheckedParameter> getParameterList()
	{
		return params;
	}

	/*
	 * Need to keep track on parameter label change.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.support.SecurityCheckParameterListenerAdapter
	 * #parameterLabelChanged
	 * (com.eviware.soapui.model.security.SecurityCheckedParameter,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void parameterLabelChanged( SecurityCheckedParameter parameter, String oldLabel, String newLabel )
	{
		SecurityCheckedParameter param = paramsMap.get( oldLabel );
		paramsMap.remove( oldLabel );
		paramsMap.put( newLabel, param );
	}

	/**
	 * This method returns parameter based on its name.
	 * 
	 * @param paramName
	 * @return parameter
	 */
	public SecurityCheckedParameter getParametarByName( String paramName )
	{
		for( SecurityCheckedParameter param : params )
			if( param.getName().equals( paramName ) )
				return param;
		return null;
	}

	public void removeParameters( int[] selected )
	{
		ArrayList<SecurityCheckedParameter> paramsToRemove = new ArrayList<SecurityCheckedParameter>();
		for( int index : selected )
			paramsToRemove.add( params.get( index ) );

		for( SecurityCheckedParameter param : paramsToRemove )
			removeParameter( param );
	}

	@Override
	public void propertyAdded( String name )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyMoved( String name, int oldIndex, int newIndex )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyRemoved( String name )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyRenamed( String oldName, String newName )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyValueChanged( String name, String oldValue, String newValue )
	{
		// TODO Auto-generated method stub
		
	}
	
	public void release() {
		securityCheck.getTestStep().removeTestPropertyListener( this );
	}

}
