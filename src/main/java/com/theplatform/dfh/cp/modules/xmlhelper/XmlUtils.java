package com.theplatform.dfh.cp.modules.xmlhelper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public interface XmlUtils
{
    /**
     * Gets the string representation of the specified document
     * @param document The document read from
     * @return String representation of the document
     * @throws TransformerException
     */
    String getDocumentXmlString(Document document) throws TransformerException;

    /**
     * Gets the string representation of the specified node
     * @param node The node to read from
     * @return String representation of the node
     * @throws TransformerException
     */
    String getNodeXmlString(Node node) throws TransformerException;

    /**
     * Creates a XmlSearchException with the specified name as the root element
     * @param rootElementName The name of the root element
     * @return Document with a single root element
     * @throws ParserConfigurationException
     */
    Document createDocument(String rootElementName) throws ParserConfigurationException;

    /**
     * Gets a Document based on the Xml string
     * @param inputXml The Xml representation string
     * @return The parsed document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    Document getDocumentFromString(String inputXml) throws ParserConfigurationException, SAXException,
            IOException;
    /**
     * Gets the child node by name in the children of the specified node. If missing the child node is created.
     * @param tagName The tag name to seek
     * @param parentNode The parent of the node attempting to set
     * @return The node if found/created, null otherwise
     */
    Node getOrCreateChildNode(String tagName, Node parentNode);

    /**
     * Gets the text of the specified subnode
     * @param parentNode The node to get the text of
     * @param subNodeName The name of the subnode to get the text of
     * @param allowNone If specified the method will return null if the node cannot be found, otherwise an exception is thrown
     * @return The text content of the node, null otherwise
     * @throws XPathExpressionException
     * @throws XmlSearchException
     */
    String getSubNodeText(Node parentNode, String subNodeName, boolean allowNone) throws
            XPathExpressionException, XmlSearchException;

    /**
     * Gets the text of the specified path
     * @param parentNode The node to get the text of
     * @param path The path to attempt to get the node from
     * @param allowNone If specified the method will return null if the node cannot be found, otherwise an exception is thrown
     * @return The text content of the node, null otherwise
     * @throws XPathExpressionException
     * @throws XmlSearchException
     */
    String getPathNodeText(Node parentNode, String path, boolean allowNone) throws
            XPathExpressionException, XmlSearchException;

    /**
     * Sets the text of the specified subnode
     * @param filterNode The filter to get the id of
     * @param subNodeName The name of the subnode to set the text of
     * @param newValue The value to set on the node
     * @throws XPathExpressionException
     * @throws XmlSearchException
     */
    void setSubNodeText(Node filterNode, String subNodeName, String newValue) throws XPathExpressionException, XmlSearchException;

    /**
     * Gets the Attribute text of the specified node
     * @param node The node to get the attribute from
     * @param attribute The attribute to get
     * @return The value of the attribute (if specified)
     * @throws XPathExpressionException
     */
    String getAttributeText(Node node, String attribute) throws XPathExpressionException;

    /**
     * Appends a new child node to the specified node
     * @param parentNode The node to append the new node to
     * @param nodeName The name of the node to create
     * @param nodeValue The value to set on the node (optional)
     * @return The newly added node
     */
    Node appendChildNode(Node parentNode, String nodeName, String nodeValue);

    XPath getxPath();
}
