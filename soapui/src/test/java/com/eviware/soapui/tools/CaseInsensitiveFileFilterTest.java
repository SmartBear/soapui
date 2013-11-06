package com.eviware.soapui.tools;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Anders Jaensson
 */
public class CaseInsensitiveFileFilterTest
{

	private MockAsWar.CaseInsensitiveFileFilter caseInsensitiveFileFilter;

	@Before
	public void setup()
	{
		caseInsensitiveFileFilter = new MockAsWar.CaseInsensitiveFileFilter();
	}

	@Test
	public void doesNotAcceptNullFile()
	{
		boolean fileAccepted = caseInsensitiveFileFilter.accept( null );

		assertThat( fileAccepted, is( false ) );
	}

	@Test
	public void doesNotAcceptEmptyFile()
	{
		boolean fileAccepted = caseInsensitiveFileFilter.accept( new File( "" ) );

		assertThat( fileAccepted, is( false ) );
	}

	@Test
	public void doesNotAcceptExcludedFileEvenIfCaseDoesNotMatch()
	{
		boolean fileAccepted = caseInsensitiveFileFilter.accept( new File( "SomeServletThing" ) );

		assertThat( fileAccepted, is( false ) );
	}

	@Test
	public void doesNotAcceptExcludedFileIfExactMatch()
	{
		boolean fileAccepted = caseInsensitiveFileFilter.accept( new File( "servlet" ) );

		assertThat( fileAccepted, is( false ) );
	}

	@Test
	public void acceptsFileIfOnlyPartOfFilenameMatches()
	{
		boolean fileAccepted = caseInsensitiveFileFilter.accept( new File( "servlek" ) );

		assertThat( fileAccepted, is( true ) );
	}

	@Test
	public void acceptsFileThatIsNotExcluded()
	{
		boolean fileAccepted = caseInsensitiveFileFilter.accept( new File( "FileToInclude" ) );

		assertThat( fileAccepted, is( true ) );
	}

}
