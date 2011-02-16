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
package com.cordys.coe.ac.scriptconnector.aclib;

import com.cordys.coe.util.soap.SOAPException;
import com.cordys.coe.util.soap.SoapFaultInfo;

import com.eibus.connector.nom.Connector;

import com.eibus.directory.soap.DirectoryException;

import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;

import com.eibus.xml.nom.Node;

/**
 * Wrapper for the NOM Connector class for SOAP messaging.
 *
 * @author  mpoyhone
 */
public class NomConnectorImpl
    implements INomConnector
{
    /**
     * Contains the actual NOM connector.
     */
    private Connector connector;

    /**
     * Constructor for NomConnectorImpl.
     *
     * @param  connector
     */
    public NomConnectorImpl(Connector connector)
    {
        this.connector = connector;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#createSoapMethod(java.lang.String,
     *       java.lang.String, java.lang.String, java.lang.String)
     */
    public int createSoapMethod(String organization, String orgUser, String methodName,
                                String namespace)
                         throws DirectoryException
    {
        return connector.createSOAPMethod(orgUser, organization, namespace, methodName);
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#send(int)
     */
    public void send(int requestMethodNode)
              throws ExceptionGroup, SOAPException
    {
        connector.send(Node.getRoot(requestMethodNode));
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#sendAndWait(int, boolean)
     */
    public int sendAndWait(int requestMethodNode, boolean checkSoapFault)
                    throws TimeoutException, ExceptionGroup, SOAPException
    {
        int responseEnvNode = connector.sendAndWait(Node.getRoot(requestMethodNode));

        return processSoapResponse(responseEnvNode, checkSoapFault);
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#sendAndWait(int, long, boolean)
     */
    public int sendAndWait(int requestMethodNode, long timeout, boolean checkSoapFault)
                    throws TimeoutException, ExceptionGroup, SOAPException
    {
        int responseEnvNode = connector.sendAndWait(Node.getRoot(requestMethodNode), timeout);

        return processSoapResponse(responseEnvNode, checkSoapFault);
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#getDefaultTimeout()
     */
    public long getDefaultTimeout()
    {
        return connector.getTimeout();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#getNomConnector()
     */
    public Connector getNomConnector()
    {
        return connector;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#setDefaultTimeout(long)
     */
    public void setDefaultTimeout(long timeout)
    {
        connector.setTimeout(timeout);
    }

    /**
     * Processes the SOAP response node. Currently this just checks for SOAP faults.
     *
     * @param   responseEnvNode  SOAP response envelope node.
     * @param   checkSoapFault   If <code>true</code> response is checked for a SOAP fault.
     *
     * @return  Processed SOAP envelope node.
     *
     * @throws  SOAPException  Thrown if a SOAP fault was found.
     */
    private int processSoapResponse(int responseEnvNode, boolean checkSoapFault)
                             throws SOAPException
    {
        if (checkSoapFault)
        {
            SoapFaultInfo faultInfo = SoapFaultInfo.findSoapFault(responseEnvNode);

            if (faultInfo != null)
            {
                String msg = faultInfo.toString();

                Node.delete(Node.getRoot(responseEnvNode));
                responseEnvNode = 0;

                throw new SOAPException(msg);
            }
        }

        return responseEnvNode;
    }
}
