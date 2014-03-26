package com.eviware.soapui.ui.support;

import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.awt.*;

/**
 * Due to a limitation in the swing implementation for OSX we, for the time being,
 * need to avoid using indeterminate, long running JProgressBars.
 * The problem is that the thread animating the progress bar can hog a full CPU core.
 */
public class JProgressBarWrapper
{


	private JProgressBar progressBar;

	public void setIndeterminate( boolean b )
	{
		if( progressBar != null )
		{
			progressBar.setIndeterminate( b );
		}
	}


	public void addToToolBar( JComponent jComponent )
	{
		if(!UISupport.isMac()) {
			progressBar = new JProgressBar(  );
			JPanel progressBarPanel = UISupport.createProgressBarPanel( progressBar, 2, false );
			progressBarPanel.setPreferredSize( new Dimension(60, 20 ));
			jComponent.add( progressBarPanel );
		}
	}
}
