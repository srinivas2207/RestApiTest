package com.rest.test.framework;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.rest.test.framework.network.RestNetworkUtil.RestCallResponse;
import com.rest.test.framework.util.PerformanceTracker;


/**
 * This class contains the parsed information from the test file.
 * Test name, List of api calls and responses.
 * @author SrinivasDonapati
 *
 */
public class ApiTestInfo {
	private RunTimeTestInfo runTimeTestInfo;
	private String testName = null;
	private String propertyFilePath = null;
	private List<ApiCallInfo> apiCallList = new ArrayList<>();
	private long apiRequestId= System.currentTimeMillis();
	
	public RunTimeTestInfo getRunTimeTestInfo()
	{
		return runTimeTestInfo;
	}

	public void setRunTimeTestInfo(RunTimeTestInfo runTimeTestInfo)
	{
		this.runTimeTestInfo = runTimeTestInfo;
	}
	
	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public List<ApiCallInfo> getApiReqList() {
		return apiCallList;
	}
	
	public ApiCallInfo createApiCallInfo(String apiName) {
		ApiCallInfo reqInfo = new ApiCallInfo();
		reqInfo.setName(apiName);
		reqInfo.setId(apiRequestId++);
		apiCallList.add(reqInfo);
		return reqInfo;
	}
	
	public String getPropertyFilePath()
	{
		return propertyFilePath;
	}

	public void setPropertyFilePath(String propertyFilePath)
	{
		this.propertyFilePath = propertyFilePath;
	}

	public class ApiCallInfo {
		private long id;
		private String name = null;
		private String url = null;
		private String method = null;
		private int reqStatus;
		private String request = null;
		private String response = null;
		private List<VariableInfo> testVariableList = new ArrayList<VariableInfo>();
		private List<VariableInfo> variableList = new ArrayList<VariableInfo>();
		private String testCondition;
		private boolean poll = false;
		private String testType = null;
		private RestCallResponse restCallResponse;
		private boolean isFirstTest;
		private boolean isLastTest;
		
		private int pollTime;
		private int pollInterval;
		private int waitTime;
		
		public ApiTestInfo getApiTestInfo() {
			return ApiTestInfo.this;
		}
		
		public boolean isFirstTest()
		{
			return isFirstTest;
		}
		public void setFirstTest(boolean isFirstTest)
		{
			this.isFirstTest = isFirstTest;
		}
		public boolean isLastTest()
		{
			return isLastTest;
		}
		public void setLastTest(boolean isLastTest)
		{
			this.isLastTest = isLastTest;
		}
		public RestCallResponse getRestCallResponse()
		{
			return restCallResponse;
		}
		public void setRestCallResponse(RestCallResponse restCallResponse)
		{
			this.restCallResponse = restCallResponse;
		}
		public boolean isPoll()
		{
			return poll;
		}
		public void setPoll(boolean poll)
		{
			this.poll = poll;
		}
		public long getId()
		{
			return id;
		}
		public void setId(long id)
		{
			this.id = id;
		}
		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
		public String getUrl()
		{
			return url;
		}
		public void setUrl(String url)
		{
			this.url = url;
		}
		public String getMethod()
		{
			return method;
		}
		public void setMethod(String method)
		{
			this.method = method;
		}
		public int getReqStatus()
		{
			return reqStatus;
		}
		public void setReqStatus(int reqStatus)
		{
			this.reqStatus = reqStatus;
		}
		public String getRequest()
		{
			return request;
		}
		public void setRequest(String request)
		{
			this.request = request;
		}
		public String getResponse()
		{
			return response;
		}
		public void setResponse(String response)
		{
			this.response = response;
		}
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
		
		public List<VariableInfo> getTestVariableList()
		{
			return testVariableList;
		}
		
		public void setTestVariableInfo(String variableInfo)
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
						testVariableList.add(varInfo);
					}
				}
			}
		}
		
		public String getTestCondition()
		{
			return testCondition;
		}
		public void setTestCondition(String testCondition)
		{
			this.testCondition = testCondition;
		}
		
		public String getTestType()
		{
			return testType;
		}

		public void setTestType(String testType)
		{
			this.testType = testType;
		}
		
		public int getPollTime()
		{
			return pollTime;
		}

		public void setPollTime(int pollTime)
		{
			this.pollTime = pollTime;
		}

		public int getPollInterval()
		{
			return pollInterval;
		}

		public void setPollInterval(int pollInterval)
		{
			this.pollInterval = pollInterval;
		}

		public int getWaitTime()
		{
			return waitTime;
		}

		public void setWaitTime(int waitTime)
		{
			this.waitTime = waitTime;
		}

	}
	
	public class VariableInfo {
		private String variableName;
		private String variableValue;
		public String getVariableName()
		{
			return variableName;
		}
		public void setVariableName(String variableName)
		{
			this.variableName = variableName;
		}
		public String getVariableValue()
		{
			return variableValue;
		}
		public void setVariableValue(String variableValue)
		{
			this.variableValue = variableValue;
		}
	}
	
	
	public static class RunTimeTestInfo {
		private Long testSuiteId = null;
		private Long testClassId = null;
		private PerformanceTracker performanceTracker;
		
		public Long getTestClassId()
		{
			return testClassId;
		}

		public void setTestClassId(Long testId)
		{
			this.testClassId = testId;
		}
		
		public Long getTestSuiteId()
		{
			return testSuiteId;
		}

		public void setTestSuiteId(Long testSuiteId)
		{
			this.testSuiteId = testSuiteId;
		}
		
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
