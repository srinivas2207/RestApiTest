package com.rest.test.framework;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.rest.test.framework.network.RestNetworkUtil.RestCallResponse;
import com.rest.test.framework.util.ApiTestConstants;
import com.rest.test.framework.util.PerformanceTracker;


/**
 * This class contains the parsed and runtime test information of a test file.<br>
 * @author SrinivasDonapati
 *
 */
public class ApiTestInfo {
	private RunTimeTestInfo runTimeTestInfo;
	private String testName = null;
	private String propertyFilePath = null;
	private List<ApiCallInfo> apiCallList = new ArrayList<>();
	private long apiRequestId= System.currentTimeMillis();
	
	/**
	 * Returns test class's runtime test info
	 * @return {@link RunTimeTestInfo}
	 */
	public RunTimeTestInfo getRunTimeTestInfo()
	{
		return runTimeTestInfo;
	}

	public void setRunTimeTestInfo(RunTimeTestInfo runTimeTestInfo)
	{
		this.runTimeTestInfo = runTimeTestInfo;
	}
	
	/**
	 * Returning test class's name.
	 * @return Name of the test class
	 */
	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	/**
	 * Returns list of Api Calls
	 * @return List of {@link ApiCallInfo}
	 */
	public List<ApiCallInfo> getApiReqList() {
		return apiCallList;
	}
	
	/**
	 * Creating ApiCallInfo Object using API Call Name
	 * @param apiName Name of the API Call
	 * @param lineNumber Line Number inside property file
	 * @return {@link ApiCallInfo} Object
	 */
	public ApiCallInfo createApiCallInfo(String apiName, int lineNumber) {
		ApiCallInfo reqInfo = new ApiCallInfo();
		reqInfo.setName(apiName);
		reqInfo.setId(apiRequestId++);
		reqInfo.setLineNumberInPropertyFile(lineNumber);
		apiCallList.add(reqInfo);
		return reqInfo;
	}
	
	/**
	 * Returns file path of the test file
	 * @return
	 */
	public String getPropertyFilePath()
	{
		return propertyFilePath;
	}

	public void setPropertyFilePath(String propertyFilePath)
	{
		this.propertyFilePath = propertyFilePath;
	}

	/**
	 * This class holds the individual API call info
	 * @author SrinivasDonapati
	 *
	 */
	public class ApiCallInfo {
		private long id;
		private String name = null;
		private String url = null;
		private String method = null;
		private int reqStatus;
		private String request = null;
		private String response = null;
		private List<VariableInfo> variableList = new ArrayList<VariableInfo>();
		private Map<String, String> headers;
		private String testCondition;
		private boolean poll = false;
		private boolean compareResponse = false;
		private RestCallResponse restCallResponse;
		private boolean isFirstTest;
		private boolean isLastTest;
		private int lineNumberInPropertyFile;
		private String logMessage;

		private int pollTime;
		private int pollInterval;
		private int waitTime;
		
		/**
		 * Returns the Test Class instance of the API call
		 * @return {@link ApiTestInfo}
		 */
		public ApiTestInfo getApiTestInfo() {
			return ApiTestInfo.this;
		}
		
		/**
		 * Checks whether the call is first of the Test class
		 * @return Position of the API call
		 */
		public boolean isFirstTest()
		{
			return isFirstTest;
		}
		
		public void setFirstTest(boolean isFirstTest)
		{
			this.isFirstTest = isFirstTest;
		}
		
		/**
		 * Checks whether the call is last of the Test class
		 * @return Position of the API call
		 */
		public boolean isLastTest()
		{
			return isLastTest;
		}
		
		public void setLastTest(boolean isLastTest)
		{
			this.isLastTest = isLastTest;
		}
		
		/**
		 * Returns the HTTP Response information of the API call
		 * @return Object of {@link RestCallResponse }
		 */
		public RestCallResponse getRestCallResponse()
		{
			return restCallResponse;
		}
		
