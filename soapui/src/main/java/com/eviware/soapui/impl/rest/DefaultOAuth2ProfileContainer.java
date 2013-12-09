package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.config.OAuth2ProfileContainerConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;

import java.util.ArrayList;
import java.util.List;

public class DefaultOAuth2ProfileContainer implements OAuth2ProfileContainer
{
	private final ModelItem modelItem;
	private final OAuth2ProfileContainerConfig configuration;
	List<OAuth2Profile> oAuth2ProfileList = new ArrayList<OAuth2Profile>(  );

	public DefaultOAuth2ProfileContainer( ModelItem modelItem, OAuth2ProfileContainerConfig configuration )
	{
		this.modelItem = modelItem;
		this.configuration = configuration;

		for( OAuth2ProfileConfig oAuth2ProfileConfig: configuration.getOAuth2ProfileList() )
		{
			oAuth2ProfileList.add( new OAuth2Profile( this, oAuth2ProfileConfig ) );
		}
	}

	@Override
	public ModelItem getModelItem()
	{
		return modelItem;
	}

	@Override
	public List<OAuth2Profile> getOAuth2ProfileList()
	{
		return oAuth2ProfileList;
	}

	@Override
	public void release()
	{

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
		PropertyExpansionsResult result = new PropertyExpansionsResult( getModelItem(), this );

		for( OAuth2Profile oAuth2Profile : oAuth2ProfileList )
		{
			result.addAll( oAuth2Profile.getPropertyExpansions() );
		}

		return result.toArray();
	}

}