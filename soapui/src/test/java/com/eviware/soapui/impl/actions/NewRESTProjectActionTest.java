package com.eviware.soapui.impl.actions;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static com.eviware.soapui.impl.actions.NewRESTProjectAction.DEFAULT_PROJECT_NAME;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-10-04
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
public class NewRESTProjectActionTest
{
	private Workspace workspace;
	private NewRESTProjectAction newRESTProjectAction;

	@Before
	public void setUp() throws IOException, XmlException
	{
		workspace = mock( Workspace.class );
		newRESTProjectAction = new NewRESTProjectAction();
	}

	@Test
	public void createsFirstRestProjectWithNameRESTProject1() throws Exception
	{
		mockWorkspaceProjects(  );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 1";
		assertThat( newRESTProjectAction.createDefaultProjectName( workspace ), Is.is( expectedFirstProjectName ) );
	}

	@Test
	public void createsProjectWithNameRESTProject2IfProjectWithNameRESTProject1AlreadyExists() throws Exception
	{
		mockWorkspaceProjects( DEFAULT_PROJECT_NAME + " 1" );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 2";
		assertThat( newRESTProjectAction.createDefaultProjectName( workspace ), Is.is( expectedFirstProjectName ) );
	}

	@Test
	public void createsProjectWithNameRESTProject3IfProjectsWithNameRESTProject1AndRESTProject2AlreadyExist()
			throws Exception
	{
		mockWorkspaceProjects( DEFAULT_PROJECT_NAME + " 1", DEFAULT_PROJECT_NAME + " 2" );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 3";
		assertThat( newRESTProjectAction.createDefaultProjectName( workspace ), Is.is( expectedFirstProjectName ) );
	}

	@Test
	public void createsProjectWithNameRESTProject2IfProjectsExistWithNameRESTProject1AndRESTProject3() throws Exception
	{
		mockWorkspaceProjects( DEFAULT_PROJECT_NAME + " 1", DEFAULT_PROJECT_NAME + " 3" );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 2";
		assertThat( newRESTProjectAction.createDefaultProjectName( workspace ), Is.is( expectedFirstProjectName ) );
	}

	private void mockWorkspaceProjects( String... projectNames )
	{
		for( String projectName : projectNames )
		{
			Project project = Mockito.mock( Project.class );
			when( workspace.getProjectByName( projectName ) ).thenReturn( project );
		}
	}
}
