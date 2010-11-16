/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.scripting;

/**
 * Test cases for Javascript with E4X extension.
 *
 * @author  mpoyhone
 */
public class E4XHandlerTest extends ScriptHandlerTestCase
{
    /**
     * Test XML for namespaces.
     */
    private static final String TEST_XML = "<data xmlns=\"sample-data\">\r\n" +
                                           "    <level1 xmlns=\"sample-level1\">\r\n" +
                                           "        <ns:level2 xmlns:ns=\"sample-level2\">\r\n" +
                                           "            <ns:field1>FIELD_1 #1</ns:field1>\r\n" +
                                           "            <ns:field1>FIELD_1 #2</ns:field1>\r\n" +
                                           "        </ns:level2>\r\n" +
                                           "    </level1>\r\n" +
                                           "</data>";
    /**
     * Expected response XML.
     */
    private static final String TEST_RESPONSE = "<field1>FIELD_1 #1</field1><field1>FIELD_1 #2</field1>";

    /**
     * Tests a script where output is set by Cordys.addResponseElement() method.
     *
     * @throws  Exception
     */
    public void testJavascriptE4X_addResponseElement()
                                              throws Exception
    {
        String script = "var res =\n" +
                        "   <result xmlns='xxx'>\n" +
                        "       <data>data value</data>\n" +
                        "   </result>;\n" +
                        "Cordys.addResponseElement(res.toXMLString());\n";

        executeStaticTest(script, "js");
    }

    /**
     * Tests a script with XML namespaces. This one uses the *::elemname notation.
     *
     * @throws  Exception
     */
    public void testJavascriptE4X_NoNamespaceDefinition()
                                                 throws Exception
    {
        String script = "var data = " + TEST_XML + ";\n" +
                        "for each (var n in data.*::level1.*::level2.*) {\n" +
                        "   Cordys.addResponseElement(n.toXMLString());\n" +
                        "}\n" +
                        "Cordys.getResponseMessage().removeChildElementNamespaces(null);\n";

        executeTest(script, "js", TEST_RESPONSE);
    }

    /**
     * Tests a script where output is set by returning the XML.
     *
     * @throws  Exception
     */
    public void testJavascriptE4X_return()
                                  throws Exception
    {
        String script = "var res =\n" +
                        "   <root>\n" +
                        "       <result xmlns='xxx'>\n" +
                        "           <data>data value</data>\n" +
                        "       </result>\n" +
                        "   </root>;\n" +
                        "res.toXMLString();\n";

        executeStaticTest(script, "js");
    }

    /**
     * Tests a script with XML namespaces. This one uses explicitly defined namespaces.
     *
     * @throws  Exception
     */
    public void testJavascriptE4X_WithNamespaceDefinitions()
                                                    throws Exception
    {
        String script = "var data = " + TEST_XML + ";\n" +
                        "var ns1 = new Namespace('sample-level1');\n" +
                        "var ns2 = new Namespace('sample-level2');\n" +
                        "default xml namespace = ns2;\n" +
                        "for each (var n in data.ns1::level1.level2.*) {\n" +
                        "   Cordys.addResponseElement(n.toXMLString());\n" +
                        "}\n" +
                        "Cordys.getResponseMessage().removeChildElementNamespaces(null);\n";

        executeTest(script, "js", TEST_RESPONSE);
    }
    
    /**
     * Tests a script with XML namespaces. This one accesses children with methods.
     *
     * @throws  Exception
     */
    public void testJavascriptE4X_WithChildMethods()
                                                    throws Exception
    {
        String script = "var data = " + TEST_XML + ";\n" +
                        "var ns1 = new Namespace('sample-level1');\n" +
                        "var ns2 = new Namespace('sample-level2');\n" +
                        "default xml namespace = ns2;\n" +
                        "for each (var n in data.ns1::level1.level2.*) {\n" +
                        "   Cordys.addResponseElement(n.toXMLString());\n" +
                        "}\n" +
                        "Cordys.getResponseMessage().removeChildElementNamespaces(null);\n";

        executeTest(script, "js", TEST_RESPONSE);
    }
    
    /**
     * Test XML with {DATA} in it.
     *
     * @throws  Exception
     */
    public void testJavascriptE4X_XMLWithBrackets()
                                                    throws Exception
    {
        String script = "var res =\n" +
                        "   <root>\n" +
                        "       <MESSAGE_ID>{'{CF1D101B-C5DA-407F-8B1C-82D3A44BD265}'}</MESSAGE_ID>\n" +
                        "   </root>;\n" +
                        "res.toXMLString();\n";

        executeTest(script, "js", "<MESSAGE_ID>{CF1D101B-C5DA-407F-8B1C-82D3A44BD265}</MESSAGE_ID>");
    }
}
