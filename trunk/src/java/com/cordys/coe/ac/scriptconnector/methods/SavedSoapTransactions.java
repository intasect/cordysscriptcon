/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.methods;

import com.cordys.coe.ac.scriptconnector.ScriptConnector;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches saved SOAP transactions.
 *
 * @author  mpoyhone
 */
public class SavedSoapTransactions
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SavedSoapTransactions.class);

    /**
     * This method processes the received request.
     *
     * @param   conn        ScriptConnector instance.
     * @param   bbRequest   The request-bodyblock.
     * @param   bbResponse  The response-bodyblock.
     *
     * @return  true if the connector has to send the response. If someone else sends the response
     *          false is returned.
     *
     * @throws  ScriptConnectorException
     */
    public static boolean processDeleteSavedSoapTransactions(ScriptConnector conn,
                                                             BodyBlock bbRequest,
                                                             BodyBlock bbResponse)
                                                      throws ScriptConnectorException
    {
        File savePath = getSavePath(conn);
        int requestNode = bbRequest.getXMLNode();
        String txnId = Node.getDataElement(requestNode, "TransactionId", "");

        if ((txnId == null) || (txnId.length() == 0))
        {
            throw new ScriptConnectorException("Parameter 'TransactionId' is not set.");
        }

        int maxFiles = conn.getScriptConfig().getMaxFilesPerTransaction();
        for (int counter = 1; counter < maxFiles; counter++)
        {
            String requestFileName = String.format("%s_request_%d.xml", txnId, counter);
            String responseFileName = String.format("%s_response_%d.xml", txnId, counter);
            File requestFile = new File(savePath, requestFileName);
            File responseFile = new File(savePath, responseFileName);

            if (!requestFile.exists())
            {
                break;
            }

            requestFile.delete();
            responseFile.delete();
        }

        return true;
    }

    /**
     * This method processes the received request.
     *
     * @param   conn        ScriptConnector instance.
     * @param   bbRequest   The request body block.
     * @param   bbResponse  The response body block.
     *
     * @return  true if the connector has to send the response. If someone else sends the response
     *          false is returned.
     *
     * @throws  ScriptConnectorException
     */
    public static boolean processGetSavedSoapTransactions(ScriptConnector conn, BodyBlock bbRequest,
                                                          BodyBlock bbResponse)
                                                   throws ScriptConnectorException
    {
        File savePath = getSavePath(conn);
        int requestNode = bbRequest.getXMLNode();
        int responseNode = bbResponse.getXMLNode();
        Document doc = Node.getDocument(responseNode);
        String txnId = Node.getDataElement(requestNode, "TransactionId", "");

        if ((txnId == null) || (txnId.length() == 0))
        {
            throw new ScriptConnectorException("Parameter 'TransactionId' is not set.");
        }

        // Get all request files which match the file pattern.
        final Pattern requestFilePattern = Pattern.compile(createFilterRegex(txnId));
        FilenameFilter filter = new FilenameFilter()
        {
            /**
             * @see  java.io.FilenameFilter#accept(java.io.File, java.lang.String)
             */
            public boolean accept(File dir, String name)
            {
                return requestFilePattern.matcher(name).matches();
            }
        };

        File[] requestFiles = savePath.listFiles(filter);
        FileEntry[] requestFileEntries = new FileEntry[requestFiles.length];

        for (int i = 0; i < requestFiles.length; i++)
        {
            requestFileEntries[i] = new FileEntry(requestFilePattern, requestFiles[i]);
        }

        // Sort the files according to the transaction ID and sequence ID.
        Comparator<FileEntry> fileComparator = new Comparator<FileEntry>()
        {
            public int compare(FileEntry e1, FileEntry e2)
            {
                int res = e1.fileTxnId.compareTo(e2.fileTxnId);

                return (res != 0) ? res : (e1.fileSeqId - e2.fileSeqId);
            }
        };

        Arrays.sort(requestFileEntries, fileComparator);

        // Read the files and create the response XML.
        for (FileEntry requestFileEntry : requestFileEntries)
        {
            File requestFile = requestFileEntry.requestFile;
            String responseFileName = String.format("%s_response_%s.xml",
                                                    requestFileEntry.fileTxnId,
                                                    requestFileEntry.fileSeqId);
            File responseFile = new File(savePath, responseFileName);

            // Create response XML for these two files.
            int txnNode = Node.getDocument(responseNode).createElement("transaction");

            try
            {
                Node.setAttribute(txnNode, "id", Integer.toString(requestFileEntry.fileSeqId));
                Node.setAttribute(txnNode, "name", requestFileEntry.fileTxnId);

                int txnRequestNode = Node.createElement("request", txnNode);

                try
                {
                    int node = doc.load(requestFile.getAbsolutePath());
                    String timestamp = Node.getAttribute(node, "scriptconnector-timestamp", "");

                    Node.removeAttribute(node, "scriptconnector-timestamp");
                    Node.appendToChildren(node, txnRequestNode);
                    Node.setAttribute(txnRequestNode, "timestamp", formatTimestamp(timestamp));
                }
                catch (Exception e)
                {
                    throw new ScriptConnectorException("Unable to read file: " + requestFile, e);
                }

                if (responseFile.exists())
                {
                    int txnResponseNode = Node.createElement("response", txnNode);

                    try
                    {
                        int node = doc.load(responseFile.getAbsolutePath());
                        String timestamp = Node.getAttribute(node, "scriptconnector-timestamp", "");

                        Node.removeAttribute(node, "scriptconnector-timestamp");
                        Node.appendToChildren(node, txnResponseNode);
                        Node.setAttribute(txnResponseNode, "timestamp", formatTimestamp(timestamp));
                    }
                    catch (Exception e)
                    {
                        throw new ScriptConnectorException("Unable to read file: " + responseFile,
                                                           e);
                    }
                }

                Node.appendToChildren(txnNode, responseNode);
                txnNode = 0;
            }
            finally
            {
                if (txnNode != 0)
                {
                    Node.delete(txnNode);
                    txnNode = 0;
                }
            }
        }

        return true;
    }

    /**
     * Marks the transaction to be saved.
     *
     * @param   conn       ScriptConnector instance.
     * @param   id         Transaction ID.
     * @param   rootNode   XML root node to be written.
     * @param   isRequest  If <code>true</code> this is the SOAP request XMl.
     *
     * @throws  ScriptConnectorException
     */
    public static void writeTransactionFile(ScriptConnector conn, String id, int rootNode,
                                            boolean isRequest)
                                     throws ScriptConnectorException
    {
        File savePath = getSavePath(conn);
        File file = null;

        int maxFiles = conn.getScriptConfig().getMaxFilesPerTransaction();
        for (int counter = 1; counter < maxFiles; counter++)
        {
            String fileName = String.format("%s_%s_%d.xml", id, isRequest ? "request" : "response",
                                            counter);
            File tmp = new File(savePath, fileName);

            try
            {
                // This is guaranteed to be atomic.
                if (tmp.createNewFile())
                {
                    file = tmp;
                    break;
                }
            }
            catch (IOException e)
            {
                throw new ScriptConnectorException("Unable to create file: " + tmp, e);
            }
        }

        if (file == null)
        {
            throw new ScriptConnectorException("Unable to create the transaction file.");
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Writing SOAP " + (isRequest ? "request" : "response") + " to file " + file);
        }

        // If the root node does have namespace declaration, declare it here.
        String prefix = Node.getPrefix(rootNode);
        String nsAttrib = (prefix != null) ? ("xmlns:" + prefix) : "xmlns";
        boolean namespaceDeclared = Node.getAttribute(rootNode, nsAttrib) != null;

        if (!namespaceDeclared)
        {
            Node.setAttribute(rootNode, nsAttrib, Node.getNamespaceURI(rootNode));
        }

        try
        {
            Node.setAttribute(rootNode, "scriptconnector-timestamp",
                              Long.toString(System.currentTimeMillis()));
            Node.writeToFile(rootNode, 0, file.getAbsolutePath(), Node.WRITE_PRETTY);
            Node.removeAttribute(rootNode, "scriptconnector-timestamp");
        }
        catch (XMLException e)
        {
            throw new ScriptConnectorException("Unable to write file: " + file, e);
        }

        // If we declared the namespace, remove the declaration.
        if (!namespaceDeclared)
        {
            Node.removeAttribute(rootNode, nsAttrib);
        }
    }

    /**
     * Creates a regular expression from filter pattern. Supported formats:
     *
     * <pre>
         a*b
         a?b
     * </pre>
     *
     * @param   filterPattern  Transaction ID pattern to be converted.
     *
     * @return  Converted regexp: ^(TXN_PATTERN)_request_(\\d+)\\.xml$
     */
    private static String createFilterRegex(String filterPattern)
    {
        StringBuilder sb = new StringBuilder(512);

        sb.append("^(");

        for (int j = 0; j < filterPattern.length(); j++)
        {
            char ch = filterPattern.charAt(j);

            switch (ch)
            {
                case '\\':
                    // Escape \
                    sb.append("\\\\");
                    break;

                case '?':
                    sb.append('.');
                    break;

                case '*':
                    sb.append(".*");
                    break;

                case '.':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '^':
                case '$':
                case '|':
                    // Escape these regexp characters.
                    sb.append("\\").append(ch);
                    break;

                default:
                    sb.append(ch);
                    break;
            }
        }

        sb.append(")_request_(\\d+)\\.xml$");

        return sb.toString();
    }

    /**
     * Formats the Java time stamp into SOAP format.
     *
     * @param   timestamp  Timestamp string.
     *
     * @return  Timestamp in SOAP format.
     */
    private static String formatTimestamp(String timestamp)
    {
        if ((timestamp == null) || (timestamp.length() == 0))
        {
            return "";
        }

        try
        {
            long ts = Long.parseLong(timestamp);
            Date d = new Date(ts);

            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(d);
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "Invalid timestamp: " + timestamp, e);
            return "";
        }
    }

    /**
     * Returns the transaction save path.
     *
     * @param   conn  ScriptConnector instance.
     *
     * @return  Transaction save path.
     *
     * @throws  ScriptConnectorException
     */
    private static File getSavePath(ScriptConnector conn)
                             throws ScriptConnectorException
    {
        File savePath = conn.getScriptConfig().getTransactionSavePath();

        if (savePath == null)
        {
            throw new ScriptConnectorException("SOAP transaction save folder is not configured.");
        }

        if (!savePath.exists())
        {
            throw new ScriptConnectorException("SOAP transaction save folder does not exist: " +
                                               savePath);
        }

        return savePath;
    }

    /**
     * File entry for processGetSavedSoapTransactions() method.
     *
     * @author  mpoyhone
     */
    private static class FileEntry
    {
        /**
         * File sequence number from the file name.
         */
        final int fileSeqId;
        /**
         * Transaction ID from the file name.
         */
        final String fileTxnId;
        /**
         * Saved request file.
         */
        final File requestFile;

        /**
         * Creates a new FileEntry object.
         *
         * @param  requestFilePattern  Request file regexp pattern.
         * @param  f                   Request file.
         */
        public FileEntry(Pattern requestFilePattern, File f)
        {
            Matcher m = requestFilePattern.matcher(f.getName());

            if (m.matches())
            {
                fileTxnId = m.group(1);
                fileSeqId = Integer.parseInt(m.group(2));
            }
            else
            {
                fileTxnId = "?";
                fileSeqId = -1;
            }

            requestFile = f;
        }
    }
}
