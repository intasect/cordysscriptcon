/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.soap;

import com.cordys.coe.ac.scriptconnector.Utils;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;

import com.eibus.connector.nom.SOAPMessage;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;
import com.eibus.xml.xpath.NodeSet;
import com.eibus.xml.xpath.ResultNode;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * SOAP message container for SOAP communication from scripts. This encapsulates the request body
 * and optionally SOAP headers. The ScriptConnectorTransaction class keeps track of instances
 * created through the createSoapMessage() method and will clear them via the clear() method after
 * the SOAP transaction ends. If an instance is created outside createSoapMessage() method, then the
 * clear() method must be called explicitly to delete the used NOM nodes.
 *
 * @author  mpoyhone
 */
public class ScriptSoapMessage
{
    /**
     * Contains XPath namespace bindings.
     */
    private static final XPathMetaInfo xpathNamespaces = new XPathMetaInfo();
    /**
     * XPath to select standard SOAP header elements.
     */
    private static final XPath cordysHeaderElements = XPath.getXPathInstance("./cordys:header");
    /**
     * XPath to select standard SOAP header elements.
     */
    private static final XPath i8nHeaderElements = XPath.getXPathInstance("./int:international");

    static
    {
        xpathNamespaces.addNamespaceBinding("int", "http://www.w3.org/2005/09/ws-i18n");
        xpathNamespaces.addNamespaceBinding("cordys", "http://schemas.cordys.com/General/1.0/");
    }

    /**
     * Used for SOAP responses which do not have a SOAP method under the SOAP body.
     */
    private boolean deleteSoapMethod;
    /**
     * Document for XML nodes.
     */
    private Document doc;
    /**
     * Destination organization DN.
     */
    private String orgDn;
    /**
     * Root node of the current SOAP header. Children of this node are added to the actual SOAP
     * header.
     */
    private int soapHeaderRoot;
    /**
     * SOAP method root node.
     */
    private int soapMethodRoot;
    /**
     * Contains the SOAP namespace prefix for this message.
     */
    private String soapNamespacePrefix;
    /**
     * If <code>true</code> children of the soapHeaderRoot node are added to the SOAP request,
     * otherwise, the whole method node is replaced with soapMethodRoot node.
     */
    private boolean useMethodChildren;
    /**
     * Sending used DN.
     */
    private String userDn;

    /**
     * Creates a new ScriptSoapMessage object.
     *
     * @param  doc  NOM document.
     */
    public ScriptSoapMessage(Document doc)
    {
        this.doc = doc;
    }

    /**
     * Adds a SOAP method child element from the given XML.
     *
     * @param   xml          XML string.
     * @param   addChildren  If <code>true</code>, only children of the given XML are added.
     *
     * @throws  ScriptConnectorException
     */
    public void addMethodChildAsString(String xml, boolean addChildren)
                                throws ScriptConnectorException
    {
        if ((xml == null) || (xml.length() == 0))
        {
            return;
        }

        int node = 0;

        try
        {
            node = doc.parseString(xml);

            if (soapMethodRoot == 0)
            {
                soapMethodRoot = doc.createElement("root");
                useMethodChildren = true;
            }

            if (addChildren)
            {
                int child = Node.getFirstElement(node);

                while (child != 0)
                {
                    int next = Node.getNextSibling(child);

                    Node.appendToChildren(child, soapMethodRoot);

                    child = next;
                }
            }
            else
            {
                Node.appendToChildren(node, soapMethodRoot);
                node = 0;
            }
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to parse the SOAP method XML.", e);
        }
        finally
        {
            if (node != 0)
            {
                Node.delete(node);
                node = 0;
            }
        }
    }