		/**
		 * Adding HTTP Response information to the API Call
		 * @param restCallResponse	HTTP Response info {@link RestCallResponse}
		 */
		public void setRestCallResponse(RestCallResponse restCallResponse)
		{
			this.restCallResponse = restCallResponse;
		}
		
		/**
		 * Checks if the API call is pollable
		 * @return true if the API Call is pollable
		 */
		public boolean isPoll()
		{
			return poll;
		}
		
		/**
		 * Setting poll information of the API Call
		 * @param poll Poll status of the calls
		 */
		public void setPoll(boolean poll)
		{
			this.poll = poll;
		}
		
		/**
		 * Returns API Call id
		 * @return Api Call Id
		 */
		public long getId()
		{
			return id;
		}
		public void setId(long id)
		{
			this.id = id;
		}
		
		/**
		 * Getting the test's line number inside property file
		 * @return Line Number
		 */
		public int getLineNumberInPropertyFile()
		{
			return lineNumberInPropertyFile;
		}

		/**
		 * Setting the test's line number inside property file
		 */
		public void setLineNumberInPropertyFile(int lineNumberInPropertyFile)
		{
			this.lineNumberInPropertyFile = lineNumberInPropertyFile;
		}
		
		/**
		 * Returns name of the API Call
		 * @return API Call Name
		 */
		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
		
		/**
		 * Returns the URL of the API Call
		 * @return URL of the test
		 */
		public String getUrl()
		{
			return url;
		}
		
		public void setUrl(String url)
		{
			this.url = url;
		}
		
		/**
		 * Returns HTTP Method of the API Call
		 * @return HTTP Method (GET | POST | PUT | DELETE)
		 */
		public String getMethod()
		{
			return method;
		}
		public void setMethod(String method)
		{
			this.method = method;
		}
		
		/**
		 * Returns the API Call info's recorded/expected status
		 * @return HTTP Status
		 */
		public int getReqStatus()
		{
			return reqStatus;
		}
		
		public void setReqStatus(int reqStatus)
		{
			this.reqStatus = reqStatus;
		}
		
		/**
		 * Returns the request body of the API Call
		 * @return HTTP Request body (XML | JSON | PLAIN TEXT)
		 */
		public String getRequest()
		{
			return request;
		}
		
		public void setRequest(String request)
		{
			this.request = request;
		}
		
		/**
		 * Returns the recorded/expected response of the API Call
		 * @return HTTP Response
		 */
		public String getResponse()
		{
			return response;
		}
		public void setResponse(String response)
		{
			this.response = response;
		}
		
		/**
		 * Returns list of Suite variables declared
		 * @return List of {@link VariableInfo}
		 */
		public List<VariableInfo> getVariableList()
		{
			return variableList;
		}
		
		public void setVariableInfo(String variableInfo)
		{
			if (variableInfo != null && variableInfo.trim().length() > 0) {
				JSONArray varList = new JSONArray(variableInfo);
				if (varList != null && varList.length() > 0) {
					for (int i = 0; i < varList.length(); i++) {
						String str = varList.getString(i);
						String variableName = varList.getString(i).substring(0, str.indexOf("="));
						String variableValue = varList.getString(i).substring(str.indexOf("=") + 1, str.length());
						VariableInfo varInfo = new VariableInfo();
						varInfo.setVariableName(variableName);
						varInfo.setVariableValue(variableValue);
						variableList.add(varInfo);
					}
				}
			}
		}
	
		
		/**
		 * Returns API test condition passed from test file
		 * @return Test condition {@link ApiTestConstants.PROPERTY_TEST_CONDITION}
		 */
		public String getTestCondition()
		{
			return testCondition;
		}
		public void setTestCondition(String testCondition)
		{
			this.testCondition = testCondition;
		}
		
		/**
		 * Checks whether the comparing response mode is on
		 * @return
		 */
		public boolean isCompareResponse()
		{
			return this.compareResponse;
		}

