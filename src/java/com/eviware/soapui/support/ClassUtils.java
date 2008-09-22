package com.eviware.soapui.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUtils
{
   public static List<Class> getImplementedAndExtendedClasses( Object obj )
   {
      ArrayList<Class> result = new ArrayList<Class>();
      addImplementedAndExtendedClasses( obj.getClass(), result );
      return result;
   }

   private static void addImplementedAndExtendedClasses( Class clazz, ArrayList<Class> result )
   {
      result.add( clazz );
//      result.addAll( Arrays.asList( clazz.getInterfaces() ));
      addImplementedInterfaces(clazz, result);
      if( clazz.getSuperclass() != null )
      {
          addImplementedAndExtendedClasses( clazz.getSuperclass(), result );
      }
   }

   private static void addImplementedInterfaces( Class intrfc, ArrayList<Class> result )
   {
//      result.add( intrfc.getClass() );
      Class<?> [] interfacesArray = intrfc.getInterfaces();
      if( interfacesArray.length > 0 )
      {
         result.addAll( Arrays.asList( interfacesArray ));
         for (int i = 0; i < interfacesArray.length; i++)
			{
				Class<?> class1 = interfacesArray[i];
				addImplementedInterfaces( class1, result );
			}
      }
   }

}
