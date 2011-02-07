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
package com.cordys.coe.ac.scriptconnector.soap;

import java.util.TimerTask;

import com.cordys.coe.ac.scriptconnector.ScriptConnector;
import com.cordys.coe.ac.scriptconnector.Utils;
import com.cordys.coe.ac.scriptconnector.methods.SavedSoapTransactions;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

/**
 * Simple task which sends a SOAP request after a delay.
 *
 * @author  mpoyhone
 */
public class DelayedSoapRequest extends TimerTask
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DelayedSoapRequest.class);
    /**
     * ScriptConnector instance.
     */
    private ScriptConnector connector;
    /**
     * SOAP request message.
     */
    private ScriptSoapMessage request;
    /**
     * If set, request and response will be saved.
     */
    private String transactionSaveId;
    /**
     * true, if request must be saved.
     */
    private boolean transactionSaveRequest;
    /**
     * true, if response must be saved.
     */
    private boolean transactionSaveResponse;
    
    /**
     * Constructor for DelayedSoapRequest.
     *
     * @param  conn               connector ScriptConnector instance.
     * @param  transactionSaveId  If set, request and response will be saved.
     * @param  msg                SOAP request to be sent.
     * @param transactionSaveResponse true if response must be saved.
     * @param transactionSaveRequest true if request must be saved.
     */
    public DelayedSoapRequest(ScriptConnector conn, String transactionSaveId, ScriptSoapMessage msg, boolean transactionSaveRequest, boolean transactionSaveResponse)
    {
        super();
        this.connector = conn;
        this.request = msg;
        this.transactionSaveId = transactionSaveId;
        this.transactionSaveRequest = transactionSaveRequest;
        this.transactionSaveResponse = transactionSaveResponse;
    }

    /**
     * @see  java.util.TimerTask#run()
     */
    @Override
    public void run()
    {
        if ((transactionSaveId != null) && transactionSaveRequest)
        {
            try
            {
                SavedSoapTransactions.writeTransactionFile(connector, transactionSaveId,
                                                           request.getSoapMethodRoot(), true);
            }
            catch (Exception e)
            {
                LOG.log(Severity.ERROR, "Unable to store SOAP request.", e);
            }
        }

        ScriptSoapMessage response = null;

        try
        {
            response = connector.sendSoapRequestAndWait(request, false);
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "SOAP request failed.", e);
        }

        try
        {
            if ((response != null) && (transactionSaveId != null) && transactionSaveResponse)
            {
                try
                {
                    int methodNode = response.getSoapMethodRoot();
                    String soapPrefix = response.getSoapNamespacePrefix();

                    if ((soapPrefix != null) && (soapPrefix.length() > 0) &&
                            soapPrefix.equals(Node.getPrefix(methodNode)))
                    {
                        // SOAP namespace needed for SOAP:Faults. This will be removed when file
                        // is read.
                        Node.setAttribute(methodNode, "xmlns:" + soapPrefix, Utils.SOAP_NAMESPACE);
                    }

                    SavedSoapTransactions.writeTransactionFile(connector, transactionSaveId,
                                                               methodNode, false);
                }
                catch (Exception e)
                {
                    LOG.log(Severity.ERROR, "Unable to store SOAP response.", e);
                }
            }
        }
        finally
        {
            if (response != null)
            {
                response.clear();
            }
        }
    }
}
