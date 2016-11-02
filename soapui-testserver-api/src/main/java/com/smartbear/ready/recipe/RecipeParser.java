package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;

/**
 * Converts a Test recipe string into a SoapUI project that can be executed locally.
 */
public interface RecipeParser {

    WsdlProject parse(String recipeJson) throws Exception;
}
