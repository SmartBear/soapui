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

import com.eviware.soapui.impl.SaveStatus;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.JettyTestCaseBase;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.XFileDialogs;
import cucumber.annotation.Before;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WsdlProjectTestCase extends JettyTestCaseBase
{
	private XFileDialogs originalFileDialogs;
	private XFileDialogs fileDialogs;
	private static final String FILE_PATH = "/tmp/thefile.xml";
	private static final String PROJECT_NAME = "ProjectName";

	@Before
	public void setup()
	{
		originalFileDialogs = UISupport.getFileDialogs();

		fileDialogs = mock( XFileDialogs.class );
		when( fileDialogs.saveAs( anyObject(), anyString(), anyString(), anyString(), isA( File.class ) ) ).thenReturn( new File( FILE_PATH ) );
		UISupport.setFileDialogs( fileDialogs );
	}

	@After
	public void teardown()
	{
		UISupport.setFileDialogs( originalFileDialogs );
	}

	@Test
	public void saveIsConsideredSuccessfulIfProjectIsClosed() throws XmlException, IOException, SoapUIException
	{
		WsdlProject project = createWsdlProject( false );

		SaveStatus saved = project.save();

		assertThat( saved, equalTo( SaveStatus.SUCCESS ) );
	}

	@Test
	public void projectIsNotSavedIfSaveAsDialogIsCancelled() throws IOException
	{
		WsdlProject project = createWsdlProject( true );
		when( fileDialogs.saveAs( anyObject(), anyString(), anyString(), anyString(), isA( File.class ) ) ).thenReturn( null );

		SaveStatus saveResult = project.save();

		assertThat( project.getPath(), is( nullValue() ) );
		assertThat( saveResult, equalTo( SaveStatus.CANCELLED ) );
	}

	@Test
	public void projectIsSavedIfWritableFileSelected() throws IOException
	{
		WsdlProject project = createWsdlProject( true );

		SaveStatus saveResult = project.save();

		assertThat( project.getPath(), equalTo( FILE_PATH ) );
		assertThat( saveResult, equalTo( SaveStatus.SUCCESS ) );
	}

	@Test
	public void projectIsNotSavedIfFileIsNotWritable() throws IOException
	{
		WsdlProject project = createWsdlProject( true );

		SaveStatus saveResult = project.save();

		assertThat( project.getPath(), equalTo( FILE_PATH ) );
		assertThat( saveResult, equalTo( SaveStatus.SUCCESS ) );
	}

	private WsdlProject createWsdlProject( boolean isOpen )
	{
		WsdlProject wsdlProject = new WsdlProject( null, null, true, isOpen, PROJECT_NAME, null )
		{
			@Override
			public SaveStatus saveIn( File projectFile ) throws IOException
			{
				// always return success for the actual save step
				return SaveStatus.SUCCESS;
			}
		};
		wsdlProject.getSettings().setBoolean( UISettings.LINEBREAK, false );

		return wsdlProject;
	}
}