		/**
		 * Setting the test's comparing response mode
		 * @param compareResponse
		 */
		public void setCompareResponse(boolean compareResponse)
		{
			this.compareResponse = compareResponse;
		}
		
		/**
		 * Returns the poll time of the API Call
		 * @return Poll time in seconds
		 */
		public int getPollTime()
		{
			return pollTime;
		}

		public void setPollTime(int pollTime)
		{
			this.pollTime = pollTime;
		}

		/**
		 * Returns poll interval of API Call
		 * @return Poll interval in seconds
		 */
		public int getPollInterval()
		{
			return pollInterval;
		}

		public void setPollInterval(int pollInterval)
		{
			this.pollInterval = pollInterval;
		}

		/**
		 * Returns waiting time before firing API Call
		 * @return Wait time in seconds
		 */
		public int getWaitTime()
		{
			return waitTime;
		}

		public void setWaitTime(int waitTime)
		{
			this.waitTime = waitTime;
		}

		/**
		 * Returns extra headers passed for API Call
		 * @return Headers
		 */
		public Map<String, String> getHeaders()
		{
			return headers;
		}

		public void setHeaders(String headerInfo)
		{
			if (headerInfo != null && headerInfo.trim().length() > 0) {
				JSONArray headerList = new JSONArray(headerInfo);
				if (headerList != null && headerList.length() > 0) {
					for (int i = 0; i < headerList.length(); i++) {
						String str = headerList.getString(i);
						String headerKey = headerList.getString(i).substring(0, str.indexOf("="));
						String headerValue = headerList.getString(i).substring(str.indexOf("=") + 1, str.length());
						
						if (headers == null) {
							headers = new HashMap<>();
						}
						headers.put(headerKey, headerValue);
					}
				}
			}
		}
		
		/**
		 * Getting the log message of the test
		 * @return
		 */
		public String getLogMessage()
		{
			return logMessage;
		}

		
		/**
		 * Setting the log message of the test
		 * @param logMessage
		 */
		public void setLogMessage(String logMessage)
		{
			this.logMessage = logMessage;
		}

	}
	
	/**
	 * This Class contains variable information passed in test file
	 * @author SrinivasDonapati
	 *
	 */
	public class VariableInfo {
		private String variableName;
		private String variableValue;
		
		/**
		 * Returns variable name
		 * @return Variable name
		 */
		public String getVariableName()
		{
			return variableName;
		}
		
		public void setVariableName(String variableName)
		{
			this.variableName = variableName;
		}
		
		/**
		 * Returns variable value
		 * @return Value of the variable (XPath | JsonPath | Constant)
		 */
		public String getVariableValue()
		{
			return variableValue;
		}
		public void setVariableValue(String variableValue)
		{
			this.variableValue = variableValue;
		}
	}
	
	/**
	 * This class holds the runtime test information, this is useful in identifying current testsuite, {@link PerformanceTracker} ..etc
	 * @author SrinivasDonapati
	 *
	 */
	public static class RunTimeTestInfo {
		private Long testSuiteId = null;
		private Long testClassId = null;
		private PerformanceTracker performanceTracker;
		
		/**
		 * Returns current test class id, useful in finding current testsuite
		 * @return
		 */
		public Long getTestClassId()
		{
			return testClassId;
		}

		public void setTestClassId(Long testId)
		{
			this.testClassId = testId;
		}
		
		/**
		 * Returns current testsuite's id
		 * @return
		 */
		public Long getTestSuiteId()
		{
			return testSuiteId;
		}

		public void setTestSuiteId(Long testSuiteId)
		{
			this.testSuiteId = testSuiteId;
		}
		
		/**
		 * Returns Performance Tracker instance assigned to the test class
		 * @return Object of {@link PerformanceTracker}
		 */
		public PerformanceTracker getPerformanceTracker()
		{
			return performanceTracker;
		}

		public void setPerformanceTracker(PerformanceTracker performanceTracker)
		{
			this.performanceTracker = performanceTracker;
		}


	}
}
