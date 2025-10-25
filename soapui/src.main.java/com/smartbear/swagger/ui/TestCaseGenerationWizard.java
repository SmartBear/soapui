package com.smartbear.swagger.ui;

import com.smartbear.swagger.utils.PropertyTransferDiscovery;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

public class TestCaseGenerationWizard extends JDialog {
    private OpenAPI openApi;
    private List<PropertyTransferDiscovery.PropertyTransfer> transfers;

    public TestCaseGenerationWizard(Frame owner, OpenAPI openApi, List<PropertyTransferDiscovery.PropertyTransfer> transfers) {
        super(owner, "Test Case Generation Wizard", true);
        this.openApi = openApi;
        this.transfers = transfers;
        initUI();
    }

    public TestCaseGenerationWizard(Frame owner) {
        super(owner, "Test Case Generation Wizard", true);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Endpoints and Methods Tree
        JTree endpointsTree = new JTree();
        endpointsTree.setModel(createTreeModel());
        JScrollPane treeScrollPane = new JScrollPane(endpointsTree);
        treeScrollPane.setBorder(BorderFactory.createTitledBorder("Endpoints and Methods"));

        // Property Transfers Table
        JTable propertyTransfersTable = new JTable();
        propertyTransfersTable.setModel(createTableModel());
        JScrollPane tableScrollPane = new JScrollPane(propertyTransfersTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Proposed Property Transfers"));

        // Main Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScrollPane, tableScrollPane);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton generateButton = new JButton("Generate");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(600, 800);
        setLocationRelativeTo(getOwner());
    }

    private DefaultTreeModel createTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("API");
        if (openApi != null) {
            openApi.getPaths().forEach((path, pathItem) -> {
                DefaultMutableTreeNode pathNode = new DefaultMutableTreeNode(path);
                if (pathItem.getGet() != null) {
                    pathNode.add(new DefaultMutableTreeNode("GET: " + getOperationId(pathItem.getGet())));
                }
                if (pathItem.getPost() != null) {
                    pathNode.add(new DefaultMutableTreeNode("POST: " + getOperationId(pathItem.getPost())));
                }
                if (pathItem.getPut() != null) {
                    pathNode.add(new DefaultMutableTreeNode("PUT: " + getOperationId(pathItem.getPut())));
                }
                if (pathItem.getDelete() != null) {
                    pathNode.add(new DefaultMutableTreeNode("DELETE: " + getOperationId(pathItem.getDelete())));
                }
                if (pathItem.getPatch() != null) {
                    pathNode.add(new DefaultMutableTreeNode("PATCH: " + getOperationId(pathItem.getPatch())));
                }
                if (pathItem.getOptions() != null) {
                    pathNode.add(new DefaultMutableTreeNode("OPTIONS: " + getOperationId(pathItem.getOptions())));
                }
                root.add(pathNode);
            });
        }
        return new DefaultTreeModel(root);
    }

    private String getOperationId(Operation operation) {
        return operation.getOperationId() != null ? operation.getOperationId() : "";
    }

    private javax.swing.table.DefaultTableModel createTableModel() {
        String[] columnNames = {"Source", "Target", "Property"};
        Object[][] data = {};
        if (transfers != null) {
            data = new Object[transfers.size()][3];
            for (int i = 0; i < transfers.size(); i++) {
                PropertyTransferDiscovery.PropertyTransfer transfer = transfers.get(i);
                data[i][0] = transfer.sourceOperationId;
                data[i][1] = transfer.targetOperationId;
                data[i][2] = transfer.propertyName;
            }
        }
        return new javax.swing.table.DefaultTableModel(data, columnNames);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TestCaseGenerationWizard wizard = new TestCaseGenerationWizard(null);
            // Using a timer to automatically close the dialog after a few seconds
            // to avoid blocking in a headless environment.
            Timer timer = new Timer(5000, e -> wizard.dispose());
            timer.setRepeats(false);
            timer.start();

            wizard.setVisible(true); // This will block until the dialog is disposed
            System.out.println("Wizard closed.");
        });
    }
}
