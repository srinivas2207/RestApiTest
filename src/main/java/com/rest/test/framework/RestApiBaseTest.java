package com.rest.test.framework;

import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.rest.test.framework.ApiTestInfo.ApiCallInfo;
import com.rest.test.framework.ApiTestInfo.VariableInfo;
import com.rest.test.framework.network.RestNetworkUtil;
import com.rest.test.framework.network.RestNetworkUtil.RestCallResponse;
import com.rest.test.framework.util.ApiTestConstants;
import com.rest.test.framework.util.ApiTestConstants.TEST_TYPE;
import com.rest.test.framework.util.DataUtil.VARIABLE_VALUE_TYPE;
import com.rest.test.framework.util.ApiTestPropertyReader;
import com.rest.test.framework.util.DataUtil;
import com.rest.test.framework.util.PerformanceTracker;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * This is base class for API tests.
 * By extending this class, child test classes will get access to 
 * overriding testing functionality
 * @author SrinivasDonapati
 *
 */
@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public abstract class RestApiBaseTest {
	private static ApiTestInfo apiTestInfo = null;
	
	private static String PROPERTY_FILE_PATH =  null;
	public ApiCallInfo apiCallInfo;
	public String apiName;
	public int randomNumber;
	public long systemTime;
	public Map<String, String> testVariableMap;
	private RestApiBaseTestSuite testSuite = null;
	
	private boolean isPerformanceTrackOn = false;
	private PerformanceTracker performanceTracker = null;
	
	public RestApiBaseTest(String apiName, ApiCallInfo apiCallInfo) {
		this.apiCallInfo = apiCallInfo;
		init();
	}
	
	private void init() {
		this.testSuite = RestApiBaseTestSuite.getTestSuite(apiCallInfo);
		
		ApiTestInfo testInfo = apiCallInfo.getApiTestInfo();
		if (testInfo.getRunTimeTestInfo().getPerformanceTracker() != null) {
			isPerformanceTrackOn = true;
			performanceTracker = testInfo.getRunTimeTestInfo().getPerformanceTracker();
		}
		
		testVariableMap = new HashMap<String, String>();
		initializeRandomValues();
	}
	
	public void setUp() {
		testSuite.setUpClass(apiCallInfo.getApiTestInfo());
	}
	
	public void tearDown() {
		testSuite.tearDownClass(apiCallInfo.getApiTestInfo());
		if (performanceTracker != null) {
			performanceTracker.downloadReport();
		}
	}
	
	@Parameters(name = "{0}")
	public synchronized static Collection<Object[]> data() throws Exception{
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTY_FILE_PATH);
		ApiTestPropertyReader props = new ApiTestPropertyReader();
		props.load(in);
		
		List<Object[]> data = new ArrayList<Object[]>();
		
		apiTestInfo = props.getApiTestInfo();
		apiTestInfo.setPropertyFilePath(PROPERTY_FILE_PATH);
		String testName = apiTestInfo.getTestName();
			
		List<ApiCallInfo> apiCallList = apiTestInfo.getApiReqList();
		if (apiCallList != null && apiCallList.size() > 0) {
			apiCallList.get(0).setFirstTest(true);
			apiCallList.get(apiCallList.size() - 1).setLastTest(true);
		}
	
		for(ApiCallInfo reqInfo : apiCallList) {
			String apiName =  reqInfo.getName();
			if (apiName == null || apiName.trim().length() == 0) {
				break;
			}
			apiName = testName + "." + apiName;
			Object[] reqObj = {apiName, reqInfo};
			data.add(reqObj);
		}
		
		return data;
	}
	
	@Test
	public void testApi() {
		try {
			if (apiCallInfo != null && apiCallInfo.isFirstTest()) {
				setUp();
			}
			
			if (apiCallInfo.getWaitTime() > 0) {
				try {
					Thread.sleep(apiCallInfo.getWaitTime() * 1000);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			handleApiRequest();
			handleApiResponse();
			
			// Updating API performance info
			if (isPerformanceTrackOn) {
				performanceTracker.setRequestStatus(apiCallInfo.getId(), true);
			}
		} catch(Exception  | AssertionError e) {
			String assertionMessage =  "Exceptions while testing request:\n";
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String stackTrace = errors.toString();
			
			assertionMessage += stackTrace;
			
			assertionMessage += "\n" + getRequestAssertionMessage();
			assertTrue(assertionMessage, false);
		} finally {
			if (apiCallInfo != null && apiCallInfo.isLastTest()) {
				tearDown();
			}
		}
	}
	
	private void initializeRandomValues() {
	    randomNumber = new Random().nextInt();
		if (randomNumber < 0) {
			randomNumber *= -1;
		}
		
		systemTime = System.currentTimeMillis();	
	}
	
	public void resolveApiCallInfo() {
		initializeStaticVariables(false);
		initializeStaticVariables(true);
		
		// Resolving dynamic fields in request URL
		String url = apiCallInfo.getUrl();
		List<String> dynUrlFields = fetchDynamicFields(url);
		url = resolveDynamicFields(url, dynUrlFields, API_TEST_INFO_TYPE.URL);
			
		// Resolving dynamic fields in request body
		String requestBody = apiCallInfo.getRequest();
		dynUrlFields = fetchDynamicFields(requestBody);
		requestBody = resolveDynamicFields(requestBody, dynUrlFields, API_TEST_INFO_TYPE.REQUEST);	
		
		url = url.replace(ApiTestConstants.PROPERTY_VARIABLE_RANDOM, randomNumber + "");
		url = url.replace(ApiTestConstants.PROPERTY_VARIABLE_SYS_TIME, systemTime + "");

		if (requestBody != null) {
			requestBody = requestBody.replace(ApiTestConstants.PROPERTY_VARIABLE_RANDOM, randomNumber + "");
			requestBody = requestBody.replace(ApiTestConstants.PROPERTY_VARIABLE_SYS_TIME, systemTime + "");
		}
		
		apiCallInfo.setUrl(url);
		apiCallInfo.setRequest(requestBody);
	}
	
	
	private List<String> fetchDynamicFields(String source) {
		if (source == null || source.trim().length() == 0) { return null; }
		
		List<String> dynUrlFields = new ArrayList<String>();
		int index = 0;
		while (index != -1 && index < source.length()) {
			int startIndex = source.indexOf("{", index);
			int endIndex = -1;
			if (startIndex != -1) {
				endIndex = source.indexOf("}", startIndex);
				if (endIndex != -1) {
					String dynField = source.substring(startIndex + 1, endIndex);
					if (!dynField.contains(":")) {
						dynUrlFields.add(dynField);
					}
				}
			}
			
			index = (startIndex == -1 ? startIndex : startIndex + 1);
		}
		return dynUrlFields;
	}
	

	
	
	private String resolveDynamicFields(String source, List<String> dynUrlFields, API_TEST_INFO_TYPE type) {
		if (dynUrlFields == null || dynUrlFields.size() == 0) {
			return source;
		}
		
		if (source == null || source.trim().length() == 0) {
			return source;
		}
		
		for(String dynField : dynUrlFields) {
			String variableVal = null;
			{
				// Fetching variable value from local map
				variableVal = testVariableMap.get(dynField);
				
				// Fetching variable value from global map
				if (variableVal == null) {
					variableVal = testSuite.getVariableValue(dynField);
				}
				
				if (variableVal == null) {
					String assertionMessage =  "Unable find the dynamic field : " + "{" + dynField + "}"  + " used in " + type.toString() + "\n";
					assertionMessage += "Resolve this issue by intializing the field in T_VARIABLES or G_VARIABLES\n";
					assertionMessage += "1. [ .., " + dynField + "=<some constant value>" + "] or \n" ;
					assertionMessage += "2. [ .., " + dynField + "=<JSON PATH>" + "]\n\n" ;
					throw new AssertionError(assertionMessage);
				} 
			}
			// Replacing all the occurrences of the variable inside url
			source = source.replaceAll("\\{" + dynField + "\\}", variableVal);
		}
		
		return source;
	}
	

	public void handleApiRequest() throws Exception {
		resolveApiCallInfo();
		
		// Tracking response time for each call
		long preRequestTime = System.currentTimeMillis();
		if (isPerformanceTrackOn) {
			performanceTracker.setApiInfo(apiCallInfo);
		}

		if (apiCallInfo.isPoll()) {
			pollRequest();
		} else {
			testSuite.getRestUtil().sendRequest(apiCallInfo);
		}
		
		long postRequestTime = System.currentTimeMillis();
		// Updating API request time, if performance tracker is on
		if (isPerformanceTrackOn) {
			performanceTracker.setRequestTime(apiCallInfo.getId(), postRequestTime - preRequestTime);
		}
	}
	
	/**
	 * Handling API request after getting response from server
	 * @param response
	 */
	public void handleApiResponse() {
		RestCallResponse restCallResponse = apiCallInfo.getRestCallResponse();
		if (restCallResponse == null ) {
			String assertionMessage = "UnSuccessfull request !\n";
			throw new AssertionError(assertionMessage);
		}
		
		if (apiCallInfo.getReqStatus() != restCallResponse.getStatus()) {
			String assertionMessage = "Expected Request status " + apiCallInfo.getReqStatus()
					+ ", But current request status is " + restCallResponse.getStatus();
			throw new AssertionError(assertionMessage);
		}
		
		initializeDynamicVariables(false);
		initializeDynamicVariables(true);
		
		evaluateTest();
	}
	
	private void evaluateTest()
	{
		evaluateExpectedExpression();

		String testType = apiCallInfo.getTestType();
		if (testType == null) { return; }

		String expectedResponse = apiCallInfo.getResponse();
		String currentResponse = apiCallInfo.getRestCallResponse().getResponse();

		if (testType.equals(TEST_TYPE.XML_UNIT.toString())) {
			RestXmlUnitTest restXmlUnitTest = new RestXmlUnitTest(expectedResponse, currentResponse);
			restXmlUnitTest.execute();
		}
		else if (testType.equals(TEST_TYPE.JSON_UNIT.toString())) {
			RestJsonUnitTest restJsonUnitTest = new RestJsonUnitTest(expectedResponse, currentResponse, false);
			restJsonUnitTest.execute();
		}
		else if (testType.equals(TEST_TYPE.STRING_UNIT.toString())) {
			expectedResponse = expectedResponse == null ? "" : expectedResponse.trim();
			currentResponse = currentResponse == null ? "" : currentResponse.trim();

			if (!expectedResponse.equals(currentResponse)) {
				String assertionMessage = "Expected and current results are not matching :";
				throw new AssertionError(assertionMessage);
			}
		}
	}
	
	private void evaluateExpectedExpression() {
		String testCondition = apiCallInfo.getTestCondition();
		if (testCondition == null || testCondition.trim().length() == 0) return;
		
		List<String> dynFields = fetchDynamicFields(testCondition);
		if (dynFields != null && dynFields.size() > 0) {
			for(String variableName : dynFields) {
				String variableValue = testVariableMap.get(variableName);
				if (variableValue == null) {
					variableValue = testSuite.getVariableValue(variableName);
				}
				
				if (variableValue == null) {
					String assertionMessage =  "Unable find the dynamic field : " + "{" + variableValue + "}"  + " used in Expected Expression:\n";
					assertionMessage += "Resolve this issue by intializing the field in T_VARIABLES or G_VARIABLES\n";
					assertionMessage += "1. [ .., " + variableName + "=<some constant value>" + "] or \n" ;
					assertionMessage += "2. [ .., " + variableName + "=<JSON PATH>" + "]\n\n" ;
					
					throw new AssertionError(assertionMessage);
				}
				testCondition = testCondition.replaceAll("\\{" + variableName + "\\}", variableValue);
			}
		}
		
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		try {
			boolean result = Boolean.parseBoolean(engine.eval(testCondition).toString());
			
			if (!result) {
				String assertionMessage =  "Evaluation result of expected expression is False for : " + testCondition; 
				throw new AssertionError(assertionMessage);
			}
		}
		catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			String assertionMessage =  "Errors while evaluating expression : " + testCondition; 
			assertionMessage += e.getMessage() + "\n";
			throw new AssertionError(assertionMessage);
		}
		
	}
	
	private void initializeStaticVariables(boolean isGlobal)
	{
		List<VariableInfo> variableList;
		if (isGlobal) {
			variableList = apiCallInfo.getVariableList();
		} else {
			variableList = apiCallInfo.getTestVariableList();
		}
		
		if (variableList != null && variableList.size() > 0) {
			for (VariableInfo varInfo : variableList) {
				String variableName = varInfo.getVariableName();
				String variableValue = varInfo.getVariableValue();

				if (DataUtil.getVariableValueType(variableValue) == VARIABLE_VALUE_TYPE.CONSTANT) {
					variableValue = variableValue.replace(ApiTestConstants.PROPERTY_VARIABLE_RANDOM, randomNumber
							+ "");
					variableValue = variableValue.replace(ApiTestConstants.PROPERTY_VARIABLE_SYS_TIME, systemTime
							+ "");

					// Storing variable values in global/test map
					if (isGlobal) {
						testSuite.setVariableValue(variableName, variableValue);
					} else {
						testVariableMap.put(variableName, variableValue);
					}
				}
			}
		}
	}
	
	private void initializeDynamicVariables(boolean isGlobal) {
		List<VariableInfo> variableList;
		if (isGlobal) {
			variableList = apiCallInfo.getVariableList();
		} else {
			variableList = apiCallInfo.getTestVariableList();
		}
		
		if (variableList != null && variableList.size() > 0) {
			for (VariableInfo varInfo : variableList) {
				String variableName = varInfo.getVariableName();
				String variableValue = varInfo.getVariableValue();
				
				String parsedVariableValue = null;

				VARIABLE_VALUE_TYPE variableType = DataUtil.getVariableValueType(variableValue);
				
				if (variableType == VARIABLE_VALUE_TYPE.JSON_PATH) {
					Object responseValue = DataUtil.getJsonPathValue(variableValue, apiCallInfo.getRestCallResponse()
							.getResponse());
					parsedVariableValue = responseValue + "";

				}
				else if (variableType == VARIABLE_VALUE_TYPE.XML_PATH) {
					Object responseValue = DataUtil.getXpathValue(variableValue, apiCallInfo.getRestCallResponse()
					                     							.getResponse());
				    parsedVariableValue = responseValue + "";
				}
				
				if (parsedVariableValue != null) {
					// Storing variable values in global/test map
					if (isGlobal) {
						testSuite.setVariableValue(variableName, parsedVariableValue);
					}
					else {
						testVariableMap.put(variableName, parsedVariableValue);
					}
				}
			}
		}
	}
	
	private static final long DEFAULT_POLL_INTERVAL = 5 * 1000;
	private static final long DEFAULT_POLL_DURATION = 300 * 1000;
	
	/**
	 * Polling request, with default interval and duration time
	 * @return Response from the server
	 * @throws Exception
	 */
	public void pollRequest() throws Exception {
		long pollTime = apiCallInfo.getPollTime() > 0 ? apiCallInfo.getPollTime() * 1000 : DEFAULT_POLL_DURATION;
		long pollInterval = apiCallInfo.getPollInterval() > 0 ? apiCallInfo.getPollInterval() * 1000 : DEFAULT_POLL_INTERVAL;
		pollRequest(pollTime, pollInterval);
	}

	/**
	 * Polling request, to check the status of the triggered operation on server
	 * @param pollDuration Time in milliseconds
	 * @param pollInterval Time in milliseconds
	 * @return Response from the server
	 * @throws Exception
	 */
	public void pollRequest(long pollDuration, long pollInterval) throws Exception {
				
		AssertionError assertionError = null;
		while(pollDuration > 0) {
			try {
				testSuite.getRestUtil().sendRequest(apiCallInfo);
				handleApiResponse();
				return;
			} catch(AssertionError AE) {
				assertionError = AE;
			}
			pollDuration -= pollInterval;
			Thread.sleep(pollInterval);
		}
		
		if (assertionError != null) {
			String assertionMessage =  "This poll request has been unsuccessfull !\n";
			assertionMessage += assertionError.getMessage();
			throw new AssertionError(assertionMessage);
		}
	}
	
	/**
	 * Setting the property file path
	 * @param fileName
	 */
	public synchronized static void setPropertyFile(String filePath) {
		PROPERTY_FILE_PATH = filePath;
	}
	
	
	public String getRequestAssertionMessage() {
		String assertionMessage =	"\nRequest Details:\n";
		assertionMessage		+=	"----------------\n";
		
		assertionMessage		+=	"*Base URL		: " + getTestSuite().getRestUtil().getBaseUrl() + "\n";
		assertionMessage		+=	"*Request URL	: " + apiCallInfo.getMethod() + ":" + apiCallInfo.getUrl() +"\n";
		if (apiCallInfo.getRequest() != null) {
			assertionMessage	+=	"*Request Body 	: " + apiCallInfo.getRequest() + "\n\n";
		}
		
		assertionMessage		+=	"*Expected Status: " + apiCallInfo.getReqStatus() + "\n";
		if (apiCallInfo.getResponse() != null) {
			assertionMessage	+=	"*Sample Response: " + apiCallInfo.getResponse() + "\n\n";
		}
		
		if (apiCallInfo.getTestCondition()!= null ) {
			assertionMessage	+=	"*Test Condition : " + apiCallInfo.getTestCondition() + "\n";
		}
		
		if (apiCallInfo.getTestVariableList()!= null && apiCallInfo.getTestVariableList().size() > 0 ) {
			assertionMessage	+=	"*Test Variable Declarions :\n";
			int i = 1;
			for (VariableInfo variableInfo : apiCallInfo.getTestVariableList()) {
				String variableName = variableInfo.getVariableName();
				String variableValue = variableInfo.getVariableValue();
				assertionMessage += (i ++) + ". " + variableName + "=" + variableValue + "\n";
			}
		}
		
		if (apiCallInfo.getVariableList()!= null && apiCallInfo.getVariableList().size() > 0 ) {
			assertionMessage	+=	"*Variable Declarions :\n";
			int i = 1;
			for (VariableInfo variableInfo : apiCallInfo.getVariableList()) {
				String variableName = variableInfo.getVariableName();
				String variableValue = variableInfo.getVariableValue();
				assertionMessage += (i ++) + ". " + variableName + "=" + variableValue + "\n";
			}
		}
		
		if (apiCallInfo.getRestCallResponse() != null) {
			assertionMessage	+=	"*Current Status		: " + apiCallInfo.getRestCallResponse().getStatus() + "\n";
			assertionMessage	+=	"*Current Response	: " + apiCallInfo.getRestCallResponse().getResponse() + "\n";
		}
	
		return assertionMessage;
	}
	
	private enum API_TEST_INFO_TYPE {
		URL, REQUEST, RESPONSE
	}
	
	public static ApiTestInfo getApiTestInfo() {
		return apiTestInfo;
	}
	
	public static void main(String[] args) {
		 ScriptEngineManager mgr = new ScriptEngineManager();
		 ScriptEngine engine = mgr.getEngineByName("JavaScript");
		 String foo = "(30==30 && \"tes\" ==\"test\")";
		 try {
			 boolean result = Boolean.parseBoolean(engine.eval(foo).toString());
			System.out.println(result);
		}
		catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RestApiBaseTestSuite getTestSuite() {
		return testSuite;
	}
	
	public RestNetworkUtil getRestUtil() {
		return testSuite.getRestUtil();
	}
}
 