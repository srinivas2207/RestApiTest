package com.rest.test.framework;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.jsonArrayToList;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil;

import com.rest.test.framework.util.ApiTestConstants;


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
    		// Ignoring failures caused due to unmatched values marked as IGNORE_VALUE
    		if (expectedValue != null
    				&& expectedValue.toString().equals(ApiTestConstants.IGNORE_VALUE)) {
    			return;
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
				if (o != null && o instanceof String && o.equals(ApiTestConstants.IGNORE_VALUE)) {
					continue;
				}
				
				if (!actualCount.containsKey(o)) {
					result.missing(key + "[]", o);
				}
				else if (actualCount.get(o) < expectedCount.get(o)) {
					result.fail(key + "[]: Expected " + expectedCount.get(o) + " occurrence(s) of " + o
							+ " but got " + actualCount.get(o) + " occurrence(s)");
				}
			}
		}

    }
}