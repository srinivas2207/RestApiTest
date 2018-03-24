package com.rest.test.framework.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;

/**
 * Utility class to handle data
 * @author SrinivasDonapati
 *
 */
@SuppressWarnings("restriction")
public class DataUtil
{
	
	private static final String XPATH_PREFIX = "XPATH:";
	private static final String JSON_PATH_PREFIX = "$";
	
	/**
	 * Parsing XML and fetching value for the XPath
	 * @param xPath
	 * @param xml
	 * @return
	 */
	public static String getXpathValue(String xPath, String xml) {

		if (xPath.startsWith(XPATH_PREFIX)) {
			xPath = xPath.replace(XPATH_PREFIX, "");
		}
		
		String xpathVal = null;
		try {
			InputSource source = new InputSource(new StringReader(xml));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			
			Document document = db.parse(source);

			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();

			xpathVal = xpath.evaluate(xPath, document);
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			String assertionMessage = "Unable to parse XPath  : " + xPath;
			throw new AssertionError(assertionMessage);
		}
		return xpathVal;
	}
	
	/**
	 * Checks if the passed data is XML or not
	 * @param data
	 * @return
	 */
	public static boolean isXMLData(String data) {
		if (data != null) {
			data = data.trim();
			if (data.startsWith("<") && data.endsWith(">")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the passed data is JSON or not
	 * @param data
	 * @return
	 */
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
	
	/**
	 * Configuring JSON Path
	 * @param json
	 * @return
	 */
	private static Object configJsonPath (String json) {
		Object jsonDoc = null;
		try {
			jsonDoc = Configuration.defaultConfiguration().jsonProvider().parse(json);
		} catch(InvalidJsonException IJE) {
			String assertionMessage = "Invalid JSON : " + json;
			throw new AssertionError(assertionMessage);
		}
		return jsonDoc;
	}
	
	/**
	 * Parsing JSON data and fetching the value for the passed JSON Path
	 * @param jsonPath
	 * @param jsonData
	 * @return
	 */
	public static Object getJsonPathValue(String jsonPath, String jsonData) {
		Object jsonDoc = configJsonPath(jsonData);		
		try {
			Object result = JsonPath.read(jsonDoc, jsonPath);
			if (result instanceof net.minidev.json.JSONArray) {
				return ((net.minidev.json.JSONArray) result).get(0);
			} else {
				return result;
			}
		} catch(Exception e) {
			String assertionMessage = "Unable to find value for JSON Path : " + jsonPath ;
			throw new AssertionError(assertionMessage);
		}
	}
	
	
	/**
	 * Returns the type of the data
	 * @param value
	 * @return
	 */
	public static VARIABLE_VALUE_TYPE getVariableValueType(String value) {
		if (value.startsWith(JSON_PATH_PREFIX)) {
			return VARIABLE_VALUE_TYPE.JSON_PATH;
		}
		
		if (value.startsWith(XPATH_PREFIX)) {
			return VARIABLE_VALUE_TYPE.XML_PATH;
		}
		
		return VARIABLE_VALUE_TYPE.CONSTANT;
	}
	
	public enum VARIABLE_VALUE_TYPE	{
		JSON_PATH, XML_PATH, CONSTANT
	}
	
}