    /**
     * Appends the header and method nodes to the SOAP envelope.
     *
     * @param  soapEnv           Destination SOAP envelope node.
     * @param  canReplaceMethod  If <code>true</code> the SOAP method node is replaced, otherwise
     *                           only the children are copied.
     */
    public void appendToSoapEnvelope(int soapEnv, boolean canReplaceMethod)
    {
        if (soapHeaderRoot != 0)
        {
            int header = SOAPMessage.getRootHeaderNode(soapEnv);

            if (header != 0)
            {
                Utils.appendToChildrenCleanNamespaces(Node.getFirstChild(soapHeaderRoot),
                                                      Node.getLastChild(soapHeaderRoot), header);
            }
        }

        int body = SOAPMessage.getRootBodyNode(soapEnv);
        int method = (body != 0) ? Node.getFirstElement(body) : 0;

        if (soapMethodRoot != 0)
        {
            if (method != 0) {
                if (useMethodChildren)
                {
                    Utils.appendToChildrenCleanNamespaces(Node.getFirstChild(soapMethodRoot),
                                                          Node.getLastChild(soapMethodRoot), method);
                }
                else
                {
                    if (canReplaceMethod)
                    {
                        Utils.insertCleanNamespaces(soapMethodRoot, method);
                        Node.delete(method);
                        soapMethodRoot = 0;
                    }
                    else
                    {
                        Utils.appendToChildrenCleanNamespaces(Node.getFirstChild(soapMethodRoot),
                                                              Node.getLastChild(soapMethodRoot),
                                                              method);
    
                        // TODO: Add attributes too.
                    }
                }
            } else {
                Utils.appendToChildrenCleanNamespaces(soapMethodRoot, 0, body);
            }
        }
        else if (deleteSoapMethod && (method != 0))
        {
            // The original SOAP message didn't have a SOAP method, so we have to
            // now delete this one too.
            Node.delete(method);
        }
    }

    /**
     * Deletes all NOM node references.
     */
    public void clear()
    {
        if (soapHeaderRoot != 0)
        {
            Node.delete(soapHeaderRoot);
            soapHeaderRoot = 0;
        }

        if (soapMethodRoot != 0)
        {
            Node.delete(soapMethodRoot);
            soapMethodRoot = 0;
        }

        useMethodChildren = false;
        deleteSoapMethod = false;
        soapNamespacePrefix = null;
    }

    /**
     * Reads the SOAP header and method from the given SOAP envelope.
     *
     * @param  soapEnv  Source SOAP envelope.
     */
    public void readFromSoapMessage(int soapEnv)
    {
        clear();

        soapNamespacePrefix = Node.getPrefix(soapEnv);

        int header = SOAPMessage.getRootHeaderNode(soapEnv);

        if (header != 0)
        {
            soapHeaderRoot = Node.unlink(header);

            // Remove any standard headers.
            deleteNodes(soapHeaderRoot, cordysHeaderElements, xpathNamespaces);
            deleteNodes(soapHeaderRoot, i8nHeaderElements, xpathNamespaces);
        }

        int body = SOAPMessage.getRootBodyNode(soapEnv);
        int method = (body != 0) ? Node.getFirstElement(body) : 0;

        if (method != 0)
        {
            soapMethodRoot = Utils.unlinkCleanNamespaces(method);
            useMethodChildren = false;
        }
        else
        {
            // No SOAP method found, so we will delete the SOAP method, when this message it written
            // to a SOAP envelope.
            deleteSoapMethod = true;
        }
    }

    /**
     * Removes all namespace declarations from the method child elements.
     *
     * @param  ignoreUris  An array of namespace URI's which are excluded from clean up.
     */
    public void removeChildElementNamespaces(String[] ignoreUris)
    {
        if (soapMethodRoot == 0)
        {
            return;
        }

        int child = Node.getFirstChild(soapMethodRoot);

        while (child != 0)
        {
            if (Node.getType(child) == NodeType.ELEMENT)
            {
                Utils.cleanXml(child, ignoreUris, false);
            }

            child = Node.getNextSibling(child);
        }
    }

    /**
     * @see  java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder(512);

        if (userDn != null)
        {
            msg.append("User DN: " + userDn).append("\n");
        }

        if (orgDn != null)
        {
            msg.append("Organization DN: " + orgDn).append("\n");
        }

        if (soapHeaderRoot != 0)
        {
            msg.append("SOAP Header XML: " + Node.writeToString(soapHeaderRoot, true));
        }

        if (soapMethodRoot != 0)
        {
            msg.append("SOAP Method XML: " + Node.writeToString(soapMethodRoot, true));
        }

        return msg.toString();
    }

    /**
     * Returns the SOAP header XML as string.
     *
     * @return  SOAP header XML as string.
     */
    public String getHeaderAsString()
    {
        return (soapHeaderRoot != 0) ? Node.writeToString(soapHeaderRoot, false) : null;
    }

