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

import com.cordys.coe.ac.scriptconnector.ScriptConnector;
import com.cordys.coe.ac.scriptconnector.ScriptConnectorTransaction;
import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;
import com.cordys.coe.ac.scriptconnector.exception.ScriptFaultException;
import com.cordys.coe.ac.scriptconnector.methods.SavedSoapTransactions;
import com.cordys.coe.ac.scriptconnector.soap.DelayedSoapRequest;
import com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

/**
 * A class to hold methods that can be called from the script.
 *
 * @author  mpoyhone
 */
public class BridgeObject
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(BridgeObject.class);
    /**
     * Contains the name to be used in the SOAP response method. If not set, the standard name is
     * used.
     */
    protected String responseMethodName;
    /**
     * Contains the namespace to be used in the SOAP response method. If not set, the standard SOAP
     * request namespace is used.
     */
    protected String responseMethodNamespace;
    /**
     * Script connector instance.
     */
    protected ScriptConnector scConnector;
    /**
     * Application connector input SOAP request.
     */
    protected ScriptSoapMessage soapRequest = null;
    /**
     * SOAP request body block.
     */
    protected ISoapRequestContext soapRequestContext;
    /**
     * Application connector output SOAP response.
     */
    protected ScriptSoapMessage soapResponse = null;
    /**
     * Indicates if response XML has been added by the script.
     */
    private boolean responseAdded;
    /**
     * Current transaction.
     */
    private final ScriptConnectorTransaction scriptConnectorTransaction;

    /**
     * Constructor for BridgeObject.
     *
     * @param  scConnector                 Script connector instance.
     * @param  scriptConnectorTransaction  Current transaction.
     * @param  soapRequestContext          Contains the SOAP request body block.
     * @param  request                     Incoming SOAP request which can be accessed by the
     *                                     script.
     */
    public BridgeObject(ScriptConnector scConnector,
                        ScriptConnectorTransaction scriptConnectorTransaction,
                        ISoapRequestContext soapRequestContext, ScriptSoapMessage request)
    {
        this.scConnector = scConnector;
        this.scriptConnectorTransaction = scriptConnectorTransaction;
        this.soapRequestContext = soapRequestContext;
        this.soapRequest = request;
        this.soapResponse = this.scriptConnectorTransaction.createSoapMessage(true);
    }

    /**
     * Adds a SOAP response child element.
     *
     * @param   response  The response to add.
     *
     * @throws  ScriptConnectorException
     */
    public void addResponseElement(String response)
                            throws ScriptConnectorException
    {
        soapResponse.addMethodChildAsString(response, false);
        responseAdded = true;
    }

    /**
     * Adds SOAP response child elements from the child elements of this XML.
     *
     * @param   response  The response to add.
     *
     * @throws  ScriptConnectorException
     */
    public void addResponseFromChildren(String response)
                                 throws ScriptConnectorException
    {
        soapResponse.addMethodChildAsString(response, true);
        responseAdded = true;
    }

    /**
     * Creates a new Script SOAP message object this object is not added to the clean up list. The
     * returned object should only be used with the scheduleSoapRequest method.
     *
     * @return  New SOAP message object.
     */
    public ScriptSoapMessage createAsyncSoapMessage()
    {
        return scriptConnectorTransaction.createSoapMessage(false);
    }

    /**
     * Creates a new Script SOAP message object and adds it to the clean up list.
     *
     * @return  New SOAP message object.
     */
    public ScriptSoapMessage createSoapMessage()
    {
        return scriptConnectorTransaction.createSoapMessage(true);
    }

    /**
     * Marks the transaction to be saved.
     *
     * @param   id  Transaction ID.
     *
     * @throws  ScriptConnectorException
     */
    public void saveSoapTransaction(String id)
                             throws ScriptConnectorException
    {
        if ((id == null) || (id.length() == 0))
        {
            throw new ScriptConnectorException("Transaction ID is not set.");
        }

        if (soapRequest == null)
        {
            throw new IllegalStateException("SOAP request message is not set.");
        }

        scriptConnectorTransaction.setTransactionSaveId(id);

        SavedSoapTransactions.writeTransactionFile(scConnector, id, soapRequest.getSoapMethodRoot(),
                                                   true);
    }

    /**
     * Schedules a SOAP request to be sent to a web service. The request is sent after the given
     * delay. This method does not wait for a response.
     *
     * @param   sUserDN   The user DN to be used in the request.
     * @param   sRequest  The request to be sent.
     * @param   lDelay    Time after the request is sent. This is in milliseconds.
     *
     * @throws  ScriptConnectorException
     */
    public void scheduleSoapRequest(String sUserDN, String sRequest, long lDelay)
                             throws ScriptConnectorException
    {
    	String transactionSaveId = getTransactionSaveId();
    	scheduleSoapRequest(sUserDN, sRequest, lDelay, transactionSaveId, true, true);
    }
    
    /**
     * Schedules a SOAP request to be sent to a web service. The request is sent after the given
     * delay. This method does not wait for a response.
     *
     * @param   sUserDN   The user DN to be used in the request.
     * @param   sRequest  The request to be sent.
     * @param   lDelay    Time after the request is sent. This is in milliseconds.
     * @param	saveId 	The transaction save Id
     * @param 	saveRequest Request will be saved when set to true
     * @param 	saveResponse Response will be saved when set to true
     *
     * @throws  ScriptConnectorException
     */
    public void scheduleSoapRequest(String sUserDN, String sRequest, long lDelay, String saveId, boolean saveRequest, boolean saveResponse)
                             throws ScriptConnectorException
    {
        ScriptSoapMessage requestMsg = new ScriptSoapMessage(scConnector.getDocument());

        requestMsg.setUserDn(sUserDN);
        requestMsg.setMethodAsString(sRequest, false);

        scheduleSoapRequestMessage(requestMsg, lDelay, saveId, saveRequest, saveResponse);
    }
    
    /**
     * Schedules a SOAP request to be sent to a web service. The request is sent after the given
     * delay. This method does not wait for a response.
     *
     * @param   request  SOAP request.
     * @param   lDelay   Time after the request is sent. This is in milliseconds.
     *
     * @throws  ScriptConnectorException
     */
    public void scheduleSoapRequestMessage(ScriptSoapMessage request, long lDelay)
                                    throws ScriptConnectorException
    {
    	String transactionSaveId = getTransactionSaveId();
    	scheduleSoapRequestMessage(request, lDelay, transactionSaveId, true, true);
    }
    
    /**
     * Schedules a SOAP request to be sent to a web service. The request is sent after the given
     * delay. This method does not wait for a response.
     *
     * @param   request  SOAP request.
     * @param   lDelay   Time after the request is sent. This is in milliseconds.
     * @param	saveId 		The transaction save Id
     * @param 	saveRequest Request will be saved when set to true
     * @param 	saveResponse Response will be saved when set to true
     * @throws  ScriptConnectorException
     */
    public void scheduleSoapRequestMessage(ScriptSoapMessage request, long lDelay, String saveId, boolean saveRequest, boolean saveResponse)
                                    throws ScriptConnectorException
    {
        DelayedSoapRequest req = new DelayedSoapRequest(scConnector,
        		saveId,
                request, saveRequest, saveResponse);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Scheduling a SOAP request after delay " + lDelay + ":\n" + request);
        }

        scConnector.scheduleTasks(req, lDelay);
    }

    /**
     * Sends a SOAP request to a web service.
     *
     * @param   sUserDN   The user DN to be used in the request.
     * @param   sRequest  The request to be sent.
     *
     * @return  The SOAP response.
     *
     * @throws  ScriptConnectorException
     */
    public String sendSoapRequest(String sUserDN, String sRequest)
                           throws ScriptConnectorException
    {
        ScriptSoapMessage requestMsg = createSoapMessage();

        requestMsg.setUserDn(sUserDN);
        requestMsg.setMethodAsString(sRequest, false);

        ScriptSoapMessage responseMsg = scConnector.sendSoapRequestAndWait(requestMsg);
        String responseStr = responseMsg.getMethodAsString();

        responseMsg.clear();

        return responseStr;
    }

    /**
     * Sends a SOAP request to a web service.
     *
     * @param   request  SOAP request.
     *
     * @return  The SOAP response.
     *
     * @throws  ScriptConnectorException
     */
    public ScriptSoapMessage sendSoapRequestMessage(ScriptSoapMessage request)
                                             throws ScriptConnectorException
    {
        ScriptSoapMessage responseMsg = scConnector.sendSoapRequestAndWait(request);

        // Add it to the garbage list.
        scriptConnectorTransaction.addSoapMessage(responseMsg);

        return responseMsg;
    }

    /**
     * Creates a SOAP fault in the SOAP response. .
     *
     * @param  faultCode    SOAP fault code.
     * @param  faultActor   SOAP fault actor.
     * @param  faultString  SOAP fault message.
     * @param  detailXml    Optional SOAP fault detail XML string.
     */
    public void throwSoapFault(String faultCode, String faultActor, String faultString,
                               String detailXml)
    {
        throw new ScriptFaultException(faultCode, faultActor, faultString, detailXml);
    }

    /**
     * Returns a custom property loaded from the custom property file.
     *
     * @param   name  Property name.
     *
     * @return  Property value or <code>null</code> if no property has been set.
     */
    public String getCustomProperty(String name)
    {
        return scConnector.getScriptConfig().getCustomProperty(name);
    }

    /**
     * Returns the SOAP request.
     *
     * @return  Returns the request.
     */
    public String getRequest()
    {
        return soapRequest.getMethodAsString();
    }

    /**
     * Returns the SOAP request body block.
     *
     * @return  Request body block.
     */
    public BodyBlock getRequestBodyBlock()
    {
        return soapRequestContext.getRequestBodyBlock();
    }

    /**
     * Returns the SOAP request message object.
     *
     * @return  Returns the request object.
     */
    public ScriptSoapMessage getRequestMessage()
    {
        return soapRequest;
    }

    /**
     * Returns the SOAP request user DN.
     *
     * @return  Calling user DN.
     */
    public String getRequestUserDN()
    {
        return soapRequestContext.getRequestUserDn();
    }

    /**
     * Returns the SOAP response body block.
     *
     * @return  Response body block.
     */
    public BodyBlock getResponseBodyBlock()
    {
        BodyBlock tmp = soapRequestContext.getRequestBodyBlock();

        return (tmp != null) ? tmp.getResponseBodyBlock() : null;
    }

    /**
     * Returns the SOAP response.
     *
     * @return  Returns the response.
     */
    public ScriptSoapMessage getResponseMessage()
    {
        return soapResponse;
    }

    /**
     * Returns the name of the SOAP response method XML element.
     *
     * @return  Returns the name of the SOAP response method XML element.
     */
    public String getResponseMethodName()
    {
        return responseMethodName;
    }

    /**
     * Returns the namespace of the SOAP response method XML element.
     *
     * @return  Returns the namespace of the SOAP response method XML element.
     */
    public String getResponseMethodNamespace()
    {
        return responseMethodNamespace;
    }

    /**
     * Returns the responseAdded.
     *
     * @return  Returns the responseAdded.
     */
    public boolean isResponseAdded()
    {
        return responseAdded;
    }

    /**
     * Sets the SOAP response. This clears all other response elements possibly set.
     *
     * @param   response  The response to set.
     *
     * @throws  ScriptConnectorException
     */
    public void setResponse(String response)
                     throws ScriptConnectorException
    {
        soapResponse.setMethodAsString(response, true);
        responseAdded = true;
    }

    /**
     * Sets the SOAP response. This clears all other response elements possibly set.
     *
     * @param   msg  The response to set.
     *
     * @throws  ScriptConnectorException
     */
    public void setResponseMessage(ScriptSoapMessage msg)
                            throws ScriptConnectorException
    {
        if (soapResponse != null)
        {
            soapResponse.clear();
        }

        soapResponse = msg;
        responseAdded = true;
    }

    /**
     * Sets the name of the SOAP response method XML element.
     *
     * @param  responseMethodName  The name of the SOAP response method XML element to be set.
     */
    public void setResponseMethodName(String responseMethodName)
    {
        this.responseMethodName = responseMethodName;
    }

    /**
     * Sets the namespace of the SOAP response method XML element.
     *
     * @param  responseMethodNamespace  The namespace of the SOAP response method XML element to be
     *                                  set.
     */
    public void setResponseMethodNamespace(String responseMethodNamespace)
    {
        this.responseMethodNamespace = responseMethodNamespace;
    }

    /**
     * Returns the transactionSaveId.
     *
     * @return  Returns the transactionSaveId.
     */
    protected String getTransactionSaveId()
    {
        return scriptConnectorTransaction.getTransactionSaveId();
    }
}
