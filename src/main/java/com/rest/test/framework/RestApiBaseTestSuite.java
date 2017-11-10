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
import com.rest.test.framework.util.PerformanceTracker;

/**
 * This is the base class used for creating test suites. <br>
 * <br>
 * Create a sub class and pass test classes info by adding below method <br>
 * <br>
 * 
 * <pre>
 * 
 * public static TestSuite suite()
 * {
 * 	List&lt;Object&gt; testList = new ArrayList&lt;&gt;();
 * 
 * 	testList.add(&quot;Twitter/profile_test.properties&quot;);
 * 	testList.add(&quot;Twitter/tweet_test.properties&quot;);
 * 	testList.add(FavouriteTweet.class);
 * 	testList.add(&quot;Twitter/delete_tweet.properties&quot;);
 * 
 * 	init(TwitterTestSuite.class, testList);
 * 
 * 	return RestApiBaseTestSuite.suite();
 * }
 * </pre>
 * 
 * <br>
 * Override {@code setUpSuite} and {@code tearDownSuite} to track test suite's life cycle <br>
 * <br>
 * Call {@code initConfig} to pass the test suite configuration from {@code setUpSuite} method
 * 
 * @author SrinivasDonapati
 */
@RunWith(AllTests.class)
public class RestApiBaseTestSuite {
	private static RestApiBaseTestSuite testSuite = null;
	private static Map<Long, RestApiBaseTestSuite> suiteMap = new HashMap<>();
	
	private RestNetworkUtil restUtil;
	private List<ApiTestInfo> apiTestList = new ArrayList<>();
	private Map<String, String> suiteVariableMap = new HashMap<String, String>();
	
	private List<Object> testList;
	private List<String> propertyFileList;
	
	private int propertyFileTestCount = 0;
	private int apiTestCount = 0;
	
	private RestAuthenticator restAuthenticator;

	/**
	 * Initializing the test suite
	 * @param childSuite Class of the test suite
	 * @param testList List of tests (property files or custom test classes)
	 */
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
	
	/**
	 * Method used by JUNIT to initialize testing
	 * @return
	 */
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
	
	/**
	 * Setting test suite configuration
	 * @param config {@link RestSuiteConfiguration}
	 */
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
	 * This class is used to convert property files into JUNIT tests
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
	
	/**
	 * Returns the variable value stored in suite
	 * @param variableName
	 * @return Variable value
	 */
	public String getVariableValue(String variableName) {
		return suiteVariableMap.get(variableName);
	}

	/**
	 * Storing Variable in suite
	 * @param variableName
	 * @param variableValue
	 * @return
	 */
	public String setVariableValue(String variableName, String variableValue) {
		return suiteVariableMap.put(variableName, variableValue);
	}

	/**
	 * Returning all the variables
	 * @return Variable map
	 */
	public Map getVariableMap() {
		return suiteVariableMap;
	}
	
	/**
	 * Clears all the variables stored
	 */
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
	 * @param apiTestInfo {@link ApiTestInfo}
	 */
	public void setUpClass(ApiTestInfo apiTestInfo) {
		this.apiTestCount++;
	}

	/**
	 * Call back after running each test class
	 * @param apiTestInfo {@link ApiTestInfo}
	 */
	public void tearDownClass(ApiTestInfo apiTestInfo) {
		if (this.apiTestCount == this.testList.size()) {
			this.tearDownSuite();
		}
	}
	
	/**
	 * Returns the running test suite
	 * @param id Test suite id
	 * @return {@link RestApiBaseTestSuite} instance
	 */
	public static RestApiBaseTestSuite getTestSuite(Long id) {
		return suiteMap.get(id);
	}
	
	/**
	 * Returns the running test suite
	 * @param apiTestInfo Test class info
	 * @return {@link RestApiBaseTestSuite} instance
	 */
	public static RestApiBaseTestSuite getTestSuite(ApiTestInfo apiTestInfo) {
		return suiteMap.get(apiTestInfo.getRunTimeTestInfo().getTestSuiteId());
	}
	
	/**
	 * Returns the running test suite
	 * @param apiCallInfo API Call info
	 * @return {@link RestApiBaseTestSuite} instance
	 */
	public static RestApiBaseTestSuite getTestSuite(ApiCallInfo apiCallInfo) {
		return suiteMap.get(apiCallInfo.getApiTestInfo().getRunTimeTestInfo().getTestSuiteId());
	}
	
	/**
	 * Adds test suite to the MAP using a unique id<br>
	 * This is is crucial in identifying running test suite
	 * @param suite
	 * @return Test suites's unique Id
	 */
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
	
	/**
	 * Returns assigned rest util for this test suite
	 * @return {@link RestNetworkUtil}
	 */
	public RestNetworkUtil getRestUtil() {
		return restUtil;
	}
	
}
