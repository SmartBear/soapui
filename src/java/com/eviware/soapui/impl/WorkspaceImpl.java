/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SoapuiWorkspaceDocumentConfig;
import com.eviware.soapui.config.WorkspaceProjectConfig;
import com.eviware.soapui.config.WorkspaceProjectConfig.Status;
import com.eviware.soapui.config.WorkspaceProjectConfig.Type;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Default Workspace implementation
 * 
 * @author Ole.Matzura
 */

public class WorkspaceImpl extends AbstractModelItem implements Workspace
{
	private final static Logger log = Logger.getLogger( WorkspaceImpl.class );
	public static final MessageSupport messages = MessageSupport.getMessages( WorkspaceImpl.class );

	private List<Project> projectList = new ArrayList<Project>();
	private SoapuiWorkspaceDocumentConfig workspaceConfig;
	private String path = null;
	private Set<WorkspaceListener> listeners = new HashSet<WorkspaceListener>();
	private ImageIcon workspaceIcon;
	private XmlBeansSettingsImpl settings;
	private StringToStringMap projectOptions;
	private ResolveDialog resolver;

	public WorkspaceImpl( String path, StringToStringMap projectOptions ) throws XmlException, IOException
	{
		if( projectOptions == null )
		{
			this.projectOptions = new StringToStringMap();
		}
		else
		{
			this.projectOptions = projectOptions;
		}
		File file = new File( path );
		this.path = file.getAbsolutePath();
		loadWorkspace( file );
		workspaceIcon = UISupport.createImageIcon( "/workspace.gif" );

		for( WorkspaceListener listener : SoapUI.getListenerRegistry().getListeners( WorkspaceListener.class ) )
		{
			addWorkspaceListener( listener );
		}
	}

