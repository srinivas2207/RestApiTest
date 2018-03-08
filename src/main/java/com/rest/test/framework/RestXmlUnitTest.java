package com.rest.test.framework;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;

/**
 * This Utility class is used for comparing two different XML data.
 *
 * * <br><br>
 * Used XmlUnit library <a href="https://github.com/xmlunit/xmlunit">https://github.com/xmlunit/xmlunit</a>
 * @author SrinivasDonapati
 *
 */
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
				
				// Handling JSON test values
				if (RestJsonUnitTest.isValidJson(actualValue) && RestJsonUnitTest.isValidJson(expectedValue) ){
					RestJsonUnitTest restJsonUnitTest = new RestJsonUnitTest(expectedValue, actualValue, false);
					restJsonUnitTest.execute();
					return ComparisonResult.EQUAL;
				}
				
				// Handling Plain String values
				RestStringUnitTest restStringUnitTest = new RestStringUnitTest(expectedValue, actualValue);
				if (restStringUnitTest.compare()) {
					return ComparisonResult.EQUAL;
				}
				
	            return outcome;
	        }
		
			// Handling attribute value mismatch
			if (controlNode instanceof Attr && testNode instanceof Attr) {
				String expectedValue = ((Attr) controlNode).getValue();
				String actualValue = ((Attr) testNode).getValue();
			
				// Handling Plain String values
				RestStringUnitTest restStringUnitTest = new RestStringUnitTest(expectedValue, actualValue);
				if (restStringUnitTest.compare()) {
					return ComparisonResult.EQUAL;
				}
				
				return outcome;
	        }

			return outcome;
		}
	}
}
