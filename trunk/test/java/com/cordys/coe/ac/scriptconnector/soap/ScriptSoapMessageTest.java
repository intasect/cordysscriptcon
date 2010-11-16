/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.soap;

import java.text.MessageFormat;

import com.cordys.coe.ac.scriptconnector.ScriptConnectorTestCase;
import com.eibus.xml.nom.Node;

/**
 * Test cases for the ScriptSoapMessage class.
 *
 * @author mpoyhone
 */
public class ScriptSoapMessageTest extends ScriptConnectorTestCase
{
    private static final String SOAP_ENV_TEMPLATE = 
        "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" +
        "        <SOAP:Header>{0}</SOAP:Header>\r\n" +
        "        <SOAP:Body>{1}</SOAP:Body>\r\n" +
        "</SOAP:Envelope>";
    /**
     * @see com.cordys.coe.ac.scriptconnector.ScriptConnectorTestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see com.cordys.coe.ac.scriptconnector.ScriptConnectorTestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#addMethodChildAsString(java.lang.String, boolean)}.
     */
    public void testAddMethodChildAsString() throws Exception
    {
        String methodString = "<mymethod><field1/><field2/></mymethod>";
        int destEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<root xmlns='xxx'></root>"));
        int expectedEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<root xmlns='xxx'><field1/><field2/><mymethod><field1/><field2/></mymethod></root>"));
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            // First add the children.
            msg.addMethodChildAsString(methodString, true);
            // Then add the whole method.
            msg.addMethodChildAsString(methodString, false);
            msg.appendToSoapEnvelope(destEnvelope, true);
            
            assertNodesEqual(expectedEnvelope, destEnvelope, true);
        }
        finally {
            msg.clear();
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#appendToSoapEnvelope(int, boolean)}.
     * 
     * Tests that SOAP namespace declarations are not added to header or method elements. SOAP method node is not 
     * present in the destination envelope.
     */
    public void testAppendToSoapEnvelope_NoDestMethod()
    {
       int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "<myheader><field1/></myheader>", "<mymethod xmlns='test'/>"));
       int destEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", ""));
       int expectedEnvelope = addNomGarbage(Node.duplicate(srcEnvelope)); 
       ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

