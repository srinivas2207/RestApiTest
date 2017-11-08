package com.rest.test.framework;

import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import org.junit.runners.Parameterized.Parameters;

import com.rest.test.framework.ApiTestInfo.ApiCallInfo;
import com.rest.test.framework.ApiTestInfo.RunTimeTestInfo;
import com.rest.test.framework.network.RestNetworkUtil;
import com.rest.test.framework.util.ApiTestConstants;
import com.rest.test.framework.util.PerformanceTracker;

@RunWith(AllTests.class)
public class RestApiBaseTestSuite {
	private static RestApiBaseTestSuite testSuite = null;
	private static Map<Long, RestApiBaseTestSuite> suiteMap = new HashMap<>();
	
	private RestNetworkUtil restUtil;
	private List<ApiTestInfo> apiTestList = new ArrayList<>();
	private Map<String, String> suiteVariableMap = new HashMap<String, String>();
	private Map<String, String> envVariableMap = new HashMap<>(); 

	private List<Object> testList;
	private List<String> propertyFileList;
	
	private int propertyFileTestCount = 0;
	private int apiTestCount = 0;
	
	private RestAuthenticator restAuthenticator;
	private boolean isPerformanceTrackOn = false;
	private boolean isPerformanceTrackAppend = false;
	

	public static synchronized void init(Class childSuite, List<Object> testList) {
		// Creating an instance of calling testsuite. This instance is useful
		// for calling Setup and teardown methods
		try {
			testSuite = null;
			testSuite = (RestApiBaseTestSuite) childSuite.getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (testSuite != null) {
			RestNetworkUtil restUtil = new RestNetworkUtil();
			testSuite.restUtil = restUtil;
			testSuite.propertyFileTestCount = 0;
			testSuite.apiTestCount = 0;
			testSuite.testList = testList;
		}
	}
	
	public static synchronized TestSuite suite() {
		if (testSuite == null) {
			return null;
		}
		
		Long suiteId = addTestSuite(testSuite);
		
		testSuite.propertyFileList = new ArrayList();
		
		TestSuite suite = new TestSuite();
		for (int i = 0; i < testSuite.testList.size(); i++) {
			if (testSuite.testList.get(i) instanceof String) {
				testSuite.propertyFileList.add(testSuite.testList.get(i).toString());
				suite.addTest(new JUnit4TestAdapter(PropertyTester.class));
			} else {
				Class test = (Class) testSuite.testList.get(i);
				suite.addTest(new JUnit4TestAdapter(test));
			}
			
			ApiTestInfo apiTestInfo = RestApiBaseTest.getApiTestInfo();	
			RunTimeTestInfo runTimeTestInfo =new ApiTestInfo.RunTimeTestInfo();
			runTimeTestInfo.setTestSuiteId(suiteId);
			apiTestInfo.setRunTimeTestInfo(runTimeTestInfo);
			testSuite.apiTestList.add(apiTestInfo);
		}
		
		// Setting up test suite
		if (testSuite != null) {
			testSuite.setUpSuite();
		}
		
		return suite;
	}
	
	
	public void initConfig(RestSuiteConfiguration config)
	{
		if(config.getBaseUrl() != null) {
			getRestUtil().setBaseUrl(config.getBaseUrl());
		}
		
		if(config.getRestAuthenticator() != null) {
			this.restAuthenticator = config.getRestAuthenticator();
		}
		
		if(config.isTrackPerformance()) {
			boolean isPerformanceAppend = config.isAppendPerformanceResults();
			String recordLocation = config.getPerformanceRecordLocation();
			for (ApiTestInfo apiTestInfo : apiTestList) {
				PerformanceTracker performanceTracker = new PerformanceTracker(apiTestInfo.getPropertyFilePath(), isPerformanceAppend);
				if (recordLocation != null && recordLocation.trim().length() > 0) {
					performanceTracker.setRecordLocation(recordLocation.trim());
				}
				apiTestInfo.getRunTimeTestInfo().setPerformanceTracker(performanceTracker);
			}
		}
	}

	/**
	 * This inner class is used to run tests, added to test suite as a property file
	 */
	public static class PropertyTester extends RestApiBaseTest {
		
		public PropertyTester(String apiName, ApiCallInfo apiCallInfo) {
			super(apiName, apiCallInfo);
			// TODO Auto-generated constructor stub
		}

		@Parameters(name = "{0}")
		public static Collection<Object[]> data() throws Exception{
			setPropertyFile(testSuite.propertyFileList.get(testSuite.propertyFileTestCount++));
			
			return RestApiBaseTest.data();
		}
	}
	
	public String getVariableValue(String variableName) {
		return suiteVariableMap.get(variableName);
	}

	public String setVariableValue(String variableName, String variableValue) {
		return suiteVariableMap.put(variableName, variableValue);
	}

	public Map getVariableMap() {
		return suiteVariableMap;
	}
	
	public void clearVariables() {
		suiteVariableMap.clear();
	}
	
	/**
	 * Callback before running test suite
	 */
	public void setUpSuite() {
		CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
		if(restAuthenticator != null) {
			try {
				restAuthenticator.setRestUtil(restUtil);
				restAuthenticator.setUp();
			} catch (Exception e) {
				String assertionMessage =  "Error while setting up Authenticator !\n";
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				String stackTrace = errors.toString();
				assertionMessage += stackTrace;
				assertTrue(assertionMessage, false);
			}
		}
	}

	/**
	 * Callback after running test suite
	 */
	public void tearDownSuite() {
		clearVariables();
		
		if(restAuthenticator != null) {
			try {
				restAuthenticator.tearDown();
			} catch (Exception e) {
				String assertionMessage =  "Error while clearing up Authenticator !\n";
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				String stackTrace = errors.toString();
				assertionMessage += stackTrace;
				assertTrue(assertionMessage, false);
			}
		}
	}
	
	/**
	 * Call back before running each test class
	 */
	public void setUpClass(ApiTestInfo apiTestInfo) {
		this.apiTestCount++;
	}

	/**
	 * Call back after running each test class
	 */
	public void tearDownClass(ApiTestInfo apiTestInfo) {
		if (this.apiTestCount == this.testList.size()) {
			this.tearDownSuite();
		}
	}
	
	public static RestApiBaseTestSuite getTestSuite(Long id) {
		return suiteMap.get(id);
	}
	
	public static RestApiBaseTestSuite getTestSuite(ApiTestInfo apiTestInfo) {
		return suiteMap.get(apiTestInfo.getRunTimeTestInfo().getTestSuiteId());
	}
	
	public static RestApiBaseTestSuite getTestSuite(ApiCallInfo apiCallInfo) {
		return suiteMap.get(apiCallInfo.getApiTestInfo().getRunTimeTestInfo().getTestSuiteId());
	}
	
	private static Long addTestSuite(RestApiBaseTestSuite suite) {
		Long id = getRandomId();
		while(suiteMap.containsKey(id)) {
			id = getRandomId(); 
		}
		suiteMap.put(id, suite);
		return id;
	}
	
	private static long getRandomId() {
		long randomNumber = 0;

		long start = 1000000000L;
		long end = 9999999999L;

		long range = end - start + 1;
		Random random = new Random();
		// compute a fraction of the range, 0 <= frac < range
		long fraction = (long) (range * random.nextDouble());
		randomNumber = (long) (fraction + start);

		return randomNumber;
	}
	
	public RestNetworkUtil getRestUtil() {
		return restUtil;
	}
	
	public String getEnvironmentValue(String key) {
		return envVariableMap.get(key);
	}
	
}
