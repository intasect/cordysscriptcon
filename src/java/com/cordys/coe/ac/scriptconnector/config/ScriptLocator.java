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
package com.cordys.coe.ac.scriptconnector.config;

import java.io.File;

/**
 * Class that binds together the method name and namespace for uniquely identifying a script.
 *
 * @author  mpoyhone
 */
public class ScriptLocator
{
    /**
     * Method name.
     */
    private String methodName;
    /**
     * Method namespace.
     */
    private String namespace;
    /**
     * Contains the script file.
     */
    private File scriptFile;

    /**
     * Constructor for Pair.
     *
     * @param  name       first Method name.
     * @param  namespace  second Method namespace
     */
    public ScriptLocator(String name, String namespace)
    {
        this.methodName = name;
        this.namespace = namespace;
    }

    /**
     * Constructor for Pair.
     *
     * @param  name        first Method name.
     * @param  namespace   second Method namespace
     * @param  scriptFile  Script file.
     */
    public ScriptLocator(String name, String namespace, File scriptFile)
    {
        this.methodName = name;
        this.namespace = namespace;
        this.scriptFile = scriptFile;
    }

    /**
     * @see  java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        final ScriptLocator other = (ScriptLocator) obj;

        if (methodName == null)
        {
            if (other.methodName != null)
            {
                return false;
            }
        }
        else if (!methodName.equals(other.methodName))
        {
            return false;
        }

        if (namespace == null)
        {
            if (other.namespace != null)
            {
                return false;
            }
        }
        else if (!namespace.equals(other.namespace))
        {
            return false;
        }
        return true;
    }

    /**
     * @see  java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = (PRIME * result) + ((methodName == null) ? 0 : methodName.hashCode());
        result = (PRIME * result) + ((namespace == null) ? 0 : namespace.hashCode());
        return result;
    }

    /**
     * @see  java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sbRes = new StringBuilder(100);

        sbRes.append("[");

        if (methodName != null)
        {
            sbRes.append(methodName);
        }
        else
        {
            sbRes.append("ANY");
        }

        sbRes.append(", ");

        if (namespace != null)
        {
            sbRes.append(namespace);
        }
        else
        {
            sbRes.append("ANY");
        }

        sbRes.append("]");

        return sbRes.toString();
    }

    /**
     * Returns the methodName.
     *
     * @return  Returns the methodName.
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * Returns the namespace.
     *
     * @return  Returns the namespace.
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * Returns the scriptFile.
     *
     * @return  Returns the scriptFile.
     */
    public File getScriptFile()
    {
        return scriptFile;
    }
}
