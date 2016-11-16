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

package com.eviware.soapui.support.xml;

import com.eviware.soapui.support.actions.FindAndReplaceable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.JComponent;

public class ProxyFindAndReplacable implements FindAndReplaceable {
    protected FindAndReplaceable proxytarget;
    protected boolean isReplaceAll = false;
    protected StringBuilder sbtartget;
    protected String oldValue;
    protected String newValue;
    protected int start;
    protected int end;

    public ProxyFindAndReplacable(FindAndReplaceable proxytarget) {
        this.proxytarget = proxytarget;
    }

    public void setSBTarget() {
        this.sbtartget = new StringBuilder();
        this.sbtartget.append(proxytarget.getText());
    }

    public FindAndReplaceable getProxytarget() {
        return proxytarget;
    }

    public int getCaretPosition() {
        return proxytarget.getCaretPosition();
    }

    public String getSelectedText() {
        return proxytarget.getSelectedText();
    }

    public int getSelectionEnd() {
        return proxytarget.getSelectionEnd();
    }

    public int getSelectionStart() {
        return proxytarget.getSelectionStart();
    }

    public String getText() {
        if (isReplaceAll) {
            return sbtartget.toString();
        } else {
            return proxytarget.getText();
        }

    }

    public String getDialogText() {
        return proxytarget.getText();
    }

    public boolean isEditable() {
        return proxytarget.isEditable();
    }

    public void select(int start, int end) {
        if (isReplaceAll) {
            this.start = start;
            this.end = end;
        } else {
            proxytarget.select(start, end);
        }

    }

    public void setSelectedText(String txt) {
        if (isReplaceAll) {
            sbtartget.replace(this.start, this.end, newValue);
        } else {
            proxytarget.setSelectedText(txt);
        }

    }

    public boolean isReplaceAll() {
        return isReplaceAll;
    }

    public void setReplaceAll(boolean isReplaceAll) {
        if (proxytarget instanceof RSyntaxTextArea) {
            this.isReplaceAll = isReplaceAll;
        } else {
            this.isReplaceAll = false;
        }
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public void flushSBText() {
        if (proxytarget instanceof RSyntaxTextArea) {
            ((RSyntaxTextArea) proxytarget).setText(sbtartget.toString());
        }

    }

    public void setCarretPosition(boolean forward) {
        if (proxytarget instanceof RSyntaxTextArea) {
            ((RSyntaxTextArea) proxytarget).setCaretPosition(forward ? getEnd() : getStart());
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public JComponent getEditComponent() {
        return proxytarget.getEditComponent();
    }
}
