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

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.actions.EnableLineNumbersAction;
import com.eviware.soapui.support.xml.actions.FormatXmlAction;
import com.eviware.soapui.support.xml.actions.GoToLineAction;
import com.eviware.soapui.support.xml.actions.InsertBase64FileTextAreaAction;
import com.eviware.soapui.support.xml.actions.LoadXmlTextAreaAction;
import com.eviware.soapui.support.xml.actions.SaveXmlTextAreaAction;
import com.eviware.soapui.ui.support.FindAndReplaceDialogView;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import java.awt.Color;

public class SyntaxEditorUtil {
    public static RSyntaxTextArea createDefaultXmlSyntaxTextArea() {
        return createDefaultSyntaxTextArea(SyntaxConstants.SYNTAX_STYLE_XML);
    }

    public static RSyntaxTextArea createDefaultJsonSyntaxTextArea() {
        return createDefaultSyntaxTextArea(SyntaxConstants.SYNTAX_STYLE_XML);
    }

    public static RSyntaxTextArea createDefaultJavaScriptSyntaxTextArea() {
        return createDefaultSyntaxTextArea(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
    }

    public static RSyntaxTextArea createDefaultSQLSyntaxTextArea() {
        RSyntaxTextArea textArea = new RSyntaxTextArea() {
            protected void configurePopupMenu(javax.swing.JPopupMenu popupMenu) {
                // Suppress superclass behavior
            }
        };
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        return decorateSyntaxArea(textArea);
    }

    private static RSyntaxTextArea decorateSyntaxArea(RSyntaxTextArea textArea) {
        textArea.setFont(UISupport.getEditorFont());
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setCaretPosition(0);
        textArea.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.WHITE));
        return textArea;
    }

    private static RSyntaxTextArea createDefaultSyntaxTextArea(String type) {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(type);
        return decorateSyntaxArea(textArea);
    }

    public static RSyntaxTextArea addDefaultActions(RSyntaxTextArea editor, RTextScrollPane scrollPane, boolean readOnly) {
        JPopupMenu popupMenu = editor.getPopupMenu();

        SaveXmlTextAreaAction saveXmlTextAreaAction = new SaveXmlTextAreaAction(editor, "Save");
        EnableLineNumbersAction enableLineNumbersAction = new EnableLineNumbersAction(scrollPane, "Toggle Line Numbers");
        GoToLineAction goToLineAction = new GoToLineAction(editor, "Go To Line");

        int cnt = popupMenu.getComponentCount();
        for (int i = cnt - 1; i >= 0; i--) {
            if (popupMenu.getComponent(i) instanceof JSeparator) {
                popupMenu.remove(popupMenu.getComponent(i));
            }
        }
        FormatXmlAction formatXmlAction = null;
        if (!readOnly) {
            formatXmlAction = new FormatXmlAction(editor);
            FindAndReplaceDialogView findAndReplaceDialog = new FindAndReplaceDialogView(editor);
            popupMenu.insert(formatXmlAction, 1);
            popupMenu.addSeparator();
            popupMenu.add(findAndReplaceDialog);
            if (UISupport.isMac()) {
                editor.getInputMap().put(KeyStroke.getKeyStroke("meta F"), findAndReplaceDialog);
            } else {
                editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl F"), findAndReplaceDialog);
            }
        }
        popupMenu.addSeparator();
        popupMenu.add(goToLineAction);
        popupMenu.add(enableLineNumbersAction);
        popupMenu.addSeparator();
        popupMenu.add(saveXmlTextAreaAction);

        LoadXmlTextAreaAction loadXmlTextAreaAction = null;
        if (!readOnly) {
            loadXmlTextAreaAction = new LoadXmlTextAreaAction(editor, "Load");
            popupMenu.add(loadXmlTextAreaAction);
            popupMenu.add(new InsertBase64FileTextAreaAction(editor, "Insert File as Base64"));
        }

        if (UISupport.isMac()) {
            editor.getInputMap().put(KeyStroke.getKeyStroke("meta S"), saveXmlTextAreaAction);
            editor.getInputMap().put(KeyStroke.getKeyStroke("control L"), enableLineNumbersAction);
            editor.getInputMap().put(KeyStroke.getKeyStroke("control G"), goToLineAction);
            if (!readOnly) {
                editor.getInputMap().put(KeyStroke.getKeyStroke("shift meta F"), formatXmlAction);
                editor.getInputMap().put(KeyStroke.getKeyStroke("meta L"), loadXmlTextAreaAction);
            }
        } else {
            editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl S"), saveXmlTextAreaAction);
            editor.getInputMap().put(KeyStroke.getKeyStroke("alt L"), enableLineNumbersAction);
            editor.getInputMap().put(KeyStroke.getKeyStroke("control G"), goToLineAction);
            if (!readOnly) {
                editor.getInputMap().put(KeyStroke.getKeyStroke("alt F"), formatXmlAction);
                editor.getInputMap().put(KeyStroke.getKeyStroke("ctrl L"), loadXmlTextAreaAction);
            }
        }
        return editor;
    }

    public static void setMediaType(RSyntaxTextArea inputArea, String mediaType) {
        if (mediaType.contains("json")) {
            inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        } else if (mediaType.contains("xml")) {
            inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        } else {
            try {
                ContentType contentType = new ContentType(mediaType);
                String subType = contentType.getSubType();
                String textContentType = "text/" + subType.replaceAll(".*\\+", "");
                if (TokenMakerFactory.getDefaultInstance().keySet().contains(textContentType)) {
                    inputArea.setSyntaxEditingStyle(textContentType);
                } else {
                    inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                }
            } catch (ParseException e) {
                inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            }
        }
        // Force rendering with new style
        inputArea.setText(inputArea.getText());
    }
}
