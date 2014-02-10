package com.eviware.soapui.model.tree.nodes;

import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProjectTreeNodeTest
{
	@Test
	public void shouldBeConstructedWithRestMockServices()
	{
		RestMockService restMockService = mock( RestMockService.class );
		when( restMockService.getPropertyNames() ).thenReturn( new String[]{} );

		Project project = mock(Project.class);
		when( project.getRestMockServiceCount() ).thenReturn( 1 );
		when( project.getRestMockServiceAt( 0 ) ).thenReturn( restMockService );
		when( project.isOpen() ).thenReturn( true );
		when( project.getPropertyNames() ).thenReturn( new String[]{} );

		WorkspaceTreeNode workspaceNode = mock(WorkspaceTreeNode.class);
		SoapUITreeModel soapUITreeModel = mock( SoapUITreeModel.class );
		when( workspaceNode.getTreeModel() ).thenReturn( soapUITreeModel );

		ProjectTreeNode projectTreeNode = new ProjectTreeNode( project, workspaceNode );

		assertThat(projectTreeNode.getChildCount(), is(1));
		assertThat( projectTreeNode.getChildNode( 0 ), instanceOf(MockServiceTreeNode.class) );
	}
}
