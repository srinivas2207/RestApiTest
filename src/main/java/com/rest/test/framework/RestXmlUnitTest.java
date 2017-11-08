package com.rest.test.framework;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;

import com.rest.test.framework.util.ApiTestConstants;


public class RestXmlUnitTest {
	public String expectedXMLResponse = null;
	public String actualXMLResponse = null;
	
	public RestXmlUnitTest(String expectedXMLResponse, String actualXMLResponse) {
		this.actualXMLResponse = actualXMLResponse;
		this.expectedXMLResponse = expectedXMLResponse;
	}
	
	public void execute() {
		Diff myDiff = DiffBuilder.compare(Input.fromString(expectedXMLResponse))
				.withTest(Input.fromString(actualXMLResponse))
				.withDifferenceEvaluator(new IgnoreTextDifferenceEvaluator())
				.build();

		if (myDiff.hasDifferences()) {
			String assertionMessage = "";
			assertionMessage += myDiff.toString();
			
			assertionMessage += "\n\nExprected XML:" +expectedXMLResponse;
			assertionMessage += "\n\nCurrent XML:" +actualXMLResponse;
			
			throw new AssertionError(assertionMessage);
		}
	}
	
	
	/**
	 * Custom difference evaluator to handle attribute values and node values
	 * @author SrinivasDonapati
	 *
	 */
	private class IgnoreTextDifferenceEvaluator implements DifferenceEvaluator {
		
		@Override
		public ComparisonResult evaluate(Comparison comparison,
				ComparisonResult outcome) {
			if (outcome == ComparisonResult.EQUAL) {
				return outcome; // only evaluate differences.
			}

			final Node controlNode  = comparison.getControlDetails().getTarget();
			final Node testNode = comparison.getTestDetails().getTarget();
		
			// Handling failures caused due to node's text mismatch
			if (controlNode instanceof Text && testNode instanceof Text) {
				String actualValue = ((Text) testNode).getData();
				String expectedValue = ((Text) controlNode).getData();
				
				// Ignoring failures, if the failure values is marked as IGNORE
				if (expectedValue.contains(ApiTestConstants.IGNORE_VALUE)) {
					return ComparisonResult.EQUAL;
				}
				
				// Handling JSON test values
				if (isValidJson(actualValue) && isValidJson(expectedValue) ){
					RestJsonUnitTest restJsonUnitTest = new RestJsonUnitTest(expectedValue, actualValue, false);
					restJsonUnitTest.execute();

					return ComparisonResult.EQUAL;
				}
				
	            return outcome;
	        }
		
			// Handling attribute value mismatch
			if (controlNode instanceof Attr && testNode instanceof Attr) {
				String expectedValue = ((Attr) controlNode).getValue();
				if (expectedValue.contains(ApiTestConstants.IGNORE_VALUE)) {
					return ComparisonResult.EQUAL;
				}
	            return outcome;
	        }

			return outcome;
		}
	}


	private boolean isValidJson(String jsonString) {
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
