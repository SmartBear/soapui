package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.smartbear.ready.recipe.teststeps.TestCaseStruct;

public interface ObjectRecipeParser extends RecipeParser{

    TestCaseStruct parseToTestCaseStruct(String recipeJson) throws Exception;

    WsdlProject parse(TestCaseStruct testCaseStruct) throws ParseException;
}
