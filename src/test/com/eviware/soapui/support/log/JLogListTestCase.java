package com.eviware.soapui.support.log;

import junit.framework.TestCase;

public class JLogListTestCase extends TestCase
{
   public void testMemory() throws Exception
   {
   	JLogList list = new JLogList( "test" );
   	
//   	for( long c = 0; c < 100000000; c++ )
//   	{
//   		list.addLine("testing");
//   		Thread.sleep( 2 );
//   		
//   		if( c % 1000 == 0 )
//   			System.out.println( c );
//   	}
   }
}
