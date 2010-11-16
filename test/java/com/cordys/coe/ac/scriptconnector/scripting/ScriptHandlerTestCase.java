/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.scripting;

import com.cordys.coe.ac.scriptconnector.ScriptConnectorTestCase;

import java.io.File;

/**
 * Base class for script handler test cases.
 *
 * @author  mpoyhone
 */
public abstract class ScriptHandlerTestCase extends ScriptConnectorTestCase
{
    /**
     * Executes the given script static content ("data value").
     *
     * @param   script     Script code.
     * @param   extension  Script extension.
     *
     * @throws  Exception
     */
    protected void executeStaticTest(String script, String extension)
                        throws Exception
    {
        executeTest(script, extension, "    <result xmlns='xxx'><data>data value</data></result>");
    }
    
    /**
     * Executes the given script with dynamic content (from Cordys.getRequestUserDN()).
     *
     * @param   script     Script code.
     * @param   extension  Script extension.
     *
     * @throws  Exception
     */
    protected void executeDynamicTest(String script, String extension)
                        throws Exception
    {
        executeTest(script, extension, "    <result xmlns='xxx'><data>dummy-user-dn</data></result>");
    }

    /**
     * Executes the given script.
     *
     * @param   script                  Script code.
     * @param   extension               Script extension.
     * @param   expectedMethodContents  SOAP response method contents.
     *
     * @throws  Exception  Thrown if the script failed.
     */
    protected void executeTest(String script, String extension, String expectedMethodContents)
                        throws Exception
    {
        File scriptFile = createTextFile("Test." + extension, script);

        String requestXml = "<Test xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\" />";
        String responseXml = "<TestResponse xmlns=\"http://schemas.cordys.com/1.0/coe/ScriptConnector\">" +
                             expectedMethodContents + "</TestResponse>";
        int expectedResponse = parse(responseXml);
        int actualResponse;

        actualResponse = executeScriptMethod(requestXml, scriptFile);

        assertNodesEqual(expectedResponse, getSoapMethod(actualResponse), true);
    }
}
