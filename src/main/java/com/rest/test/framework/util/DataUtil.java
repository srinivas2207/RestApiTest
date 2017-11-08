package com.rest.test.framework.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.minidev.json.JSONArray;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;

@SuppressWarnings("restriction")
public class DataUtil
{
	
	private static final String XPATH_PREFIX = "XPATH:";
	private static final String JSON_PATH_PREFIX = "$";
	
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
	
	public static boolean isXMLData(String data) {
		if (data != null) {
			data = data.trim();
			if (data.startsWith("<") && data.endsWith(">")) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isJsonData(String data)
	{
		if ((data.startsWith("{") && data.endsWith("}")) || (data.startsWith("[") && data.endsWith("]"))) { return true; }
		return false;
	}
	
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
	
	public static Object getJsonPathValue(String jsonPath, String jsonData) {
		Object jsonDoc = configJsonPath(jsonData);		
		try {
			Object result = JsonPath.read(jsonDoc, jsonPath);
			if (result instanceof JSONArray) {
				return ((JSONArray) result).get(0);
			} else {
				return result;
			}
		} catch(Exception e) {
			String assertionMessage = "Unable to find value for JSON Path : " + jsonPath ;
			throw new AssertionError(assertionMessage);
		}
	}
	
	
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
