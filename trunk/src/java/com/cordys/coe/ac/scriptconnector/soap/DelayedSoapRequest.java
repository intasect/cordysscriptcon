/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
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
