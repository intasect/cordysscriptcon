/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.samples;

import com.cordys.coe.ac.scriptconnector.ScriptConnectorTestCase;
import com.cordys.coe.ac.scriptconnector.aclib.NomConnectorStub;

import com.eibus.xml.nom.Node;

import java.io.File;

import java.text.MessageFormat;

/**
 * Test cases for the provided sample scripts.
 *
 * @author  mpoyhone
 */
public class SamplesTest extends ScriptConnectorTestCase
{
    /**
     * Test case for sample script AsyncSoapCall.js.
     *
     * @throws  Exception
     */
    public void testSample_AsyncSoapCall()
                                  throws Exception
    {
        // Use a shorter wait time.
        File scriptFile = new File("src/samples/AsyncSoapCall.js");
        String script = readTextFile(scriptFile).replaceFirst("10000", "10");

        // Create the request and response templates.
        String requestXml = "<AsyncSoapCall xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\" />";
        int expectedResponse = parse("<AsyncSoapCallResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\" />");
        int expectedConnectorRequest = parse("<UpdateXMLObject\r\n" +
                                             "    xmlns=\"http://schemas.cordys.com/1.0/xmlstore\">\r\n" +
                                             "    <tuple\r\n" +
                                             "        isFolder=\"false\"\r\n" +
                                             "        key=\"/Cordys/ScriptConnector/Test/AsyncSoapCallOutput\"\r\n" +
                                             "        unconditional=\"true\"\r\n" +
                                             "        version=\"organization\">\r\n" +
                                             "        <new>\r\n" +
                                             "            <delaytest>\r\n" +
                                             "                <data>Async Call Works</data>\r\n" +
                                             "            </delaytest>\r\n" +
                                             "        </new>\r\n" +
                                             "    </tuple>\r\n" +
                                             "</UpdateXMLObject>");
        int actualResponse;
        int actualConnectorRequest;

        nomConnector = new NomConnectorStub(dDoc);
        actualResponse = executeScriptMethod(requestXml, script);
        actualConnectorRequest = nomConnector.waitForRequest(0);
        addNomGarbage(Node.getRoot(actualConnectorRequest));

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
        assertNodesEqual(expectedConnectorRequest, getSoapMethod(actualConnectorRequest), true);
    }

    /**
     * Test case for sample script EchoMethod.js.
     *
     * @throws  Exception
     */
    public void testSample_EchoMethod()
                               throws Exception
    {
        File scriptFile = new File("src/samples/EchoMethod.js");
        String requestXml = "    <EchoMethod xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                            "        <test>something</test>\r\n" +
                            "    </EchoMethod>";
        int expectedResponse = parse("  <EchoMethodResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                     "    <Success>\r\n" +
                                     "      <Message>Hello dummy-user-dn</Message>\r\n" +
                                     "      <OriginalRequest>\r\n" +
                                     "        <EchoMethod xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                     "          <test>something</test>\r\n" +
                                     "        </EchoMethod>\r\n" +
                                     "      </OriginalRequest>\r\n" +
                                     "    </Success>\r\n" +
                                     "  </EchoMethodResponse>");
        int actualResponse;

        actualResponse = executeScriptMethod(requestXml, scriptFile);

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
    }

    /**
     * Test case for sample script ListFiles.js.
     *
     * @throws  Exception
     */
    public void testSample_ListFiles()
                              throws Exception
    {
        File scriptFile = new File("src/samples/ListFiles.js");
        String requestXml = MessageFormat.format("<ListFiles xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                                 "    <dir>{0}</dir>\r\n" +
                                                 "</ListFiles>", configFolder.getAbsolutePath());
        String responseXml = MessageFormat.format("<ListFilesResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                                  "    <files>\r\n" +
                                                  "      <file>{0}\\config.properties</file>\r\n" +
                                                  "      <file>{0}\\test.txt</file>\r\n" +
                                                  "    </files>\r\n" +
                                                  "  </ListFilesResponse>",
                                                  configFolder.getAbsolutePath());
        int expectedResponse = parse(responseXml);
        int actualResponse;

        createTextFile("test.txt", "dummy text file");

        actualResponse = executeScriptMethod(requestXml, scriptFile);

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
    }

    /**
     * Test case for sample script LoggingSample.js. This test cases does not check that the log
     * entry was actually written, only that the script is executed correctly.
     *
     * @throws  Exception
     */
    public void testSample_LoggingSample()
                                  throws Exception
    {
        File scriptFile = new File("src/samples/LoggingSample.js");
        String requestXml = "<LoggingSample xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\" />";
        String responseXml = "<LoggingSampleResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\" />";
        int expectedResponse = parse(responseXml);
        int actualResponse;

        actualResponse = executeScriptMethod(requestXml, scriptFile);

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
    }

