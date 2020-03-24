package com.theplatform.dfh.cp.modules.xmlhelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtilsImpl implements XmlUtils
{
    private static final String YES = "yes";
    private XPath xPath = XPathFactory.newInstance().newXPath();

    @Override
    public String getDocumentXmlString(Document document) throws
            TransformerException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, YES);
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    @Override
    public Document createDocument(String rootElementName) throws ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document templateDoc = db.newDocument();
        Element rootElement = templateDoc.createElement(rootElementName);
        templateDoc.appendChild(rootElement);
        return templateDoc;
    }

    @Override
    public Document getDocumentFromString(String inputXml) throws ParserConfigurationException, SAXException,
            IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new InputSource(new StringReader(inputXml)));
    }

    @Override
    public Node getOrCreateChildNode(String tagName, Node parentNode)
    {
        try
        {
            Node childNode = (Node)xPath.evaluate("./" + tagName, parentNode, XPathConstants.NODE);
            if(childNode != null)
            {
                return childNode;
            }
            Element element = parentNode.getOwnerDocument().createElement(tagName);
            parentNode.appendChild(element);
            return element;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    @Override
    public String getSubNodeText(Node parentNode, String subNodeName, boolean allowNone) throws
            XPathExpressionException, XmlSearchException
    {
        Node subNode = (Node) xPath.evaluate("./" + subNodeName, parentNode, XPathConstants.NODE);
        if (subNode != null)
        {
            return subNode.getTextContent();
        }
        if(allowNone)
        {
            return null;
        }
        throw new XmlSearchException(String.format("Failed to find subnode: %1$s", subNodeName));
    }

    @Override
    public String getPathNodeText(Node parentNode, String path, boolean allowNone) throws
            XPathExpressionException, XmlSearchException
    {
        Node subNode = (Node) xPath.evaluate(path, parentNode, XPathConstants.NODE);
        if (subNode != null)
        {
            return subNode.getTextContent();
        }
        if(allowNone)
        {
            return null;
        }
        throw new XmlSearchException(String.format("Failed to find subnode: %1$s", path));
    }

    @Override
    public void setSubNodeText(Node filterNode, String subNodeName, String newValue) throws XPathExpressionException,
        XmlSearchException
    {
        Node subNode = (Node) xPath.evaluate("./" + subNodeName, filterNode, XPathConstants.NODE);
        if (subNode != null)
        {
            subNode.setTextContent(newValue);
            return;
        }
        throw new XmlSearchException(String.format("Failed to find subnode: %1$s", subNodeName));
    }

    @Override
    public String getAttributeText(Node node, String attribute) throws XPathExpressionException
    {
        return (String) xPath.evaluate("./@" + attribute, node, XPathConstants.STRING);
    }

    @Override
    public Node appendChildNode(Node parentNode, String nodeName, String nodeValue)
    {
        Element element = parentNode.getOwnerDocument().createElement(nodeName);
        if(nodeValue != null)
        {
            element.setTextContent(nodeValue);
        }
        parentNode.appendChild(element);
        return element;
    }

    @Override
    public String getNodeXmlString(Node node) throws TransformerException
    {
        StringWriter stringWriter = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, YES);
        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    @Override
    public XPath getxPath()
    {
        return xPath;
    }

    public XmlUtilsImpl setxPath(XPath xPath)
    {
        this.xPath = xPath;
        return this;
    }
}
