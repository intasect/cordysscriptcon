/**
 * © 2004 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys R&D B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.aclib.SoapRequestContextImpl;
import com.cordys.coe.ac.scriptconnector.config.ScriptConnectorConfiguration;
import com.cordys.coe.ac.scriptconnector.config.ScriptLocator;
import com.cordys.coe.ac.scriptconnector.config.SoapMethodInfo;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;
import com.cordys.coe.ac.scriptconnector.exception.ScriptFaultException;
import com.cordys.coe.ac.scriptconnector.exception.SoapFaultWrapException;
import com.cordys.coe.ac.scriptconnector.methods.SavedSoapTransactions;
import com.cordys.coe.ac.scriptconnector.processor.IScriptPreProcessor;
import com.cordys.coe.ac.scriptconnector.scripting.BridgeObject;
import com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript;
import com.cordys.coe.ac.scriptconnector.scripting.IScriptHandler;
import com.cordys.coe.ac.scriptconnector.soap.ScriptSoapMessage;
import com.cordys.coe.util.general.Util;
import com.eibus.connector.nom.SOAPMessage;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.fault.Fault;
import com.eibus.soap.fault.FaultDetail;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

/**
 * This class is the Implementation of ApplicationTransaction. This class will receive the request
 * process it if it is a valid one.
 */
