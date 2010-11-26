package com.eviware.soapui.security.fuzzer;

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
				"admin'--"

			};
			sqlFuzzer = new Fuzzer (values);
		}
		sqlFuzzer.resetFuzzer();
		return sqlFuzzer;
	}
	
	public boolean hasNext() {
		return currentIndex < config.getValueList().size();
	}
	
	
	
}
