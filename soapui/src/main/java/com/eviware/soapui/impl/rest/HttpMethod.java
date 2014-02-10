package com.eviware.soapui.impl.rest;

/**
* Each value in this enumeration represents an officially supported HTTP method ("verb").
*/
public enum HttpMethod
{
	GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH;

	public static String[] getMethodsAsStringArray()
	{
		return new String[] { GET.toString(), POST.toString(), PUT.toString(), DELETE.toString(), HEAD.toString(),
				OPTIONS.toString(), TRACE.toString(), PATCH.toString() };
	}

	public static HttpMethod[] getMethods()
	{
		return new HttpMethod[] { GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH };
	}
}
