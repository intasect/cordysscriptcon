/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import junit.framework.TestCase;

/**
 * Test cases for the Utils class.
 *
 * @author  mpoyhone
 */
public class UtilsTest extends TestCase
{
    /**
     * DOCUMENTME.
     */
    private static final Document doc = new Document();

    /**
     * Test method for {@link com.cordys.coe.ac.scriptconnector.Utils#cleanXml(int,
     * java.lang.String[], boolean)}.
     *
     * @throws  Exception  DOCUMENTME
     */
    public void testCleanXml_RemoveNamespaces()
                                       throws Exception
    {
        int node = 0;

        try
        {
            node = doc.parseString("<root xmlns='abc' xmlns:n1='n1:abc'><a/><b xmlns=''/><n1:c/></root>");
            Utils.cleanXml(node, null, false);

            assertEquals("<root><a/><b/><c/></root>", Node.writeToString(node, false));
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
     * Test method for {@link com.cordys.coe.ac.scriptconnector.Utils#cleanXml(int,
     * java.lang.String[], boolean)}.
     *
     * @throws  Exception  DOCUMENTME
     */
    public void testCleanXml_RemoveNamespaces_AttributePrefix()
                                                       throws Exception
    {
        int node = 0;

        try
        {
            node = doc.parseString("<root xmlns:n1='n1:abc' n1:attr=\"value\"></root>");
            Utils.cleanXml(node, null, false);

            assertEquals("<root attr=\"value\"/>", Node.writeToString(node, false));
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
     * Test method for {@link com.cordys.coe.ac.scriptconnector.Utils#cleanXml(int,
     * java.lang.String[], boolean)}.
     *
     * @throws  Exception  DOCUMENTME
     */
    public void testCleanXml_RemoveNamespaces_AttributePrefix_Filtered()
                                                                throws Exception
    {
        int node = 0;

        try
        {
            node = doc.parseString("<root xmlns:n1='n1:abc' n1:attr=\"value\"></root>");
            Utils.cleanXml(node, new String[] { "n1:abc" }, false);

            assertEquals("<root xmlns:n1=\"n1:abc\" n1:attr=\"value\"/>",
                         Node.writeToString(node, false));
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
     * Test method for {@link com.cordys.coe.ac.scriptconnector.Utils#cleanXml(int,
     * java.lang.String[], boolean)}.
     *
     * @throws  Exception  DOCUMENTME
     */
    public void testCleanXml_RemoveNamespaces_Filtered()
                                                throws Exception
    {
        int node = 0;

        try
        {
            node = doc.parseString("<root xmlns='abc' xmlns:n1='n1:abc'><a/><b xmlns=''/><n1:c/></root>");
            Utils.cleanXml(node, new String[] { "abc" }, false);

            assertEquals("<root xmlns=\"abc\"><a/><b/><c/></root>",
                         Node.writeToString(node, false));
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
     * Test method for {@link com.cordys.coe.ac.scriptconnector.Utils#cleanXml(int,
     * java.lang.String[], boolean)}.
     *
     * @throws  Exception  DOCUMENTME
     */
    public void testCleanXml_RemoveNamespaces_PreservePrefix()
                                                      throws Exception
    {
        int node = 0;

        try
        {
            node = doc.parseString("<root xmlns='abc' xmlns:n1='n1:abc'><a/><b xmlns=''/><n1:c/></root>");
            Utils.cleanXml(node, new String[] { "n1:abc" }, false);

            assertEquals("<root xmlns:n1=\"n1:abc\"><a/><b/><n1:c/></root>",
                         Node.writeToString(node, false));
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
     * Test method for {@link com.cordys.coe.ac.scriptconnector.Utils#cleanXml(int,
     * java.lang.String[], boolean)}.
     *
     * @throws  Exception  DOCUMENTME
     */
    public void testCleanXml_RemoveWhitespace()
                                       throws Exception
    {
        int node = 0;

        try
        {
            node = doc.parseString("<root>    <a/>\r\n<b>  \n  </b><c>va  lue</c></root>");
            Utils.cleanXml(node, new String[] { "n1:abc" }, true);

            assertEquals("<root><a/><b/><c>va  lue</c></root>", Node.writeToString(node, false));
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
     * @see  junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown()
                     throws Exception
    {
        super.tearDown();
    }

    /**
     * @see  junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp()
                  throws Exception
    {
        super.setUp();
    }
}
