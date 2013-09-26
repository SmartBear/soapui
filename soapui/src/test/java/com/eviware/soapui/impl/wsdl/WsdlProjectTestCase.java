/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.JettyTestCaseBase;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XFileDialogs;
import cucumber.annotation.Before;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.eviware.soapui.utils.StubbedDialogs.hasConfirmationWithQuestion;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WsdlProjectTestCase extends JettyTestCaseBase
{
	private static final String PROJECT_NAME = "ProjectName";
	private static final String FILE_NAME = "thefile.xml";
	private static final String FILE_PATH = "/tmp/" + FILE_NAME;

	private XFileDialogs originalFileDialogs;
	private XFileDialogs fileDialogs;
	private XDialogs originalDialogs;
	private StubbedDialogs dialogs;

	private File file;

	@Before
	public void setup()
	{
		originalFileDialogs = UISupport.getFileDialogs();
		originalDialogs = UISupport.getDialogs();

		fileDialogs = mock( XFileDialogs.class );
		UISupport.setFileDialogs( fileDialogs );

		dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
	}

	@Test
	public void saveIsConsideredSuccessfulIfProjectIsClosed() throws XmlException, IOException, SoapUIException
	{
		WsdlProject project = createWsdlProject( false, false );
		SaveStatus saved = project.save();

		assertThat( saved, equalTo( SaveStatus.SUCCESS ) );
	}

	@Test
	public void projectIsNotSavedIfSaveAsDialogIsCancelled() throws IOException
	{
		saveAsDialogShouldReturn( null );

		WsdlProject project = createWsdlProject( true, false );
		SaveStatus saveResult = project.save();

		assertThat( project.getPath(), is( nullValue() ) );
		assertThat( saveResult, equalTo( SaveStatus.CANCELLED ) );
	}

	@Test
	public void projectIsSavedIfWritableFileSelected() throws IOException
	{
		file = FileBuilder.file().canWrite( true ).build();
		saveAsDialogShouldReturn( file );

		WsdlProject project = createWsdlProject( true, false );
		SaveStatus saveResult = project.save();

		assertThat( project.getPath(), equalTo( FILE_PATH ) );
		assertThat( saveResult, equalTo( SaveStatus.SUCCESS ) );
	}

	@Test
	public void confirmIfTryingToOverwriteExistingFile() throws IOException
	{
		dialogs.mockConfirmWithReturnValue( true );
		file = FileBuilder.file().canWrite( false, true ).exists( true ).build();

		saveAsDialogShouldReturn( file );

		WsdlProject project = createWsdlProject( true, false );
		SaveStatus saveStatus = project.save();

		assertThat( dialogs.getConfirmations(), hasConfirmationWithQuestion( "File [" + FILE_NAME + "] exists, overwrite?" ) );
		assertThat( saveStatus, equalTo( SaveStatus.SUCCESS ) );
	}

	@Test
	public void cancelSaveOfExistingFileIfNotWritableAndNoNewFileSelected() throws IOException
	{
		dialogs.mockConfirmWithReturnValue( true );
		file = FileBuilder.file().canWrite( false ).exists( true ).build();
		saveAsDialogShouldReturn( null );

		WsdlProject project = createWsdlProject( true, true );

		SaveStatus saveResult = project.save();
		assertThat( saveResult, equalTo( SaveStatus.CANCELLED ) );
	}

	@Test
	public void doNotSaveExistingFileIfNotWritableAndWeDontWantToSave() throws IOException
	{
		dialogs.mockConfirmWithReturnValue( false );
		file = FileBuilder.file().canWrite( false ).exists( true ).build();

		WsdlProject project = createWsdlProject( true, true );
		SaveStatus saveResult = project.save();

		assertThat( saveResult, equalTo( SaveStatus.DONT_SAVE ) );
	}

	@Test
	public void askForNewFileNameIfSelectedFileIsNotWritable() throws IOException
	{
		dialogs.mockConfirmWithReturnValue( true );
		file = FileBuilder.file().canWrite( false, true ).exists( true ).build();
		saveAsDialogShouldReturn( file );

		WsdlProject project = createWsdlProject( true, false );
		SaveStatus saveResult = project.save();

		assertThat( saveResult, equalTo( SaveStatus.SUCCESS ) );
		assertThat( dialogs.getConfirmations(), hasConfirmationWithQuestion( "Project file [" + FILE_PATH + "] can not be written to, save to new file?" ) );
	}

	@Test
	public void shouldBePossibleToCancelIfFileIsNotWritable() throws IOException
	{
		// cancel "save to new file?" dialog
		dialogs.mockConfirmWithReturnValue( null );
		file = FileBuilder.file().canWrite( false ).exists( true ).build();
		saveAsDialogShouldReturn( file );

		WsdlProject project = createWsdlProject( true, true );
		SaveStatus saveResult = project.save();

		assertThat( dialogs.getConfirmations(), hasConfirmationWithQuestion( "Project file [" + FILE_PATH + "] can not be written to, save to new file?" ) );
		assertThat( saveResult, equalTo( SaveStatus.CANCELLED ) );
	}

	private WsdlProject createWsdlProject( boolean isOpen, boolean exitingProject )
	{
		WsdlProject wsdlProject = new WsdlProject( null, null, true, isOpen, PROJECT_NAME, null )
		{
			@Override
			public SaveStatus saveIn( File projectFile ) throws IOException
			{
				// always return success for the actual save step
				return SaveStatus.SUCCESS;
			}

			@Override
			public File createFile( String path )
			{
				return file;
			}
		};
		wsdlProject.getSettings().setBoolean( UISettings.LINEBREAK, false );
		if( exitingProject )
		{
			wsdlProject.path = FILE_PATH;
		}
		return wsdlProject;
	}

	@After
	public void teardown()
	{
		UISupport.setFileDialogs( originalFileDialogs );
		UISupport.setDialogs( originalDialogs );
	}

	static class FileBuilder
	{

		private File file = mock( File.class );

		private FileBuilder()
		{
		}

		static FileBuilder file()
		{
			return new FileBuilder();
		}

		FileBuilder canWrite( Boolean firstInvocation, Boolean... commingInvocations )
		{
			when( file.canWrite() ).thenReturn( firstInvocation, commingInvocations );
			return this;
		}

		FileBuilder exists( boolean exists )
		{
			when( file.exists() ).thenReturn( exists );
			return this;
		}

		File build()
		{
			when( file.getAbsolutePath() ).thenReturn( FILE_PATH );
			when( file.getName() ).thenReturn( FILE_NAME );
			return file;
		}

	}

	private void saveAsDialogShouldReturn( File file, File... files )
	{
		when( fileDialogs.saveAs( anyObject(), anyString(), anyString(), anyString(), isA( File.class ) ) ).thenReturn( file, files );
	}
}
