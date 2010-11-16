/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.exception;

/**
 * Exception class for SOAP faults thrown by scripts.
 *
 * @author  mpoyhone
 */
public class ScriptFaultException extends RuntimeException
{
    /**
     * SOAP fault detail XML as string.
     */
    private String detailXml;
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
     * @param  detailXml    SOAP fault detail XML as string.
     */
    public ScriptFaultException(String faultCode, String faultActor, String faultString,
                                String detailXml)
    {
        super();
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultActor = faultActor;
        this.detailXml = detailXml;
    }

    /**
     * Returns the detailXml.
     *
     * @return  Returns the detailXml.
     */
    public String getDetailXml()
    {
        return detailXml;
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

        if (detailXml != null)
        {
            msg.append("Fault Details:").append(detailXml);
        }

        return msg.toString();
    }
}
