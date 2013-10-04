/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.boundary;

/**
 * @author nebojsa.tasic
 */
public interface Boundary
{
	public static final int LENGTH = 1;
	public static final int MIN_LENGTH = 2;
	public static final int MAX_LENGTH = 3;
	public static final int TOTAL_DIGITS = 4;
	public static final int FRACTION_DIGITS = 5;
	public static final int MAX_EXCLISIVE = 6;
	public static final int MIN_EXCLISIVE = 7;
	public static final int MAX_INCLISIVE = 8;
	public static final int MIN_INCLISIVE = 9;

	String outOfBoundary( int restrictionAttribute, String value );
}
