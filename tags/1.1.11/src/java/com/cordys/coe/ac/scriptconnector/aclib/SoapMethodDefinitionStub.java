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

import com.eibus.xml.nom.Node;

/**
 * An implementation of the SOAP method definition interface for stubs.
 *
 * @author  mpoyhone
 */
public class SoapMethodDefinitionStub
    implements ISoapMethodDefinition
{
    /**
     * Contains a dummy DN.
     */
    private String methodDn;
    /**
     * Contains the SOAP method name.
     */
    private String methodName;
    /**
     * Contains the SOAP method namespace.
     */
    private String methodNamespace;
    /**
     * Contains a dummy type.
     */
    private String methodType = "DUMMY";
    /**
     * Contains method WSDL, if set.
     */
    private String methodWsdl;

    /**
     * Constructor for SoapMethodDefinitionStub.
     *
     * @param  requestMethodNode
     */
    public SoapMethodDefinitionStub(int requestMethodNode)
    {
        methodName = Node.getLocalName(requestMethodNode);
        methodNamespace = Node.getNamespaceURI(requestMethodNode);
        methodDn = "dummy-dn-for-" + methodName;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getImplementation()
     */
    public int getImplementation()
    {
        throw new IllegalStateException("getImplementation() is not supported.");
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getMethodDN()
     */
    public String getMethodDN()
    {
        return methodDn;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getMethodName()
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getMethodNamespace()
     */
    public String getMethodNamespace()
    {
        return methodNamespace;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getType()
     */
    public String getType()
    {
        return methodType;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getWSDL()
     */
    public String getWSDL()
    {
        return methodWsdl;
    }

    /**
     * Sets the methodDn.
     *
     * @param  methodDn  The methodDn to be set.
     */
    public void setMethodDn(String methodDn)
    {
        this.methodDn = methodDn;
    }

    /**
     * Sets the methodName.
     *
     * @param  methodName  The methodName to be set.
     */
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    /**
     * Sets the methodNamespace.
     *
     * @param  methodNamespace  The methodNamespace to be set.
     */
    public void setMethodNamespace(String methodNamespace)
    {
        this.methodNamespace = methodNamespace;
    }

    /**
     * Sets the methodType.
     *
     * @param  methodType  The methodType to be set.
     */
    public void setMethodType(String methodType)
    {
        this.methodType = methodType;
    }

    /**
     * Sets the methodWsdl.
     *
     * @param  methodWsdl  The methodWsdl to be set.
     */
    public void setMethodWsdl(String methodWsdl)
    {
        this.methodWsdl = methodWsdl;
    }
}
