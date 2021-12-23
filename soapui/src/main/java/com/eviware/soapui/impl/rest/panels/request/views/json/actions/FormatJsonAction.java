package com.eviware.soapui.impl.rest.panels.request.views.json.actions;

import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.UISupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

public class FormatJsonAction extends AbstractAction {
    private final static String TITLE = "Format JSON";
    private final static String TOOLTIP = "Pretty-prints JSON";

    private final static Logger log = LogManager.getLogger(FormatJsonAction.class);
    private final RSyntaxTextArea textArea;

    public FormatJsonAction(String title, RSyntaxTextArea textArea) {
        super(title);
        this.textArea = textArea;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/format_request.gif"));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        if (UISupport.isMac()) {
            String keyStroke = "shift meta F";
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke(keyStroke));
            textArea.getInputMap().put(KeyStroke.getKeyStroke(keyStroke), this);
        } else {
            String keyStroke = "alt F";
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke(keyStroke));
            textArea.getInputMap().put(KeyStroke.getKeyStroke(keyStroke), this);
        }
    }

    public FormatJsonAction(RSyntaxTextArea textArea) {
        this(TITLE, textArea);
    }

    protected boolean isValidJson(String jsonText) {
        return JsonUtil.isValidJson(jsonText);
    }

    public void actionPerformed(ActionEvent event) {
        try {
            Rectangle visibleRect = textArea.getVisibleRect();
            String message = textArea.getText();
            if (!isValidJson(message)) {
                return;
            }
            JsonNode json = JsonUtil.parseTrimmedTextToJsonNode(message);
            textArea.setText(JsonUtil.format(json));
            textArea.setCaretPosition(0);
            SwingUtilities.invokeLater(() -> {
                textArea.scrollRectToVisible(visibleRect);
            });
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
