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
