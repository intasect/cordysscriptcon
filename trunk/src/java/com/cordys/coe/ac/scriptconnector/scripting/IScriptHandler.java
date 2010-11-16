/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.scripting;

import com.cordys.coe.ac.scriptconnector.ScriptConnectorTransaction;

/**
 * Interface for executing a script for a SOAP method.
 *
 * @author  mpoyhone
 */
public interface IScriptHandler
{
    /**
     * Executes the given script.
     *
     * @param   script        Script object.
     * @param   bridgeObject  Script bridge object.
     * @param   transaction   Current SOAP transaction.
     *
     * @throws  Exception
     */
    void executeScript(ConfiguredScript script, BridgeObject bridgeObject,
                       ScriptConnectorTransaction transaction)
                throws Exception;
}
