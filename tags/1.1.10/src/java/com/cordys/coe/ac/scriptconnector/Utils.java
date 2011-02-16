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
package com.cordys.coe.ac.scriptconnector;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;

/**
 * Contains utility methods for ScriptConnector.
 *
 * @author  mpoyhone
 */
public class Utils
{
    /**
     * SOAP namespace constant.
     */
    public static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * Same an Node.appendToChildren() but removes all extra namespace declarations (e.g.
     * xmlns:SOAP).
     *
     * @param  fromNode
     * @param  toNode
     * @param  destParentNode
     */
    public static void appendToChildrenCleanNamespaces(int fromNode, int toNode, int destParentNode)
    {
        for (int node = fromNode; node != 0;)
        {
            int nextNode = Node.getNextSibling(node);

            if (Node.getType(node) == NodeType.ELEMENT)
            {
                int newNode = Node.appendToChildren(node, destParentNode);

                cleanSoapNamespace(newNode);
            }
            else
            {
                Node.appendToChildren(node, destParentNode);
            }

            // Check if we have reached the last node. Note that the node reference is no longer
            // valid, but we just use the value here for comparison.
            if (node == toNode)
            {
                break;
            }

            node = nextNode;
        }
    }

    /**
     * Removes the SOAP namespace declaration from the given nodes.
     *
     * @param  node  fromNode
     */
    public static void cleanSoapNamespace(int node)
    {
        for (int i = 0, count = Node.getNumAttributes(node); i < count; i++)
        {
            String name = Node.getAttributeName(node, i + 1);
            boolean isNamespaceDecl = false;

            if ("xmlns".equals(name))
            {
                isNamespaceDecl = true;
            }
            else
            {
                String prefix = Node.getAttributePrefix(node, i + 1);

                if ((prefix != null) && prefix.equals("xmlns"))
                {
                    isNamespaceDecl = true;
                }
            }

            if (isNamespaceDecl)
            {
                String uri = Node.getAttribute(node, name);

                if (SOAP_NAMESPACE.equals(uri))
                {
                    Node.removeAttribute(node, name);
                    i--;
                }
            }
        }
    }

    /**
     * Removes all namespace declarations and namespace prefixes from the XML. A namespace prefix
     * will not be removed in the bound namespace URI is in the ignore list. Also whitespace text
     * nodes can be removed.
     *
     * @param  node              Root node.
     * @param  ignoreUris        Optional array of namespace URI's which will be not removed. Can be
     *                           <code>null</code>.
     * @param  removeWhitespace  If <code>true</code>, whitespace text node are removed.
     */
    public static void cleanXml(int node, String[] ignoreUris, boolean removeWhitespace)
    {
        // Remove all attributes which are not in the ignore list.
        int attrCount = Node.getNumAttributes(node);

        if (attrCount > 0)
        {
            String[] removeAttrTable = new String[attrCount];

            for (int i = 0; i < attrCount; i++)
            {
                String name = Node.getAttributeName(node, i + 1);
                boolean remove = false;

                if ("xmlns".equals(name))
                {
                    remove = true;
                }
                else
                {
                    String prefix = Node.getAttributePrefix(node, i + 1);

                    if ((prefix != null) && prefix.equals("xmlns"))
                    {
                        remove = true;
                    }
                }

                if (remove && (ignoreUris != null))
                {
                    String uri = Node.getAttribute(node, name);

                    for (String tmp : ignoreUris)
                    {
                        if ((tmp != null) && tmp.equals(uri))
                        {
                            remove = false;
                            break;
                        }
                    }
                }

                removeAttrTable[i] = (remove ? name : null);
            }

            for (String name : removeAttrTable)
            {
                if (name != null)
                {
                    Node.removeAttribute(node, name);
                }
            }
        }

        // Go through all child nodes.
        int child = Node.getFirstChild(node);

        while (child != 0)
        {
            int type = Node.getType(child);

            switch (type)
            {
                case NodeType.ELEMENT:
                    cleanXml(child, ignoreUris, removeWhitespace);
                    break;

                case NodeType.DATA:
                    if (removeWhitespace)
                    {
                        String value = Node.getData(child);

                        if (value != null)
                        {
                            int count = value.length();
                            boolean isWhitespace = true;

                            for (int i = 0; i < count; i++)
                            {
                                if (!Character.isWhitespace(value.charAt(i)))
                                {
                                    isWhitespace = false;
                                    break;
                                }
                            }

                            if (isWhitespace)
                            {
                                int nextChild = Node.getNextSibling(child);

                                Node.delete(child);
                                child = nextChild;
                                continue;
                            }
                        }
                    }
                    break;
            }

            child = Node.getNextSibling(child);
        }

        // Remove the namespace prefix.
        String prefix = Node.getPrefix(node);

        if (prefix != null)
        {
            // Remove the prefix only if the node namespace URI is not in the
            // ignore URI list.
            boolean removePrefix = true;

            if (ignoreUris != null)
            {
                String uri = Node.getNamespaceURI(node);

                for (String tmp : ignoreUris)
                {
                    if ((tmp != null) && tmp.equals(uri))
                    {
                        removePrefix = false;
                        break;
                    }
                }
            }

            if (removePrefix)
            {
                Node.setName(node, Node.getLocalName(node));
            }
        }
    }

    /**
     * Same an Node.insert() but removes all extra namespace declarations (e.g. xmlns:SOAP).
     *
     * @param  node
     * @param  destNode
     */
    public static void insertCleanNamespaces(int node, int destNode)
    {
        if (Node.getType(node) == NodeType.ELEMENT)
        {
            int newNode = Node.insert(node, destNode);

            cleanSoapNamespace(newNode);
        }
        else
        {
            Node.insert(node, destNode);
        }
    }

    /**
     * Same an Node.unlink() but removes all extra namespace declarations (e.g. xmlns:SOAP).
     *
     * @param   node
     *
     * @return  same an Node.unlink() but removes all extra namespace declarations (e.g.
     */
    public static int unlinkCleanNamespaces(int node)
    {
        if (Node.getType(node) == NodeType.ELEMENT)
        {
            int newNode = Node.unlink(node);

            cleanSoapNamespace(newNode);

            return newNode;
        }
        else
        {
            return Node.unlink(node);
        }
    }
}
