package com.eviware.soapui.support;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class ModelItemNamerTest
{
	private static final String DEFAULT_PROJECT_NAME = "";
	private Workspace workspace;

	@Before
	public void setUp() throws IOException, XmlException
	{
		workspace = mock( Workspace.class );
	}

	@Test
	public void createsFirstRestProjectWithNameRESTProject1() throws Exception
	{
		mockWorkspaceProjects(  );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 1";
		assertThat( ModelItemNamer.createName( DEFAULT_PROJECT_NAME, workspace.getProjectList() ), Is.is( expectedFirstProjectName ) );
	}

	@Test
	public void createsProjectWithNameRESTProject2IfProjectWithNameRESTProject1AlreadyExists() throws Exception
	{
		mockWorkspaceProjects( DEFAULT_PROJECT_NAME + " 1" );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 2";
		assertThat( ModelItemNamer.createName( DEFAULT_PROJECT_NAME, workspace.getProjectList() ), Is.is( expectedFirstProjectName ) );
	}

	@Test
	public void createsProjectWithNameRESTProject3IfProjectsWithNameRESTProject1AndRESTProject2AlreadyExist()
			throws Exception
	{
		mockWorkspaceProjects( DEFAULT_PROJECT_NAME + " 1", DEFAULT_PROJECT_NAME + " 2" );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 3";
		assertThat( ModelItemNamer.createName( DEFAULT_PROJECT_NAME, workspace.getProjectList() ), Is.is( expectedFirstProjectName ) );
	}

	@Test
	public void createsProjectWithNameRESTProject4IfProjectsExistWithNameRESTProject1AndRESTProject3() throws Exception
	{
		mockWorkspaceProjects( DEFAULT_PROJECT_NAME + "1", DEFAULT_PROJECT_NAME + "3" );  //REST Project1, REST Project3
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 4";
		assertThat( ModelItemNamer.createName( DEFAULT_PROJECT_NAME, workspace.getProjectList() ),Is.is( expectedFirstProjectName ) );
	}

	@Test
	public void doesNotThrowAnExceptionIfProjectExistsWithNameRESTProject3x() throws Exception
	{
		mockWorkspaceProjects( DEFAULT_PROJECT_NAME + " 3x" );
		String expectedFirstProjectName = DEFAULT_PROJECT_NAME + " 1";
		assertThat( ModelItemNamer.createName( DEFAULT_PROJECT_NAME, workspace.getProjectList() ), Is.is( expectedFirstProjectName ) );
	}

	private void mockWorkspaceProjects( String... projectNames )
	{
		List projectList = new ArrayList<Project>(  );
		for( String projectName : projectNames )
		{
			Project project = Mockito.mock( Project.class );
			when( project.getName() ).thenReturn( projectName );
			projectList.add( project );
		}
		when( workspace.getProjectList() ).thenReturn( projectList );
	}
}
