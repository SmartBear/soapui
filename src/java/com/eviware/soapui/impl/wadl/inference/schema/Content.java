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

package com.eviware.soapui.impl.wadl.inference.schema;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wadl.inference.schema.content.EmptyContent;
import com.eviware.soapui.impl.wadl.inference.schema.content.SequenceContent;
import com.eviware.soapui.impl.wadl.inference.schema.content.SimpleContent;
import com.eviware.soapui.inferredSchema.ContentConfig;
import com.eviware.soapui.inferredSchema.EmptyContentConfig;
import com.eviware.soapui.inferredSchema.SequenceContentConfig;
import com.eviware.soapui.inferredSchema.SimpleContentConfig;

/**
 * This class represents the content of an XML element. It does not take into
 * account any attributes that the element may have.
 * 
 * @author Dain Nilsson
 */
public interface Content
{

	/**
	 * Validates an XML document contained in a given Context object.
	 * 
	 * @param context
	 *           A Context object containing the XML data to be validated, and
	 *           other needed contextual variables.
	 * @return Returns a Content object that is valid for the element/attribute,
	 *         quite possibly this Content instance itself.
	 * @throws XmlException
	 */
	public Content validate( Context context ) throws XmlException;

	public String toString( String attrs );

	/**
	 * Save the Content to an XmlObject.
	 */
	public ContentConfig save();

	/**
	 * A static factory class for creating new instances.
	 * 
	 * @author Dain Nilsson
	 */
	public class Factory
	{

		/**
		 * Creates a new, empty, Content.
		 * 
		 * @param schema
		 *           The Schema in which the Content will live.
		 * @return Returns the newly created Content.
		 */
		public static Content newContent( Schema schema )
		{
			return new EmptyContent( schema, false );
		}

		/**
		 * Constructs a Content object using previously saved data.
		 * 
		 * @param xml
		 *           XmlObject to which data has previously been saved.
		 * @param schema
		 *           The Schema in which to place the newly constructed Content.
		 * @return Returns the newly constructed Content.
		 */
		public static Content parse( ContentConfig xml, Schema schema )
		{
			if( xml instanceof EmptyContentConfig )
				return new EmptyContent( ( EmptyContentConfig )xml, schema );
			if( xml instanceof SimpleContentConfig )
				return new SimpleContent( ( SimpleContentConfig )xml, schema );
			if( xml instanceof SequenceContentConfig )
				return new SequenceContent( ( SequenceContentConfig )xml, schema );
			return null;
		}
	}
}
