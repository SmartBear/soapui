package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class AttachmentUtilsTest
{
	@Test
	public void actionIsNotWithinTypeElement() throws Exception
	{
		String contentType = AttachmentUtils.buildRootPartContentType( "SendFile", SoapVersion.Soap12 );

		String expectedContentType = "application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"; action=\"SendFile\"";
		assertThat( contentType, is( expectedContentType ) );

	}

	@Test
	public void actionIsNotPartOfTypeElementInMTOMContentType()
	{
		String contentType = AttachmentUtils.buildMTOMContentType( "boundary=\"----=_Part_10_7396679.1285664994648\"", "SendFile", SoapVersion.Soap12 );
		String expectedContentType = "multipart/related; type=\"application/xop+xml\"; " +
				"start=\"<rootpart@soapui.org>\"; start-info=\"application/soap+xml\"; action=\"SendFile\"; " +
				"boundary=\"----=_Part_10_7396679.1285664994648\"";
		assertThat( contentType, is( expectedContentType ) );
	}
}
