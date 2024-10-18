package com.smartbear.soapui.cucumber;

import com.smartbear.soapui.utils.IntegrationTest;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(IntegrationTest.class)
@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "com.smartbear.soapui.stepdefs.java",
        features = "src/test/resources/features/",
        tags = "@AutomatedWithJava",
        plugin = {"html:target/cucumber-html-report", "json:target/cucumber-java-results.json"})
public class CucumberJavaRunner {
}