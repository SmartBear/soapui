
package com.eviware.soapui.ui.desktop;

import com.eviware.soapui.model.ModelItem;

public abstract class DesktopPanelFactory<T extends ModelItem> {
    public abstract DesktopPanel buildDesktopPanel(T modelItem);
    public abstract Class<T> getTargetModelItem();
}
