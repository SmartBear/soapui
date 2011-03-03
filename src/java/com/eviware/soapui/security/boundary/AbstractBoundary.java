/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.boundary;

/**
 * @author nebojsa.tasic
 */
public abstract class AbstractBoundary implements Boundary
{
	
	public static final String XSD_LENGTH = "xsd:length";
	public static final String XSD_MAX_LENGTH = "xsd:maxLength";
	public static final String XSD_MIN_LENGTH = "xsd:minLength";
	
	public static Boundary factory( String type )
	{
		if( "xsd:string".equals( type ) )
		{
			return new StringBoundary();
		}
		return null;
	}
	
	
	public static String outOfBoundaryValue( String baseType,  String nodeName, String nodeValue )
	{
		Boundary boundary = AbstractBoundary.factory( baseType );
		
		if( AbstractBoundary.XSD_MIN_LENGTH.equals( nodeName ) )
		{
			return  boundary.outOfBoundary( Boundary.MIN_LENGTH, nodeValue );
		}
		else if( AbstractBoundary.XSD_MAX_LENGTH.equals( nodeName ) )
		{
			return  boundary.outOfBoundary( Boundary.MAX_LENGTH, nodeValue);
		}
		else if( AbstractBoundary.XSD_LENGTH.equals( nodeName ) )
		{
			return  boundary.outOfBoundary( Boundary.LENGTH, nodeValue );
		}
		return null;
	}
}
