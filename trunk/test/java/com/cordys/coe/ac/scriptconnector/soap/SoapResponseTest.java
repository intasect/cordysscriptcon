/*
 *   Copyright 2004 Cordys R&D B.V. 
 *
 *   This file is part of the Cordys Script Connector. 
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.cordys.coe.ac.scriptconnector.soap;

import com.cordys.coe.ac.scriptconnector.ScriptConnectorTestCase;
import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.aclib.NomConnectorStub;
import com.cordys.coe.ac.scriptconnector.aclib.SoapMethodDefinitionStub;
import com.cordys.coe.util.FileUtils;

import com.eibus.xml.nom.Node;

import java.text.MessageFormat;

/**
 * Test cases for connector SOAP response manipulation.
 *
 * @author  mpoyhone
 */
public class SoapResponseTest extends ScriptConnectorTestCase
{
    /**
     * Tests that the response element uses the [RequestName]Response format.
     *
     * @throws  Exception
     */
    public void testResponse_NoModificationForResponseElement()
                                                       throws Exception
    {
        testResponseElementModification("SiebelAccountInsert_InputResponse", "http://siebel.com/asi/",
                                        null, null, null);
    }

    /**
     * Tests that an empty SOAP:Body is returned by then connector if an external SOAP method didn't
     * return a SOAP method (i.e. also had an empty body).
     *
     * @throws  Exception
     */
    public void testResponse_noSoapMethod()
                                   throws Exception
    {
        String script = "var req = <ExternalCall xmlns='xxx' />;\n" +
                        "var reqMsg = Cordys.createSoapMessage();\n" +
                        "var resMsg;\n" +
                        "reqMsg.setMethodAsString(req.toXMLString(), false);\n" +
                        "resMsg = Cordys.sendSoapRequestMessage(reqMsg);\n" +
                        "Cordys.setResponseMessage(resMsg);\n";
        String requestXml = "<Test xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\" />";
        int connectorResponse = parse("<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" +
                                      "    <SOAP:Header/>\r\n" +
                                      "    <SOAP:Body />\r\n" +
                                      "</SOAP:Envelope>");
        int actualResponse;
        int actualConnectorRequest;
        NomConnectorStub.WaitThread responseWaiter;

        nomConnector = new NomConnectorStub(dDoc);
        responseWaiter = nomConnector.startWaitThread(connectorResponse);
        actualResponse = executeScriptMethodReturnEnvelope(requestXml, script);
        actualConnectorRequest = responseWaiter.getRequestNode();
        addNomGarbage(Node.getRoot(actualConnectorRequest));

        assertNodesEqual(connectorResponse, actualResponse, true);
    }

    /**
     * Test case for Cordys.getResponseMessage().removeChildElementNamespaces() method.
     *
     * @throws  Exception
     */
    public void testResponse_removeChildElementNamespaces()
                                                   throws Exception
    {
        String script = "var res = <result xmlns='xxx'><data xmlns='yyy'>data value</data></result>;\n" +
                        "Cordys.addResponseElement(res);\n" +
                        "Cordys.getResponseMessage().removeChildElementNamespaces(null);\n";
        String requestXml = "<Test xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\" />";
        String responseXml = "<TestResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">" +
                             "    <result><data>data value</data></result>" +
                             "</TestResponse>";
        int expectedResponse = parse(responseXml);
        int actualResponse;

        actualResponse = executeScriptMethod(requestXml, script);

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
    }

    /**
     * Tests that the response element name is correctly read from the WSDL.
     *
     * @throws  Exception
     */
    public void testResponse_ResponseElementFromWSDL()
                                              throws Exception
    {
        String wsdl = FileUtils.readTextResourceContents("SiebelAccount.wsdl",
                                                         SoapResponseTest.class);

        testResponseElementModification("SiebelAccountInsert_Output", "http://siebel.com/asi/",
                wsdl, null, null);
    }

    /**
     * Tests that the response element name is correctly set by Cordys.setResponseMethodName().
     *
     * @throws  Exception
     */
    public void testResponse_ResponseElementViaMethod()
                                               throws Exception
    {
        testResponseElementModification("Test_Reply", "http://siebel.com/asi/", null, "Test_Reply",
                                        null);
    }
    

    /**
     * Tests that the response element namespace is correctly set by
     * Cordys.setResponseMethodNamespace().
     *
     * @throws  Exception
     */
    public void testResponse_ResponseElementNamespaceViaMethod()
                                                    throws Exception
    {
        testResponseElementModification("SiebelAccountInsert_InputResponse", "xxx", null, null,
                                        "xxx");
    }
    
    /**
     * Tests that the response element name namespace is correctly set by
     * Cordys object methods even when the WSDL is available.
     *
     * @throws  Exception
     */
    public void testResponse_ResponseElementWsdlOverrideViaMethod()
                                                    throws Exception
    {
        String wsdl = FileUtils.readTextResourceContents("SiebelAccount.wsdl",
                SoapResponseTest.class);
        
        testResponseElementModification("MyResponse", "xxx", wsdl, "MyResponse",
                                        "xxx");
    }

    /**
     * Executes a test for the SOAP response method manipulation.
     * @param   expectedElemName   Expected response element name.
     * @param   expectedNamespace  Expected response namespace.
     * @param   wsdl               WSDL to be used or <code>null</code>.
     * @param   setElemName        Response element name is set to this. <code>null</code> if not
     *                             needed.
     * @param   setNamespace       Response namespace is set to this. <code>null</code> if not
     *                             needed.
     *
     * @throws  Exception
     */
    private final void testResponseElementModification(String expectedElemName,
                                                       String expectedNamespace,
                                                       String wsdl, String setElemName,
                                                       String setNamespace)
                                                throws Exception
    {
        String script = "Cordys.addResponseElement(<Test>abc</Test>.toXMLString());\n" +
                        "Cordys.addResponseElement(<StatusObject>OK</StatusObject>.toXMLString());\n";

        if (setElemName != null)
        {
            script += "Cordys.setResponseMethodName(\"" + setElemName + "\");\n";
        }

        if (setNamespace != null)
        {
            script += "Cordys.setResponseMethodNamespace(\"" + setNamespace + "\");\n";
        }

        String requestXml = "<SiebelAccountInsert_Input xmlns=\"http://siebel.com/asi/\" />";
        String responseXml = MessageFormat.format("<{0} xmlns=\"{1}\">" +
                                                  "    <Test>abc</Test>" +
                                                  "    <StatusObject>OK</StatusObject>" +
                                                  "</{0}>", expectedElemName, expectedNamespace);
        int expectedResponse = parse(responseXml);
        int actualResponse;
        ISoapRequestContext requestContext = createSoapRequest(requestXml);

        if (wsdl != null)
        {
            ((SoapMethodDefinitionStub) requestContext.getMethodDefinition()).setMethodWsdl(wsdl);
        }

        executeScriptMethod(requestContext, script);
        actualResponse = requestContext.getResponseMethodNode();

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
    }
}
