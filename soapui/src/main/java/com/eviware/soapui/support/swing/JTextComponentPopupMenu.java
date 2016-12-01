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

package com.eviware.soapui.support.swing;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.Undoable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.event.ActionEvent;

public final class JTextComponentPopupMenu extends JPopupMenu implements PopupMenuListener {
    private final JTextComponent textComponent;
    private CutAction cutAction;
    private CopyAction copyAction;
    private PasteAction pasteAction;
    private ClearAction clearAction;
    private SelectAllAction selectAllAction;
    private UndoAction undoAction;
    private RedoAction redoAction;

    public static JTextComponentPopupMenu add(JTextComponent textComponent) {
        JPopupMenu componentPopupMenu = textComponent instanceof RSyntaxTextArea ? ((RSyntaxTextArea) textComponent)
                .getPopupMenu() : textComponent.getComponentPopupMenu();
        //		JPopupMenu componentPopupMenu = textComponent.getComponentPopupMenu();

        // double-check
        if (componentPopupMenu instanceof JTextComponentPopupMenu) {
            return (JTextComponentPopupMenu) componentPopupMenu;
        }

        JTextComponentPopupMenu popupMenu = new JTextComponentPopupMenu(textComponent);
        if (componentPopupMenu != null && componentPopupMenu.getComponentCount() > 0) {

            while (componentPopupMenu.getComponentCount() > 0) {
                Component comp = componentPopupMenu.getComponent(componentPopupMenu.getComponentCount() - 1);
                if (comp instanceof AbstractButton) {
                    if ("Copy".equals(((AbstractButton) comp).getText())
                            || "Cut".equals(((AbstractButton) comp).getText())
                            || "Paste".equals(((AbstractButton) comp).getText())
                            || "Undo".equals(((AbstractButton) comp).getText())
                            || "Redo".equals(((AbstractButton) comp).getText())
                            || "Can\'t Redo".equals(((AbstractButton) comp).getText())
                            || "Can\'t Undo".equals(((AbstractButton) comp).getText())
                            || "Select All".equals(((AbstractButton) comp).getText())) {
                        componentPopupMenu.remove(comp);
                        continue;
                    }
                    popupMenu.insert(comp, 0);
                }
                componentPopupMenu.remove(comp);
            }

            popupMenu.insert(new JSeparator(), 0);
        }

        if (componentPopupMenu != null) {
            for (PopupMenuListener listener : componentPopupMenu.getPopupMenuListeners()) {
                popupMenu.addPopupMenuListener(listener);
            }
        }

        if (textComponent instanceof RSyntaxTextArea) {
            ((RSyntaxTextArea) textComponent).setPopupMenu(popupMenu);
        } else {
            textComponent.setComponentPopupMenu(popupMenu);
        }
        return popupMenu;
    }

    private JTextComponentPopupMenu(JTextComponent textComponent) {
        super("Edit");
        this.textComponent = textComponent;

        if (textComponent instanceof Undoable || textComponent instanceof RSyntaxTextArea) {
            undoAction = new UndoAction();
            add(undoAction);

            redoAction = new RedoAction();
            add(redoAction);

            addSeparator();
        }

        cutAction = new CutAction();
        add(cutAction);
        copyAction = new CopyAction();
        add(copyAction);
        pasteAction = new PasteAction();
        add(pasteAction);
        clearAction = new ClearAction();
        add(clearAction);
        addSeparator();
        selectAllAction = new SelectAllAction();
        add(selectAllAction);

        addPopupMenuListener(this);
    }

    private final class CutAction extends AbstractAction {
        public CutAction() {
            super("Cut");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu X"));
        }

        public void actionPerformed(ActionEvent e) {
            textComponent.cut();
        }
    }

    private final class CopyAction extends AbstractAction {
        public CopyAction() {
            super("Copy");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu C"));
        }

        public void actionPerformed(ActionEvent e) {
            textComponent.copy();
        }
    }

    private final class PasteAction extends AbstractAction {
        public PasteAction() {
            super("Paste");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu V"));
        }

        public void actionPerformed(ActionEvent e) {
            textComponent.paste();
        }
    }

    private final class ClearAction extends AbstractAction {
        public ClearAction() {
            super("Clear");
        }

        public void actionPerformed(ActionEvent e) {
            textComponent.setText("");
        }
    }

    private final class SelectAllAction extends AbstractAction {
        public SelectAllAction() {
            super("Select All");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu A"));
        }

        public void actionPerformed(ActionEvent e) {
            textComponent.selectAll();
        }
    }

    private final class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu Z"));
        }

        public void actionPerformed(ActionEvent e) {
            if (textComponent instanceof RSyntaxTextArea) {
                ((RSyntaxTextArea) textComponent).undoLastAction();
            } else {
                ((Undoable) textComponent).undo();
            }
        }
    }

    private final class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu Y"));
        }

        public void actionPerformed(ActionEvent e) {
            if (textComponent instanceof RSyntaxTextArea) {
                ((RSyntaxTextArea) textComponent).redoLastAction();
            } else {
                ((Undoable) textComponent).redo();
            }
        }
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (textComponent instanceof Undoable) {
            undoAction.setEnabled(((Undoable) textComponent).canUndo());
            redoAction.setEnabled(((Undoable) textComponent).canRedo());
        }

        if (textComponent instanceof RSyntaxTextArea) {
            undoAction.setEnabled(((RSyntaxTextArea) textComponent).canUndo());
            redoAction.setEnabled(((RSyntaxTextArea) textComponent).canRedo());
        }

        cutAction.setEnabled(textComponent.getSelectionEnd() != textComponent.getSelectionStart());
        copyAction.setEnabled(cutAction.isEnabled());
        clearAction.setEnabled(cutAction.isEnabled());
        selectAllAction.setEnabled(textComponent.getText().length() > 0);
    }
}
