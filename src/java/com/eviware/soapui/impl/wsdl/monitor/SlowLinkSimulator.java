/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eviware.soapui.impl.wsdl.monitor;

/**
 * class to simulate slow connections by slowing down the system
 */
public class SlowLinkSimulator
{

	/**
	 * Field delayBytes
	 */
	private int delayBytes;

	/**
	 * Field delayTime
	 */
	private int delayTime;

	/**
	 * Field currentBytes
	 */
	private int currentBytes;

	/**
	 * Field totalBytes
	 */
	private int totalBytes;

	/**
	 * construct
	 * 
	 * @param delayBytes
	 *           bytes per delay; set to 0 for no delay
	 * @param delayTime
	 *           delay time per delay in milliseconds
	 */
	public SlowLinkSimulator( int delayBytes, int delayTime )
	{
		this.delayBytes = delayBytes;
		this.delayTime = delayTime;
	}

	/**
	 * construct by copying delay bytes and time, but not current count of bytes
	 * 
	 * @param that
	 *           source of data
	 */
	public SlowLinkSimulator( SlowLinkSimulator that )
	{
		this.delayBytes = that.delayBytes;
		this.delayTime = that.delayTime;
	}

	/**
	 * how many bytes have gone past?
	 * 
	 * @return integer
	 */
	public int getTotalBytes()
	{
		return totalBytes;
	}

	/**
	 * log #of bytes pumped. Will pause when necessary. This method is not
	 * synchronized
	 * 
	 * @param bytes
	 */
	public void pump( int bytes )
	{
		totalBytes += bytes;
		if( delayBytes == 0 )
		{

			// when not delaying, we are just a byte counter
			return;
		}
		currentBytes += bytes;
		if( currentBytes > delayBytes )
		{

			// we have overshot. lets find out how far
			int delaysize = currentBytes / delayBytes;
			long delay = delaysize * ( long )delayTime;

			// move byte counter down to the remainder of bytes
			currentBytes = currentBytes % delayBytes;

			// now wait
			try
			{
				Thread.sleep( delay );
			}
			catch( InterruptedException e )
			{
				// ignore the exception
			}
		}
	}

	/**
	 * get the current byte count
	 * 
	 * @return integer
	 */
	public int getCurrentBytes()
	{
		return currentBytes;
	}

	/**
	 * set the current byte count
	 * 
	 * @param currentBytes
	 */
	public void setCurrentBytes( int currentBytes )
	{
		this.currentBytes = currentBytes;
	}
}
