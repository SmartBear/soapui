package com.smartbear.soapui.cucumber;

import com.smartbear.soapui.utils.IntegrationTest;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(IntegrationTest.class)
@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "com.smartbear.soapui.stepdefs.java",
        features = "src/test/resources/features/",
        tags = "@AutomatedWithJava",
        format = "json:target/cucumber-java-results.json")
public class CucumberJavaRunner {
}