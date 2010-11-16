/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.aclib;

import com.eibus.directory.soap.DN;

import com.eibus.soap.MethodDefinition;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;

/**
 * Implements the SOAP method definition interface.
 *
 * @author  mpoyhone
 */
public class SoapMethodDefinitionImpl
    implements ISoapMethodDefinition
{
    /**
     * Contains the actual method definition.
     */
    private MethodDefinition methodDefinition;

    /**
     * Constructor for SoapMethodDefinitionImpl.
     *
     * @param  methodDefinition
     */
    public SoapMethodDefinitionImpl(MethodDefinition methodDefinition)
    {
        this.methodDefinition = methodDefinition;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getImplementation()
     */
    public int getImplementation()
    {
        return methodDefinition.getImplementation();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getMethodDN()
     */
    public String getMethodDN()
    {
        DN tmpDn = methodDefinition.getMethodDN();
        String methodDn = (tmpDn != null) ? tmpDn.toString() : null;

        return methodDn;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getMethodName()
     */
    public String getMethodName()
    {
        return methodDefinition.getMethodName();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getMethodNamespace()
     */
    public String getMethodNamespace()
    {
        return methodDefinition.getNamespace();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getType()
     */
    public String getType()
    {
        return methodDefinition.getType();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.aclib.ISoapMethodDefinition#getWSDL()
     */
    public String getWSDL()
    {
        LDAPEntry methodLdapEntry = methodDefinition.getEntry();
        LDAPAttribute attr;
        String value;

        value = null;

        if ((attr = methodLdapEntry.getAttribute("busmethodwsdl")) != null)
        {
            value = attr.getStringValue();
        }

        if (value == null)
        {
            if ((attr = methodLdapEntry.getAttribute("busmethodsignature")) != null)
            {
                value = attr.getStringValue();
            }
        }

        return ((value != null) && (value.length() > 0)) ? value : null;
    }
}
