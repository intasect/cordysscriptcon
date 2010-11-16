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
