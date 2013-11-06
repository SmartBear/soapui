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

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.model.ModelItem;

public abstract class AbstractBeforeModelItemDropHandler<T1 extends ModelItem, T2 extends ModelItem> extends
		AbstractModelItemDropHandler<T1, T2>
{
	protected AbstractBeforeModelItemDropHandler( Class<T1> sourceClass, Class<T2> targetClass )
	{
		super( sourceClass, targetClass );
	}

	@Override
	boolean canCopyAfter( T1 source, T2 target )
	{
		return false;
	}

	@Override
	boolean canMoveAfter( T1 source, T2 target )
	{
		return false;
	}

	@Override
	boolean copyAfter( T1 source, T2 target )
	{
		return false;
	}

	@Override
	String getCopyAfterInfo( T1 source, T2 target )
	{
		return null;
	}

	@Override
	String getMoveAfterInfo( T1 source, T2 target )
	{
		return null;
	}

	@Override
	boolean moveAfter( T1 source, T2 target )
	{
		return false;
	}

	@Override
	boolean canCopyOn( T1 source, T2 target )
	{
		return false;
	}

	@Override
	boolean canMoveOn( T1 source, T2 target )
	{
		return false;
	}

	@Override
	boolean copyOn( T1 source, T2 target )
	{
		return false;
	}

	@Override
	String getCopyOnInfo( T1 source, T2 target )
	{
		return null;
	}

	@Override
	String getMoveOnInfo( T1 source, T2 target )
	{
		return null;
	}

	@Override
	boolean moveOn( T1 source, T2 target )
	{
		return false;
	}
}
