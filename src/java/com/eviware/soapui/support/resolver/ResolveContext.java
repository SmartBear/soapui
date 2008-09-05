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

package com.eviware.soapui.support.resolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;

public class ResolveContext<T extends AbstractWsdlModelItem<?>>
{
	private List<PathToResolve> pathsToResolve = new ArrayList<PathToResolve>();
	private final T project;

	public ResolveContext(T project)
	{
		this.project = project;
	}

	public T getProject()
	{
		return project;
	}

	public PathToResolve addPathToResolve(AbstractWsdlModelItem<?> owner, String description, String path,
			SoapUIAction<AbstractWsdlModelItem<?>> defaultAction)
	{
		PathToResolve pathToResolve = new PathToResolve(owner, description, path, defaultAction);
		pathsToResolve.add(pathToResolve);
		return pathToResolve;
	}
	
	public PathToResolve addPathToResolve(AbstractWsdlModelItem<?> owner, String description, String path,
			Resolver resolver)
	{
		PathToResolve pathToResolve = new PathToResolve(owner, description, path, null);
		pathToResolve.addResolvers(resolver);
		pathsToResolve.add(pathToResolve);
		return pathToResolve;
	}

	public class PathToResolve
	{
		private final AbstractWsdlModelItem<?> owner;
		private final String description;
		private List<Resolver> resolvers = new ArrayList<Resolver>();
		private final String path;
		private SoapUIAction<AbstractWsdlModelItem<?>> defaultAction;
		private Resolver resolver;

		public PathToResolve(AbstractWsdlModelItem<?> owner, String description, String path, SoapUIAction<AbstractWsdlModelItem<?>> defaultAction)
		{
			this.owner = owner;
			this.description = description;
			this.path = path;
			this.defaultAction = defaultAction;
		}

		public SoapUIAction<?> getDefaultAction()
		{
			return defaultAction;
		}

		public void setDefaultAction(SoapUIAction<AbstractWsdlModelItem<?>> defaultAction)
		{
			this.defaultAction = defaultAction;
		}
		
		public void addResolvers( Resolver ... resolvers )
		{
			for( Resolver res : resolvers ) {
				this.resolvers.add(res);
			}
		}

		public AbstractWsdlModelItem<?> getOwner()
		{
			return owner;
		}

		public String getDescription()
		{
			return description;
		}

		public Resolver getResolver()
		{
			return resolver;
		}

		public String getPath()
		{
			return path;
		}

		public boolean resolve()
		{
			if (resolver != null)
				return resolver.resolve();

			if (defaultAction != null)
			{
				defaultAction.perform(owner, this);
				return true;
			}

			return false;
		}

		public void setResolver(Object resolveOrDefaultAction)
		{
			this.resolver = (Resolver) resolveOrDefaultAction;
		}

		public ArrayList<Resolver> getResolvers()
		{
			return (ArrayList<Resolver>) resolvers;
		}
	}

	public interface Resolver
	{
		public boolean resolve();

//		public boolean apply();

		public boolean isResolved();

		public String getResolvedPath();

		public Object getDescription();
		
	}

	public boolean isEmpty()
	{
		return pathsToResolve.isEmpty();
	}

	public List<PathToResolve> getPathsToResolve()
	{
		return pathsToResolve;
	}

	public int getUnresolvedCount()
	{
		int resultCnt = 0;

		for (PathToResolve ptr : pathsToResolve)
		{
			if (ptr.getResolver() == null || !ptr.getResolver().isResolved())
				resultCnt++;
		}

		return resultCnt;
	}

	public int apply()
	{
		int resultCnt = 0;

		for (PathToResolve ptr : pathsToResolve)
		{
			if (ptr.resolve())
				resultCnt++;
		}

		return resultCnt;
	}

	public abstract static class FileResolver implements Resolver
	{
		private String title;
		private String extension;
		private String fileType;
		private String current;
		private File result;
		private boolean resolved;

		public FileResolver(String title, String extension, String fileType, String current)
		{
			super();

			this.title = title;
			this.extension = extension;
			this.fileType = fileType;
			this.current = current;
		}

		public boolean isResolved()
		{
			return resolved;
		}

		public String getResolvedPath()
		{
			return result == null ? null : result.getAbsolutePath();
		}

		public abstract boolean apply(File newFile);

		public boolean resolve()
		{
			result = UISupport.getFileDialogs().open(this, title, extension, fileType, current);
			resolved = apply(result);
			return resolved;
		}
		
		public Object getDescription()
		{
			return title;
		}
		
	}

	public abstract static class DirectoryResolver implements Resolver
	{
		private String title;
		private String current;
		private File result;
		private boolean resolved;

		public DirectoryResolver(String title, String current)
		{
			super();

			this.title = title;
			this.current = current;
		}

		public boolean isResolved()
		{
			return resolved;
		}

		public String getResolvedPath()
		{
			return result == null ? null : result.getAbsolutePath();
		}

		public abstract boolean apply(File newFile);

		public boolean resolve()
		{
			result = UISupport.getFileDialogs().openDirectory(this, title,
					StringUtils.isNullOrEmpty(current) ? null : new File(current));
			resolved = apply(result);
			return resolved;
		}
		
		public Object getDescription()
		{
			return title;
		}
	}

}
