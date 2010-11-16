/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.aclib;

import com.eibus.connector.nom.SOAPMessage;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.text.MessageFormat;

/**
 * A stub implementation of the ISoapRequestContext for test cases.
 *
 * @author  mpoyhone
 */
public class SoapRequestContextStub
    implements ISoapRequestContext
{
    /**
     * Defines the SOAP namespace.
     */
    public static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope";

    /**
     * Template for the response SOAP envelope.
     */
    public static final String SOAP_ENV_TEMPLATE = "<SOAP:Envelope xmlns:SOAP=\"" + SOAP_NAMESPACE +
                                                   "/\">" +
                                                   "<SOAP:Header></SOAP:Header>" +
                                                   "<SOAP:Body>{0}</SOAP:Body>" +
                                                   "</SOAP:Envelope>";
    /**
     * Template for the response SOAP method.
     */
    public static final String SOAP_METHOD_TEMPLATE = "<{0} xmlns=\"{1}\"/>";

    /**
     * Contains SOAP method definition object.
     */
    private ISoapMethodDefinition methodDef;
    /**
     * Contains the request SOAP envelope node.
     */
    private int requestEnvNode;
    /**
     * Contains the request method node.
     */
    private int requestMethodNode;
    /**
     * Contains the response SOAP envelope node.
     */
    private int responseEnvNode;
    /**
     * Contains the response method node.
     */
    private int responseMethodNode;

    /**
     * Constructor for SoapRequestImpl.
     *
     * @param  requestMethod  Request method node. This node is cloned.
     * @param  def            Method definition object.
     */
    public SoapRequestContextStub(int requestMethod, ISoapMethodDefinition def)
    {
        this.methodDef = def;
        this.responseEnvNode = requestMethod;

        if (Node.getParent(requestMethod) == 0)
        {
            // Add the SOAP envelope around the method.
            String tmp = MessageFormat.format(SOAP_ENV_TEMPLATE,
                                              Node.writeToString(requestMethod, false));

            try
            {
                this.requestEnvNode = Node.getDocument(requestMethod).parseString(tmp);
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Unable to parse the SOAP request.", e);
            }
        }
        else
        {
            this.requestEnvNode = Node.duplicate(Node.getRoot(requestMethod));
        }

        this.requestMethodNode = Node.getFirstElement(SOAPMessage.getRootBodyNode(requestEnvNode));

        String responseMethodXml = MessageFormat.format(SOAP_METHOD_TEMPLATE,
                                                        Node.getLocalName(requestMethod) +
                                                        "Response",
                                                        Node.getNamespaceURI(requestMethod));
        String responseXml = MessageFormat.format(SOAP_ENV_TEMPLATE, responseMethodXml);

        try
        {
            this.responseEnvNode = Node.getDocument(requestMethod).parseString(responseXml);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to parse the SOAP response template.", e);
        }

        this.responseMethodNode = Node.getFirstElement(SOAPMessage.getRootBodyNode(responseEnvNode));

        if (this.methodDef == null)
        {
            this.methodDef = new SoapMethodDefinitionStub(this.responseEnvNode);
        }
    }

    /**
     * Constructor for SoapRequestImpl.
     *
     * @param  requestMethod   Request method node. This node is cloned.
     * @param  def             Method definition object.
     * @param  responseMethod  Response method node. This node is cloned.
     */
    public SoapRequestContextStub(int requestMethod, ISoapMethodDefinition def, int responseMethod)
    {
        this.methodDef = def;
        this.responseEnvNode = Node.duplicate(Node.getRoot(requestMethod));
        this.responseMethodNode = Node.duplicate(Node.getRoot(responseMethod));

        this.requestMethodNode = Node.getFirstElement(SOAPMessage.getRootBodyNode(responseEnvNode));
        this.responseMethodNode = Node.getFirstElement(SOAPMessage.getRootBodyNode(responseEnvNode));

        if (this.methodDef == null)
        {
            this.methodDef = new SoapMethodDefinitionStub(this.responseEnvNode);
        }
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#addResponseElement(java.lang.String)
     */
    public int addResponseElement(String elemName)
    {
        return Node.createElement(elemName, responseMethodNode);
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#addResponseElement(int)
     */
    public int addResponseElement(int node)
    {
        return Node.appendToChildren(node, responseMethodNode);
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#addResponseElement(java.lang.String,
     *       java.lang.String)
     */
    public int addResponseElement(String elemName, String elemValue)
    {
        return Node.createTextElement(elemName, elemValue, responseMethodNode);
    }

    /**
     * Deletes the NOM nodes.
     */
    public void cleanup()
    {
        if (requestEnvNode != 0)
        {
            Node.delete(requestEnvNode);
            requestEnvNode = 0;
            requestMethodNode = 0;
        }

        if (responseEnvNode != 0)
        {
            Node.delete(responseEnvNode);
            responseEnvNode = 0;
            responseMethodNode = 0;
        }
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getMethodDefinition()
     */
    public ISoapMethodDefinition getMethodDefinition()
    {
        if (methodDef == null)
        {
            throw new IllegalStateException("Method definition is not set.");
        }

        return methodDef;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.ISoapRequestContext#getNomDocument()
     */
    public Document getNomDocument()
    {
        return Node.getDocument(responseMethodNode);
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestBodyBlock()
     */
    public BodyBlock getRequestBodyBlock()
    {
        throw new IllegalStateException("getRequestBodyBlock() is not supported.");
    }

    /**
     * Returns the requestEnvNode.
     *
     * @return  Returns the requestEnvNode.
     */
    public int getRequestEnvNode()
    {
        return requestEnvNode;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestMethodNode()
     */
    public int getRequestMethodNode()
    {
        return requestMethodNode;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestOrganizationDn()
     */
    public String getRequestOrganizationDn()
    {
        return "dummy-org-dn";
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestUserDn()
     */
    public String getRequestUserDn()
    {
        return "dummy-user-dn";
    }

    /**
     * Returns the responseEnvNode.
     *
     * @return  Returns the responseEnvNode.
     */
    public int getResponseEnvNode()
    {
        return responseEnvNode;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getResponseMethodNode()
     */
    public int getResponseMethodNode()
    {
        return responseMethodNode;
    }
}
