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

package com.eviware.soapui.support.editor.inspectors.attachments;

import java.util.HashMap;

/**
 * Handles drop of files on the AttachementPanel
 * 
 * @author ole.matzura
 */

public class ContentTypeHandler
{
	private static final HashMap<String, String> suffixToContentType;
	public static final String DEFAULT_CONTENTTYPE = "application/octet-stream";

	public static String getContentTypeFromFilename( String fileName )
	{
		String suffix = getSuffixFromFilename( fileName );

		if( suffixToContentType.containsKey( suffix ) )
			return suffixToContentType.get( suffix );

		return DEFAULT_CONTENTTYPE;
	}

	public static String getSuffixFromFilename( String fileName )
	{
		if( fileName == null || fileName.length() == 0 )
			return "";

		int pos = fileName.lastIndexOf( "." ) + 1;
		int len = fileName.length();

		String suffix = "";
		if( pos < len )
			suffix = fileName.substring( pos, len );

		return suffix;

	}

	public static String getExtensionForContentType( String contentType )
	{
		int ix = contentType.indexOf( ';' );
		if( ix > 0 )
			contentType = contentType.substring( 0, ix );

		for( String key : suffixToContentType.keySet() )
		{
			if( suffixToContentType.get( key ).equals( contentType ) )
				return key;
		}

		return "dat";
	}

