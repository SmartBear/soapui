package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Guava function that expands properties in a string, using a ModelItem as context.
 */
public class PropertyExpansionFunction implements Function<String,String>
{

	private ModelItem contextModelItem;

	/**
	 * Constructs a function object
	 * @param contextModelItem the model item to be used as context
	 */
	public PropertyExpansionFunction( ModelItem contextModelItem )
	{
		this.contextModelItem = contextModelItem;
	}

	@Nullable
	@Override
	public String apply( @Nullable String unexpandedString )
	{
		if (unexpandedString == null)
		{
			return null;
		}
		return PropertyExpander.expandProperties(contextModelItem, unexpandedString);
	}
}
