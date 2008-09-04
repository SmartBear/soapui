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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ResolveContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractPathPropertySupport 
{
	private final String propertyName;
	private final AbstractWsdlModelItem<?> modelItem;

	public AbstractPathPropertySupport(AbstractWsdlModelItem<?> modelItem, String propertyName)
	{
		this.modelItem = modelItem;
		this.propertyName = propertyName;
	}

	public String set(String value, boolean notify)
	{
		String old = get();
		value = PathUtils.relativizeResourcePath( value, modelItem );
		try
		{
			setPropertyValue(PathUtils.normalizePath( value ));
			if( notify )
				notifyUpdate(value, old);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return old;
	}
	
	public String get()
	{
		try
		{
			return getPropertyValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String getPropertyName()
	{
		return propertyName;
	}

	public AbstractWsdlModelItem<?> getModelItem()
	{
		return modelItem;
	}

	public abstract void setPropertyValue(String value) throws Exception;

	protected void notifyUpdate(String value, String old)
	{
		modelItem.notifyPropertyChanged( modelItem.getClass().getName() + "@" + propertyName, old, value );
	}

	public String expand(TestRunContext context)
	{
		try
		{
			return PathUtils.expandPath( getPropertyValue(), modelItem, context );
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String expand()
	{
		try
		{
			return PathUtils.resolveResourcePath( getPropertyValue(), modelItem );
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public String expandUrl()
	{
		String result = expand();
		try
		{
			if( PathUtils.isFilePath(result) && !result.startsWith( "file:" ))
         {
            result = new File( result ).toURI().toURL().toString();
         }
         else
         {
            result = new URL( result ).toString();
         }
      }
		catch (MalformedURLException e)
		{
			SoapUI.logError(e);
		}
		
		return result;
	}

	public abstract String getPropertyValue() throws Exception;

	public void resolveFile( ResolveContext context, String errorDescription)
	{
		resolveFile(context, errorDescription, null, null, true);
	}
	
	public void resolveFile(ResolveContext context, String errorDescription, String extension, String fileType, final boolean notify  )
	{
		String source = expand();
		if( StringUtils.hasContent(source) )
		{
			try
			{
				new URL( source );
			}
			catch( Exception e )
			{
				File file = new File( source );
				if( !file.exists())
				{
					context.addPathToResolve( modelItem, errorDescription, source, new ResolveContext.FileResolver( "Select File", 
							extension, fileType, file.getParent()) {
						
						@Override
						public boolean apply(File newFile)
						{
							set(newFile.getAbsolutePath(), notify);
							return true;
						}
					} );
				}
			}
		}
	}
	
	public void resolveFolder(ResolveContext context, String errorDescription, final boolean notify  )
	{
		String source = expand();
		if( StringUtils.hasContent(source) )
		{
			try
			{
				new URL( source );
			}
			catch( Exception e )
			{
				File file = new File( source );
				if( !file.exists() || !file.isDirectory())
				{
					context.addPathToResolve( modelItem, errorDescription, source, new ResolveContext.DirectoryResolver( "Select Directory", source )
					{
						@Override
						public boolean apply(File newFile)
						{
							set(newFile.getAbsolutePath(), notify);
							return true;
						}
					} );
				}
			}
		}
	}
}