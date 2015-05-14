package com.eviware.soapui.support.action.swing;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.ui.toolbar.ToolbarItem;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

public class SwingToolbarActionDelegate<T extends ModelItem> extends SwingActionDelegate<T> implements ToolbarItem {

    private JButton button;

    public SwingToolbarActionDelegate(SoapUIActionMapping<T> mapping, T target) {
        super(mapping, target);
    }

    public SwingToolbarActionDelegate(SoapUIActionMapping<T> mapping) {
        super(mapping);
    }

    @Override
    public JComponent getComponent() {
        if (button == null) {
            prepareComponent();
        }
        return button;
    }

    @Override
    public int getToolbarIndex() {
        return getMapping().getToolbarIndex();
    }

    private void prepareComponent() {
        button = new JButton(this);

        button.setBorderPainted(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);

        if (StringUtils.hasContent(getMapping().getIconPath())) {
            button.setIcon(UISupport.createImageIcon(getMapping().getIconPath()));
        }
    }
}