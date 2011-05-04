package com.eviware.soapui.security.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AttachmentHolder
{

	List<AttachmentElement> list;

	public void addElement( File file, String contentType, Boolean enabled, Boolean cached )
	{
		if( list == null )
		{
			list = new ArrayList<AttachmentElement>();
		}

		list.add( new AttachmentElement( file, enabled, cached ) );
	}

	public void removeElement( int i )
	{
		if( list != null )
		{
			list.remove( i );
		}
	}

	public int size()
	{
		if( list != null )
		{
			return list.size();
		}
		else
		{
			return 0;
		}
	}

	public void clear()
	{
		if( list != null )
		{
			list.clear();
		}
	}

	public List<AttachmentElement> getList()
	{
		return list;
	}

}