       try {
           msg.readFromSoapMessage(srcEnvelope);
           msg.appendToSoapEnvelope(destEnvelope, true);
       
           assertNodesEqual(expectedEnvelope, destEnvelope, true);
       }
       finally {
           msg.clear();
       }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#appendToSoapEnvelope(int, boolean)}.
     * 
     * Tests that SOAP namespace declarations are not added to header or method elements. SOAP method node is  
     * present in the destination envelope.
     */
    public void testAppendToSoapEnvelope_WithDestMethod()
    {
       int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "<myheader><field1/></myheader>", "<mymethod xmlns='test'/>"));
       int destEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<tobereplaced />"));
       int expectedEnvelope = addNomGarbage(Node.duplicate(srcEnvelope)); 
       ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

       try {
           msg.readFromSoapMessage(srcEnvelope);
           msg.appendToSoapEnvelope(destEnvelope, true);
       
           assertNodesEqual(expectedEnvelope, destEnvelope, true);
       }
       finally {
           msg.clear();
       }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#appendToSoapEnvelope(int, boolean)}.
     * 
     * Tests that SOAP namespace declarations are not added to header or method elements. SOAP method node is  
     * present in the destination envelope. This method node is not replaced.
     */
    public void testAppendToSoapEnvelope_WithDestMethod_NoReplace()
    {
       int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "<myheader><field1/></myheader>", "<mymethod xmlns='test'><a/><b/></mymethod>"));
       int destEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<root xmlns='xxx'></root>"));
       int expectedEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "<myheader><field1/></myheader>", "<root xmlns='xxx'><a xmlns='test' /><b xmlns='test'/></root>"));
       ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

       try {
           msg.readFromSoapMessage(srcEnvelope);
           msg.appendToSoapEnvelope(destEnvelope, false);
       
           assertNodesEqual(expectedEnvelope, destEnvelope, true);
       }
       finally {
           msg.clear();
       }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#readFromSoapMessage(int)}.
     */
    public void testReadFromSoapMessage()
    {
        String soapHeaderTemplate = "<SOAP:Header xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">{0}</SOAP:Header>";
        String headerString = "<myheader><field1/></myheader>";
        String methodString = "<mymethod xmlns=\"test\"><a/><b/></mymethod>";
        
        int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, headerString, methodString));
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.readFromSoapMessage(srcEnvelope);
            
            assertEquals(MessageFormat.format(soapHeaderTemplate, headerString), msg.getHeaderAsString());
            assertEquals(methodString, msg.getMethodAsString());
        }
        finally {
            msg.clear();
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#removeChildElementNamespaces(java.lang.String[])}.
     */
    public void testRemoveChildElementNamespaces()
    {
        String methodString = "<mymethod xmlns=\"test\"><a xmlns='yy'/><ns1:b xmlns:ns1=\"urn:ns1\"/><c><ns2:d xmlns:ns2='uri:ns2' /></c></mymethod>";
        String expectedMethodString = "<mymethod xmlns=\"test\"><a/><b/><c><d/></c></mymethod>";
        
        int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", methodString));
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.readFromSoapMessage(srcEnvelope);
            msg.removeChildElementNamespaces(null);
            
            assertEquals(expectedMethodString, msg.getMethodAsString());
        }
        finally {
            msg.clear();
        }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#removeChildElementNamespaces(java.lang.String[])}.
     */
    public void testRemoveChildElementNamespaces_ignoreUris()
    {
        String methodString = "<mymethod xmlns=\"test\" ><a xmlns='yy'/><ns1:b xmlns:ns1=\"urn:ns1\" /><c><ns2:d xmlns:ns2='uri:ns2' /></c></mymethod>";
        String expectedMethodString = "<mymethod xmlns=\"test\"><a xmlns=\"yy\"/><b/><c><d/></c></mymethod>";
        
        int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", methodString));
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.readFromSoapMessage(srcEnvelope);
            msg.removeChildElementNamespaces(new String[] { "yy" });
            
            assertEquals(expectedMethodString, msg.getMethodAsString());
        }
        finally {
            msg.clear();
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#getMethodName()}.
     */
    public void testGetMethodName()
    {
        int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<mymethod xmlns='test'/>"));
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.readFromSoapMessage(srcEnvelope);
            
            assertEquals("mymethod", msg.getMethodName());
        }
        finally {
            msg.clear();
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#getNamespace()}.
     */
    public void testGetNamespace()
    {
        int srcEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<mymethod xmlns='test'/>"));
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.readFromSoapMessage(srcEnvelope);
            
            assertEquals("test", msg.getNamespace());
        }
        finally {
            msg.clear();
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#getOrgDn()}.
     */
    public void testGetOrgDn()
    {
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);
        
        assertNull(msg.getOrgDn());
        msg.setOrgDn("xxx");
        assertEquals("xxx", msg.getOrgDn());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#getUserDn()}.
     */
    public void testGetUserDn()
    {
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);
        
        assertNull(msg.getUserDn());
        msg.setUserDn("xxx");
        assertEquals("xxx", msg.getUserDn());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#setHeaderAsString(java.lang.String)}.
     */
    public void testSetHeaderAsString() throws Exception
    {
        String headerString = "<myheader><field1/><field2/></myheader>";
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.setHeaderAsString(headerString);
            
            assertEquals(headerString, msg.getHeaderAsString());
        }
        finally {
            msg.clear();
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#setMethodAsString(java.lang.String, boolean)}.
     */
    public void testSetMethodAsString() throws Exception
    {
        String methodString = "<mymethod><field1/><field2/></mymethod>";
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.setMethodAsString(methodString, false);
            
            assertEquals(methodString, msg.getMethodAsString());
        }
        finally {
            msg.clear();
        }
    }
    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage#setMethodAsString(java.lang.String, boolean)}.
     */
    public void testSetMethodAsString_useChildren() throws Exception
    {
        String methodString = "<mymethod><field1/><field2/></mymethod>";
        int destEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<root xmlns='xxx'></root>"));
        int expectedEnvelope = parse(MessageFormat.format(SOAP_ENV_TEMPLATE, "", "<root xmlns='xxx'><field1/><field2/></root>"));
        ScriptSoapMessage msg = new ScriptSoapMessage(dDoc);

        try {
            msg.setMethodAsString(methodString, true);
            // Even though we specify that we can replace the method, the
            // 'use children' flag prohibits that.
            msg.appendToSoapEnvelope(destEnvelope, true);
            
            assertNodesEqual(expectedEnvelope, destEnvelope, true);
        }
        finally {
            msg.clear();
        }
    }
}
