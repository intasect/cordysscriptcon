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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;

import com.cordys.coe.ac.scriptconnector.ScriptConnectorTransaction;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;
import com.cordys.coe.ac.scriptconnector.exception.ScriptFaultException;
import com.cordys.coe.ac.scriptconnector.exception.SoapFaultWrapException;
import com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript.E4XScript;
import com.eibus.util.logger.CordysLogger;

/**
 * Script handler for Javascript with E4X extension.
 *
 * @author  mpoyhone
 */
public class E4XHandler
    implements IScriptHandler
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(E4XHandler.class);

    /**
     * @see  com.cordys.coe.ac.scriptconnector.scripting.IScriptHandler#executeScript(com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript,
     *       com.eibus.soap.BodyBlock, com.eibus.soap.BodyBlock, com.eibus.soap.SOAPTransaction)
     */
    public void executeScript(ConfiguredScript script, BridgeObject bridgeObject,
                              ScriptConnectorTransaction transaction)
                       throws Exception
    {
        ConfiguredScript.E4XScript e4xScript = (E4XScript) script;
        Context cContext = ContextFactory.getGlobal().enterContext();

        try
        {
            cContext.setLanguageVersion(Context.VERSION_DEFAULT);

            synchronized (e4xScript)
            {
                if (!e4xScript.isLoaded())
                {
                    loadScript(cContext, e4xScript);
                }
            }

            // Create a local copy of the shared scope
            Scriptable scriptScope;

            scriptScope = cContext.newObject(e4xScript.sSharedScope);
            scriptScope.setPrototype(e4xScript.sSharedScope);
            scriptScope.setParentScope(null);

            // Initialize the java <-> javascript bridge object
            scriptScope.put("Cordys", scriptScope, bridgeObject);
            scriptScope.put("LOG", scriptScope, LOG);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Executing script: " + script.getScriptName());
            }

            // Execute the script.
            Object result;

            try
            {
                result = e4xScript.sScript.exec(cContext, scriptScope);
            }
            catch (org.mozilla.javascript.WrappedException e)
            {
                Throwable wrapped = e.getWrappedException();

                // Pass through exceptions thrown from Cordys.* methods.
                // This way SOAP faults look cleaner, especially for
                // methods that send SOAP requests.
                if (wrapped != null)
                {
                    if (wrapped instanceof SoapFaultWrapException)
                    {
                        throw (SoapFaultWrapException) wrapped;
                    }
                    if (wrapped instanceof ScriptFaultException)
                    {
                        throw (ScriptFaultException) wrapped;
                    }
                    else if (wrapped instanceof ScriptConnectorException)
                    {
                        throw (ScriptConnectorException) wrapped;
                    }
                    else
                    {
                        throw e;
                    }
                }
                else
                {
                    throw e;
                }
            }

            if (!bridgeObject.isResponseAdded())
            {
                // Script didn't use the Cordys object method to set the response XML,
                // so try to fetch it from the eval method result or from
                // the script output writer.
                String responseXml = null;

                if (result != null)
                {
                    String tmp = Context.toString(result);

                    if ((tmp != null) && (tmp.length() > 0) && !"undefined".equals(tmp))
                    {
                        responseXml = tmp;
                    }
                }

                if (responseXml != null)
                {
                    bridgeObject.addResponseFromChildren(responseXml);
                }
            }
        }
        catch (EcmaError e)
        {
            // Pass any known exceptions through,
            throw e;
        }
        catch (SoapFaultWrapException e)
        {
            // Pass the SOAP fault exception through
            throw e;
        }
        catch (ScriptFaultException e)
        {
            // Pass the SOAP fault exception through
            throw e;
        }
        catch (ScriptConnectorException e)
        {
            // Pass any known exceptions through,
            throw e;
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to execute script " +
                                               e4xScript.getScriptName(), e);
        }
        finally
        {
            Context.exit();
        }
    }

    /**
     * Loads the script if it has not yet been loaded. Calls to this method must be synchronized
     * over the script object.
     *
     * @param   context  Rhino context.
     * @param   script   Script object to be loaded.
     *
     * @throws  ScriptConnectorException
     */
    private void loadScript(Context context, ConfiguredScript.E4XScript script)
                     throws ScriptConnectorException
    {
        Reader rFileInput = null;
        File file = script.getTimestamp().getFile();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading script from file: " + file);
        }

        try
        {
            rFileInput = new FileReader(file);
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to open the script file " + file, e);
        }

        try
        {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed.
            script.sSharedScope = context.initStandardObjects(null, true);

            // Seal the script scope, so scripts cannot modify it anymore.
            // csScript.sSharedScope.sealObject();
            // Load the file and compile the script.
            script.sScript = context.compileReader(rFileInput, file.getAbsolutePath(), 0, null);
            script.setLoaded(true);
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to compile script " + file, e);
        }
        finally
        {
            if (rFileInput != null)
            {
                try
                {
                    rFileInput.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }
}
