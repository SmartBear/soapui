package com.eviware.soapui.support.components;

import javax.swing.*;

public class JInspectorPanelFactory {

   public static Class<? extends JInspectorPanel> inspectorPanelClass = JInspectorPanelImpl.class;

   public static JInspectorPanel build( JComponent contentComponent )
   {
      try {
         return inspectorPanelClass.getConstructor( JComponent.class ).newInstance( contentComponent );
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static JInspectorPanel build( JComponent contentComponent, int orientation )
   {
       try {
         return inspectorPanelClass.getConstructor( JComponent.class, int.class ).newInstance( contentComponent, orientation );
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }
}
