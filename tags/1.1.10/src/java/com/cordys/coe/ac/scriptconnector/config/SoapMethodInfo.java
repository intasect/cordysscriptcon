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

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.cordys.coe.ac.scriptconnector.ScriptConnector;
import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.util.xml.dom.XMLHelper;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

/**
 * Contains cached information about the Cordys method.
 *
 * @author  mpoyhone
 */
public class SoapMethodInfo
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SoapMethodInfo.class);
    /**
     * Contains the method LDAP DN.
     */
    private String methodDn;
    /**
     * Contains the response element name read from the WSDL.
     */
    private String responseElementName;
    /**
     * Contains the response element namespace read from the WSDL.
     */
    private String responseElementNamespace;

    /**
     * Constructor for SoapMethodInfo.
     *
     * @param  connector       Script connector instance.
     * @param  requestContext  SOAP request.
     */
    public SoapMethodInfo(ScriptConnector connector, ISoapRequestContext requestContext)
    {
        parse(requestContext);
    }

    /**
     * Returns the methodDn.
     *
     * @return  Returns the methodDn.
     */
    public String getMethodDn()
    {
        return methodDn;
    }

    /**
     * Returns the responseElementName.
     *
     * @return  Returns the responseElementName.
     */
    public String getResponseElementName()
    {
        return responseElementName;
    }

    /**
     * Returns the responseElementNamespace.
     *
     * @return  Returns the responseElementNamespace.
     */
    public String getResponseElementNamespace()
    {
        return responseElementNamespace;
    }

    /**
     * Parses the SOAP method info..
     *
     * @param  requestContext  SOAP request.
     */
    private void parse(ISoapRequestContext requestContext)
    {
        // Use the method name and namespace from the actual request.
        String methodName = Node.getLocalName(requestContext.getRequestMethodNode());
        String methodNamespace = Node.getNamespaceURI(requestContext.getRequestMethodNode());

        if ((methodName == null) || (methodName.length() == 0))
        {
            if (LOG.isWarningEnabled())
            {
                LOG.log(Severity.WARN, "Method name is null or an empty string.");
            }
            return;
        }

        QName methodQName = new QName(methodNamespace, methodName);

        methodDn = requestContext.getMethodDefinition().getMethodDN();

        if ((methodDn == null) || (methodDn.length() == 0))
        {
            throw new IllegalArgumentException("Method DN could not be determined.");
        }

        String wsdlStr = requestContext.getMethodDefinition().getWSDL();

        if (wsdlStr != null)
        {
            // WSDL found from LDAP, try to parse it and find the response element name.
            Document doc;

            try
            {
                doc = XMLHelper.createDocumentFromXML(wsdlStr);
            }
            catch (Exception e)
            {
                LOG.log(Severity.ERROR, "Unable to parse the WSDL", e);
                return;
            }

            try
            {
                WSDLFactory factory = WSDLFactory.newInstance();
                WSDLReader reader = factory.newWSDLReader();
                Definition wsdlDef;

                reader.setFeature("javax.wsdl.importDocuments", true);
                wsdlDef = reader.readWSDL(null, doc);

                Message requestMsg = null;
                Message responseMsg = null;

                msg_loop:
                for (Object mo : getWsdlMessages(wsdlDef))
                {
                    Message msg = (Message) mo;

                    for (Object po : msg.getParts().values())
                    {
                        Part part = (Part) po;
                        QName elementName = part.getElementName();

                        if (methodQName.equals(elementName))
                        {
                            requestMsg = msg;
                            break msg_loop;
                        }
                    }
                }

                if (requestMsg != null)
                {
                    port_loop:
                    for (Object po : getWsdlPortTypes(wsdlDef))
                    {
                        PortType type = (PortType) po;

                        for (Object oo : type.getOperations())
                        {
                            Operation operation = (Operation) oo;
                            Input opInput = operation.getInput();
                            Message opInputMsg = (opInput != null) ? opInput.getMessage() : null;

                            if (opInputMsg != null)
                            {
                                if (opInputMsg.getQName().equals(requestMsg.getQName()))
                                {
                                    Output opOutput = operation.getOutput();

                                    responseMsg = opOutput.getMessage();
                                    break port_loop;
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("No input message found from the WSDL.");
                    }
                }

                if (responseMsg != null)
                {
                    Collection<?> outputParts = responseMsg.getParts().values();

                    if (outputParts.size() > 0)
                    {
                        Part outputPart = (Part) outputParts.iterator().next();

                        if (outputPart != null)
                        {
                            QName outputElementQName = outputPart.getElementName();

                            if (outputElementQName != null)
                            {
                                responseElementName = outputElementQName.getLocalPart();
                                responseElementNamespace = outputElementQName.getNamespaceURI();

                                if (LOG.isDebugEnabled())
                                {
                                    LOG.debug(String.format("Found response element name '%s' and namespace '%s'.",
                                                            responseElementName,
                                                            responseElementNamespace));
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("No output message found from the WSDL.");
                    }
                }
            }
            catch (Exception e)
            {
                LOG.log(Severity.ERROR, "Unable to parse the WSDL.", e);
                return;
            }
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("WSDL is not set in LDAP.");
            }
        }
    }

    /**
     * Recursively returns all WSDL message object from the definition.
     *
     * @param   def  Definition object.
     *
     * @return  A list of found message objects.
     */
    private List<Message> getWsdlMessages(Definition def)
    {
        List<Message> res = new ArrayList<Message>(10);

        getWsdlMessages(def, res, new IdentityHashMap<Definition, Boolean>());

        return res;
    }

    /**
     * Recursively returns all WSDL message object from the definition.
     *
     * @param  def      Definition object.
     * @param  resList  Message objects are addef to this list.
     * @param seenDefinitions Contains seen definitions to overcome infinite loops.
     */
    private void getWsdlMessages(Definition def, List<Message> resList, Map<Definition, Boolean> seenDefinitions)
    {
        if (seenDefinitions.containsKey(def)) {
            return;
        }
        
        seenDefinitions.put(def, Boolean.TRUE);
        
        for (Object mo : def.getMessages().values())
        {
            Message msg = (Message) mo;

            resList.add(msg);
        }

        for (Object ilo : def.getImports().values())
        {
            Vector<?> impList = (Vector<?>) ilo;

            if (impList != null)
            {
                for (Object io : impList)
                {
                    Import imp = (Import) io;
                    Definition impDef = imp.getDefinition();

                    if (impDef != null)
                    {
                        getWsdlMessages(impDef, resList, seenDefinitions);
                    }
                }
            }
        }
    }

    /**
     * Recursively returns all WSDL port type object from the definition.
     *
     * @param   def  Definition object.
     *
     * @return  A list of found port type objects.
     */
    private List<PortType> getWsdlPortTypes(Definition def)
    {
        List<PortType> res = new ArrayList<PortType>(10);

        getWsdlPortTypes(def, res, new IdentityHashMap<Definition, Boolean>());

        return res;
    }

    /**
     * Recursively returns all WSDL port type object from the definition.
     *
     * @param  def      Definition object.
     * @param  resList  Port type objects are added to this list.
     */
    private void getWsdlPortTypes(Definition def, List<PortType> resList, Map<Definition, Boolean> seenDefinitions)
    {
        if (seenDefinitions.containsKey(def)) {
            return;
        }
        
        seenDefinitions.put(def, Boolean.TRUE);
        
        for (Object mo : def.getPortTypes().values())
        {
            PortType portType = (PortType) mo;

            resList.add(portType);
        }

        for (Object ilo : def.getImports().values())
        {
            Vector<?> impList = (Vector<?>) ilo;

            if (impList != null)
            {
                for (Object io : impList)
                {
                    Import imp = (Import) io;
                    Definition impDef = imp.getDefinition();

                    if (impDef != null)
                    {
                        getWsdlPortTypes(impDef, resList, seenDefinitions);
                    }
                }
            }
        }
    }
}
