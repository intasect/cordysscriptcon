/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.aclib;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Document;

/**
 * Interface for accessing information about the incoming SOAP request and setting the SOAP
 * response.
 *
 * @author  mpoyhone
 */
public interface ISoapRequestContext
{
    /**
     * Adds an empty XML element to the response.
     *
     * @param   elemName
     *
     * @return  Created XML node.
     */
    int addResponseElement(String elemName);

    /**
     * Adds the given XML node to the response.
     *
     * @param   node  Node to be added.
     *
     * @return  Node reference in the SOAP response.
     */
    int addResponseElement(int node);

    /**
     * Adds a new text element to the SOAP response.
     *
     * @param   elemName   Element name.
     * @param   elemValue  Text.
     *
     * @return  Created element node.
     */
    int addResponseElement(String elemName, String elemValue);

    /**
     * Returns the SOAP method definition.
     *
     * @return
     */
    ISoapMethodDefinition getMethodDefinition();

    /**
     * Returns the current NOM document.
     *
     * @return
     */
    Document getNomDocument();

    /**
     * Returns the actual SOAP request body block.
     *
     * @return
     */
    BodyBlock getRequestBodyBlock();

    /**
     * Returns the SOAP request method node.
     *
     * @return
     */
    int getRequestMethodNode();

    /**
     * Returns the SOAP request organization DN.
     *
     * @return  returns the SOAP request organization DN.
     */
    String getRequestOrganizationDn();

    /**
     * Returns the SOAP request user DN.
     *
     * @return  returns the SOAP request user DN.
     */
    String getRequestUserDn();

    /**
     * Returns the SOAP response method node.
     *
     * @return
     */
    int getResponseMethodNode();
}
