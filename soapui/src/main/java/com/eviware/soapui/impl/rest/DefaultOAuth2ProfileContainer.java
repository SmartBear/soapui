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

		buildOAuth2ProfileList(  );
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
	public ArrayList<String> getOAuth2ProfileNameList()
	{
		ArrayList<String> profileNameList = new ArrayList<String>();
		for( OAuth2Profile profile : getOAuth2ProfileList() )
		{
			profileNameList.add( profile.getName() );
		}
		return profileNameList;
	}

	@Override
	public OAuth2Profile getProfileByName( String profileName )
	{
		for( OAuth2Profile profile : getOAuth2ProfileList() )
		{
			if( profile.getName().equals( profileName ) )
			{
				return profile;
			}
		}
		return null;
	}

	@Override
	public void release()
	{
		//FIXME: Add implementation when we implement the GUI with listeners
	}

	@Override
	public OAuth2Profile addNewOAuth2Profile(String profileName)
	{
		OAuth2ProfileConfig profileConfig = configuration.addNewOAuth2Profile();
		profileConfig.setName( profileName );

		OAuth2Profile oAuth2Profile = new OAuth2Profile( this, profileConfig );
		buildOAuth2ProfileList( );

		return oAuth2Profile;
	}

	@Override
	public void removeProfile( String profileName )
	{
		for( int count = 0; count < configuration.sizeOfOAuth2ProfileArray(); count++ )
		{
			if( configuration.getOAuth2ProfileArray( count ).getName().equals( profileName ) )
			{
				configuration.removeOAuth2Profile( count );
				break;
			}
		}
		buildOAuth2ProfileList( );
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

	private void buildOAuth2ProfileList( )
	{
		oAuth2ProfileList.clear();
		for( OAuth2ProfileConfig profileConfig : configuration.getOAuth2ProfileList() )
		{
			oAuth2ProfileList.add( new OAuth2Profile( this, profileConfig ) );
		}
	}

}