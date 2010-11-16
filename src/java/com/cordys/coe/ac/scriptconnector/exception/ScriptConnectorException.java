/**
 * © 2004 Cordys R&D B.V. All rights reserved.
 * The computer program(s) is the proprietary information of Cordys R&D B.V.
 * and provided under the relevant License Agreement containing restrictions
 * on use and disclosure. Use is subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.exception;

/**
 * General Exception class for the ScriptConnector.
 */
public class ScriptConnectorException extends Exception
{
    /**
     * Creates a new instance of <code>ScriptConnectorException</code> without a cause.
     */
    public ScriptConnectorException()
    {
    }

    /**
     * Creates a new instance of <code>ScriptConnectorException</code> based on the the throwable.
     *
     * @param  tThrowable  The source throwable.
     */
    public ScriptConnectorException(Throwable tThrowable)
    {
        super(tThrowable);
    }

    /**
     * Constructs an instance of <code>TranslatorException</code> with the specified detail message.
     *
     * @param  sMessage  the detail message.
     */
    public ScriptConnectorException(String sMessage)
    {
        super(sMessage);
    }

    /**
     * Creates a new instance of <code>ScriptConnectorException</code> based on the the throwable.
     *
     * @param  sMessage    The additional message.
     * @param  tThrowable  The cause.
     */
    public ScriptConnectorException(String sMessage, Throwable tThrowable)
    {
        super(sMessage, tThrowable);
    }

    /**
     * Returns a String-representation of the object.
     *
     * @return  A String-representation of the object.
     */
    @Override
    public String toString()
    {
        return super.toString();
    }
}
