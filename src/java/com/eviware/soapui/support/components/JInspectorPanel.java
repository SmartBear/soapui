package com.eviware.soapui.support.components;

import javax.swing.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ole
 * Date: Aug 29, 2008
 * Time: 11:20:52 PM
 * To change this template use File | Settings | File Templates.
 */
public interface JInspectorPanel {
   public <T extends Inspector> T addInspector( final T inspector )  ;

   JComponent getComponent();

   void setDefaultDividerLocation(float v);

   public void activate( Inspector inspector );

   void setCurrentInspector(String s);

   void setDividerLocation(int i);

   void setResizeWeight(double v);

   List<Inspector> getInspectors();

   Inspector getCurrentInspector();

   Inspector getInspectorByTitle(String title);

   void deactivate();

   void removeInspector(Inspector inspector);

   void setContentComponent(JComponent component);

   int getDividerLocation();

   Inspector getInspector(String inspectorId);

   void setInspectorVisible(Inspector inspector, boolean b);

   void setResetDividerLocation();

   void release();
}
