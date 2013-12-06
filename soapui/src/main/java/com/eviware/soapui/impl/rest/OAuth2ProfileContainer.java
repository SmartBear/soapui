package com.eviware.soapui.impl.rest;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;

import java.util.List;

public interface OAuth2ProfileContainer extends PropertyExpansionContainer
{

	public ModelItem getModelItem();

	public List<OAuth2Profile> getOAuth2ProfileList();

	void release();
}
