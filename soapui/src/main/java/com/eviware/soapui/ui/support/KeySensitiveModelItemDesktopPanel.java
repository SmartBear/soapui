/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.ui.support;

import com.eviware.soapui.model.ModelItem;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Adds KeyListener to panels and handles it.
 *
 * @param <T>
 * @author robert.nemet
 */
@SuppressWarnings("serial")
public abstract class KeySensitiveModelItemDesktopPanel<T extends ModelItem> extends ModelItemDesktopPanel<T> implements
        KeyListener {
    public KeySensitiveModelItemDesktopPanel(T modelItem) {
        super(modelItem);

        this.addKeyListener(this);
    }

    @Override
    protected boolean release() {
        removeKeyListener(this);
        return super.release();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_F2:
                renameModelItem();
                break;
            case KeyEvent.VK_F9:
                cloneModelItem();
                break;
        }
        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    protected void renameModelItem() {
    }

    ;

    protected void cloneModelItem() {
    }

    ;


}
