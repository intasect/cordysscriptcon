/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.processor;

import com.cordys.coe.ac.scriptconnector.ScriptConnector;
import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;
import com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript;

import java.util.Map;

/**
 * Interface for a pre-processor which gets called before the script is executed.
 *
 * @author  mpoyhone
 */
public interface IScriptPreProcessor
{
    /**
     * Called before the script is executed. The pre-processor can modify the SOAP request and
     * response. It can also create the SOAP response and return <code>false</code> in which case
     * the script is not executed.
     *
     * @param   connector       Script Connector instance.
     * @param   script          Configured script object.
     * @param   requestContext  SOAP request
     *
     * @return  <code>true</code> if the script can be executed, <code>false</code> if the SOAP
     *          transaction should return without executing the scritp.
     *
     * @throws  ScriptConnectorException
     */
    boolean execute(ScriptConnector connector, ConfiguredScript script,
                    ISoapRequestContext requestContext)
             throws ScriptConnectorException;

    /**
     * Initializes the pre-processor.
     *
     * @param   connector  Script Connector instance.
     * @param   params     A map containing the parameters. These are read from the configuration
     *                     file.
     *
     * @return  <code>true</code> if this pre-processor should be enabled.
     *
     * @throws  ScriptConnectorException  Thrown if the initialization failed.
     */
    boolean initialize(ScriptConnector connector, Map<String, String> params)
                throws ScriptConnectorException;
}
