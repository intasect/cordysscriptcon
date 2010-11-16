/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.aclib;

import com.cordys.coe.util.soap.SOAPException;

import com.eibus.connector.nom.Connector;

import com.eibus.directory.soap.DirectoryException;

import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;

/**
 * Simple interface for Cordys BCP NOM connector.
 *
 * @author  mpoyhone
 */
public interface INomConnector
{
    /**
     * Creates a SOAP method envelope.
     *
     * @param   organization  Organization DN to which the SOAP request is to be sent. If <code>
     *                        null</code>, the current one is used.
     * @param   orgUser       Sending organization user DN. If <code>null</code> the SYSTEM user is
     *                        used.
     * @param   methodName    SOAP method name.
     * @param   namespace     SOAP method namespace.
     *
     * @return  The method node of the SOAP request.
     *
     * @throws  DirectoryException  Thrown if the operation failed.
     */
    int createSoapMethod(String organization, String orgUser, String methodName, String namespace)
                  throws DirectoryException;

    /**
     * Sends a SOAP request to the Cordys BCP bus. This method does not wait for a response.
     *
     * @param   requestMethodNode  SOAP request method node or envelope node.
     *
     * @throws  ExceptionGroup  Thrown if the operation failed.
     * @throws  SOAPException   Thrown if the operation failed.
     */
    void send(int requestMethodNode)
       throws ExceptionGroup, SOAPException;

    /**
     * Sends a SOAP request to the Cordys BCP bus and waits for a response.
     *
     * @param   requestMethodNode  SOAP request method node or envelope node.
     * @param   checkSoapFault     If <code>true</code> this method throws a SoapFaultException when
     *                             a SOAP:Fault is received.
     *
     * @return  SOAP response envelope node.
     *
     * @throws  TimeoutException  Thrown if the operation timed out.
     * @throws  ExceptionGroup    Thrown if the operation failed.
     * @throws  SOAPException     Thrown if the operation failed.
     */
    int sendAndWait(int requestMethodNode, boolean checkSoapFault)
             throws TimeoutException, ExceptionGroup, SOAPException;

    /**
     * Sends a SOAP request to the Cordys BCP bus and waits for a response.
     *
     * @param   requestMethodNode  SOAP request method node or envelope node.
     * @param   timeout            Request timeout in milliseconds.
     * @param   checkSoapFault     If <code>true</code> this method throws a SoapFaultException when
     *                             a SOAP:Fault is received.
     *
     * @return  SOAP response envelope node.
     *
     * @throws  TimeoutException  Thrown if the operation timed out.
     * @throws  ExceptionGroup    Thrown if the operation failed.
     * @throws  SOAPException     Thrown if the operation failed.
     */
    int sendAndWait(int requestMethodNode, long timeout, boolean checkSoapFault)
             throws TimeoutException, ExceptionGroup, SOAPException;

    /**
     * Returns the default request timeout value.
     *
     * @return  Timeout value in milliseconds.
     */
    long getDefaultTimeout();

    /**
     * Returns the underlying connector object.
     *
     * @return
     */
    Connector getNomConnector();

    /**
     * Sets the default request timeout value.
     *
     * @param  timeout  Timeout value in milliseconds.
     */
    void setDefaultTimeout(long timeout);
}