	public void switchWorkspace( File file ) throws SoapUIException
	{
		// check first if valid workspace file
		if( file.exists() )
		{
			try
			{
				SoapuiWorkspaceDocumentConfig.Factory.parse( file );
			}
			catch( Exception e )
			{
				throw new SoapUIException( messages.get( "FailedToLoadWorkspaceException" ) + e.toString() );
			}
		}

		fireWorkspaceSwitching();

		while( projectList.size() > 0 )
		{
			Project project = projectList.remove( 0 );
			try
			{
				fireProjectRemoved( project );
			}
			finally
			{
				project.release();
			}
		}

		try
		{
			String oldName = getName();

			loadWorkspace( file );
			this.path = file.getAbsolutePath();

			for( Project project : projectList )
			{
				fireProjectAdded( project );
			}

			notifyPropertyChanged( ModelItem.NAME_PROPERTY, oldName, getName() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		fireWorkspaceSwitched();
	}

	public void loadWorkspace( File file ) throws XmlException, IOException
	{
		if( file.exists() )
		{
			log.info( messages.get( "FailedToLoadWorkspaceFrom", file.getAbsolutePath() ) );
			workspaceConfig = SoapuiWorkspaceDocumentConfig.Factory.parse( file );
			if( workspaceConfig.getSoapuiWorkspace().getSettings() == null )
				workspaceConfig.getSoapuiWorkspace().addNewSettings();
			setPath( file.getAbsolutePath() );
			settings = new XmlBeansSettingsImpl( this, SoapUI.getSettings(), workspaceConfig.getSoapuiWorkspace()
					.getSettings() );

			boolean closeOnStartup = getSettings().getBoolean( UISettings.CLOSE_PROJECTS );
			List<WorkspaceProjectConfig> projects = workspaceConfig.getSoapuiWorkspace().getProjectList();
			for( int i = 0; i < projects.size(); i++ )
			{
				WorkspaceProjectConfig wsc = projects.get( i );
				String str = PathUtils.denormalizePath( wsc.getStringValue() );

				str = PathUtils.adjustRelativePath( str, getProjectRoot(), this );

				try
				{
					// WsdlProject project = new WsdlProject( str, this, false,
					// !closeOnStartup &&
					// wsc.getStatus() != Status.CLOSED && wsc.getType() !=
					// Type.REMOTE, wsc.getName(), null);
					WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( str,
							this, false, !closeOnStartup && wsc.getStatus() != Status.CLOSED && wsc.getType() != Type.REMOTE,
							wsc.getName(), null );

					projectList.add( project );
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( messages.get( "FailedToLoadProjectInWorkspace", str ) + e.getMessage() );

					SoapUI.logError( e );
				}
			}
		}
		else
		{
			workspaceConfig = SoapuiWorkspaceDocumentConfig.Factory.newInstance();
			workspaceConfig.addNewSoapuiWorkspace().setName( messages.get( "DefaultWorkspaceName" ) );
			workspaceConfig.getSoapuiWorkspace().addNewSettings();

			settings = new XmlBeansSettingsImpl( this, SoapUI.getSettings(), workspaceConfig.getSoapuiWorkspace()
					.getSettings() );
		}
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public Map<String, Project> getProjects()
	{
		Map<String, Project> result = new HashMap<String, Project>();

		for( Project project : projectList )
		{
			result.put( project.getName(), project );
		}

		return result;
	}

	public void setName( String name )
	{
		String oldName = getName();

		workspaceConfig.getSoapuiWorkspace().setName( name );
		notifyPropertyChanged( ModelItem.NAME_PROPERTY, oldName, name );
	}

	public void setDescription( String description )
	{
		String oldDescription = getDescription();

		workspaceConfig.getSoapuiWorkspace().setDescription( description );
		notifyPropertyChanged( ModelItem.DESCRIPTION_PROPERTY, oldDescription, description );
	}

	public String getName()
	{
		return workspaceConfig.getSoapuiWorkspace().isSetName() ? workspaceConfig.getSoapuiWorkspace().getName()
				: messages.get( "DefaultWorkspaceName" );
	}

	public Project getProjectAt( int index )
	{
		return projectList.get( index );
	}

	public Project getProjectByName( String projectName )
	{
		for( Project project : projectList )
		{
			if( project.getName().equals( projectName ) )
				return project;
		}

		return null;
	}

	public int getProjectCount()
	{
		return projectList.size();
	}

	public void onClose()
	{
		save( !getSettings().getBoolean( UISettings.AUTO_SAVE_PROJECTS_ON_EXIT ) );
	}

	public void save( boolean workspaceOnly )
	{
		save( workspaceOnly, false );
	}

	public void save( boolean workspaceOnly, boolean skipProjectsWithRunningTests )
	{
		try
		{
			// not saved?
			if( path == null )
			{
				File file = UISupport.getFileDialogs().saveAs( this, messages.get( "SaveWorkspace.Title" ), ".xml",
						"XML Files (*.xml)", null );
				if( file == null )
					return;

				path = file.getAbsolutePath();
			}

			List<WorkspaceProjectConfig> projects = new ArrayList<WorkspaceProjectConfig>();

			// save projects first
			for( int c = 0; c < getProjectCount(); c++ )
			{
				WsdlProject project = ( WsdlProject )getProjectAt( c );

				if( !workspaceOnly )
				{
					if( skipProjectsWithRunningTests && SoapUI.getTestMonitor().hasRunningTests( project ) )
					{
						log.warn( messages.get( "ProjectHasRunningTests.Warning", project.getName() ) );
					}
					else
					{
						String path = project.getPath();
						if( !StringUtils.hasContent( path ))
						{
							if( UISupport.confirm( messages.get( "ProjectHasNotBeenSaved.Label", project.getName() ), messages
									.get( "ProjectHasNotBeenSaved.Title" ) ) )
							{
								project.save();
							}
						}
						else
						{
							project.save();
						}
					}
				}

				String path = project.getPath();
				if( path != null )
				{
					path = PathUtils.createRelativePath( path, getProjectRoot(), this );

					WorkspaceProjectConfig wpc = WorkspaceProjectConfig.Factory.newInstance();
					wpc.setStringValue( PathUtils.normalizePath( path ) );
					if( project.isRemote() )
						wpc.setType( Type.REMOTE );

					if( !project.isOpen() )
					{
						if( project.getEncrypted() == 0 )
						{
							wpc.setStatus( Status.CLOSED );
						}
						else
						{
							wpc.setStatus( Status.CLOSED_AND_ENCRYPTED );
						}
					}

					wpc.setName( project.getName() );
					projects.add( wpc );
				}
			}

			workspaceConfig.getSoapuiWorkspace().setProjectArray(
					projects.toArray( new WorkspaceProjectConfig[projects.size()] ) );
			workspaceConfig.getSoapuiWorkspace().setSoapuiVersion( SoapUI.SOAPUI_VERSION );

			File workspaceFile = new File( path );
			workspaceConfig.save( workspaceFile, new XmlOptions().setSavePrettyPrint() );

			log.info( messages.get( "SavedWorkspace.Info", workspaceFile.getAbsolutePath() ) ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch( IOException e )
		{
			log.error( messages.get( "FailedToSaveWorkspace.Error" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
	}

	public void addWorkspaceListener( WorkspaceListener listener )
	{
		listeners.add( listener );
	}

	public void removeWorkspaceListener( WorkspaceListener listener )
	{
		listeners.remove( listener );
	}

	public Project importProject( String fileName ) throws SoapUIException
	{
		File projectFile = new File( fileName );
		WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew(
				projectFile.getAbsolutePath(), this );

		projectList.add( project );
		fireProjectAdded( project );

		resolveProject( project );

		save( true );

		return project;
	}

	public void resolveProject( WsdlProject project )
	{
		if( resolver == null )
		{
			resolver = new ResolveDialog( "Resolve Project", "Resolve imported project", null );
			resolver.setShowOkMessage( false );
		}

		resolver.resolve( project );
	}

	public WsdlProject createProject( String name ) throws SoapUIException
	{
		File projectFile = new File( createProjectFileName( name ) );
		File file = UISupport.getFileDialogs().saveAs( this, messages.get( "CreateProject.Title" ), ".xml",
				"XML Files (*.xml)", projectFile );
		if( file == null )
			return null;

		return createProject( name, file );
	}

	public WsdlProject createProject( String name, File file ) throws SoapUIException
	{
		File projectFile = file;
		while( projectFile != null && projectFile.exists() )
		{
			Boolean result = Boolean.FALSE;
			while( !result.booleanValue() )
			{
				result = UISupport.confirmOrCancel( messages.get( "OverwriteProject.Label" ), messages
						.get( "OverwriteProject.Title" ) );
				if( result == null )
					return null;
				if( result.booleanValue() )
				{
					projectFile.delete();
				}
				else
				{
					projectFile = UISupport.getFileDialogs().saveAs( this, messages.get( "CreateProject.Title" ), ".xml",
							"XML Files (*.xml)", projectFile ); //$NON-NLS-1$
					if( projectFile == null )
						return null;
					else
						break;
				}
			}
		}

		// WsdlProject project = new WsdlProject( projectFile == null ? null :
		// projectFile.getAbsolutePath(), this );
		WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( WsdlProjectFactory.WSDL_TYPE )
				.createNew( null, this );

		project.setName( name );
		projectList.add( project );

		fireProjectAdded( project );

		try
		{
			if( projectFile != null )
				project.saveAs( projectFile.getAbsolutePath() );
		}
		catch( IOException e )
		{
			log.error( messages.get( "FailedToSaveProject.Error" ) + e.getMessage(), e );
		}
		// save( true );

		return project;
	}

	private void fireProjectAdded( Project project )
	{
		for( Iterator<WorkspaceListener> iter = listeners.iterator(); iter.hasNext(); )
		{
			WorkspaceListener listener = iter.next();
			listener.projectAdded( project );
		}
	}

	private void fireWorkspaceSwitching()
	{
		for( Iterator<WorkspaceListener> iter = listeners.iterator(); iter.hasNext(); )
		{
			WorkspaceListener listener = iter.next();
			listener.workspaceSwitching( this );
		}
	}

	private void fireWorkspaceSwitched()
	{
		for( Iterator<WorkspaceListener> iter = listeners.iterator(); iter.hasNext(); )
		{
			WorkspaceListener listener = iter.next();
			listener.workspaceSwitched( this );
		}
	}

	private String createProjectFileName( String name )
	{
		return name + "-soapui-project.xml"; //$NON-NLS-1$
	}

	public void removeProject( Project project )
	{
		int ix = projectList.indexOf( project );
		if( ix == -1 )
			throw new RuntimeException( "Project [" + project.getName() + "] not available in workspace for removal" );

		projectList.remove( ix );

		try
		{
			fireProjectRemoved( project );
		}
		finally
		{
			project.release();
			// workspaceConfig.getSoapuiWorkspace().removeProject( ix );
		}
	}

	public Project reloadProject( Project project ) throws SoapUIException
	{
		int ix = projectList.indexOf( project );
		if( ix == -1 )
			throw new RuntimeException( "Project [" + project.getName() //$NON-NLS-1$
					+ "] not available in workspace for reload" ); //$NON-NLS-1$

		projectList.remove( ix );
		fireProjectRemoved( project );

		String tempName = project.getName();
		project.release();
		project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( project.getPath(), this,
				false, true, tempName, null );
		projectList.add( ix, project );

		fireProjectAdded( project );

		return project;
	}

	private void fireProjectRemoved( Project project )
	{
		WorkspaceListener[] listenerArray = listeners.toArray( new WorkspaceListener[listeners.size()] );
		for( int c = 0; c < listenerArray.length; c++ )
		{
			listenerArray[c].projectRemoved( project );
		}
	}

	public ImageIcon getIcon()
	{
		return workspaceIcon;
	}

	public Settings getSettings()
	{
		return settings;
	}

	public int getIndexOfProject( Project project )
	{
		return projectList.indexOf( project );
	}

	public String getPath()
	{
		return path;
	}

	public String getProjectRoot()
	{
		return workspaceConfig.getSoapuiWorkspace().getProjectRoot();
	}

	public void setProjectRoot( String workspaceRoot )
	{
		workspaceConfig.getSoapuiWorkspace().setProjectRoot( workspaceRoot );
	}

	public void release()
	{
		settings.release();

		for( Project project : projectList )
			project.release();
	}

	public List<? extends Project> getProjectList()
	{
		return projectList;
	}

	public String getDescription()
	{
		return workspaceConfig.getSoapuiWorkspace().getDescription();
	}

	public WsdlProject importRemoteProject( String url ) throws SoapUIException
	{
		// WsdlProject project = new WsdlProject( url, this, false );
		WsdlProject project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( url, this,
				false );
		projectList.add( project );
		fireProjectAdded( project );

		resolveProject( project );

		save( true );

		return project;
	}

	public void closeProject( Project project )
	{
		int oldProjectEncrypt = ( ( WsdlProject )project ).getEncrypted();
		int ix = projectList.indexOf( project );
		if( ix == -1 )
			throw new RuntimeException( "Project [" + project.getName() + "] not available in workspace for close" );

		projectList.remove( ix );
		fireProjectRemoved( project );

		String name = project.getName();
		project.release();

		try
		{
			// project = new WsdlProject( project.getPath(), this, false, false,
			// name, null );
			project = ProjectFactoryRegistry.getProjectFactory( WsdlProjectFactory.WSDL_TYPE ).createNew(
					project.getPath(), this, false, false, name, null );
			( ( WsdlProject )project ).setEncrypted( oldProjectEncrypt );
			projectList.add( ix, project );
			fireProjectAdded( project );
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( messages.get( "FailedToCloseProject.Error", name ) + e.getMessage() );
			SoapUI.logError( e );
		}
	}

	public List<Project> getOpenProjectList()
	{
		List<Project> availableProjects = new ArrayList<Project>();

		for( Project project : projectList )
			if( project.isOpen() )
				availableProjects.add( project );

		return availableProjects;
	}

	public Project openProject( Project project ) throws SoapUIException
	{
		return reloadProject( project );
	}

	public String getId()
	{
		return String.valueOf( hashCode() );
	}

	public List<? extends ModelItem> getChildren()
	{
		return getProjectList();
	}

	public ModelItem getParent()
	{
		return null;
	}

	public void inspectProjects()
	{
		for( int cnt = 0; cnt < projectList.size(); cnt++ )
		{
			Project project = projectList.get( cnt );
			if( project.isOpen() )
				project.inspect();
		}
	}

	public String getProjectPassword( String name )
	{
		return projectOptions.get( name );
	}

	public void clearProjectPassword( String name )
	{
		projectOptions.remove( name );
	}

}
