package com.eviware.soapui.analytics;

public enum ProductArea {
    NO ("NotSpecified"), // something default for usage in unclear circumstances
    MAIN_MENU ("MainMenu"), // the menu on the top which contains File, Project, Suite, Case,....
    NAVIGATOR_TREE ("Navigator"), // Actions from Navigator tree
    MAIN_EDITOR ("TestEditor"), // Actions raised by TestStep/TestCase/TestSuite/LoadTest/Virt/Security test editors (the list likely is not complete)
    STATIC_MAIN_TOOLBAR ("StaticMainToolbar"); // Relatively static main toolbar which contains Proxy, Environments, Plugins, ... disregard to active module;

    private final String productArea;

    ProductArea(String productArea) {
        this.productArea = productArea;
    }

    public String getProductArea() {
        return productArea;
    }
}
