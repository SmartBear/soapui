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

package com.eviware.soapui.support.types;

import java.util.ArrayList;
import java.util.Collection;

public class TupleList<T1 extends Object, T2 extends Object> extends ArrayList<TupleList.Tuple>
{
	public TupleList()
	{
	}

	public TupleList( Collection<? extends Tuple> c )
	{
		super( c );
	}

	public TupleList( int initialCapacity )
	{
		super( initialCapacity );
	}

	public void add( T1 value1, T2 value2 )
	{
		add( new Tuple( value1, value2 ) );
	}

	public class Tuple
	{
		private T1 value1;
		private T2 value2;

		public Tuple( T1 value1, T2 value2 )
		{
			this.value1 = value1;
			this.value2 = value2;
		}

		public T1 getValue1()
		{
			return value1;
		}

		public void setValue1( T1 value1 )
		{
			this.value1 = value1;
		}

		public T2 getValue2()
		{
			return value2;
		}

		public void setValue2( T2 value2 )
		{
			this.value2 = value2;
		}

		public String toString()
		{
			return TupleList.this == null ? value1 + " : " + value2 : TupleList.this.toStringHandler( this );
		}
	}

	protected String toStringHandler( Tuple tuple )
	{
		return tuple.value1 + " : " + tuple.value2;
	}
}
