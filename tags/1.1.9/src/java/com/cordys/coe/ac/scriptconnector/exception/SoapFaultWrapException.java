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
