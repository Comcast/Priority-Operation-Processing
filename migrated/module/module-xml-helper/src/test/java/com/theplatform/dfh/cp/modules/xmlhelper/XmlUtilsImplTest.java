package com.theplatform.dfh.cp.modules.xmlhelper;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathConstants;
import java.io.IOException;

public class XmlUtilsImplTest
{
    private XmlUtils xmlUtils = new XmlUtilsImpl();
    private final String SUBNODE_NAME = "subnode";
    private final String SUBNODE_TEXT = "45";
    private final String SUBNODE_ATTRIB_NAME ="attrib";
    private final String SUBNODE_ATTRIB_VALUE ="62";

    private final String SUBOBJECT_NAME = "another";
    private final String SUBOBJECT_TEXT="data";
    private final String SUBOBJECT_ATTRIB_NAME ="item";
    private final String SUBOBJECT_ATTRIB_VALUE ="55";

/* basic.xml for reference
<sample>
    <subnode attrib="62">45</subnode>
    <subobject>
        <another item="55">data</another>
    </subobject>
</sample>
 */

    @Test
    public void testGetDocumentXmlString() throws Exception
    {
        Assert.assertNotNull(getBasicDocument());
    }

    @Test
    public void testGetSubNodeText() throws Exception
    {
        Assert.assertEquals(xmlUtils.getSubNodeText(getSampleNode(), SUBNODE_NAME, false), SUBNODE_TEXT);
    }

    @Test(expectedExceptions = XmlSearchException.class)
    public void testInvalidGetSubNodeText() throws Exception
    {
        xmlUtils.getSubNodeText(getSampleNode(), SUBNODE_NAME+ "invalid", false);
    }

    @Test
    public void testInvalidGetSubNodeTextIgnored() throws Exception
    {
        Assert.assertNull(xmlUtils.getSubNodeText(getSampleNode(), SUBNODE_NAME+ "invalid", true));
    }

    @Test
    public void testGetPathNodeText() throws Exception
    {
        Document document = getBasicDocument();
        Assert.assertEquals(xmlUtils.getPathNodeText(document, "/sample/subobject/another", false), SUBOBJECT_TEXT);
    }

    @Test(expectedExceptions = XmlSearchException.class)
    public void testInvalidGetPathNodeText() throws Exception
    {
        Document document = getBasicDocument();
        xmlUtils.getPathNodeText(document, "/sample/subobject/anotherX", false);
    }

    @Test
    public void testInvalidGetPathNodeTextIgnored() throws Exception
    {
        Document document = getBasicDocument();
        Assert.assertNull(xmlUtils.getPathNodeText(document, "/sample/subobject/anotherX", true));
    }

    @Test
    public void testGetAttributeText() throws Exception
    {
        Document document = getBasicDocument();
        Node subNode = (Node) xmlUtils.getxPath().evaluate("./sample/subnode", document, XPathConstants.NODE);

        Assert.assertEquals(xmlUtils.getAttributeText(subNode, SUBNODE_ATTRIB_NAME), SUBNODE_ATTRIB_VALUE);
    }

    @Test
    public void testGetOrCreateChildNode_Existing() throws Exception
    {
        Node sampleNode = getSampleNode();
        Node expectedNode = (Node) xmlUtils.getxPath().evaluate("./subnode", sampleNode, XPathConstants.NODE);
        Assert.assertEquals(xmlUtils.getOrCreateChildNode(SUBNODE_NAME, sampleNode), expectedNode);
    }

    @Test
    public void testGetOrCreateChildNode_Missing() throws Exception
    {
        Node sampleNode = getSampleNode();
        Assert.assertNotNull(xmlUtils.getOrCreateChildNode(SUBNODE_NAME + "invalid", sampleNode));
    }

    public Node getSampleNode() throws Exception
    {
        Document document = getBasicDocument();
        return (Node) xmlUtils.getxPath().evaluate("./sample", document, XPathConstants.NODE);
    }

    public Document getBasicDocument() throws Exception
    {
        return xmlUtils.getDocumentFromString(getStringFromResourceFile("/basic.xml"));
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }
}