	static
	{
		suffixToContentType = new HashMap<String, String>();
		suffixToContentType.put( "html", "text/html" );
		suffixToContentType.put( "htm", "text/html" );
		suffixToContentType.put( "txt", "text/plain" );
		suffixToContentType.put( "xml", "text/xml" );
		suffixToContentType.put( "wsdl", "text/xml" );
		suffixToContentType.put( "xsd", "text/xml" );
		suffixToContentType.put( "c", "text/plain" );
		suffixToContentType.put( "c++", "text/plain" );
		suffixToContentType.put( "pl", "text/plain" );
		suffixToContentType.put( "c", "text/plain" );
		suffixToContentType.put( "h", "text/plain" );
		// suffixToContentType.put("", "text/richtext");
		// suffixToContentType.put("", "text/x-setext");
		// suffixToContentType.put("", "text/enriched");
		// suffixToContentType.put("", "text/tab-separated-values");
		// suffixToContentType.put("", "text/sgml");
		suffixToContentType.put( "talk", "text/x-speech" );
		suffixToContentType.put( "css", "text/css" );
		// suffixToContentType.put("", "application/dsssl");
		suffixToContentType.put( "gif", "image/gif" );
		suffixToContentType.put( "xbm", "image/x-xbitmap" );
		suffixToContentType.put( "xpm", "image/x-xpixmap" );
		suffixToContentType.put( "png", "image/x-png" );
		suffixToContentType.put( "ief", "image/ief" );
		suffixToContentType.put( "jpeg", "image/jpeg" );
		suffixToContentType.put( "jpg", "image/jpeg" );
		suffixToContentType.put( "jpe", "image/jpeg" );
		suffixToContentType.put( "tiff tif", "image/tiff" );
		suffixToContentType.put( "tif", "image/tiff" );
		suffixToContentType.put( "rgb", "image/rgb" );
		// suffixToContentType.put("", "image/x-rgb");
		suffixToContentType.put( "g3f ", "image/g3fax" );
		suffixToContentType.put( "xwd ", "image/x-xwindowdump" );
		suffixToContentType.put( "pict", "image/x-pict" );
		suffixToContentType.put( "ppm", "image/x-portable-pixmap" );
		suffixToContentType.put( "pgm", "image/x-portable-graymap" );
		suffixToContentType.put( "pbm", "image/x-portable-bitmap" );
		suffixToContentType.put( "pnm", "image/x-portable-anymap" );
		suffixToContentType.put( "bmp", "image/x-ms-bmp" );
		suffixToContentType.put( "ras", "image/x-cmu-raster" );
		suffixToContentType.put( "pcd", "image/x-photo-cd" );
		suffixToContentType.put( "cgm", "image/cgm" );
		// suffixToContentType.put("", "image/naplps");
		suffixToContentType.put( "mil", "image/x-cals" );
		suffixToContentType.put( "cal", "image/x-cals" );
		suffixToContentType.put( "fif", "image/fif" );
		suffixToContentType.put( "dsf", "image/x-mgx-dsf" );
		suffixToContentType.put( "cmx", "image/x-cmx" );
		suffixToContentType.put( "wi", "image/wavelet" );
		suffixToContentType.put( "dwg", "image/vnd.dwg" );
		// suffixToContentType.put("", "image/x-dwg");
		suffixToContentType.put( "dxf", "image/vnd.dxf" );
		// suffixToContentType.put("", "image/x-dxf");
		suffixToContentType.put( "svf", "image/vnd.svf" );
		// suffixToContentType.put("", "also vector/x-svf");
		suffixToContentType.put( "au snd", "audio/basic" );
		suffixToContentType.put( "snd", "audio/basic" );
		suffixToContentType.put( "aif", "audio/x-aiff" );
		suffixToContentType.put( "aiff", "audio/x-aiff" );
		suffixToContentType.put( "aifc", "audio/x-aiff" );
		suffixToContentType.put( "wav", "audio/x-wav" );
		suffixToContentType.put( "mpa", "audio/x-mpeg" );
		suffixToContentType.put( "abs", "audio/x-mpeg" );
		suffixToContentType.put( "mpega", "audio/x-mpeg" );
		suffixToContentType.put( "mp2a", "audio/x-mpeg-2" );
		suffixToContentType.put( "mpa2", "audio/x-mpeg-2" );
		suffixToContentType.put( "es", "audio/echospeech" );
		suffixToContentType.put( "vox", "audio/voxware" );
		suffixToContentType.put( "lcc", "application/fastman" );
		suffixToContentType.put( "ra", "application/x-pn-realaudio" );
		suffixToContentType.put( "ram", "application/x-pn-realaudio" );
		// suffixToContentType.put("", "application/vnd.music-niff");
		suffixToContentType.put( "mmid", "x-music/x-midi" );
		suffixToContentType.put( "skp", "application/vnd.koan" );
		// suffixToContentType.put("", "application/x-koan");
		suffixToContentType.put( "talk", "text/x-speech" );
		suffixToContentType.put( "mpeg", "video/mpeg" );
		suffixToContentType.put( "mpg", "video/mpeg" );
		suffixToContentType.put( "mpe", "video/mpeg" );
		suffixToContentType.put( "mpv2", "video/mpeg-2" );
		suffixToContentType.put( "mp2v", "video/mpeg-2" );
		suffixToContentType.put( "qt", "video/quicktime" );
		suffixToContentType.put( "mov", "video/quicktime" );
		suffixToContentType.put( "avi", "video/x-msvideo" );
		suffixToContentType.put( "movie", "video/x-sgi-movie" );
		suffixToContentType.put( "vdo", "video/vdo" );
		suffixToContentType.put( "viv", "video/vnd.vivo" );
		// suffixToContentType.put("", "video/vivo");
		suffixToContentType.put( "pac", "application/x-ns-proxy-autoconfig" );
		// suffixToContentType.put("", "application/x-www-form-urlencoded");
		// suffixToContentType.put("", "application/x-www-local-exec");
		// suffixToContentType.put("", "multipart/x-mixed-replace");
		// suffixToContentType.put("", "multipart/form-data");
		suffixToContentType.put( "ice", "x-conference/x-cooltalk" );
		// suffixToContentType.put("", "application/x-chat");
		suffixToContentType.put( "ai", "application/postscript" );
		suffixToContentType.put( "eps", "application/postscript" );
		suffixToContentType.put( "ps", "application/postscript" );
		suffixToContentType.put( "rtf", "application/rtf" );
		suffixToContentType.put( "pdf", "application/pdf" );
		// suffixToContentType.put("", "application/x-pdf");
		suffixToContentType.put( "mif", "application/vnd.mif" );
		// suffixToContentType.put("", "application/x-mif");
		suffixToContentType.put( "t", "application/x-troff" );
		suffixToContentType.put( "tr", "application/x-troff" );
		suffixToContentType.put( "roff", "application/x-troff" );
		suffixToContentType.put( "man", "application/x-troff-man" );
		suffixToContentType.put( "me", "application/x-troff-me" );
		suffixToContentType.put( "ms", "application/x-troff-ms" );
		suffixToContentType.put( "latex", "application/x-latex" );
		suffixToContentType.put( "tex", "application/x-tex" );
		suffixToContentType.put( "texinfo", "application/x-texinfo" );
		suffixToContentType.put( "texi ", "application/x-texinfo" );
		suffixToContentType.put( "dvi", "application/x-dvi" );
		// suffixToContentType.put("", "application/macwriteii");
		suffixToContentType.put( "doc", "application/msword" );
		// suffixToContentType.put("", "application/wordperfect5.1");
		// suffixToContentType.put("", "application/sgml");
		suffixToContentType.put( "oda", "application/oda" );
		suffixToContentType.put( "evy", "application/envoy" );
		// suffixToContentType.put("", "application/wita");
		// suffixToContentType.put("", "application/dec-dx");
		// suffixToContentType.put("", "application/dca-rft");
		// suffixToContentType.put("", "application/commonground");
		suffixToContentType.put( "fm", "application/vnd.framemaker" );
		suffixToContentType.put( "frm", "application/vnd.framemaker" );
		suffixToContentType.put( "frame", "application/vnd.framemaker" );
		// suffixToContentType.put("", "application/x-framemaker");
		// suffixToContentType.put("", "application/remote-printing");
		suffixToContentType.put( "gtar", "application/x-gtar" );
		suffixToContentType.put( "tar", "application/x-tar" );
		suffixToContentType.put( "ustar", "application/x-ustar" );
		suffixToContentType.put( "bcpio", "application/x-bcpio" );
		suffixToContentType.put( "cpio", "application/x-cpio" );
		suffixToContentType.put( "shar", "application/x-shar" );
		suffixToContentType.put( "zip", "application/zip" );
		suffixToContentType.put( "hqx", "application/mac-binhex40" );
		suffixToContentType.put( "sit", "application/x-stuffit" );
		suffixToContentType.put( "sea", "application/x-stuffit" );
		suffixToContentType.put( "fif", "application/fractals" );
		suffixToContentType.put( "bin", "application/octet-stream" );
		suffixToContentType.put( "uu", "application/octet-stream" );
		suffixToContentType.put( "exe", "application/octet-stream" );
		suffixToContentType.put( "src", "application/x-wais-source" );
		suffixToContentType.put( "wsrc", "application/x-wais-source" );
		suffixToContentType.put( "hdf", "application/hdf" );
		suffixToContentType.put( "js", "text/javascript" );
		suffixToContentType.put( "json", "text/javascript" );
		suffixToContentType.put( "ls", "text/javascript" );
		suffixToContentType.put( "mocha", "text/javascript" );
		// suffixToContentType.put("", "application/x-javascript");
		// suffixToContentType.put("", "text/vbscript");
		suffixToContentType.put( "sh", "application/x-sh" );
		suffixToContentType.put( "csh", "application/x-csh" );
		suffixToContentType.put( "pl", "application/x-perl" );
		suffixToContentType.put( "tcl ", "application/x-tcl" );
		// suffixToContentType.put("", "application/atomicmail");
		// suffixToContentType.put("", "application/slate");
		// suffixToContentType.put("", "application/octet-stream");
		// suffixToContentType.put("", "application/riscos");
		// suffixToContentType.put("", "application/andrew-inset");
		suffixToContentType.put( "spl", "application/futuresplash" );
		suffixToContentType.put( "mbd", "application/mbedlet" );
		// suffixToContentType.put("", "application/x-director");
		// suffixToContentType.put("", "application/x-sprite");
		suffixToContentType.put( "rad", "application/x-rad-powermedia" );
		suffixToContentType.put( "ppz", "application/mspowerpoint" );
		suffixToContentType.put( "css", "application/x-pointplus" );
		suffixToContentType.put( "asp", "application/x-asap" );
		suffixToContentType.put( "asn", "application/astound" );
		suffixToContentType.put( "axs", "application/x-olescript" );
		suffixToContentType.put( "ods", "application/x-oleobject" );
		suffixToContentType.put( "opp", "x-form/x-openscape" );
		suffixToContentType.put( "wba", "application/x-webbasic" );
		suffixToContentType.put( "frm", "application/x-alpha-form" );
		suffixToContentType.put( "wfx", "x-script/x-wfxclient" );
		// suffixToContentType.put("", "application/octet-stream");
		// suffixToContentType.put("", "application/cals-1840");
		suffixToContentType.put( "pcn", "application/x-pcn" );
		// suffixToContentType.put("", "application/vnd.ms-excel");
		// suffixToContentType.put("", "application/x-msexcel");
		// suffixToContentType.put("", "application/ms-excel");
		suffixToContentType.put( "ppt", "application/vnd.ms-powerpoint" );
		// suffixToContentType.put("", "application/ms-powerpoint");
		// suffixToContentType.put("", "application/vnd.ms-project");
		// suffixToContentType.put("", "application/vnd.ms-works");
		// suffixToContentType.put("", "application/vnd.ms-tnef");
		// suffixToContentType.put("", "application/vnd.artgalry");
		suffixToContentType.put( "svd", "application/vnd.svd" );
		// suffixToContentType.put("", "application/vnd.truedoc");
		suffixToContentType.put( "ins", "application/x-net-install" );
		suffixToContentType.put( "ccv", "application/ccv" );
		suffixToContentType.put( "vts", "workbook/formulaone" );
		// suffixToContentType.put("", "application/cybercash");
		// suffixToContentType.put("", "application/applefile");
		// suffixToContentType.put("", "application/activemessage");
		// suffixToContentType.put("", "application/x400-bp");
		// suffixToContentType.put("", "application/news-message-id");
		// suffixToContentType.put("", "application/news-transmission");
		// suffixToContentType.put("", "multipart/mixed");
		// suffixToContentType.put("", "multipart/alternative");
		// suffixToContentType.put("", "multipart/related");
		// suffixToContentType.put("", "multipart/digest");
		// suffixToContentType.put("", "multipart/report");
		// suffixToContentType.put("", "multipart/parallel");
		// suffixToContentType.put("", "multipart/appledouble");
		// suffixToContentType.put("", "multipart/header-set");
		// suffixToContentType.put("", "multipart/voice-message");
		// suffixToContentType.put("", "multipart/form-data");
		// suffixToContentType.put("", "multipart/x-mixed-replace");
		// suffixToContentType.put("", "message/rfc822");
		// suffixToContentType.put("", "message/partial");
		// suffixToContentType.put("", "message/external-body");
		// suffixToContentType.put("", "message/news");
		// suffixToContentType.put("", "message/http");
		suffixToContentType.put( "wrl", "x-world/x-vrml" );
		suffixToContentType.put( "vrml", "x-world/x-vrml" );
		suffixToContentType.put( "vrw", "x-world/x-vream" );
		suffixToContentType.put( "p3d", "application/x-p3d" );
		suffixToContentType.put( "svr", "x-world/x-svr" );
		suffixToContentType.put( "wvr", "x-world/x-wvr" );
		suffixToContentType.put( "3dmf", "x-world/x-3dmf" );
		// suffixToContentType.put("", "chemical/*");
		suffixToContentType.put( "ma", "application/mathematica" );
		suffixToContentType.put( "msh", "x-model/x-mesh" );
		suffixToContentType.put( "v5d", "application/vis5d" );
		suffixToContentType.put( "igs", "application/iges" );
		suffixToContentType.put( "dwf ", "drawing/x-dwf" );
		suffixToContentType.put( "showcase", "application/x-showcase" );
		suffixToContentType.put( "slides", "application/x-showcase" );
		suffixToContentType.put( "sc", "application/x-showcase" );
		suffixToContentType.put( "show", "application/x-showcase" );
		suffixToContentType.put( "ins", "application/x-insight" );
		suffixToContentType.put( "insight", "application/x-insight" );
		suffixToContentType.put( "ano", "application/x-annotator" );
		suffixToContentType.put( "dir", "application/x-dirview" );
		suffixToContentType.put( "lic", "application/x-enterlicense" );
		suffixToContentType.put( "faxmgr", "application/x-fax-manager" );
		suffixToContentType.put( "faxmgrjob", "application/x-fax-manager-job" );
		suffixToContentType.put( "icnbk", "application/x-iconbook" );
		suffixToContentType.put( "wb", "application/x-inpview" );
		suffixToContentType.put( "inst ", "application/x-install" );
		suffixToContentType.put( "mail", "application/x-mailfolder" );
		suffixToContentType.put( "pp ppages", "application/x-ppages" );
		suffixToContentType.put( "sgi-lpr", "application/x-sgi-lpr" );
		suffixToContentType.put( "tardist", "application/x-tardist" );
		suffixToContentType.put( "ztardist", "application/x-ztardist" );
		suffixToContentType.put( "wkz", "application/x-wingz" );
		suffixToContentType.put( "iv", "graphics/x-inventor" );
		suffixToContentType.put( "dat", DEFAULT_CONTENTTYPE );
	}

}
