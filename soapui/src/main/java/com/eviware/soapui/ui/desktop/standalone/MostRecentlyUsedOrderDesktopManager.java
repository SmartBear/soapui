package com.eviware.soapui.ui.desktop.standalone;

import com.eviware.soapui.SoapUI;

import javax.swing.*;
import java.beans.PropertyVetoException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A DesktopManager managing the internal frames in Desktop using a Most-Recently-Used order when changing
 * the active internal frame. A Deque (a stack-like data structure) is used to keep track of frames.
 * <ul>
 * <li><code>activateFrame(JInternalFrame)</code> puts the frame at top of stack : if frame was already present
 * in stack, remove it then add it at top, otherwise, add it at top.</li>
 * <li><code>deactivateFrame(JInternalFrame)</code> is a noop on the stack, delegate to superclass.</li>
 * <li><code>closeFrame(JInternalFrame)</code> removes frame from the stack and selects the frame at top of stack.</li>
 * <li><code>iconifyFrame(JInternalFrame></code> is like <code>closeFrame</code> as far as this manager is concerned,
 * but with iconifyFrame on superclass called.</li>
 * <li><code>deiconifyFrame</code> delegates to superclass to bring back the frame on desktop, puts it at top of stack
 * and makes sure it is selected.</li>
 * </ul>
 * At anytime, there is one frame selected (unless there are no (open) frames at all) and that frame is the top of
 * stack.
 */
public class MostRecentlyUsedOrderDesktopManager extends DefaultDesktopManager
{
	// Keep desktop panel list (JInternalFrame) of existing internal frames in a most-recently-used order (i.e. a stack).
	Deque<JInternalFrame> mostRecentlyUsedFrames = new ArrayDeque<JInternalFrame>();

	@Override
	public void activateFrame( JInternalFrame f )
	{
		if( f == null )
		{
			return;
		}
		super.activateFrame( f );
		if( !mostRecentlyUsedFrames.isEmpty() && f.equals( mostRecentlyUsedFrames.getFirst() ) )
		{
			selectTopFrame( null );
			return;
		}
		else if( !mostRecentlyUsedFrames.isEmpty() && mostRecentlyUsedFrames.contains( f ) )
		{
			mostRecentlyUsedFrames.remove( f );
		}
		JInternalFrame previousTop = mostRecentlyUsedFrames.isEmpty() ? null : mostRecentlyUsedFrames.getFirst();
		mostRecentlyUsedFrames.addFirst( f );
		selectTopFrame( previousTop );
	}

	@Override
	public void deactivateFrame( JInternalFrame f )
	{
		super.deactivateFrame( f );
	}

	@Override
	public void closeFrame( JInternalFrame f )
	{
		mostRecentlyUsedFrames.remove( f );
		super.closeFrame( f );
		selectTopFrame( f );
	}

	@Override
	public void iconifyFrame( JInternalFrame f )
	{
		mostRecentlyUsedFrames.remove( f );
		selectTopFrame( f );
		super.iconifyFrame( f );
	}

	@Override
	public void deiconifyFrame( JInternalFrame f )
	{
		super.deiconifyFrame( f );
		activateFrame( f );
	}

	protected void selectTopFrame( JInternalFrame previousTopFrame )
	{
		JInternalFrame topFrame = null;
		try
		{
			if( mostRecentlyUsedFrames.isEmpty() )
			{
				return;
			}
			else
			{
				topFrame = mostRecentlyUsedFrames.getFirst();
			}
			if( previousTopFrame != null && !previousTopFrame.equals( topFrame ) )
			{
				if( previousTopFrame.isSelected() )
				{
					previousTopFrame.setSelected( false );
				}
			}
			if( !topFrame.isSelected() )
			{
				topFrame.setSelected( true );
			}
		}
		catch( PropertyVetoException pve )
		{
		}
	}
}
