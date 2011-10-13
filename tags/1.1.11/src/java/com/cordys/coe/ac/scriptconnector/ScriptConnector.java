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
package com.cordys.coe.ac.scriptconnector;

import com.cordys.coe.ac.scriptconnector.aclib.INomConnector;
import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.aclib.NomConnectorImpl;
import com.cordys.coe.ac.scriptconnector.config.ScriptConnectorConfiguration;
import com.cordys.coe.ac.scriptconnector.config.SoapMethodInfo;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;
import com.cordys.coe.ac.scriptconnector.exception.SoapFaultWrapException;
import com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage;
import com.cordys.coe.util.soap.SoapFaultInfo;

import com.eibus.connector.nom.Connector;

import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.io.File;

import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An application connector that can run javascripts.
 */
public class ScriptConnector extends ApplicationConnector
{
    /**
     * Identifies the Logger.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(ScriptConnector.class);
    /**
     * Holds the name of the connector.
     */
    private static final String CONNECTOR_NAME = "ScriptConnector Connector";
    /**
     * Holds the configuration object for this connector.
     */
    protected ScriptConnectorConfiguration acConfiguration;
    /**
     * NOM document for parsing the XML.
     */
    protected Document dDoc = new Document();
    /**
     * Holds the connector to use for sending messages to Cordys.
     */
    protected INomConnector nomConnector;
    /**
     * Method information parsing is locked using this object.
     */
    private Object methodInfoParseMutex = new Object();
    /**
     * Contains cached infomation for each called SOAP method.
     */
    private ConcurrentMap<String, SoapMethodInfo> soapMethodMap = new ConcurrentHashMap<String, SoapMethodInfo>();
    /**
     * Timer object for executing delayed tasks.
     */
    private Timer timer = new Timer();

    /**
     * This method creates the transaction that will handle the requests.
     *
     * @param   stTransaction  The SOAP-transaction containing the message.
     *
     * @return  The newly created transaction.
     */
    @Override
    public ApplicationTransaction createTransaction(SOAPTransaction stTransaction)
    {
        return new ScriptConnectorTransaction(this);
    }

