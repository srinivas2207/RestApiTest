package com.rest.test.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.rest.test.framework.ApiTestInfo;
import com.rest.test.framework.ApiTestInfo.ApiCallInfo;

/**
 * Custom property reader to parse the test files
 * 
 * @author SrinivasDonapati
 */
public class ApiTestPropertyReader extends Properties
{
	private static final long serialVersionUID = 1L;
	private List<String> propertyList = new ArrayList<String>();

	public void load(InputStream fis) throws IOException
	{
		Scanner in = new Scanner(fis);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		while (in.hasNext()) {
			String lineTxt = in.nextLine(); // .replace("\\", "\\\\");
			
			propertyList.add(lineTxt);
			
			out.write(lineTxt.getBytes());
			out.write("\n".getBytes());
		}

		try {
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		InputStream is = new ByteArrayInputStream(out.toByteArray());
		super.load(is);
	}

	public ApiTestInfo getApiTestInfo()
	{
		ApiTestInfo apiTestInfo = new ApiTestInfo();
		ApiCallInfo apiCallInfo = null;
		
		int lineNumber = 0;

		for (String property : propertyList) {
			lineNumber++;
			
			if (property == null || property.trim().length() == 0 ||
					property.trim().startsWith("#")) {
				continue;
			}
			
			if (property.startsWith(ApiTestConstants.PROPERTY_TEST_NAME)) {
				apiTestInfo.setTestName(getPropertyValue(property));
			}

			else if (property.startsWith(ApiTestConstants.PROPERTY_API)) {
				String apiName = getPropertyValue(property);
				apiCallInfo = apiTestInfo.createApiCallInfo(apiName, lineNumber);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_URL)) {
				String url = getPropertyValue(property);
				apiCallInfo.setUrl(url);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_REQ_METHOD)) {
				String requestMethod = getPropertyValue(property);
				apiCallInfo.setMethod(requestMethod);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_REQUEST)) {
				String request = getPropertyValue(property);
				apiCallInfo.setRequest(request);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_POLL)) {
				apiCallInfo.setPoll(true);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_RESPONSE)) {
				String response = getPropertyValue(property);
				apiCallInfo.setResponse(response);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_REQ_STATUES)) {
				String status = getPropertyValue(property);
				apiCallInfo.setReqStatus(Integer.parseInt(status));
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_TEST_CONDITION)) {
				String testCondition = getPropertyValue(property);
				apiCallInfo.setTestCondition(testCondition);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_COMPARE_RESPONSE)) {
				String compareResponse = getPropertyValue(property);
				if (compareResponse != null
						&& (compareResponse.trim().equalsIgnoreCase("true") || compareResponse.trim()
								.equalsIgnoreCase("on"))) {
					apiCallInfo.setCompareResponse(true);
				}
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_VARS)) {
				String vars = getPropertyValue(property);
				apiCallInfo.setVariableInfo(vars);
			} 
			else if (property.startsWith(ApiTestConstants.PROPERTY_TEST_WAIT_TIME)) {
				String str = getPropertyValue(property);
				int waitTime = Integer.parseInt(str);
				apiCallInfo.setWaitTime(waitTime);
			} 
			else if (property.startsWith(ApiTestConstants.PROPERTY_POLL_TIME)) {
				String str = getPropertyValue(property);
				int pollTime = Integer.parseInt(str);
				apiCallInfo.setPollTime(pollTime);
			} 
			else if (property.startsWith(ApiTestConstants.PROPERTY_POLL_INTERVAL)) {
				String str = getPropertyValue(property);
				int pollInterval = Integer.parseInt(str);
				apiCallInfo.setPollInterval(pollInterval);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_HEADERS)) {
				String headersStr = getPropertyValue(property);
				apiCallInfo.setHeaders(headersStr);
			}
			else if (property.startsWith(ApiTestConstants.PROPERTY_LOG_MSG)) {
				String logMessage = getPropertyValue(property);
				apiCallInfo.setLogMessage(logMessage);
			}
		}

		return apiTestInfo;
	}

	private String getPropertyValue(String property)
	{
		String propertyValue = null;
		int propertyValIndex = property.indexOf('=');
		if (propertyValIndex != -1 && propertyValIndex < property.length()) {
			propertyValue = property.substring(propertyValIndex + 1, property.length());
		}

		if (propertyValue != null) {
			propertyValue = propertyValue.trim();
		}
		return propertyValue;
	}

}
