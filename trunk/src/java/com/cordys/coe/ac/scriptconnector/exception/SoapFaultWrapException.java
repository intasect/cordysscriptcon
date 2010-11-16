/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.exception;

/**
 * Exception class which holds the full SOAP fault thrown by an external SOAP service.
 *
 * @author  mpoyhone
 */
public class SoapFaultWrapException extends RuntimeException
{
    /**
     * SOAP fault detail XML as string.
     */
    private String soapFaultXml;
    /**
     * SOAP fault actor.
     */
    private String faultActor;
    /**
     * SOAP fault code.
     */
    private String faultCode;
    /**
     * SOAP fault string.
     */
    private String faultString;

    /**
     * Constructor for ScriptFaultException.
     *
     * @param  faultCode    SOAP fault code.
     * @param  faultActor   SOAP fault actor.
     * @param  faultString  SOAP fault string.
     * @param  soapFaultXml    SOAP fault XML as string.
     */
    public SoapFaultWrapException(String faultCode, String faultActor, String faultString,
                                String soapFaultXml)
    {
        super();
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultActor = faultActor;
        this.soapFaultXml = soapFaultXml;
    }

    /**
     * Returns the soapFaultXml.
     *
     * @return  Returns the soapFaultXml.
     */
    public String getSoapFaultXml()
    {
        return soapFaultXml;
    }

    /**
     * Returns the faultActor.
     *
     * @return  Returns the faultActor.
     */
    public String getFaultActor()
    {
        return faultActor;
    }

    /**
     * Returns the faultCode.
     *
     * @return  Returns the faultCode.
     */
    public String getFaultCode()
    {
        return faultCode;
    }

    /**
     * Returns the faultString.
     *
     * @return  Returns the faultString.
     */
    public String getFaultString()
    {
        return faultString;
    }

    /**
     * @see  java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage()
    {
        StringBuilder msg = new StringBuilder(512);

        msg.append("SOAP Fault received\n");
        msg.append("Fault Code: ").append(faultCode).append("\n");
        msg.append("Fault String: ").append(faultString).append("\n");
        msg.append("Fault Actor: ").append(faultActor).append("\n");

        if (soapFaultXml != null)
        {
            msg.append("Full SOAP fault:").append(soapFaultXml);
        }

        return msg.toString();
    }
}
