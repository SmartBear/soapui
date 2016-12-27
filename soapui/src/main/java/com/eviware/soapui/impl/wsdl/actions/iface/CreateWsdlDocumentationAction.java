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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class CreateWsdlDocumentationAction extends AbstractSoapUIAction<WsdlInterface> {
    public static final String SOAPUI_ACTION_ID = "CreateWsdlDocumentationAction";

    private static final String REPORT_DIRECTORY_SETTING = CreateWsdlDocumentationAction.class.getSimpleName()
            + "@report-directory";
    private XFormDialog dialog;
    private static Map<String, Transformer> transformers;

    public CreateWsdlDocumentationAction() {
        super("CreateWsdlDocumentationAction", "Generate Documentation",
                "Generate simple HTML Documentation for this WSDL");
    }

    public void perform(WsdlInterface target, Object param) {
        try {
            if (dialog == null) {
                dialog = ADialogBuilder.buildDialog(Form.class);
            }

            Settings settings = target.getSettings();
            dialog.setValue(Form.OUTPUT_FOLDER, settings.getString(REPORT_DIRECTORY_SETTING, ""));

            if (!dialog.show()) {
                return;
            }

            settings.setString(REPORT_DIRECTORY_SETTING, dialog.getValue(Form.OUTPUT_FOLDER));

            final File reportDirectory = new File(settings.getString(REPORT_DIRECTORY_SETTING, ""));
            String reportDirAbsolutePath = reportDirectory.getAbsolutePath();
            String filename = reportDirAbsolutePath + File.separatorChar + "report.xml";
            String reportUrl = transform(target, reportDirAbsolutePath, filename);
            Tools.openURL(reportUrl);
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }

    private static String transform(WsdlInterface target, String reportDirAbsolutePath, String filename)
            throws Exception {
        if (transformers == null) {
            initTransformers();
        }

        Transformer transformer = transformers.get("WSDL");
        if (transformer == null) {
            throw new Exception("Missing transformer for format [WSDL]");
        }

        transformer.setParameter("output.dir", reportDirAbsolutePath);

        String reportFile = reportDirAbsolutePath + File.separatorChar + "wsdl-report.html";
        StreamResult result = new StreamResult(new FileWriter(reportFile));

        WsdlDefinitionExporter exporter = new WsdlDefinitionExporter(target);
        String infile = exporter.export(reportDirAbsolutePath);

        transformer.transform(new StreamSource(new FileReader(infile)), result);

        String reportUrl = new File(reportFile).toURI().toURL().toString();
        return reportUrl;
    }

    protected static void initTransformers() throws Exception {
        transformers = new HashMap<String, Transformer>();
        TransformerFactory xformFactory = TransformerFactory.newInstance();

        Transformer transformer = xformFactory.newTransformer(new StreamSource(SoapUI.class
                .getResourceAsStream("/com/eviware/soapui/resources/doc/wsdl-viewer.xsl")

                // new File( "C:\\dev\\wsdl-viewer-1.3\\wsdl-viewer.xsl")
        ));
        transformers.put("WSDL", transformer);
    }

    @AForm(description = "Creates an HTML-Report for the interface WSDL", name = "Create Documentation", helpUrl = HelpUrls.CREATEWADLDOC_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    public interface Form {
        @AField(name = "Output Folder", description = "The folder where to create the report", type = AFieldType.FOLDER)
        public final static String OUTPUT_FOLDER = "Output Folder";
    }
}
