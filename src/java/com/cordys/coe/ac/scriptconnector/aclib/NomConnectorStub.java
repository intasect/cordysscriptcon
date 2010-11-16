/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.aclib;

import com.cordys.coe.util.soap.SOAPException;
import com.cordys.coe.util.soap.SoapFaultInfo;

import com.eibus.connector.nom.Connector;

import com.eibus.directory.soap.DirectoryException;

import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.exception.IllegalOperationException;

import java.text.MessageFormat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stub for the NOM connector interface.
 *
 * @author  mpoyhone
 */
public class NomConnectorStub
    implements INomConnector
{
    /**
     * SOAP message template.
     */
    protected static final String sSoapMessageTemplate = "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                                                         "<SOAP:Header>" +
                                                         "<header xmlns=\"http://schemas.cordys.com/General/1.0/\">" +
                                                         "<sender>" +
                                                         "<component>NomConnectorStub</component>" +
                                                         "<reply-to></reply-to>" +
                                                         "<user>{2}</user>" +
                                                         "</sender>" +
                                                         "<receiver><component>{3}</component></receiver>" +
                                                         "<msg-id>Fixed Message ID</msg-id>" +
                                                         "</header>" +
                                                         "</SOAP:Header>" +
                                                         "<SOAP:Body>" +
                                                         "<{0} xmlns=\"{1}\">" +
                                                         "</{0}>" +
                                                         "</SOAP:Body>" +
                                                         "</SOAP:Envelope>";
    /**
     * Default request wait timeout in milliseconds.
     */
    private long defaultTimeout = 10000L;
    /**
     * NOM document.
     */
    private Document doc;
    /**
     * Used to exchange the request and response nodes in a thread-safe fashion.
     */
    private Exchanger<Integer> requestEnchanger = new Exchanger<Integer>();

    /**
     * Constructor for NomConnectorStub.
     *
     * @param  doc
     */
    public NomConnectorStub(Document doc)
    {
        super();
        this.doc = doc;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#createSoapMethod(java.lang.String,
     *       java.lang.String, java.lang.String, java.lang.String)
     */
    public int createSoapMethod(String organization, String orgUser, String methodName,
                                String namespace)
                         throws DirectoryException
    {
        int env = 0;

        try
        {
            env = doc.parseString(MessageFormat.format(sSoapMessageTemplate, methodName, namespace,
                                                       orgUser, organization));
        }
        catch (Exception e)
        {
            throw new DirectoryException("Unable to parse the SOAP request template", 0, e);
        }

        return Node.getFirstChild(Node.getLastChild(env));
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#send(int)
     */
    public void send(int requestMethodNode)
              throws ExceptionGroup, SOAPException
    {
        if (requestMethodNode == 0)
        {
            throw new IllegalArgumentException("Request node is not set.");
        }

        int node = Node.duplicate(Node.getRoot(requestMethodNode));
        int res = 0;

        try
        {
            res = requestEnchanger.exchange(node);
        }
        catch (InterruptedException e)
        {
            Node.delete(node);
            throw new SOAPException("sendAndWait was interrupted.");
        }
        finally
        {
            if (res != 0)
            {
                // The response is never returned by this class.
                Node.delete(res);
                res = 0;
            }
        }
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#sendAndWait(int, boolean)
     */
    public int sendAndWait(int requestMethodNode, boolean checkSoapFault)
                    throws TimeoutException, ExceptionGroup, SOAPException
    {
        return sendAndWait(requestMethodNode, 10000L, checkSoapFault);
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#sendAndWait(int, long, boolean)
     */
    public int sendAndWait(int requestMethodNode, long timeout, boolean checkSoapFault)
                    throws TimeoutException, ExceptionGroup, SOAPException
    {
        if (requestMethodNode == 0)
        {
            throw new IllegalArgumentException("Request node is not set.");
        }

        int node = Node.duplicate(Node.getRoot(requestMethodNode));
        int res;

        try
        {
            res = requestEnchanger.exchange(node, timeout, TimeUnit.MILLISECONDS);
        }
        catch (java.util.concurrent.TimeoutException e)
        {
            Node.delete(node);
            throw new SOAPException("Timeout after " + timeout +
                                    " ms while waiting for a SOAP response.");
        }
        catch (InterruptedException e)
        {
            Node.delete(node);
            throw new SOAPException("sendAndWait was interrupted.");
        }

        if (checkSoapFault)
        {
            SoapFaultInfo faultInfo = SoapFaultInfo.findSoapFault(Node.getRoot(res));

            if (faultInfo != null)
            {
                String msg = faultInfo.toString();

                Node.delete(Node.getRoot(res));
                res = 0;

                throw new SOAPException(msg);
            }
        }

        return res;
    }

    /**
     * Starts a thread which will wait for the response. The response can be obtained from the wait
     * thread.
     *
     * @return  WaitThread object.
     */
    public WaitThread startWaitThread()
    {
        WaitThread w = new WaitThread(null);

        new Thread(w).start();

        return w;
    }

    /**
     * Starts a thread which will wait for the response. The response can be obtained from the wait
     * thread.
     *
     * @param   requestMethodNode  Request that will be sent.
     *
     * @return  WaitThread object.
     */
    public WaitThread startWaitThread(int requestMethodNode)
    {
        WaitThread w = new WaitThread(requestMethodNode);

        new Thread(w).start();

        return w;
    }

    /**
     * Waits until a SOAP request is send to this connector. This will return a dummy SOAP response
     * to the caller.
     *
     * @return  The received SOAP request node.
     *
     * @throws  SOAPException  Thrown if the the response XML could not be created or the request
     *                         was not received in time.
     */
    public int waitForRequest()
                       throws SOAPException
    {
        int response;

        try
        {
            response = createSoapMethod(null, null, "DummyResponse", "http://DummyNamespace");
        }
        catch (DirectoryException e)
        {
            throw new SOAPException("Unable to create the repsonse.");
        }

        try
        {
            return waitForRequest(response);
        }
        finally
        {
            Node.delete(Node.getRoot(response));
        }
    }

    /**
     * Waits until a SOAP request is send to this connector. This will return the given SOAP
     * response to the caller.
     *
     * @param   responseMethodNode  SOAP response.
     *
     * @return  The received SOAP request node.
     *
     * @throws  SOAPException  Thrown if the request was not received in time.
     */
    public int waitForRequest(int responseMethodNode)
                       throws SOAPException
    {
        int node;

        if (responseMethodNode != 0)
        {
            node = Node.duplicate(Node.getRoot(responseMethodNode));
        }
        else
        {
            node = 0;
        }

        try
        {
            return requestEnchanger.exchange(node);
        }
        catch (InterruptedException e)
        {
            if (node != 0)
            {
                Node.delete(node);
            }

            throw new SOAPException("waitForRequest was interrupted.");
        }
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#getDefaultTimeout()
     */
    public long getDefaultTimeout()
    {
        return defaultTimeout;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#getNomConnector()
     */
    public Connector getNomConnector()
    {
        throw new IllegalOperationException("getNomConnector() is not supported.");
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.INomConnector#setDefaultTimeout(long)
     */
    public void setDefaultTimeout(long timeout)
    {
        defaultTimeout = timeout;
    }

    /**
     * This thread will wait for the response.
     *
     * @author  mpoyhone
     */
    public class WaitThread
        implements Runnable
    {
        /**
         * Contains the request node.
         */
        private AtomicInteger requestNode = new AtomicInteger(0);
        /**
         * Used to notify that the request has been received.
         */
        private CountDownLatch requestSignal = new CountDownLatch(1);
        /**
         * Response node.
         */
        private Integer responseNode;
        /**
         * Filled by the wait thread.
         */
        private Exception waitException;

        /**
         * Constructor for WaitThread.
         *
         * @param  responseNode
         */
        private WaitThread(Integer responseNode)
        {
            super();
            this.responseNode = responseNode;
        }

        /**
         * @see  java.lang.Runnable#run()
         */
        public void run()
        {
            try
            {
                int res;

                if (responseNode == null)
                {
                    res = waitForRequest();
                }
                else
                {
                    res = waitForRequest(responseNode);
                }

                requestNode.set(res);
            }
            catch (Exception e)
            {
                waitException = e;
            }
            finally
            {
                requestSignal.countDown();
            }
        }

        /**
         * Waits until the request has been received and returns the request node.
         *
         * @return  Request node.
         *
         * @throws  Exception
         */
        public int getRequestNode()
                           throws Exception
        {
            requestSignal.await();

            if (waitException != null)
            {
                throw waitException;
            }

            return requestNode.get();
        }
    }
}
