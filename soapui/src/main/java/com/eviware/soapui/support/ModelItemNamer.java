package com.eviware.soapui.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;

import java.util.List;

/**
 * Utility class to create unique names for model items
 *
 * Creates a new name from the baseName and the next available number.
 *
 * Example:
 * 	Given
 *	 		baseName: "Project"
 * 		items: "Project 1", "Project 2", "Project 3", "Project 5"
 * 	Then
 * 		Returns the name "Project 6"
 *
 * @author Anders Jaensson
 * @author Prakash Jat
 */
public class ModelItemNamer
{
	private ModelItemNamer()
	{
	}

	public static String createName( String baseName, List<? extends ModelItem> modelItems )
	{
		int maxExistingIndex = 0;
		for( ModelItem modelItem : modelItems )
		{
			String name = modelItem.getName();
			if( name.contains( baseName ) )
			{
				try
				{
					int beginIndex = name.indexOf( baseName ) + baseName.length();
					int indexInProjectName = Integer.parseInt( name.substring( beginIndex ).trim() );
					if( indexInProjectName > maxExistingIndex )
					{
						maxExistingIndex = indexInProjectName;
					}
				}
				catch( Exception e )
				{
					//Do nothing, at worst it will create the modelItem with same name
				}
			}
		}

		return baseName + " " + ( ++maxExistingIndex );
	}
}