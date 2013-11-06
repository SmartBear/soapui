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

package com.eviware.soapui.support.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * @author Angelo De Caro
 */
public class MonitorPanel extends JPanel
{
	public Surface surf;

	public MonitorPanel( MonitorSource monitorSource )
	{
		setLayout( new BorderLayout() );
		add( surf = createSurface() );
		setMonitorSource( monitorSource );

		JPopupMenu popup = new JPopupMenu();
		popup.add( new GCAction() );

		setComponentPopupMenu( popup );
	}

	public void start()
	{
		surf.start();
	}

	public void stop()
	{
		surf.stop();
	}

	public MonitorSource getMonitorSource()
	{
		return surf.getMonitorSource();
	}

	public void setMonitorSource( MonitorSource monitorSource )
	{
		surf.setMonitorSource( monitorSource );
	}

	protected Surface createSurface()
	{
		return new Surface( null );
	}

	protected class Surface extends JPanel implements Runnable
	{
		private Thread thread;
		private MonitorSource monitorSource;

		private long sleepAmount = 1000;

		private int w, h;
		private BufferedImage backImage;
		private Graphics2D backImageGrfx;

		private Font font = new Font( "Times New Roman", Font.PLAIN, 11 );
		private int columnInc;

		private double[] points;
		private int validPoints;

		private int ascent, descent;

		private Rectangle2D mfRect = new Rectangle2D.Float();
		private Rectangle2D muRect = new Rectangle2D.Float();
		private Line2D graphLine = new Line2D.Float();
		private Color graphColor = new Color( 46, 139, 87 );
		private Color mfColor = new Color( 0, 100, 0 );

		public Surface( MonitorSource monitorSource )
		{
			this.monitorSource = monitorSource;
			setBackground( Color.black );
		}

		public void paint( Graphics g )
		{
			Dimension d = getSize();

			if( d.width != w || d.height != h )
			{
				w = d.width;
				h = d.height;

				backImage = ( BufferedImage )createImage( w, h );
				backImageGrfx = backImage.createGraphics();
				backImageGrfx.setFont( font );

				FontMetrics fm = backImageGrfx.getFontMetrics( font );
				ascent = fm.getAscent();
				descent = fm.getDescent();
			}

			backImageGrfx.setBackground( Color.DARK_GRAY );
			backImageGrfx.clearRect( 0, 0, w, h );

			float totalMemory = monitorSource.getTotal();
			float usedMemory = monitorSource.getUsed();
			float freeMemory = totalMemory - usedMemory;

			// Draw allocated and used strings
			backImageGrfx.setColor( Color.green );
			backImageGrfx.drawString( String.valueOf( ( int )totalMemory >> 10 ) + "K allocated", 4.0f,
					( float )ascent + 0.5f );
			String usedStr = String.valueOf( ( ( int )usedMemory ) >> 10 ) + "K used";
			backImageGrfx.drawString( usedStr, 4, h - descent );

			// Calculate remaining size
			float ssH = ascent + descent;
			float remainingHeight = ( h - ssH * 2 - 0.5f );
			float blockHeight = remainingHeight / 10;
			float blockWidth = 20.0f;

			// Memory Free
			backImageGrfx.setColor( mfColor );
			int MemUsage = ( int )( freeMemory / totalMemory * 10 );
			int i = 0;
			for( ; i < MemUsage; i++ )
			{
				mfRect.setRect( 5, ssH + i * blockHeight, blockWidth, blockHeight - 1 );
				backImageGrfx.fill( mfRect );
			}

			// Memory Used
			backImageGrfx.setColor( Color.green );
			for( ; i < 10; i++ )
			{
				muRect.setRect( 5, ssH + i * blockHeight, blockWidth, blockHeight - 1 );
				backImageGrfx.fill( muRect );
			}

			// Draw History Graph
			backImageGrfx.setColor( graphColor );
			int graphX = 30;
			int graphY = ( int )ssH;
			int graphW = w - graphX - 5;
			if( graphW < 0 )
				graphW = 0;
			int graphH = ( int )( ssH + ( 9 * blockHeight ) + blockHeight - 1 );

			i = 0;
			for( ; i < 10; i++ )
			{
				muRect.setRect( graphX, ssH + i * blockHeight - 1, graphW, blockHeight );
				backImageGrfx.draw( muRect );
			}

			// Draw animated column movement
			int graphColumn = graphW / 15;

			if( columnInc == 0 )
			{
				columnInc = graphColumn;
			}

			for( int j = graphX + columnInc; j < graphW + graphX; j += graphColumn )
			{
				graphLine.setLine( j, graphY, j, ssH + i * blockHeight - 1 );
				backImageGrfx.draw( graphLine );
			}

			--columnInc;

			if( points == null )
			{
				points = new double[graphW];
				validPoints = 0;
			}
			else if( points.length != graphW )
			{
				double[] tmp;
				if( validPoints < graphW )
				{
					tmp = new double[validPoints];
					System.arraycopy( points, 0, tmp, 0, tmp.length );
				}
				else
				{
					tmp = new double[graphW];
					System.arraycopy( points, points.length - tmp.length, tmp, 0, tmp.length );
					validPoints = tmp.length - 2;
				}
				points = new double[graphW];
				System.arraycopy( tmp, 0, points, 0, tmp.length );
			}
			else
			{
				backImageGrfx.setColor( Color.yellow );
				int x = w - 5;
				int sum = graphH - ( ascent + descent );

				for( int j = x - validPoints, k = 0; k < validPoints; k++ , j++ )
				{
					if( k != 0 )
					{
						if( points[k] != points[k - 1] )
						{
							backImageGrfx.drawLine( j - 1, graphY + ( int )( sum * points[k - 1] ), j, graphY
									+ ( int )( sum * points[k] ) );
						}
						else
						{
							backImageGrfx.fillRect( j, graphY + ( int )( sum * points[k] ), 1, 1 );
						}
					}
				}
			}

			g.drawImage( backImage, 0, 0, this );
		}

