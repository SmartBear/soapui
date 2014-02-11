package com.smartbear.soapui.utils;

import com.google.common.collect.Lists;
import com.smartbear.soapui.utils.fest.WorkspaceUtils;
import org.apache.commons.lang.StringUtils;
import org.fest.swing.exception.LocationUnavailableException;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTreeFixture;
import org.fest.swing.fixture.JTreeNodeFixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectUtils
{
	public static final int TTL = 5;

	public static JTreeNodeFixture getTreeNode( FrameFixture rootWindow, String path )
	{
		JTreeFixture tree = WorkspaceUtils.getNavigatorPanel( rootWindow ).tree();

		waitUntilPathIsExpanded( tree, path, TTL );

		return tree.node( path );
	}


	private static void waitUntilPathIsExpanded( JTreeFixture tree, String path, int timeToLoop )
	{
		try
		{
			sleepForAWhile( 500 );
			String parentPath = path.substring( 0, path.lastIndexOf( '/' ) );
			expandPathRecursively( tree, parentPath );
		}
		catch( LocationUnavailableException e)
		{
			if( timeToLoop < 1 )
			{
				throw e;
			}
			waitUntilPathIsExpanded( tree, path, timeToLoop - 1 );
		}

	}

	private static void expandPathRecursively( JTreeFixture treeFixture, String parentPath )
	{
		for( String node : allRecursivePaths( parentPath ) )
		{
			treeFixture.expandPath( node );
		}
	}

	private static List<String> allRecursivePaths( String fullPath )
	{
		List<String> nodes = Arrays.asList( fullPath.split( "\\/" ) );
		List<String> result = new ArrayList<String>();

		for( int i = 0; i < nodes.size(); i++)
		{
			result.add( getSubPath( nodes, i ) );
		}

		return result;
	}

	private static String getSubPath( List<String> nodes, int lastNodeIndex )
	{
		List<String> subPaths = nodes.subList( 0, lastNodeIndex + 1 );
		return StringUtils.join( subPaths, '/' );
	}

	private static void sleepForAWhile( int milliSeconds )
	{
		try
		{
			Thread.sleep( milliSeconds );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}

}
