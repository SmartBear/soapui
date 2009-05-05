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

package com.eviware.soapui.impl.wsdl.support;

import java.util.concurrent.Future;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.support.UISupport;

/**
 * Class to animate the icon of a ModelItem
 * 
 * @author ole.matzura
 */

public class ModelItemIconAnimator<T extends AbstractWsdlModelItem<?>> implements Runnable
{
	private final T target;
	private int index = 0;
	private boolean stopped = true;
	private boolean enabled = true;
	private ImageIcon baseIcon;
	private ImageIcon[] animateIcons;
	private Future<?> future;

	public ModelItemIconAnimator( T target, String baseIcon, String animationBaseIcon, int num, String type )
	{
		this.target = target;
		this.baseIcon = UISupport.createImageIcon( baseIcon );

		animateIcons = new ImageIcon[num];

		for( int c = 0; c < animateIcons.length; c++ )
			animateIcons[c] = UISupport.createImageIcon( animationBaseIcon + "_" + ( c + 1 ) + "." + type );
	}

	public void stop()
	{
		stopped = true;
	}

	public int getIndex()
	{
		return index;
	}

	public boolean isStopped()
	{
		return stopped;
	}

	public void start()
	{
		if( !enabled || future != null )
			return;

		stopped = false;
		future = SoapUI.getThreadPool().submit( this );
	}

	public ImageIcon getBaseIcon()
	{
		return baseIcon;
	}

	public ImageIcon getIcon()
	{
		if( !isStopped() )
		{
			return animateIcons[getIndex()];
		}

		return baseIcon;
	}

	public void run()
	{
		if( future != null )
		{
			Thread.currentThread().setName( "ModelItemIconAnimator for " + target.getName() );
		}

		while( !stopped )
		{
			try
			{
				if( stopped )
					break;

				index = index >= animateIcons.length - 1 ? 0 : index + 1;
				target.setIcon( getIcon() );
				Thread.sleep( 500 );
			}
			catch( InterruptedException e )
			{
				SoapUI.logError( e );
			}
		}

		target.setIcon( getIcon() );
		future = null;
		// iconAnimationThread = null;
	}

	public T getTarget()
	{
		return target;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
		if( !stopped )
			stopped = enabled;
	}
}