		public void run()
		{
			while( thread != null )
			{
				float totalMemory = monitorSource.getTotal();
				float usedMemory = monitorSource.getUsed();
				float freeMemory = totalMemory - usedMemory;

				if( points == null )
				{
					points = new double[1];
					validPoints = 0;
				}
				else if( points.length < validPoints + 1 )
				{
					double[] tmp;

					int graphW = validPoints + 1;
					if( validPoints < graphW )
					{
						tmp = new double[validPoints];
						System.arraycopy( points, 0, tmp, 0, tmp.length );
					}
					else
					{
						tmp = new double[graphW];
						System.arraycopy( points, points.length - tmp.length, tmp, 0, tmp.length );
						validPoints = tmp.length - 2;
					}
					points = new double[graphW];
					System.arraycopy( tmp, 0, points, 0, tmp.length );
				}
				else
				{
					points[validPoints] = ( freeMemory / totalMemory );
					if( validPoints + 2 == points.length )
					{
						// throw out oldest point
						System.arraycopy( points, 1, points, 0, validPoints );
						--validPoints;
					}
					else
					{
						validPoints++ ;
					}
				}

				repaint();

				try
				{
					Thread.sleep( sleepAmount );
				}
				catch( InterruptedException e )
				{
					break;
				}
			}
		}

		public void start()
		{
			if( monitorSource == null )
				throw new IllegalStateException( "Monitor Source cannot be null." );

			thread = new Thread( this );
			thread.setPriority( Thread.MIN_PRIORITY );
			thread.setDaemon( true );
			thread.setName( "MemoryMonitor" );
			thread.start();
		}

		public synchronized void stop()
		{
			thread = null;
			notify();
		}

		public MonitorSource getMonitorSource()
		{
			return monitorSource;
		}

		public void setMonitorSource( MonitorSource monitorSource )
		{
			this.monitorSource = monitorSource;
		}

		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}

		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}

		public Dimension getPreferredSize()
		{
			return new Dimension( 135, 80 );
		}
	}

	private class GCAction extends AbstractAction
	{
		private GCAction()
		{
			super( "Run GC" );
			putValue( SHORT_DESCRIPTION, "Runs finalization and garbage collector" );
		}

		public void actionPerformed( ActionEvent e )
		{
			System.runFinalization();
			Runtime.getRuntime().gc();
		}
	}
}
