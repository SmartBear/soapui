package com.eviware.soapui.security.fuzzer;

import java.util.List;

import com.eviware.soapui.config.FuzzerConfig;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStepInterface;
import com.eviware.soapui.model.testsuite.TestStep;

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
			HttpTestRequestInterface<?> request = (( HttpTestRequestStepInterface )testStep).getTestRequest();
			request.setPropertyValue(param, config.getValueArray(currentIndex));
			currentIndex++;
		}
	}
	
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
	
	public boolean hasNext() {
		return currentIndex < config.getValueList().size();
	}
	
	public void addValue(String value) {
		config.getValueList().add(value);
	}
	
	public List<String> getValues() {
		return config.getValueList();
	}
	
	public void removeValue(String value) {
		config.getValueList().remove(value);
	}
}
