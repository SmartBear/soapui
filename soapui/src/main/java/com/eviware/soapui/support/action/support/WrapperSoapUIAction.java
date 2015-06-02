package com.eviware.soapui.support.action.support;

import com.eviware.soapui.model.ModelItem;

import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Created by ole on 20/08/14.
 */
public class WrapperSoapUIAction extends AbstractSoapUIAction {
    private final Action swingAction;

    public WrapperSoapUIAction(Action swingAction) {
        super(swingAction.getClass().getSimpleName(),
                String.valueOf(swingAction.getValue(Action.NAME)),
                String.valueOf(swingAction.getValue(Action.SHORT_DESCRIPTION)));
        this.swingAction = swingAction;
    }

    @Override
    public void perform(ModelItem target, Object param) {
        swingAction.actionPerformed(new ActionEvent(target, 1, String.valueOf(param)));
    }
}
