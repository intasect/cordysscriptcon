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
