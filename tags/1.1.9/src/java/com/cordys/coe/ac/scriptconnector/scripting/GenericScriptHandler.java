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

import com.cordys.coe.ac.scriptconnector.ScriptConnectorTransaction;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;
import com.cordys.coe.util.FileUtils;

import com.eibus.util.logger.CordysLogger;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

/**
 * Script handler for Java scripting API scripts. This handler will read the script from file
 * system.
 *
 * @author  mpoyhone
 */
public class GenericScriptHandler
    implements IScriptHandler
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(GenericScriptHandler.class);
    /**
     * Contains a shared script engine manager instance for loading scripts.
     */
    private static ScriptEngineManager scriptManager = new ScriptEngineManager();

    /**
     * @see  com.cordys.coe.ac.scriptconnector.scripting.IScriptHandler#executeScript(com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript,
     *       com.eibus.soap.BodyBlock, com.eibus.soap.BodyBlock, com.eibus.soap.SOAPTransaction)
     */
    public void executeScript(ConfiguredScript script, BridgeObject bridgeObject,
                              ScriptConnectorTransaction transaction)
                       throws Exception
    {
        ConfiguredScript.GenericScript genericScript = (ConfiguredScript.GenericScript) script;

        synchronized (genericScript)
        {
            if (!genericScript.isLoaded())
            {
                loadScript(genericScript);
            }
        }

        SimpleScriptContext ctx = new SimpleScriptContext();
        StringWriter out = new StringWriter(1024);

        ctx.setWriter(out);

        ScriptEngine engine = scriptManager.getEngineByExtension(genericScript.getScriptType());

        if (engine == null)
        {
            throw new ScriptConnectorException("No scripting engine found with type: " +
                                               genericScript.getScriptType());
        }

        Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);

        bindings.put("Cordys", bridgeObject);
        bindings.put("LOG", LOG);

        Object result = engine.eval(genericScript.scriptText, ctx);

        if (!bridgeObject.isResponseAdded())
        {
            // Script didn't use the Cordys object method to set the response XML,
            // so try to fetch it from the eval method result or from
            // the script output writer.
            String responseXml = null;

            if (result != null)
            {
                responseXml = result.toString();
            }
            else
            {
                StringBuffer sb = out.getBuffer();

                if (sb.length() > 0)
                {
                    responseXml = sb.toString();
                }
            }

            if (responseXml != null)
            {
                bridgeObject.addResponseFromChildren(responseXml);
            }
        }
    }

    /**
     * Loads the script if it has not yet been loaded. Calls to this method must be synchronized
     * over the script object.
     *
     * @param   script  Script object to be loaded.
     *
     * @throws  IOException  Thrown if the script file could not be read.
     */
    private void loadScript(ConfiguredScript.GenericScript script)
                     throws IOException
    {
        Reader r = null;

        try
        {
            r = new FileReader(script.getTimestamp().getFile());
            script.scriptText = FileUtils.readReaderContents(r);
        }
        finally
        {
            FileUtils.closeReader(r);
        }
    }
}
