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

/**
 * Interface for accessing information from the SOAP method definition.
 *
 * @author  mpoyhone
 */
public interface ISoapMethodDefinition
{
    /**
     * Returns the method implementation XML.
     *
     * @return
     */
    int getImplementation();

    /**
     * Returns the method LDAP DN.
     *
     * @return
     */
    String getMethodDN();

    /**
     * Returns the method name.
     *
     * @return
     */
    String getMethodName();

    /**
     * Returns the method namespace.
     *
     * @return
     */
    String getMethodNamespace();

    /**
     * Returns the method type.
     *
     * @return
     */
    String getType();

    /**
     * Returns the method WSDL as a string.
     *
     * @return
     */
    String getWSDL();
}
