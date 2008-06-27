/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */


package com.eviware.soapui.support.dnd.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;

public class DragAndDropSupport
{

	public static boolean copyTestStep( WsdlTestStep source, WsdlTestCase target, int defaultPosition )
	{
		String name = UISupport.prompt( "Enter name for copied TestStep", "Copy TestStep",  
					target == source.getTestCase() ? "Copy of " + source.getName() : source.getName() );
		if( name == null)
			return false;
	
		WsdlProject sourceProject = source.getTestCase().getTestSuite().getProject();
		WsdlProject targetProject = target.getTestSuite().getProject();
		
		if( sourceProject != targetProject )
		{
			if( !importRequiredInterfaces( targetProject, new HashSet<WsdlInterface>( source.getRequiredInterfaces() ), "Copy Test Step" ))
				return false;
		}
	
		target.importTestStep( source, name, defaultPosition, true );
		
		return true;
	}

	public static boolean importRequiredInterfaces( WsdlProject project, Set<WsdlInterface> requiredInterfaces, String title )
	{
		if( requiredInterfaces.size() > 0 && project.getInterfaceCount() > 0 )
		{
			Map<String,WsdlInterface> bindings = new HashMap<String,WsdlInterface>();
			for( WsdlInterface iface : requiredInterfaces )
			{
				bindings.put( iface.getTechnicalId(), iface );
			}
			
			for( Interface iface : project.getInterfaceList() )
			{
				bindings.remove( iface.getTechnicalId());
			}
	
			requiredInterfaces.retainAll( bindings.values() );
		}
		
		if( requiredInterfaces.size() > 0 )
		{
			String msg = "Target project [" + project.getName()  +"] is missing required interfaces;\r\n\r\n";
			for( WsdlInterface iface : requiredInterfaces )
			{
				msg += iface.getName() + " [" + iface.getBindingName() + "]\r\n";
			}
			msg += "\r\nThese will be cloned to the target project as well";
			
			if( !UISupport.confirm( msg, title))
				return false;
			
			for( WsdlInterface iface : requiredInterfaces )
			{
				project.importInterface( iface, true, true );
			}
		}
		
		return true;
	}

	public static boolean moveTestStep( WsdlTestStep source, WsdlTestCase target, int defaultPosition )
	{
		if( source.getTestCase() == target )
		{
			int ix = target.getIndexOfTestStep( source );
			
			if( defaultPosition == -1 )
			{
				target.moveTestStep( ix, target.getTestStepCount()-ix );
			}
			else if( ix >= 0 && defaultPosition != ix )
			{
				int offset = defaultPosition - ix;
				if( offset > 0 )
					offset--;
				target.moveTestStep( ix, offset);
			}
		}
		else
		{
			String name = UISupport.prompt( "Enter name for moved TestStep", "Move TestStep", source.getName() );
			if( name == null)
				return false;
			
			WsdlProject sourceProject = source.getTestCase().getTestSuite().getProject();
			WsdlProject targetProject = target.getTestSuite().getProject();
			
			if( sourceProject != targetProject )
			{
				if( !importRequiredInterfaces( targetProject, new HashSet<WsdlInterface>( source.getRequiredInterfaces() ), "Move Test Step" ))
					return false;
			}
			
			target.importTestStep( source, name, defaultPosition, false );
			source.getTestCase().removeTestStep( source );
		}
		
		return true;
	}

}
