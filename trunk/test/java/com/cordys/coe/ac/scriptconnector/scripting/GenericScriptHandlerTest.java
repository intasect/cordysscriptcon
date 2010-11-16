/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.scripting;


/**
 * Test cases for non-Javascript scripts.
 *
 * @author mpoyhone
 */
public class GenericScriptHandlerTest extends ScriptHandlerTestCase
{
    /**
     * Tests a Groovy script where output is set by Cordys.addResponseElement() method.
     */
    public void testGeneric_Groovy_addResponseElement() throws Exception
    {
        String script = "def res = \"\"\"\n" +
        		        "   <result xmlns='xxx'>\n" +
        		        "       <data>${Cordys.getRequestUserDN()}</data>\n" +
        		        "   </result>\n" +
        		        "\"\"\";\n" +
        		        "Cordys.addResponseElement(res);\n";
        
        executeDynamicTest(script, "groovy");
    }
    
    /**
     * Tests a Groovy script where output is set by printing the XML.
     */
    public void testGeneric_Groovy_print() throws Exception
    {
        String script = "def res = \"\"\"\n" +
                        "   <root>\n" +
                        "       <result xmlns='xxx'>\n" +
                        "           <data>${Cordys.getRequestUserDN()}</data>\n" +
                        "       </result>\n" +
                        "   </root>\n" +
                        "\"\"\";\n" +
                        "print res;\n";
        
        executeDynamicTest(script, "groovy");
    }
    
    /**
     * Tests a Groovy script where output is set by returning the XML.
     */
    public void testGeneric_Groovy_return() throws Exception
    {
        String script = "def res = \"\"\"\n" +
                        "   <root>\n" +
                        "       <result xmlns='xxx'>\n" +
                        "           <data>${Cordys.getRequestUserDN()}</data>\n" +
                        "       </result>\n" +
                        "   </root>\n" +
                        "\"\"\";\n" +
                        "res;\n";
        
        executeDynamicTest(script, "groovy");
    }
    
    /**
     * Tests a Freemarker script.
     */
    public void testGeneric_Freemarker() throws Exception
    {
        String script = "<root>\n" +
        		        "   <result xmlns='xxx'>\n" +
                        "      <data>${Cordys.getRequestUserDN()}</data>\n" +
                        "   </result>\n" +
                        "</root>\n";
        
        executeDynamicTest(script, "fm");
    }
}
