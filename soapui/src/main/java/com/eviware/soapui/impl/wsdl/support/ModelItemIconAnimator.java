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

package com.eviware.soapui.impl.wsdl.support;

import java.util.concurrent.Future;

import javax.swing.ImageIcon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.support.AbstractAnimatableModelItem;
import com.eviware.soapui.support.UISupport;

/**
 * Class to animate the icon of a ModelItem
 * 
 * @author ole.matzura
 */

public class ModelItemIconAnimator<T extends AbstractAnimatableModelItem<?>> implements Runnable
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
		if( !enabled )
			return;

		/*
		 * mock service to be run needs to be stopped first.
		 * 
		 * if service is restart action occurs while it is running, than run()
		 * needs to finish first so service can be started again. If that is 
		 * case than force stopping mock service.
		 * 
		 */
		if( isStopped() )
		{
			if( future != null && !future.isDone() )
			{
				future.cancel( true );
				while( future != null )
					try
					{
						Thread.sleep( 500 );
					}
					catch( InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			stopped = false;
			future = SoapUI.getThreadPool().submit( this );
		}
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
			if( System.getProperty( "soapui.enablenamedthreads" ) != null )
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
//				SoapUI.log( "Mock Service Force Stopped!" );
				stopped = true;
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
