package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.OAuth2ProfileContainerConfig;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;

import java.util.ArrayList;
import java.util.List;

public interface OAuth2ProfileContainer extends PropertyExpansionContainer
{

	public WsdlProject getProject();

	public List<OAuth2Profile> getOAuth2ProfileList();

	public void release();

	public OAuth2ProfileContainerConfig getConfig();

	public OAuth2Profile addNewOAuth2Profile( String profileName );

	public void removeProfile(String profileName);

	ArrayList<String> getOAuth2ProfileNameList();

	OAuth2Profile getProfileByName( String profileName );
}
