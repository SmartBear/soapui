package com.eviware.soapui.security.fuzzer;

import java.util.List;

import com.eviware.soapui.config.FuzzerConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStepInterface;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * A simple Fuzzer implementation. This will also provide different fuzzer instances
 * IF we find that this class gets to complex, we could refactor it to use  Fuzzer registry
 * 
 * @author nenad.ristic
 *
 */
public class Fuzzer {
	private FuzzerConfig config;
	private int currentIndex = 0;
	private static Fuzzer sqlFuzzer;
	
	private Fuzzer(String[] values) {
		config = FuzzerConfig.Factory.newInstance();
		config.setValueArray(values);
	}
	
	/**
	 * Resets the fuzzer so that it starts from the beginning of the list
	 */
	public void resetFuzzer() {
		currentIndex = 0;
	}
	
	/**
	 * Gets the TestStep with the appropriate parameter fuzzed with the next fuzz value
	 * Later we can make this more complex to use various encodings
	 * 
	 * @param testStep The TestStep to fuzz
	 * @param param the Parameter to fuzz
	 * 
	 */
	public void  getNextFuzzedTestStep(TestStep testStep, String param) {
		if (currentIndex < config.getValueList().size()) {
			if ( testStep instanceof WsdlTestRequestStep ) {
				AbstractHttpRequest<?> request = ((WsdlTestRequestStep) testStep).getHttpRequest();
				request.getRequestContent();
				String newContent = XmlUtils.setXPathContent(request.getRequestContent(), param.substring(param.lastIndexOf("\n") + 1), config.getValueArray(currentIndex));
				request.setRequestContent(newContent);
			} else {
				HttpTestRequestInterface<?> request = (( HttpTestRequestStepInterface )testStep).getTestRequest();
				request.setPropertyValue(param, config.getValueArray(currentIndex));
			}
			currentIndex++;
		}
	}
	
	/**
	 * Gets the TestStep with the appropriate parameters fuzzed with the next fuzz value
	 * Later we can make this more complex to use various encodings
	 * 
	 * @param testStep The TestStep to fuzz
	 * @param params the List of Parameters to fuzz
	 * 
	 */
	public void  getNextFuzzedTestStep(TestStep testStep, List<String> params) {
		if (currentIndex < config.getValueList().size()) {
			HttpTestRequestInterface<?> request = (( HttpTestRequestStepInterface )testStep).getTestRequest();
			for (String param : params)
				request.setPropertyValue(param, config.getValueArray(currentIndex));
			currentIndex++;
		}
	}
	
	/**
	 * This creates a  fuzzer designed to test for a SQL injection
	 * This will be sufficient to test if the vulnerability exists, although
	 * not its extent, since deep testing that could be dangerous to the database.
	 * 
	 * @return
	 */
	public static Fuzzer getSQLFuzzer() {
		if (sqlFuzzer == null) {
			String[] values = {
				"' or '1'='1",
				"'--",
				"1'",
				"admin'--",
				"/*!10000%201/0%20*/",
				"/*!10000 1/0 */",
				"1/0",
				"'%20o/**/r%201/0%20--",
				"' o/**/r 1/0 --",
				";",
				"'%20and%201=2%20--",
				"' and 1=2 --",
				"test’%20UNION%20select%201,%20@@version,%201,%201;–",
				"test’ UNION select 1, @@version, 1, 1;–"
			};
			sqlFuzzer = new Fuzzer (values);
		}
		sqlFuzzer.resetFuzzer();
		return sqlFuzzer;
	}
	
	/**
	 * Checks if there are any more values that could be used for fuzzing
	 * @return
	 */
	public boolean hasNext() {
		return currentIndex < config.getValueList().size();
	}
	
	/**
	 * Add another fuzzing value
	 * @param value
	 */
	public void addValue(String value) {
		config.getValueList().add(value);
	}
	
	/**
	 * Gets the current list of values used in fuzzing
	 * @return
	 */
	public List<String> getValues() {
		return config.getValueList();
	}
	
	/**
	 * Removes one of the values
	 * @param value
	 */
	public void removeValue(String value) {
		config.getValueList().remove(value);
	}
}
