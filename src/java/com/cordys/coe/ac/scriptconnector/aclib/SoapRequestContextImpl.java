/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.aclib;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Implementation of the ISoapRequestContext for application connector SOAP requests.
 *
 * @author  mpoyhone
 */
public class SoapRequestContextImpl
    implements ISoapRequestContext
{
    /**
     * Contains SOAP method definition object.
     */
    private ISoapMethodDefinition methodDef;
    /**
     * Request body block.
     */
    private BodyBlock requestBlock;
    /**
     * Response body block.
     */
    private BodyBlock responseBlock;

    /**
     * Constructor for SoapRequestImpl.
     *
     * @param  requestBlock
     * @param  responseBlock
     */
    public SoapRequestContextImpl(BodyBlock requestBlock, BodyBlock responseBlock)
    {
        this.requestBlock = requestBlock;
        this.responseBlock = responseBlock;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#addResponseElement(java.lang.String)
     */
    public int addResponseElement(String elemName)
    {
        return Node.createElement(elemName, responseBlock.getXMLNode());
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#addResponseElement(int)
     */
    public int addResponseElement(int node)
    {
        return Node.appendToChildren(node, responseBlock.getXMLNode());
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#addResponseElement(java.lang.String,
     *       java.lang.String)
     */
    public int addResponseElement(String elemName, String elemValue)
    {
        return Node.createTextElement(elemName, elemValue, responseBlock.getXMLNode());
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getMethodDefinition()
     */
    public ISoapMethodDefinition getMethodDefinition()
    {
        if (methodDef == null)
        {
            methodDef = new SoapMethodDefinitionImpl(requestBlock.getMethodDefinition());
        }
        return methodDef;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.ISoapRequestContext#getNomDocument()
     */
    public Document getNomDocument()
    {
        return Node.getDocument(responseBlock.getXMLNode());
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestBodyBlock()
     */
    public BodyBlock getRequestBodyBlock()
    {
        return requestBlock;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestMethodNode()
     */
    public int getRequestMethodNode()
    {
        return requestBlock.getXMLNode();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestOrganizationDn()
     */
    public String getRequestOrganizationDn()
    {
        return requestBlock.getSOAPTransaction().getIdentity().getUserOrganization();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getRequestUserDn()
     */
    public String getRequestUserDn()
    {
        return requestBlock.getSOAPTransaction().getIdentity().getOrgUserDN();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext#getResponseMethodNode()
     */
    public int getResponseMethodNode()
    {
        return responseBlock.getXMLNode();
    }
}
