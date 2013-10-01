package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the RestResourceDesktopPanel class.
 */
public class RestResourceDesktopPanelTest
{

	private RestResource parentResource;
	private RestResource childResource;
	private RestResourceDesktopPanel resourceDesktopPanel;

	@Before
	public void setUp() throws Exception
	{

		parentResource = ModelItemFactory.makeRestResource();
		parentResource.setPath( "/parent" );

		childResource = parentResource.addNewChildResource( "child", "the_child" );
		resourceDesktopPanel = new RestResourceDesktopPanel( childResource );
	}

	@Test
	public void displaysFullPathForChildResource() throws Exception
	{
		assertThat( resourceDesktopPanel.pathTextField.getText(), is( childResource.getFullPath() ));
	}

	@Test
	public void changesOnlyChildPathWhenUpdatingPathField() throws Exception
	{
		String originalParentPath = parentResource.getFullPath();
		String newChildPath = "new_cool_path";
		resourceDesktopPanel.pathTextField.setText( "/new_parent/" + newChildPath);

		assertThat(childResource.getPath(), is(newChildPath));
		assertThat( parentResource.getPath(), is(originalParentPath) );
	}


}
