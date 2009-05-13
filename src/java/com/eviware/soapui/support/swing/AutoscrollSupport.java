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

package com.eviware.soapui.support.swing;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class AutoscrollSupport implements Autoscroll
{
	private static final int AUTOSCROLL_MARGIN = 12;

	Component comp;
	Insets insets;
	Insets scrollUnits;

	public AutoscrollSupport( Component comp, Insets insets )
	{
		this( comp, insets, insets );
	}

	public AutoscrollSupport( Component comp, Insets insets, Insets scrollUnits )
	{
		this.comp = comp;
		this.insets = insets;
		this.scrollUnits = scrollUnits;
	}

	public AutoscrollSupport( Component comp )
	{
		this( comp, new Insets( AUTOSCROLL_MARGIN, AUTOSCROLL_MARGIN, AUTOSCROLL_MARGIN, AUTOSCROLL_MARGIN ) );
	}

	public void autoscroll( Point cursorLoc )
	{
		JViewport viewport = getViewport();
		if( viewport == null )
			return;
		Point viewPos = viewport.getViewPosition();
		int viewHeight = viewport.getExtentSize().height;
		int viewWidth = viewport.getExtentSize().width;

		// resolve scrolling
		if( ( cursorLoc.y - viewPos.y ) < insets.top )
		{ // scroll up
			viewport.setViewPosition( new Point( viewPos.x, Math.max( viewPos.y - scrollUnits.top, 0 ) ) );
		}
		else if( ( viewPos.y + viewHeight - cursorLoc.y ) < insets.bottom )
		{ // scroll down
			viewport.setViewPosition( new Point( viewPos.x, Math.min( viewPos.y + scrollUnits.bottom, comp.getHeight()
					- viewHeight ) ) );
		}
		else if( ( cursorLoc.x - viewPos.x ) < insets.left )
		{ // scroll left
			viewport.setViewPosition( new Point( Math.max( viewPos.x - scrollUnits.left, 0 ), viewPos.y ) );
		}
		else if( ( viewPos.x + viewWidth - cursorLoc.x ) < insets.right )
		{ // scroll right
			viewport.setViewPosition( new Point( Math.min( viewPos.x + scrollUnits.right, comp.getWidth() - viewWidth ),
					viewPos.y ) );
		}
	}

	public Insets getAutoscrollInsets()
	{
		Rectangle raOuter = comp.getBounds();
		Rectangle raInner = comp.getParent().getBounds();
		return new Insets( raInner.y - raOuter.y + AUTOSCROLL_MARGIN, raInner.x - raOuter.x + comp.getWidth(),
				raOuter.height - raInner.height - raInner.y + raOuter.y + AUTOSCROLL_MARGIN, raOuter.width - raInner.width
						- raInner.x + raOuter.x + AUTOSCROLL_MARGIN );
	}

	JViewport getViewport()
	{
		return ( JViewport )SwingUtilities.getAncestorOfClass( JViewport.class, comp );
	}
}