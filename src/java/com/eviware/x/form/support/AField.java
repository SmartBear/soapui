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

package com.eviware.x.form.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A field in an AForm
 * 
 * @author ole.matzura
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface AField
{
	public enum AFieldType
	{
		BOOLEAN, STRING, FILE, FOLDER, INT, ENUMERATION, PASSWORD, FILELIST, RADIOGROUP, STRINGAREA, MULTILIST, STRINGLIST, TABLE, ACTION, COMPONENT, SEPARATOR
	}

	public String name() default "";

	public String description();

	public AFieldType type() default AFieldType.STRING;

	public String group() default "";

	public String[] values() default "";

	public String defaultValue() default "";

	boolean enabled() default true;
}
