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
