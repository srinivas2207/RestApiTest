package com.rest.test.framework;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.jsonArrayToList;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil;

import com.rest.test.framework.util.ApiTestConstants;


/**
 * This utility class is used for comparing two different JSON data.
 * <br><br>
 * Used JSONassert library <a href="https://github.com/skyscreamer/JSONassert">https://github.com/skyscreamer/JSONassert</a>
 * 
 * @author SrinivasDonapati
 *
 */
public class RestJsonUnitTest {
	private String expectedJson = null;
	private String actualJson = null;
	private boolean isStrictMode = false;
	
	/**
	 * 
	 * @param expectedJson JSON data from expected response
	 * @param actualJson JSON data from actual response
	 * @param strict JSON comparison test mode
	 */
	public RestJsonUnitTest(String expectedJson, String actualJson, boolean strict) {
		this.expectedJson = expectedJson;
		this.actualJson = actualJson;
		this.isStrictMode = strict;
	}
	
	private void fixJsonStrings() {
		if (expectedJson != null) {
			expectedJson = expectedJson.replace("\\n", "");
			expectedJson = expectedJson.replace("\\\"", "'");
		}
		
		if (actualJson != null) {
			actualJson = actualJson.replace("\\n", "");
			actualJson = actualJson.replace("\\\"", "'");
		}
	}
	
	/**
	 * Running JSON Unit
	 */
	public void execute() {
		fixJsonStrings();
		
		if (expectedJson==actualJson) return;
        if (expectedJson==null){
            throw new AssertionError("Expected JSON string is null.");
        }else if (actualJson==null){
            throw new AssertionError("Actual JSON string is null.");
        }
        
        JSONComparator jsonComparator =  new RestJsonComparator(isStrictMode ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT);
        JSONCompareResult result = JSONCompare.compareJSON(expectedJson, actualJson, jsonComparator);
        if (result.failed()) {
        	String assertionMessage = "Exceptions while comparing JSON Data: \n";
        	assertionMessage += "Expected JSON :" + expectedJson + "\n";
        	assertionMessage += "Current JSON :" + actualJson + "\n\n";
        	assertionMessage +=  result.getMessage();	
        	throw new AssertionError(assertionMessage);
        }
	}
    
    /**
     * Custom JSON comparator to handle assertion failures
     * @author SrinivasDonapati
     *
     */
    private class RestJsonComparator extends DefaultComparator{

    	public RestJsonComparator(JSONCompareMode mode) {
    		super(mode);
    	}
    	
    	@Override
        public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result)
                throws JSONException {
    		if (expectedValue != null && actualValue != null && !isValidJson(expectedValue.toString())
					&& !isValidJson(actualValue.toString())) {
				// Handling Plain String values
				RestStringUnitTest restStringUnitTest = new RestStringUnitTest(expectedValue.toString(),
						actualValue.toString());
				if (restStringUnitTest.compare()) { return; }
			}
    		super.compareValues(prefix, expectedValue, actualValue, result);
        }
    	
		@Override
		protected void compareJSONArrayOfSimpleValues(String key, JSONArray expected, JSONArray actual,
														JSONCompareResult result) throws JSONException
		{
			Map<Object, Integer> expectedCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(expected));
			Map<Object, Integer> actualCount = JSONCompareUtil.getCardinalityMap(jsonArrayToList(actual));
			for (Object o : expectedCount.keySet()) {
				
				// Ignoring comparing of an array item, which is marked as
				// IGNORE_STRING
				if (o != null && o.toString().contains(ApiTestConstants.IGNORE_STRING)) {
					continue;
				}

				// Finding the matched string inside array, if the expected
				// array element contains IGNORE_VALUE
				if (o != null && o.toString().contains(ApiTestConstants.IGNORE_VALUE)) {
					if (_findInArray(o.toString(), actual)) {
						continue;
					}
				}

				if (!actualCount.containsKey(o)) {
					result.missing(key + "[]", o);
				} else if (actualCount.get(o) < expectedCount.get(o)) {
					result.fail(key + "[]: Expected " + expectedCount.get(o) + " occurrence(s) of " + o + " but got "
							+ actualCount.get(o) + " occurrence(s)");
				}
			}
		}
		
		/**
		 * Finding the matched element inside an array
		 * 
		 * @param expected
		 * @param actualArr
		 * @return
		 */
		private boolean _findInArray(String expected, JSONArray actualArr) {
			if (actualArr == null || actualArr.length() == 0) {
				return false;
			}

			for (int i = 0; i < actualArr.length(); i++) {
				String actualVal = actualArr.get(i).toString();
				// Handling Plain String values
				RestStringUnitTest restStringUnitTest = new RestStringUnitTest(expected, actualVal);
				if (restStringUnitTest.compare()) {
					return true;
				}
			}

			return false;
		}

    }
    
    public static boolean isValidJson(String jsonString) {
		try {
			JSONObject obj = new JSONObject(jsonString);
			return true;
		} catch (Exception objCreateExcepetion) {
			try {
				JSONArray arr = new JSONArray(jsonString);
				return true;
			} catch (Exception arrayCreateException) {

			}
		}
		return false;
	}
}