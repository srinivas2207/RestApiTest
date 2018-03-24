package com.rest.test.framework;

import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.rest.test.framework.ApiTestInfo.ApiCallInfo;
import com.rest.test.framework.ApiTestInfo.VariableInfo;
import com.rest.test.framework.network.RestNetworkUtil;
import com.rest.test.framework.network.RestNetworkUtil.RestCallResponse;
import com.rest.test.framework.unit.RestJsonUnitTest;
import com.rest.test.framework.unit.RestStringUnitTest;
import com.rest.test.framework.unit.RestXmlUnitTest;
import com.rest.test.framework.util.ApiTestConstants;
import com.rest.test.framework.util.DataUtil.VARIABLE_VALUE_TYPE;
import com.rest.test.framework.util.ApiTestPropertyReader;
import com.rest.test.framework.util.DataUtil;
import com.rest.test.framework.util.PerformanceTracker;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * This is the base class used for creating test classes.
 * <br><br>
 * 
 * Create a sub class and pass test info by adding below method <br><br>
 * <pre>	@Parameters(name = "{0}")
	public static Collection<Object[]> data() throws Exception{
		setPropertyFile("../../test_file.properties");
		return RestApiBaseTest.data();
	}
 * </pre>
 * 
 * 
 * <br> 
 * Override {@code setUp} and {@code tearDown} to track test class's life cycle
 * <br><br>
 * Override {@code handleApiRequest} and {@code handleApiReponse} to track API Calls's life cycle
 * 
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
	private RestApiBaseTestSuite testSuite = null;
	
	private boolean isPerformanceTrackOn = false;
	private PerformanceTracker performanceTracker = null;
	
	/**
	 * Default constructor used by {@link Parameterized} to pass list of {@link ApiCallInfo} as individual tests
	 * @param apiName Name of the API Call
	 * @param apiCallInfo Api Call Info
	 */
	public RestApiBaseTest(String apiName, ApiCallInfo apiCallInfo) {
		this.apiCallInfo = apiCallInfo;
		init();
	}
	
	/**
	 * Initializing API Call tests
	 */
	private void init() {
		this.testSuite = RestApiBaseTestSuite.getTestSuite(apiCallInfo);
		
		ApiTestInfo testInfo = apiCallInfo.getApiTestInfo();
		if (testInfo.getRunTimeTestInfo().getPerformanceTracker() != null) {
			isPerformanceTrackOn = true;
			performanceTracker = testInfo.getRunTimeTestInfo().getPerformanceTracker();
		}
		initializeRandomValues();
	}
	
	/**
	 * Setting up test class
	 */
	public void setUp() {
		testSuite.setUpClass(apiCallInfo.getApiTestInfo());
	}
	
	/**
	 * Tearing down test class
	 */
	public void tearDown() {
		testSuite.tearDownClass(apiCallInfo.getApiTestInfo());
		if (performanceTracker != null) {
			performanceTracker.downloadReport();
		}
	}
	
	/**
	 * Converts and returns {@link ApiTestInfo} into JUNIT tests
	 * @return List of Api Calls as tests
	 * @throws Exception
	 */
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
	
	/**
	 * JUNIT test method used to test each API call
	 */
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
			String assertionMessage =  "Exceptions while testing request ";
			assertionMessage += apiCallInfo.getName() + " at line number "
					+ apiCallInfo.getLineNumberInPropertyFile() + " of "
					+ apiCallInfo.getApiTestInfo().getPropertyFilePath();

			assertionMessage += "\n\nException Details : \n";
			
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
	
	/**
	 * Initializing random and system time values can be used in TEST_VARS or SUITE_VARS by using
	 * RANDOM_VALUE or SYSTEM_TIME
	 */
	private void initializeRandomValues() {
		
		// Generating a random number with the length 4
		Random rnd = new Random();
		randomNumber = 1000 + rnd.nextInt(9000);
		
		systemTime = System.currentTimeMillis();	
	}
	
	/**
	 * Resolving API Call dependencies <br>
	 * <li>Fetching constant variables</li>
	 * <li>Replacing variables in URL and Request Body</li>
	 */
	private void resolveApiCallInfo() {
		initializeConstantVariables();
		
		// Resolving dynamic fields in request URL
		String url = apiCallInfo.getUrl();
		List<String> varsUsed = fetchVariableNamesUsed(url);
		url = resolveDynamicVariablesUsed(url, varsUsed, API_TEST_INFO_TYPE.URL.toString());
			
		// Resolving dynamic fields in request body
		String requestBody = apiCallInfo.getRequest();
		varsUsed = fetchVariableNamesUsed(requestBody);
		requestBody = resolveDynamicVariablesUsed(requestBody, varsUsed, API_TEST_INFO_TYPE.REQUEST.toString());	
		
		url = url.replace(ApiTestConstants.PROPERTY_VARIABLE_RANDOM, randomNumber + "");
		url = url.replace(ApiTestConstants.PROPERTY_VARIABLE_SYS_TIME, systemTime + "");

		if (requestBody != null) {
			requestBody = requestBody.replace(ApiTestConstants.PROPERTY_VARIABLE_RANDOM, randomNumber + "");
			requestBody = requestBody.replace(ApiTestConstants.PROPERTY_VARIABLE_SYS_TIME, systemTime + "");
		}
		
		apiCallInfo.setUrl(url);
		apiCallInfo.setRequest(requestBody);
	}
	
	
	/**
	 * Returns list of variable names used in passed String.
	 * <b>{variable_name}</b> format is used to find variables used.
	 * @param source
	 * @return
	 */
	private List<String> fetchVariableNamesUsed(String source) {
		if (source == null || source.trim().length() == 0) { return null; }
		
		List<String> varsList = new ArrayList<String>();
		int index = 0;
		while (index != -1 && index < source.length()) {
			int startIndex = source.indexOf("{", index);
			int endIndex = -1;
			if (startIndex != -1) {
				endIndex = source.indexOf("}", startIndex);
				if (endIndex != -1) {
					String var = source.substring(startIndex + 1, endIndex);
					if (!var.contains(":")) {
						varsList.add(var);
					}
				}
			}
			
			index = (startIndex == -1 ? startIndex : startIndex + 1);
		}
		return varsList;
	}
	
	/**
	 * Replacing all the dynamic variables with the variable values from test and suite variables
	 * @param source Source string to be resolved
	 * @param usedVarsList dynamic variable list
	 * @param type Type of source
	 * @return Resolved data
	 */
	private String resolveDynamicVariablesUsed(String source, List<String> usedVarsList, String type) {
		if (usedVarsList == null || usedVarsList.size() == 0) {
			return source;
		}
		
		if (source == null || source.trim().length() == 0) {
			return source;
		}
		
		for(String dynField : usedVarsList) {
			String variableVal = null;
			{
				// Fetching variable value from global map
				variableVal = testSuite.getVariableValue(dynField);
				
				if (variableVal == null) {
					String assertionMessage =  "Unable find the dynamic field : " + "{" + dynField + "}"  + " used in " + type + "\n";
					assertionMessage += "Resolve this issue by intializing the field in TEST_VARS\n";
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
	

	/**
	 * Converting API Call info into HTTP request<br>
	 * This method is called before sending API Call to server <br>
	 * It includes request initialization and resolving API Call Info.
	 * @throws Exception
	 */
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
	 * Handling API Call info after getting response from server.<br>
	 * <li>Checking the HTTP status</li>
	 * <li>Fetching variable information</li>
	 * <li>Evaluating test</li>
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
		
		initializeDynamicVariables();
		logMessage();
		evaluateTest();
	}
	
	
	/**
	 * Evaluating test
	 * <li>Evaluating test condition</li>
	 * <li>Comparing responses</li>
	 */
	private void evaluateTest()
	{
		evaluateExpectedExpression();

		if (!apiCallInfo.isCompareResponse()) {
			return;
		}
		
		String expectedResponse = apiCallInfo.getResponse();
		String currentResponse = apiCallInfo.getRestCallResponse().getResponse();
		
		// skipping comparing responses, if expected and actual responses are empty
		if ((currentResponse == null || currentResponse.trim().length() == 0)
				&& (expectedResponse == null || expectedResponse.trim().length() == 0)) {
			return;
		}
		
		if (DataUtil.isValidJson(currentResponse)) {
			RestJsonUnitTest restJsonUnitTest = new RestJsonUnitTest(expectedResponse, currentResponse, false);
			restJsonUnitTest.execute();
		} else if (DataUtil.isXMLData(currentResponse)) {
			RestXmlUnitTest restXmlUnitTest = new RestXmlUnitTest(expectedResponse, currentResponse);
			restXmlUnitTest.execute();
		} else {
			expectedResponse = expectedResponse == null ? "" : expectedResponse.trim();
			currentResponse = currentResponse == null ? "" : currentResponse.trim();
			String assertionMessage = "Expected and current results are not matching :";
			if (!new RestStringUnitTest(expectedResponse, currentResponse).compare()) {	
				throw new AssertionError(assertionMessage);
			}
		}
	}
	
	/**
	 * Evaluating test condition
	 */
	private void evaluateExpectedExpression() {
		String testCondition = apiCallInfo.getTestCondition();
		if (testCondition == null || testCondition.trim().length() == 0) return;
		
		List<String> usedVarsList = fetchVariableNamesUsed(testCondition);
		if (usedVarsList != null && usedVarsList.size() > 0) {
			for(String variableName : usedVarsList) {
				String variableValue = testSuite.getVariableValue(variableName);
				
				if (variableValue == null) {
					String assertionMessage =  "Unable find the dynamic field : " + "{" + variableValue + "}"  + " used in Expected Expression:\n";
					assertionMessage += "Resolve this issue by intializing the field in TEST_VARS\n";
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
	
	/**
	 * Fetching constant variables from test info
	 */
	private void initializeConstantVariables()
	{
		List<VariableInfo> variableList;
		
		variableList = apiCallInfo.getVariableList();
		
		if (variableList != null && variableList.size() > 0) {
			for (VariableInfo varInfo : variableList) {
				String variableName = varInfo.getVariableName();
				String variableValue = varInfo.getVariableValue();

				if (DataUtil.getVariableValueType(variableValue) == VARIABLE_VALUE_TYPE.CONSTANT) {
					variableValue = variableValue.replace(ApiTestConstants.PROPERTY_VARIABLE_RANDOM, randomNumber
							+ "");
					variableValue = variableValue.replace(ApiTestConstants.PROPERTY_VARIABLE_SYS_TIME, systemTime
							+ "");

					// Storing variable values in global variable map
					testSuite.setVariableValue(variableName, variableValue);
				}
			}
		}
	}
	
	/**
	 * Fetching dynamic variables from the response
	 */
	private void initializeDynamicVariables() {
		List<VariableInfo> variableList;
		variableList = apiCallInfo.getVariableList();
		
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
					// Storing variable values in global variable map
					testSuite.setVariableValue(variableName, parsedVariableValue);
				}
			}
		}
	}
	
	private static final long DEFAULT_POLL_INTERVAL = 5 * 1000;
	private static final long DEFAULT_POLL_DURATION = 300 * 1000;
	
	/**
	 * Polling request
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
	 * Passing test file path
	 * @param filePath Path of the property file
	 */
	public synchronized static void setPropertyFile(String filePath) {
		PROPERTY_FILE_PATH = filePath;
	}
	
	
	/**
	 * Returns detailed assertion message
	 * @return Assertion message
	 */
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
	
	/**
	 * Printing the message of the API Test, provided using property LOG_MSG
	 */
	private void logMessage() {
		if (apiCallInfo.getLogMessage() != null && apiCallInfo.getLogMessage().trim().length() > 0) {
			String logMessage = apiCallInfo.getLogMessage().trim();
			
			String logMessagePrefix = "Log[" + apiCallInfo.getApiTestInfo().getTestName() + "." + apiCallInfo.getName()+ "]";
			logMessagePrefix += ":\n";
			
			//Printing test details, If the log message is marked to print test details
			if (logMessage.equalsIgnoreCase(ApiTestConstants.PROPERTY_TEST_DETAILS)) {
				logMessage = getRequestAssertionMessage();
				System.out.println(logMessagePrefix + logMessage);
				return;
			}
			
			List<String> varsUsed = fetchVariableNamesUsed(logMessage);
			String resLogMessage = logMessage;	
			try {
				// replacing variables with the values
				resLogMessage = resolveDynamicVariablesUsed(logMessage, varsUsed, "LOG");
			}catch(Exception  | AssertionError e){
			}	
			System.out.println(logMessagePrefix + resLogMessage);
		}
	}
	
	/**
	 * Enum to define data type
	 * @author SrinivasDonapati
	 */
	private enum API_TEST_INFO_TYPE {
		URL, REQUEST, RESPONSE
	}
	
	/**
	 * Returns API test info.<br>
	 * {@link ApiTestInfo} is created after test initialization.
	 * @return {@link ApiTestInfo}
	 */
	public static ApiTestInfo getApiTestInfo() {
		return apiTestInfo;
	}
	
	/**
	 * Returns running test suite instance
	 * @return Instance of {@link RestApiBaseTestSuite}
	 */
	public RestApiBaseTestSuite getTestSuite() {
		return testSuite;
	}
	
	/**
	 * Returns currently Rest util
	 * @return Instance of {@link RestNetworkUtil}
	 */
	public RestNetworkUtil getRestUtil() {
		return testSuite.getRestUtil();
	}
}
 