    /**
     * Test case for sample script ReadFromXMLStore.js.
     *
     * @throws  Exception
     */
    public void testSample_ReadFromXMLStore()
                                     throws Exception
    {
        File scriptFile = new File("src/samples/ReadFromXMLStore.js");
        String requestXml = "    <ReadFromXMLStore xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                            "      <CITY>Putten</CITY>\r\n" +
                            "      <FIRST_NAME>Test</FIRST_NAME>\r\n" +
                            "      <LAST_NAME>Person</LAST_NAME>\r\n" +
                            "    </ReadFromXMLStore>";
        int expectedResponse = parse("  <ReadFromXMLStoreResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                     "    <result>\r\n" +
                                     "      <DATA xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                     "        <PERSON_DATA>\r\n" +
                                     "          <SSN>22-33-44-123</SSN>\r\n" +
                                     "        </PERSON_DATA>\r\n" +
                                     "      </DATA>\r\n" +
                                     "    </result>\r\n" +
                                     "  </ReadFromXMLStoreResponse>");
        int expectedConnectorRequest = parse("<GetXMLObject xmlns=\"http://schemas.cordys.com/1.0/xmlstore\">\r\n" +
                                             "   <key version=\"organization\">/scriptsample/Putten/Person/Test</key>\r\n" +
                                             "</GetXMLObject>\r\n");
        int connectorResponse = parse("<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" +
                                      "        <SOAP:Body>\r\n" +
                                      "                <GetXMLObjectResponse xmlns=\"http://schemas.cordys.com/1.0/xmlstore\">\r\n" +
                                      "                        <tuple xmlns=\"http://schemas.cordys.com/1.0/xmlstore\"\r\n" +
                                      "                                xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n" +
                                      "                                lastModified=\"1209992762432\"\r\n" +
                                      "                                key=\"/scriptsample/Putten/Person/Test\"\r\n" +
                                      "                                level=\"organization\"\r\n" +
                                      "                                name=\"Test\"\r\n" +
                                      "                                original=\"/scriptsample/Putten/Person/Test\">\r\n" +
                                      "                                <old>\r\n" +
                                      "                                        <DATA\r\n" +
                                      "                                                xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                      "                                                <PERSON_DATA>\r\n" +
                                      "                                                        <SSN>22-33-44-123</SSN>\r\n" +
                                      "                                                </PERSON_DATA>\r\n" +
                                      "                                        </DATA>\r\n" +
                                      "                                </old>\r\n" +
                                      "                        </tuple>\r\n" +
                                      "                </GetXMLObjectResponse>\r\n" +
                                      "        </SOAP:Body>\r\n" +
                                      "</SOAP:Envelope>");
        int actualResponse;
        int actualConnectorRequest;
        NomConnectorStub.WaitThread responseWaiter;

        nomConnector = new NomConnectorStub(dDoc);
        responseWaiter = nomConnector.startWaitThread(connectorResponse);
        actualResponse = executeScriptMethod(requestXml, scriptFile);
        actualConnectorRequest = responseWaiter.getRequestNode();
        addNomGarbage(Node.getRoot(actualConnectorRequest));

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
        assertNodesEqual(expectedConnectorRequest, getSoapMethod(actualConnectorRequest), true);
    }

    /**
     * Test case for sample script SoapCall.js.
     *
     * @throws  Exception
     */
    public void testSample_SoapCall()
                             throws Exception
    {
    }

    /**
     * Test case for sample script WriteToXMLStore.js.
     *
     * @throws  Exception
     */
    public void testSample_WriteToXMLStore()
                                    throws Exception
    {
        File scriptFile = new File("src/samples/WriteToXMLStore.js");
        String requestXml = "    <WriteToXMLStore xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                            "      <CITY>Putten</CITY>\r\n" +
                            "      <FIRST_NAME>Test</FIRST_NAME>\r\n" +
                            "      <LAST_NAME>Person</LAST_NAME>\r\n" +
                            "      <DATA>\r\n" +
                            "        <PERSON_DATA>\r\n" +
                            "          <SSN>22-33-44-123</SSN>\r\n" +
                            "        </PERSON_DATA>\r\n" +
                            "      </DATA>\r\n" +
                            "    </WriteToXMLStore>";
        int expectedResponse = parse("  <WriteToXMLStoreResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                     "    <result>OK</result>\r\n" +
                                     "  </WriteToXMLStoreResponse>");
        int expectedConnectorRequest = parse("<UpdateXMLObject\r\n" +
                                             "        xmlns=\"http://schemas.cordys.com/1.0/xmlstore\">\r\n" +
                                             "        <tuple\r\n" +
                                             "                isFolder=\"false\"\r\n" +
                                             "                key=\"/scriptsample/Putten/Person/Test\"\r\n" +
                                             "                unconditional=\"true\"\r\n" +
                                             "                version=\"organization\">\r\n" +
                                             "                <new>\r\n" +
                                             "                        <DATA\r\n" +
                                             "                                xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">\r\n" +
                                             "                                <PERSON_DATA>\r\n" +
                                             "                                        <SSN>22-33-44-123</SSN>\r\n" +
                                             "                                </PERSON_DATA>\r\n" +
                                             "                        </DATA>\r\n" +
                                             "                </new>\r\n" +
                                             "        </tuple>\r\n" +
                                             "</UpdateXMLObject>");
        int actualResponse;
        int actualConnectorRequest;
        NomConnectorStub.WaitThread responseWaiter;

        nomConnector = new NomConnectorStub(dDoc);
        responseWaiter = nomConnector.startWaitThread();
        actualResponse = executeScriptMethod(requestXml, scriptFile);
        actualConnectorRequest = responseWaiter.getRequestNode();
        addNomGarbage(Node.getRoot(actualConnectorRequest));

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
        assertNodesEqual(expectedConnectorRequest, getSoapMethod(actualConnectorRequest), true);
    }
}