    /**
     * Returns the SOAP method XML as string.
     *
     * @return  SOAP method XML as string.
     */
    public String getMethodAsString()
    {
        return (soapMethodRoot != 0) ? Node.writeToString(soapMethodRoot, false) : null;
    }

    /**
     * Returns the SOAP method name.
     *
     * @return  The SOAP method name or null if the method node is not set.
     */
    public String getMethodName()
    {
        if (soapMethodRoot == 0)
        {
            return null;
        }

        return Node.getLocalName(soapMethodRoot);
    }

    /**
     * Returns the SOAP method namespace.
     *
     * @return  The SOAP method namespace or null if the method node is not set.
     */
    public String getNamespace()
    {
        if (soapMethodRoot == 0)
        {
            return null;
        }

        return Node.getNamespaceURI(soapMethodRoot);
    }

    /**
     * Returns the orgDn.
     *
     * @return  Returns the orgDn.
     */
    public String getOrgDn()
    {
        return orgDn;
    }

    /**
     * Returns the soapHeaderRoot.
     *
     * @return  Returns the soapHeaderRoot.
     */
    public int getSoapHeaderRoot()
    {
        return soapHeaderRoot;
    }

    /**
     * Returns the soapMethodRoot.
     *
     * @return  Returns the soapMethodRoot.
     */
    public int getSoapMethodRoot()
    {
        return soapMethodRoot;
    }

    /**
     * Returns the soapNamespacePrefix.
     *
     * @return  Returns the soapNamespacePrefix.
     */
    public String getSoapNamespacePrefix()
    {
        return soapNamespacePrefix;
    }

    /**
     * Returns the userDn.
     *
     * @return  Returns the userDn.
     */
    public String getUserDn()
    {
        return userDn;
    }

    /**
     * Sets the SOAP header from the XML string.
     *
     * @param   xml  New SOAP header XML. Children of this XML are added to the actual header.
     *
     * @throws  ScriptConnectorException
     */
    public void setHeaderAsString(String xml)
                           throws ScriptConnectorException
    {
        int node = 0;

        try
        {
            if ((xml != null) && (xml.length() > 0))
            {
                node = doc.parseString(xml);
            }
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to parse the SOAP header XML.", e);
        }

        if (soapHeaderRoot != 0)
        {
            Node.delete(soapHeaderRoot);
        }

        soapHeaderRoot = node;
    }

    /**
     * Sets the SOAP method from the XML string.
     *
     * @param   xml          New SOAP method XML.
     * @param   addChildren  If <code>true</code>, the children of this XML are added to the SOAP
     *                       message appendToSoapEnvelope() method.
     *
     * @throws  ScriptConnectorException
     */
    public void setMethodAsString(String xml, boolean addChildren)
                           throws ScriptConnectorException
    {
        int node = 0;

        try
        {
            if ((xml != null) && (xml.length() > 0))
            {
                node = doc.parseString(xml);
            }
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to parse the SOAP method XML.", e);
        }

        if (soapMethodRoot != 0)
        {
            Node.delete(soapMethodRoot);
        }

        soapMethodRoot = node;
        useMethodChildren = addChildren;
    }

    /**
     * Sets the orgDn.
     *
     * @param  orgDn  The orgDn to be set.
     */
    public void setOrgDn(String orgDn)
    {
        this.orgDn = orgDn;
    }

    /**
     * Sets the soapNamespacePrefix.
     *
     * @param  soapNamespacePrefix  The soapNamespacePrefix to be set.
     */
    public void setSoapNamespacePrefix(String soapNamespacePrefix)
    {
        this.soapNamespacePrefix = soapNamespacePrefix;
    }

    /**
     * Sets the userDn.
     *
     * @param  userDn  The userDn to be set.
     */
    public void setUserDn(String userDn)
    {
        this.userDn = userDn;
    }

    /**
     * Utility method to delete nodes with the given XPath.
     *
     * @param  rootNode           XPath root node.
     * @param  xpath              XPath object.
     * @param  namespaceMappings  Optional namespace mappings.
     */
    private static void deleteNodes(int rootNode, XPath xpath, XPathMetaInfo namespaceMappings)
    {
        NodeSet ns = xpath.selectNodeSet(rootNode, namespaceMappings);

        while (ns.hasNext())
        {
            long resNode = ns.next();
            int node = ResultNode.getElementNode(resNode);

            if (node != 0)
            {
                Node.delete(node);
            }
        }
    }
}
