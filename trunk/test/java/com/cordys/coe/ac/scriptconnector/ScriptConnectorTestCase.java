/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.aclib.NomConnectorStub;
import com.cordys.coe.ac.scriptconnector.aclib.SoapMethodDefinitionStub;
import com.cordys.coe.ac.scriptconnector.aclib.SoapRequestContextStub;
import com.cordys.coe.ac.scriptconnector.config.ScriptConnectorConfiguration;
import com.cordys.coe.ac.scriptconnector.config.ScriptLocator;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.test.junit.FileTestUtils;
import com.cordys.coe.util.test.junit.NomTestCase;
import com.eibus.connector.nom.SOAPMessage;
import com.eibus.util.logger.config.LoggerConfigurator;
import com.eibus.xml.nom.Node;

/**
 * Base class for ScriptConnector test cases.
 *
 * @author mpoyhone
 */
public abstract class ScriptConnectorTestCase extends NomTestCase
{
    protected File configFolder;
    protected boolean cleanConfigFolder = true;
    /**
     * Contains a stub NOM connector for catching SOAP requests from the application connector.
     */
    protected NomConnectorStub nomConnector;
    /**
     * Contains file where the current logger configuration was loaded.
     */
    protected static File loggerConfigFile;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        String className = getClass().getName().replaceFirst(".*\\.([^.]+)$", "$1");
        
        configFolder = new File("./build/test/" + className);

        if (cleanConfigFolder) {
            try
            {
                FileTestUtils.initializeFolder(configFolder);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fail(e.getMessage());
            }        
        }
        
        initLogging("Log4jConfiguration.xml", ScriptConnectorTestCase.class);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public static void initLogging(String logFilePath, Class<?> refClass)
    {
        File file = FileUtils.getResourceFile(logFilePath, refClass);
        
        initLogging(file);
    }

    public static void initLogging(File logFile)
    {
        if (loggerConfigFile != null && loggerConfigFile.equals(logFile)) {
            return;
        }
        
        LoggerConfigurator.initLogger(logFile.getAbsolutePath());
        loggerConfigFile = logFile;
    }
    
    public File createTextFile(String name, String contents) throws IOException {
        return createTextFile(name, contents, "UTF-8");
    }
    
    public File createTextFile(String name, String contents, String encoding) throws IOException {
        return createTextFile(new File(configFolder, name), contents, encoding);
    }
    
    public File createTextFile(File f, String contents) throws IOException {
        return createTextFile(f, contents, "UTF-8");
    }
    
    public File createTextFile(File f, String contents, String encoding) throws IOException {
        OutputStream os = null;
        
        try {
            os = new FileOutputStream(f);
            os.write(contents.getBytes(encoding));
        }
        finally {
            FileUtils.closeStream(os);
        }
        
        return f;
    }
    
    public String readTextFile(File file) throws IOException {
        return readTextFile(file, "UTF-8");
    }
    
    public String readTextFile(File file, String encoding) throws IOException {
        InputStream is = null;
        
        try {
            is = new FileInputStream(file);
            return FileUtils.readTextStreamContents(is, encoding);
        }
        finally {
            FileUtils.closeStream(is);
        }
    }
    
    public ISoapRequestContext createSoapRequest(String requestXml)
    {
        int requestNode = parse(requestXml);
        
        SoapMethodDefinitionStub def = new SoapMethodDefinitionStub(requestNode);
        SoapRequestContextStub request = new SoapRequestContextStub(requestNode, def);
        
        addNomGarbage(request.getRequestEnvNode());
        addNomGarbage(request.getResponseEnvNode());
        
        return request;
    }
    
    public int createAppConfigXml(File configFile, File customPropFile, File txnSaveFolder)
    {
        String tmp = MessageFormat.format("<configuration>" +
                "   <configFile>{0}</configFile>" +
                "   <customPropertyFile>{1}</customPropertyFile>" +
                "   <txnSaveFolder>{2}</txnSaveFolder>" +
                "</configuration>", configFile != null ? configFile.getAbsolutePath() : "",
                        customPropFile != null ? customPropFile.getAbsolutePath() : "",
                                txnSaveFolder != null ? txnSaveFolder : "");
        
        return parse(tmp);
    }
    
    public int executeScriptMethod(String requestXml, String script) throws Exception
    {
        ISoapRequestContext request = createSoapRequest(requestXml);
        
        executeScriptMethod(request, script);
        
        return request.getResponseMethodNode();
    }
    
    public void executeScriptMethod(ISoapRequestContext request, String script) throws Exception
    {
        int methodNode = request.getRequestMethodNode();
        String name = Node.getLocalName(methodNode);
        
        File scriptFile = createTextFile(name + ".js", script);
        
        executeScriptMethod(request, scriptFile);
    }
    
    public int executeScriptMethod(String requestXml, File scriptFile) throws Exception
    {
        ISoapRequestContext request = createSoapRequest(requestXml);
        
        executeScriptMethod(request, scriptFile);
        
        return request.getResponseMethodNode();
    }
    
    public void executeScriptMethod(ISoapRequestContext request, File scriptFile) throws Exception
    {
        int methodNode = request.getRequestMethodNode();
        String name = Node.getLocalName(methodNode);
        String namespace = Node.getNamespaceURI(methodNode);
        ScriptLocator scriptId = new ScriptLocator(name, namespace, scriptFile); 
        File configFile = createTextFile("config.properties", name  + "=" + scriptFile.getAbsolutePath().replace('\\', '/'));
        ScriptConnectorConfiguration config  = new ScriptConnectorConfiguration(null, createAppConfigXml(configFile, null, null), configFolder);
        ScriptConnector connector = new ScriptConnectorStub(config, nomConnector);
        ScriptConnectorTransaction txn = new ScriptConnectorTransaction(connector);
        
        txn.executeScriptMethod(request, scriptId);
        txn.cleanSoapMessages();
    }
    
    public int executeScriptMethodReturnEnvelope(String requestXml, String script) throws Exception
    {
        ISoapRequestContext request = createSoapRequest(requestXml);
        int responseEnvNode = Node.getRoot(request.getResponseMethodNode());
        
        executeScriptMethod(request, script);
        
        return responseEnvNode;
    }
    
    public int getSoapMethod(int envelope)
    {
        int res = SOAPMessage.getRootBodyNode(envelope);
        
        return res != 0 ? Node.getFirstChildElement(res) : 0;
    }
}
