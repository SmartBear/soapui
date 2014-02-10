package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.config.OAuth2ProfileContainerConfig;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;

import java.util.ArrayList;
import java.util.List;

public class DefaultOAuth2ProfileContainer implements OAuth2ProfileContainer
{
	private final WsdlProject project;
	private final OAuth2ProfileContainerConfig configuration;
	List<OAuth2Profile> oAuth2ProfileList = new ArrayList<OAuth2Profile>();

	public DefaultOAuth2ProfileContainer( WsdlProject project, OAuth2ProfileContainerConfig configuration )
	{
		this.project = project;
		this.configuration = configuration;

		// Pre load the container with an empty profile at the project initialization rather than in the request
		if( configuration.getOAuth2Profile() == null )
		{
			oAuth2ProfileList.add( new OAuth2Profile( this, configuration.addNewOAuth2Profile() ) );
		}
		else
		{
			oAuth2ProfileList.add( new OAuth2Profile( this, configuration.getOAuth2Profile() ) );
		}
	}

	@Override
	public WsdlProject getProject()
	{
		return project;
	}

	@Override
	public List<OAuth2Profile> getOAuth2ProfileList()
	{
		return oAuth2ProfileList;
	}

	@Override
	public void release()
	{
		//FIXME: Add implementation when we implement the GUI with listeners
	}

	@Override
	public OAuth2Profile addNewOAuth2Profile()
	{
		OAuth2Profile oAuth2Profile = new OAuth2Profile( this, configuration.addNewOAuth2Profile() );
		oAuth2ProfileList.add( oAuth2Profile );

		return oAuth2Profile;
	}

	@Override
	public OAuth2ProfileContainerConfig getConfig()
	{
		return configuration;
	}

	@Override
	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( project, this );

		for( OAuth2Profile oAuth2Profile : oAuth2ProfileList )
		{
			result.addAll( oAuth2Profile.getPropertyExpansions() );
		}

		return result.toArray();
	}

}