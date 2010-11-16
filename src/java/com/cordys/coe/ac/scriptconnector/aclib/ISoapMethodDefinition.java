/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
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