public class ScriptConnectorTransaction
    implements ApplicationTransaction
{
    /**
     * Identifies the Logger.
     */
    static CordysLogger LOG = CordysLogger.getCordysLogger(ScriptConnector.class);
    /**
     * Method type for ScriptConnector internal methods.
     */
    private static final String SCRIPT_TYPE = "SCRIPT";
    /**
     * Optional ID used when the connector SOAP request and response are to be save in a file.
     */
    protected String transactionSaveId;
    /**
     * Holds the script connector instance.
     */
    final ScriptConnector scConnector;
    /**
     * Contains all SOAP messages create for this transaction.
     */
    final List<ScriptSoapMessage> soapMessageList = new LinkedList<ScriptSoapMessage>();

    /**
     * Creates the transaction object.
     *
     * @param  scConnector  The script connector instance.
     */
    public ScriptConnectorTransaction(ScriptConnector scConnector)
    {
        this.scConnector = scConnector;
    }

    /**
     * This will be called when a transaction is being aborted.
     */
    public void abort()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Transaction aborted.");
        }
    }

    /**
     * Adds the given SOAP message to the garbage list..
     *
     * @param  msg  SOAP message to be added.
     */
    public void addSoapMessage(ScriptSoapMessage msg)
    {
        soapMessageList.add(msg);
    }

    /**
     * This method returns returns if this transaction can process requests of the given type.
     *
     * @param   sType  The type of message that needs to be processed
     *
     * @return  true if the type can be processed. Otherwise false.
     */
    public boolean canProcess(String sType)
    {
        // This connector processes every method attached to the SOAP node.
        return true;
    }

    /**
     * Cleans SOAP messages from the clean up list. This will delete all NOM nodes.
     */
    public void cleanSoapMessages()
    {
        for (ScriptSoapMessage msg : soapMessageList)
        {
            msg.clear();
        }

        soapMessageList.clear();
    }

    /**
     * This method is called when the transaction is committed.
     */
    public void commit()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Transaction Committed.");
        }
    }

    /**
     * Creates a new Script SOAP message object and adds it to the clean up list.
     *
     * @param   addToGarbageList  If <code>true</code> the message is added to the clean up list.
     *
     * @return  New SOAP message object.
     */
    public ScriptSoapMessage createSoapMessage(boolean addToGarbageList)
    {
        ScriptSoapMessage res = new ScriptSoapMessage(scConnector.getDocument());

        if (addToGarbageList)
        {
            soapMessageList.add(res);
        }

        return res;
    }

    /**
     * This method processes the received request.
     *
     * @param   bbRequest   The request-bodyblock.
     * @param   bbResponse  The response-bodyblock.
     *
     * @return  true if the connector has to send the response. If someone else sends the response
     *          false is returned.
     */
    public boolean process(BodyBlock bbRequest, BodyBlock bbResponse)
    {
        boolean bReturn = true;
        int responseEnvNode = 0;

        try
        {
            ScriptConnectorConfiguration acConfig = scConnector.getScriptConfig();

            if (acConfig == null)
            {
                throw new ScriptConnectorException("ScriptConnector is not initialized properly.");
            }

            // Get the script configuration parameters.
            int iRequestNode = bbRequest.getXMLNode();
            String methodName = Node.getLocalName(iRequestNode);
            String methodNamespace = Node.getNamespaceURI(iRequestNode);
            ScriptLocator scriptId = new ScriptLocator(methodName, methodNamespace);

            // Get the response envelope node so that we can log it at the end.
            responseEnvNode = Node.getRoot(bbResponse.getXMLNode());

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Incoming request: " +
                          Node.writeToString(Node.getRoot(iRequestNode), true));
            }

            String type = bbRequest.getMethodDefinition().getType();
            boolean internalMethod = false;

            if (SCRIPT_TYPE.equals(type))
            {
                int implNode = bbRequest.getMethodDefinition().getImplementation();
                String action = (implNode != 0) ? Node.getDataElement(implNode, "action", "")
                                                : null;

                if ((action != null) && (action.length() > 0))
                {
                    if ("GetSavedSoapTransactions".equals(action))
                    {
                        SavedSoapTransactions.processGetSavedSoapTransactions(scConnector,
                                                                              bbRequest,
                                                                              bbResponse);
                        internalMethod = true;
                    }
                    else if ("DeleteSavedSoapTransactions".equals(action))
                    {
                        SavedSoapTransactions.processDeleteSavedSoapTransactions(scConnector,
                                                                                 bbRequest,
                                                                                 bbResponse);
                        internalMethod = true;
                    }
                }
            }

            if (!internalMethod)
            {
                ISoapRequestContext requestContext = new SoapRequestContextImpl(bbRequest,
                                                                                bbResponse);

                executeScriptMethod(requestContext, scriptId);
            }
        }
        catch (SoapFaultWrapException e)
        {
            // Handle a SOAP fault returned by an external service.
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(e, LogMessages.TRANSACTION_FAILED, e.getMessage());
            }

            // Get the SOAP fault details.
            String faultXml = e.getSoapFaultXml();
            int faultNode = 0;

            if ((faultXml != null) && (faultXml.length() > 0))
            {
                // Parse the SOAP fault detail XML.
                try
                {
                    faultNode = scConnector.getDocument().parseString(faultXml);

                    // Remove the SOAP namespace declaration.
                   // Node.removeAttribute(faultNode, "xmlns:" + Node.getPrefix(faultNode));
                }
                catch (Exception e2)
                {
                    LOG.log(Severity.ERROR, "Unable to parse the SOAP fault XML.", e);
                }

                if (faultNode != 0) {
                    int bodyNode = SOAPMessage.getRootBodyNode(responseEnvNode);

                    if (bodyNode != 0) {
                        Node.appendToChildren(faultNode, bodyNode);
                    }

                    if (bbResponse.getXMLNode() != 0) {
                        // Delete the response method node.
                        Node.delete(bbResponse.getXMLNode());
                    }
                } else {
                    bbResponse.createSOAPFault(new QName("Server.Error"), LogMessages.TRANSACTION_FAILED, "Unable to return the external SOAP:Fault");
                }
            }

            if (bbRequest.isAsync())
            {
                bbRequest.continueTransaction();
                bReturn = false;
            }
        }
        catch (ScriptFaultException e)
        {
            // Handle a SOAP fault thrown by the script.
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(e, LogMessages.TRANSACTION_FAILED, e.getMessage());
            }

            // Get the SOAP fault details.
            String faultCode = e.getFaultCode();
            String faultString = e.getFaultString();
            String detailXml = e.getDetailXml();
            int detailRootNode = 0;

            if ((detailXml != null) && (detailXml.length() > 0))
            {
                // Parse the SOAP fault detail XML.
                try
                {
                    detailRootNode = scConnector.getDocument().parseString(detailXml);
                }
                catch (Exception e2)
                {
                    LOG.log(Severity.ERROR, "Unable to parse the SOAP fault detail XML.", e);
                }
            }

            // Create the SOAP fault.
            try
            {
                Fault fault = bbResponse.createSOAPFault(new QName(faultCode), LogMessages.TRANSACTION_FAILED, faultString);
                FaultDetail detail = fault.getDetail();

                if (detailRootNode != 0)
                {
                    detail.addDetailEntry(detailRootNode);
                    detailRootNode = 0;
                }
            }
            finally
            {
                if (detailRootNode != 0)
                {
                    Node.delete(detailRootNode);
                    detailRootNode = 0;
                }
            }

            if (bbRequest.isAsync())
            {
                bbRequest.continueTransaction();
                bReturn = false;
            }
        }
        catch (Exception e)
        {
            // Handle any unknown error.
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(e, LogMessages.TRANSACTION_FAILED, e.getMessage());
            }

            String sMessage = e.getMessage() + "\n" + Util.getStackTrace(e);

            Fault fault =  bbResponse.createSOAPFault(new QName("Server.Exception"), LogMessages.TRANSACTION_FAILED, sMessage);
            FaultDetail detail = fault.getDetail();

            detail.addDetailEntry(e);

            if (bbRequest.isAsync())
            {
                bbRequest.continueTransaction();
                bReturn = false;
            }
        }
        finally
        {
            // Clear all SOAP messages created during this transaction. This will
            // delete the NOM nodes.
            cleanSoapMessages();
        }

        if (transactionSaveId != null)
        {
            try
            {
                SavedSoapTransactions.writeTransactionFile(scConnector, transactionSaveId,
                                                           bbResponse.getXMLNode(), false);
            }
            catch (ScriptConnectorException e)
            {
                LOG.log(Severity.ERROR, "Unable to write SOAP response file.", e);
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Sending response: " + Node.writeToString(responseEnvNode, true));
        }

        return bReturn;
    }

    /**
     * Returns the transactionSaveId.
     *
     * @return  Returns the transactionSaveId.
     */
    public String getTransactionSaveId()
    {
        return transactionSaveId;
    }

    /**
     * Sets the transactionSaveId.
     *
     * @param  transactionSaveId  The transactionSaveId to be set.
     */
    public void setTransactionSaveId(String transactionSaveId)
    {
        this.transactionSaveId = transactionSaveId;
    }

    /**
     * Executes a script that is identified with the script id.
     *
     * @param   requestContext  SOAP request context.
     * @param   scriptId        ID of the script to be executed.
     *
     * @throws  Exception  Thrown if the operation failed.
     */
    protected void executeScriptMethod(ISoapRequestContext requestContext, ScriptLocator scriptId)
                                throws Exception
    {
        ScriptConnectorConfiguration acConfig = scConnector.getScriptConfig();

        // Fetch or load the script.
        ConfiguredScript csScript = acConfig.getScript(scriptId);

        if (csScript == null)
        {
            throw new ScriptConnectorException("Script " + scriptId +
                                               " is not configured for this connector.");
        }

        IScriptPreProcessor preProcessor = acConfig.findPreProcessor(scriptId);

        if (preProcessor != null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Executing script pre-processor: " + preProcessor.getClass().getName());
            }

            if (!preProcessor.execute(scConnector, csScript, requestContext))
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Pre-processor returned false, so the script will not be executed.");
                }

                return;
            }
        }

        // Get the SOAP message info before the method node in unlinked by ScriptSoapMessage
        SoapMethodInfo methodInfo = scConnector.getSoapMethodInfo(requestContext);

        // Create a SOAP message object for our request. This can be accessed from the script.
        ScriptSoapMessage requestMsg = createSoapMessage(true);

        requestMsg.readFromSoapMessage(Node.getRoot(requestContext.getRequestMethodNode()));

        // Create the script bridge object and script handlers and execute the script.
        IScriptHandler handler = csScript.createHandler();

        if (handler == null)
        {
            throw new ScriptConnectorException("Handler is not set for script: " +
                                               csScript.getScriptName());
        }

        BridgeObject bridgeObject = new BridgeObject(scConnector, this, requestContext, requestMsg);

        handler.executeScript(csScript, bridgeObject, this);

        // Create SOAP response from the script result.
        createSoapResponse(methodInfo, bridgeObject, requestContext);
    }

    /**
     * Creates the connector SOAP response based on the script output.
     *
     * @param  methodInfo      Original SOAP request method info object.
     * @param  bridgeObject    Script bridge object which contains the SOAP response.
     * @param  requestContext  SOAP request context.
     */
    private void createSoapResponse(SoapMethodInfo methodInfo, BridgeObject bridgeObject,
                                    ISoapRequestContext requestContext)
    {
        ScriptSoapMessage scriptResponse = bridgeObject.getResponseMessage();
        int responseEnvelopeNode = Node.getRoot(requestContext.getResponseMethodNode());
        int responseBodyNode = SOAPMessage.getRootBodyNode(responseEnvelopeNode);

        if (scriptResponse != null)
        {
            // This might modify the response method node or even delete it.
            scriptResponse.appendToSoapEnvelope(responseEnvelopeNode, false);
        }

        // Modify the response element name, if the script had requested that. Other wise try to
        // get the response element name from the WSDL.
        int responseMethodNode = Node.getFirstChildElement(responseBodyNode);

        if (responseMethodNode != 0)
        {
            String responseElementName = bridgeObject.getResponseMethodName();

            if ((responseElementName == null) || (responseElementName.length() == 0))
            {
                responseElementName = methodInfo.getResponseElementName();
            }

            if ((responseElementName != null) && (responseElementName.length() > 0))
            {
                String prefix = Node.getPrefix(responseMethodNode);

                if (prefix != null)
                {
                    responseElementName = prefix + ":" + responseElementName;
                }

                Node.setName(responseMethodNode, responseElementName);
            }

            // Modify the method namespace, if the script had requested that.
            String methodNamespace = bridgeObject.getResponseMethodNamespace();

            if ((methodNamespace == null) || (methodNamespace.length() == 0))
            {
                methodNamespace = methodInfo.getResponseElementNamespace();
            }

            if ((methodNamespace != null) && (methodNamespace.length() > 0))
            {
                String prefix = Node.getPrefix(responseMethodNode);
                String nsAttrName;

                if (prefix != null)
                {
                    nsAttrName = "xmlns:" + prefix;
                }
                else
                {
                    nsAttrName = "xmlns";
                }

                Node.setAttribute(responseMethodNode, nsAttrName, methodNamespace);
            }
        }
    }
}