    /**
     * This method gets called when the processor is started. It reads the configuration of the
     * processor and creates the connector with the proper parameters. It will also create a client
     * connection to Cordys.
     *
     * @param  pProcessor  The processor that is started.
     */
    @Override
    public void open(Processor pProcessor)
    {
        try
        {
            File installationFolder = getCordysInstallationFolder();

            if (installationFolder == null)
            {
                throw new ScriptConnectorException("Unable to determine the Cordys installation folder.");
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Connector installation folder is: " + installationFolder);
            }

            // Get the configuration
            acConfiguration = new ScriptConnectorConfiguration(this, getConfiguration(),
                                                               installationFolder);

            // Open the client connector
            Connector conn = Connector.getInstance(CONNECTOR_NAME);

            if (!conn.isOpen())
            {
                conn.open();
            }

            nomConnector = new NomConnectorImpl(conn);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("ScriptConnector started.");
            }
        }
        catch (Exception e)
        {
            LOG.error(e, LogMessages.CONNECTOR_INITIALIZATION_FAILED);

            throw new IllegalStateException("Connector initialization failed.", e);
        }
    }

    /**
     * This method gets called when the processor is ordered to rest.
     *
     * @param  processor  The processor that is to be in reset state
     */
    @Override
    public void reset(Processor processor)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Processor reset.");
        }

        // Clear the SOAP method information cache.
        soapMethodMap.clear();
    }

    /**
     * Adds a task to be scheduled.
     *
     * @param  task   Task to be scheduled.
     * @param  delay  Time after the task is executed. This is in milliseconds.
     */
    public void scheduleTasks(TimerTask task, long delay)
    {
        timer.schedule(task, delay);
    }

    /**
     * Sends a SOAP request. This method does not receive for a response.
     *
     * @param   msg  SOAP request to be sent.
     *
     * @throws  ScriptConnectorException
     */
    public void sendSoapRequest(ScriptSoapMessage msg)
                         throws ScriptConnectorException
    {
        int requestEnvNode = 0;

        try
        {
            requestEnvNode = createSoapRequest(msg);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Sending an asynchronous SOAP request: " +
                          Node.writeToString(requestEnvNode, true));
            }

            nomConnector.send(requestEnvNode);
        }
        catch (ScriptConnectorException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("SOAP request failed.", e);
        }
        finally
        {
            if (requestEnvNode != 0)
            {
                Node.delete(requestEnvNode);
                requestEnvNode = 0;
            }
        }
    }

    /**
     * Sends a SOAP request and returns a response. This checks for SOAP:Fault.
     *
     * @param   msg  SOAP request to be sent.
     *
     * @return  Received SOAP message. Caller is responsible for deleting this.
     *
     * @throws  ScriptConnectorException
     */
    public ScriptSoapMessage sendSoapRequestAndWait(ScriptSoapMessage msg)
                                             throws ScriptConnectorException
    {
        return sendSoapRequestAndWait(msg, true);
    }

    /**
     * Sends a SOAP request and returns a response.
     *
     * @param   msg             SOAP request to be sent.
     * @param   checkSoapFault  If <code>true</code>, an exception is thrown when SOAP:Fault is
     *                          received.
     *
     * @return  Received SOAP message. Caller is responsible for deleting this.
     *
     * @throws  ScriptConnectorException
     */
    public ScriptSoapMessage sendSoapRequestAndWait(ScriptSoapMessage msg, boolean checkSoapFault)
                                             throws ScriptConnectorException
    {
        int requestEnvNode = 0;
        int responseEnvNode = 0;

        try
        {
            requestEnvNode = createSoapRequest(msg);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Sending a SOAP request: " + Node.writeToString(requestEnvNode, true));
            }

            responseEnvNode = nomConnector.sendAndWait(requestEnvNode,
                                                       acConfiguration.getSoapRequestTimeout(),
                                                       false);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Received a SOAP response: " + Node.writeToString(responseEnvNode, true));
            }

            if (checkSoapFault)
            {
                SoapFaultInfo faultInfo = SoapFaultInfo.findSoapFault(responseEnvNode);

                if (faultInfo != null)
                {
                    String faultCode = faultInfo.getFaultcode();
                    String faultString = faultInfo.getFaultstring();
                    String faultActor = faultInfo.getFaultactor();
                    String faultStr = null;
                    int faultNode = SoapFaultInfo.findSoapFaultNode(responseEnvNode);

                    if (faultNode != 0)
                    {
                        // Define the SOAP namespace.
                        Node.setNSDefinition(faultNode, Node.getPrefix(faultNode),
                                             Node.getNamespaceURI(faultNode));
                        faultStr = Node.writeToString(faultNode, false);
                    }

                    // Delete the response (including the fault XML).
                    Node.delete(responseEnvNode);
                    responseEnvNode = 0;

                    throw new SoapFaultWrapException(faultCode, faultActor, faultString, faultStr);
                }
            }

            ScriptSoapMessage res = new ScriptSoapMessage(dDoc);

            res.readFromSoapMessage(responseEnvNode);

            return res;
        }
        catch (SoapFaultWrapException e)
        {
            throw e;
        }
        catch (ScriptConnectorException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("SOAP request failed.", e);
        }
        finally
        {
            if (responseEnvNode != 0)
            {
                Node.delete(responseEnvNode);
                responseEnvNode = 0;
            }

            if (requestEnvNode != 0)
            {
                Node.delete(requestEnvNode);
                requestEnvNode = 0;
            }
        }
    }

    /**
     * Returns the Cordys installation folder.
     *
     * @return
     */
    public File getCordysInstallationFolder()
    {
        String sCordysDir = EIBProperties.getInstallDir();
        File fCordysDir;

        if (sCordysDir != null)
        {
            fCordysDir = new File(sCordysDir);
        }
        else
        {
            return null;
        }
        return fCordysDir;
    }

    /**
     * Returns the shared NOM document.
     *
     * @return  The shared NOM document.
     */
    public Document getDocument()
    {
        return dDoc;
    }

    /**
     * Returns the DN of the organization where this connector is running in.
     *
     * @return  Organization DN.
     */
    public String getOrganizationDn()
    {
        return getProcessor().getOrganization();
    }

    /**
     * Returns the configuration object.
     *
     * @return  returns the configuration object.
     */
    public ScriptConnectorConfiguration getScriptConfig()
    {
        return acConfiguration;
    }

    /**
     * Returns cached SOAP method information for the given SOAP method.
     *
     * @param   requestContext  Current SOAP request.
     *
     * @return  SOAP method information or <code>null</code> if no information could be found.
     */
    public SoapMethodInfo getSoapMethodInfo(ISoapRequestContext requestContext)
    {
        String methodDn = requestContext.getMethodDefinition().getMethodDN();

        if (methodDn == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("No method DN found from the method definition.");
            }
            return null;
        }

        SoapMethodInfo methodInfo = soapMethodMap.get(methodDn);

        if (methodInfo != null)
        {
            return methodInfo;
        }

        // Parse the method information.
        synchronized (methodInfoParseMutex)
        {
            // Try to fetch it again, if someone else has already parsed it.
            methodInfo = soapMethodMap.get(methodDn);

            if (methodInfo != null)
            {
                return methodInfo;
            }

            methodInfo = new SoapMethodInfo(this, requestContext);
            soapMethodMap.put(methodDn, methodInfo);
        }

        return methodInfo;
    }

    /**
     * Creates a SOAP request envelope for the passed SOAP method.
     *
     * @param   msg  SOAP message object containing the SOAP method.
     *
     * @return  SOAP request envelope for the passed SOAP method.
     *
     * @throws  ScriptConnectorException
     */
    private int createSoapRequest(ScriptSoapMessage msg)
                           throws ScriptConnectorException
    {
        String userDn = msg.getUserDn();
        String orgDn = msg.getOrgDn();

        if ((orgDn == null) || (orgDn.length() == 0))
        {
            orgDn = getOrganizationDn();
        }

        int requestEnvNode = 0;

        try
        {
            String methodName = msg.getMethodName();
            String namespace = msg.getNamespace();

            // Create the SOAP request, add our method next to the created method and
            // delete the created method.
            requestEnvNode = Node.getRoot(nomConnector.createSoapMethod(orgDn, userDn, methodName,
                                                                        namespace));
            msg.appendToSoapEnvelope(requestEnvNode, true);

            return requestEnvNode;
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("SOAP request failed.", e);
        }
    }
}
