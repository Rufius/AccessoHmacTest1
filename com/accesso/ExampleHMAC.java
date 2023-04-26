package com.accesso;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class is for demonstration purposes only. It is not intended to be production ready. The intent is for it to be a working example that can be
 * used to understand the steps required to generate a correct HMAC.<br>
 * <br>
 *
 * Implementors of the HMAC algorithm should use <b>ALL</b> attributes found in the XML document when calculating the HMAC.<br>
 * <br>
 *
 * Created by <i><b>accesso</b></i>
 */
public class ExampleHMAC
{
	/**
	 * Perform all steps to calculate the HMAC and add it to the XML document.
	 *
	 * @return Returns an XML document that has the correctly calculated HMAC.
	 */
	public static Document buildHMACRequest(String xmlString)
	{
		// --------------------------------------------------
		// Declare Variables
		// --------------------------------------------------

		// Convert the XML string to a XML document.
		Document xml = stringToXML(xmlString);

		// Get the root xml element to work with.
		Element rootElement = xml.getDocumentElement();

		// Declare the private key that will be used to generate the HMAC. DANGER! THIS SHOULD NOT BE HARDCODED IN PRODUCTION IMPLEMENTATIONS!
		String privateKey = "abcdefg876543";

		// Declare the list that will store all attributes. NOT YET SORTED! After the items are added to the list it MUST be sorted before it is used to
		// generate the HMAC.
		List<String> sortedAttributes = new ArrayList<>();

		// Declare the variable that will store the concatenated attribute data. This string will be passed to the HMAC calculator.
		StringBuilder messageToCalculate = new StringBuilder();

		// Declare the variable that will be used to hold the calculated HMAC value.
		String hmac = "";

		// --------------------------------------------------
		// Extract attributes from the XML
		// --------------------------------------------------

		try
		{
			// Note that attributes of child nodes would need to be included as well. The XPath pattern /@* retrieves all attributes from everywhere in the
			// document.
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile("//@*");
			Object result = expr.evaluate(rootElement, XPathConstants.NODESET);
			NodeList rootElementAttributes = (NodeList) result;

			// Iterate the attributes and store them in a list.
			for (int i = 0; i < rootElementAttributes.getLength(); i++)
			{
				// Extract each node.
				Node attribute = rootElementAttributes.item(i);

				// Store the attribute as a name/value pair concatenated with the equals sign.
				sortedAttributes.add(attribute.getNodeName() + "=" + attribute.getNodeValue());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// --------------------------------------------------
		// Prepare the data for HMAC calculation
		// --------------------------------------------------

		// IMPORTANT STEP! Sort the attributes so they are in a consistent order.
		Collections.sort(sortedAttributes);

		// Iterate the sorted map and add each value to the message string.
		for (String entry : sortedAttributes)
		{
			messageToCalculate.append(entry);
		}

		// --------------------------------------------------
		// Calculate the HMAC
		// --------------------------------------------------

		try
		{
			// Build out the MAC objects so the token can be generated. They key needs to be converted form base64 to a byte array.
			SecretKeySpec secretKey = new SecretKeySpec(DatatypeConverter.parseBase64Binary(privateKey), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(secretKey);

			// Generate the token and convert the token bytes to a hex string.
			byte[] token = mac.doFinal(messageToCalculate.toString().getBytes("UTF-8"));
			hmac = DatatypeConverter.printHexBinary(token);

			// Since the root element came from the xml Document, adding it to the root element adds it to the document.
			rootElement.setAttribute("hmac", hmac);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Return the xml Document with the newly generated HMAC.
		return xml;
	}

	/**
	 * Convert a string to an XML document.<br>
	 * Code from <a href="http://stackoverflow.com/questions/3888033/how-to-convert-string-to-xml-file-in-java">Stack Overflow</a>
	 *
	 * @param xmlSource
	 * @return
	 */
	private static Document stringToXML(String xmlSource)
	{
		Document xml = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();

			xml = builder.parse(new InputSource(new StringReader(xmlSource)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return xml;
	}

	/**
	 * Convert a xml document to a string.<br>
	 * Code from <a href="http://stackoverflow.com/questions/2567416/xml-document-to-string">Stack Overflow</a>
	 *
	 * @param doc
	 * @return
	 */
	public static String toString(Document doc)
	{
		try
		{
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Error converting to String", ex);
		}
	}

}