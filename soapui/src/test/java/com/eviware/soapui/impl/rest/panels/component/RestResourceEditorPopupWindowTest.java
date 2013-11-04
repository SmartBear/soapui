package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.impl.rest.RestResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static com.eviware.soapui.utils.ModelItemFactory.makeRestResource;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Unit tests for the RestResourceEditorPopupWindow class.
 */
public class RestResourceEditorPopupWindowTest
{

	private RestResource parentResource;
	private RestResource childResource;
	private RestResource grandchildResource;

	@Before
	public void setUp() throws Exception
	{
		parentResource = makeRestResource();
		parentResource.setPath( "the_parent" );
		childResource = parentResource.addNewChildResource( "child", "the_child" );
		grandchildResource = childResource.addNewChildResource( "grandchild", "the_grandchild" );
	}

	@Test
	public void displaysCorrectNumberOfFields() throws Exception
	{
		assertThat( new RestResourceEditorPopupWindow( parentResource, parentResource ).restSubResourceTextFields,
				is( aCollectionWithSize( 1 )));
		assertThat( new RestResourceEditorPopupWindow( childResource, childResource ).restSubResourceTextFields,
				is( aCollectionWithSize( 2 )));
		assertThat( new RestResourceEditorPopupWindow( grandchildResource, grandchildResource ).restSubResourceTextFields,
				is( aCollectionWithSize( 3 )));
	}

	@Test
	public void displaysPathsOfResourcesInFields() throws Exception
	{
		RestResourceEditorPopupWindow popupWindow = new RestResourceEditorPopupWindow( grandchildResource, grandchildResource );
		List<RestResourceEditorPopupWindow.RestSubResourceTextField> resourceTextFields = popupWindow.restSubResourceTextFields;
		assertThat(resourceTextFields.get(0).getTextField().getText(), is( parentResource.getPath() ));
		assertThat(resourceTextFields.get(1).getTextField().getText(), is( childResource.getPath() ));
		assertThat(resourceTextFields.get(2).getTextField().getText(), is( grandchildResource.getPath() ));
	}

	@Test
	public void basePathNotAddedToResourceFields() throws Exception
	{
		final String basePath = "/base";
		parentResource.getInterface().setBasePath( basePath );
		RestResourceEditorPopupWindow popupWindow = new RestResourceEditorPopupWindow( grandchildResource, grandchildResource );

		List<RestResourceEditorPopupWindow.RestSubResourceTextField> resourceTextFields = popupWindow.restSubResourceTextFields;
		assertThat(resourceTextFields.get(0).getTextField().getText(), not( containsString( basePath ) ));
		assertThat(resourceTextFields.get(1).getTextField().getText(), not( containsString( basePath ) ));
		assertThat(resourceTextFields.get(2).getTextField().getText(), not( containsString( basePath ) ));
	}

	@Test
	public void displaysFieldForBasePath() throws Exception
	{
		final String basePath = "/base";
		parentResource.getInterface().setBasePath( basePath );
		RestResourceEditorPopupWindow popupWindow = new RestResourceEditorPopupWindow( parentResource, parentResource );

		assertThat(popupWindow.basePathTextField, is( notNullValue() ));
		assertThat(popupWindow.basePathTextField.getText(), is( basePath ));
	}
}
