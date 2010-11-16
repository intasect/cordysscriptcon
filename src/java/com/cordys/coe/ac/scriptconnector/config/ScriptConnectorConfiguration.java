/**
 * © 2004 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys R&D B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.config;

import com.cordys.coe.ac.scriptconnector.ScriptConnector;
import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;
import com.cordys.coe.ac.scriptconnector.processor.IScriptPreProcessor;
import com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript;
import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.XMLProperties;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds the configuration details for the ScriptConnector.
 */
public class ScriptConnectorConfiguration
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ScriptConnectorConfiguration.class);
    /**
     * Property name for the default namespace definition.
     */
    private static final String PROP_NAME_DEFAULT_NAMESPACE = "property.file.default.namespace";
    /**
     * Property name for the file include property.
     */
    private static final String PROP_NAME_INCLUDE = "property.file.include";
    /**
     * Property name for the method namespace property.
     */
    private static final String PROP_POSTFIX_NAMESPACE = ".namespace";
    /**
     * Property name for the method pre-processor property.
     */
    private static final Pattern PROP_PATTERN_PREPROCESSOR = Pattern.compile("^([^.]+)\\.preprocessor$");
    /**
     * Property name for the method pre-processor property.
     */
    private static final Pattern PROP_PATTERN_PREPROCESSOR_PARAMS = Pattern.compile("^([^.]+)\\.preprocessor\\.(.+)$");
    /**
     * Singleton instance to represent a <code>null</code> value in the preprocessor map.
     */
    private static final IScriptPreProcessor NULL_PREPROCESSOR = new NullPreProcessor();
    /**
     * A map containing script preprocessors.
     */
    protected ConcurrentMap<ScriptLocator, IScriptPreProcessor> mPreProcessorMap = new ConcurrentHashMap<ScriptLocator, IScriptPreProcessor>();
    /**
     * A map containing the configured scripts.
     */
    protected Map<ScriptLocator, ConfiguredScript> mScriptMap = new HashMap<ScriptLocator, ConfiguredScript>();
    /**
     * Optional parent folder where to save the transaction request and response.
     */
    protected File transactionSavePath;
    /**
     * Location of the script configuration file.
     */
    private File configFile;
    /**
     *  Maximum number of files per transaction.
     */
    private int maxFilesPerTransaction;
    /**
     * Contains configuration files and last modification time. This is used for determining if the
     * scripts need to be reloaded.
     */
    private List<FileTimestamp> configFileTimestampList = new ArrayList<FileTimestamp>(10);
    /**
     * Connector instance.
     */
    private ScriptConnector connector;
    /**
     * Contains properties loaded from the custom property file.
     */
    private Map<String, String> customProperties = new HashMap<String, String>();
    /**
     * Location of the custom property file.
     */
    private File customPropertyFile;
    /**
     * Last modification time of the custom property file used for determining if the file needs to
     * be reloaded.
     */
    private FileTimestamp customPropertyFileTimestamp;
    /**
     * Contains the connector installation folder.
     */
    private File installationFolder;
    /**
     * SOAP request timeout in milliseconds from the configuration. Default is 30000.
     */
    private long soapRequestTimeout = 30000L;
    /**
     * Default value for maximum log files per transaction.
     */
    private static int DEFAULT_MAX_FILES_PER_TRANSACTION = 500;
    /**
     * Holds the XMLProperties object to extract the value for different configuration keys.
     */
    private XMLProperties xpBase;

    /**
     * Creates the constructor.This loads the configuration object and pass it to XMLProperties for
     * processing.
     *
     * @param   connector           Script connector instance.
     * @param   iConfigNode         The XML-node that contains the configuration.
     * @param   installationFolder
     *
     * @throws  ScriptConnectorException  Thrown if the configuration was not valid
     */
    public ScriptConnectorConfiguration(ScriptConnector connector, int iConfigNode,
                                        File installationFolder)
                                 throws ScriptConnectorException
    {
        this.connector = connector;
        this.installationFolder = installationFolder;

        if (iConfigNode == 0)
        {
            throw new ScriptConnectorException("Configuration not found");
        }

        if (!Node.getName(iConfigNode).equals("configuration"))
        {
            throw new ScriptConnectorException("Root-tag of the configuration should be <configuration>");
        }

        try
        {
            xpBase = new XMLProperties(iConfigNode);
        }
        catch (GeneralException e)
        {
            throw new ScriptConnectorException("Exception while creating the configuration-object.",
                                               e);
        }

        // Get the configuration file path and load the script objects.
        configFile = getConfigFile();

        if (configFile == null)
        {
            throw new ScriptConnectorException("Configuration file not set.");
        }

        if (!configFile.isAbsolute())
        {
            configFile = new File(installationFolder, configFile.getPath());
        }

        if (!configFile.exists())
        {
            throw new ScriptConnectorException("Configuration file doest not exist: " + configFile);
        }

        loadScripts();

        // Get the custom property file path and load it.
        customPropertyFile = getCustomPropertyFile();

        if (customPropertyFile != null)
        {
            if (!customPropertyFile.isAbsolute())
            {
                customPropertyFile = new File(installationFolder, customPropertyFile.getPath());
            }

            if (!customPropertyFile.exists())
            {
                throw new ScriptConnectorException("Custom property file doest not exist: " +
                                                   customPropertyFile);
            }

            loadCustomProperties();
            customPropertyFileTimestamp = new FileTimestamp(customPropertyFile);
        }

        // Get the transaction save folder.
        transactionSavePath = getTransactionSaveFolder();

        if (transactionSavePath != null)
        {
            if (!transactionSavePath.isAbsolute())
            {
                transactionSavePath = new File(installationFolder, transactionSavePath.getPath());
            }

            if (!transactionSavePath.exists())
            {
                if (!transactionSavePath.mkdirs())
                {
                    throw new ScriptConnectorException("Unable to create SOAP transaction save folder: " +
                                                       transactionSavePath);
                }
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Saving SOAP transactions under folder: " + transactionSavePath);
            }
        }

        // Get the SOAP request timeout parameter.
        String tmpStr = xpBase.getStringValue("soapRequestTimeout");

        if ((tmpStr != null) && (tmpStr.length() > 0))
        {
            try
            {
                soapRequestTimeout = (long) (Double.parseDouble(tmpStr) * 1000);
            }
            catch (Exception e)
            {
                throw new ScriptConnectorException("Invalid SOAP request timeout value: " + tmpStr);
            }
        }
        
        
        initializeMaxFilesPerTransaction();
    }

	/**
	 * Initialize the value of max files per transaction
	 * 
	 * @throws ScriptConnectorException
	 */
	private void initializeMaxFilesPerTransaction() throws ScriptConnectorException
	{
		maxFilesPerTransaction = 0;
		boolean ok = false;
		String sMaxFilesPerTransaction = xpBase.getStringValue("maxFiles");
		
        if ((sMaxFilesPerTransaction != null) && (sMaxFilesPerTransaction.length() > 0))
        {
		    try
		    {
		    	maxFilesPerTransaction = Integer.parseInt(sMaxFilesPerTransaction);
		        if (maxFilesPerTransaction > 0)
		        {
		        	ok = true;
		        }
		    }
		    catch (Exception e)
		    {
		    }	    
        } else {
            maxFilesPerTransaction = DEFAULT_MAX_FILES_PER_TRANSACTION;
            ok = true;
        }
        
        if (!ok)
        {
        	throw new ScriptConnectorException("Invalid Max Files per transaction value : " + sMaxFilesPerTransaction);
        }
	}
	
    /**
     * Tries to find the pre-processor by the given locator. This method operates like findScript().
     *
     * @param   scriptId  Script ID to be located.
     *
     * @return  Script pre-processor or <code>null</code> if none was found.
     */
    public IScriptPreProcessor findPreProcessor(ScriptLocator scriptId)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Trying to find script pre-processor with locator: " + scriptId);
        }

        IScriptPreProcessor processor;

        if ((processor = mPreProcessorMap.get(scriptId)) == null)
        {
            // Try [*, namespace]
            ScriptLocator tmpScriptId = new ScriptLocator(null, scriptId.getNamespace());

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Trying to find script pre-processor with locator: " + tmpScriptId);
            }

            if ((processor = mPreProcessorMap.get(tmpScriptId)) == null)
            {
                // Try [method name, *]
                tmpScriptId = new ScriptLocator(scriptId.getMethodName(), null);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Trying to find script pre-processor with locator: " + tmpScriptId);
                }

                if ((processor = mPreProcessorMap.get(tmpScriptId)) == null)
                {
                    // Try [*, *]
                    tmpScriptId = new ScriptLocator(null, null);

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Trying to find script pre-processor with locator: " +
                                  tmpScriptId);
                    }

                    processor = mPreProcessorMap.get(tmpScriptId);
                }
            }
        }

        return (processor != NULL_PREPROCESSOR) ? processor : null;
    }

    /**
     * Returns the configFile configuration value.
     *
     * @return  configFile value.
     */
    public File getConfigFile()
    {
        String path = xpBase.getStringValue("configFile");

        if ((path == null) || (path.length() == 0))
        {
            return null;
        }

        return new File(path);
    }

    /**
     * Returns a custom property loaded from the file.
     *
     * @param   name  Property name.
     *
     * @return  Property value or <code>null</code> if no property has been set.
     */
    public String getCustomProperty(String name)
    {
        return customProperties.get(name);
    }

    /**
     * Returns the customPropertyFile configuration value.
     *
     * @return  customPropertyFile value.
     */
    public File getCustomPropertyFile()
    {
        String path = xpBase.getStringValue("customPropertyFile");

        if ((path == null) || (path.length() == 0))
        {
            return null;
        }

        return new File(path);
    }

    /**
     * Returns the installationFolder.
     *
     * @return  Returns the installationFolder.
     */
    public File getInstallationFolder()
    {
        return installationFolder;
    }

    /**
     * Returns the configured script by script ID. If the script is not loaded or a newer version is
     * available, the script file is loaded and the script is compiled.
     *
     * @param   scriptId  The script's ID.
     *
     * @return  The loaded script object, or null if no script was found.
     *
     * @throws  Exception  Thrown if the script loading failed.
     */
    public ConfiguredScript getScript(ScriptLocator scriptId)
                               throws Exception
    {
        ConfiguredScript csScript;

        synchronized (mScriptMap)
        {
            // Check if the script property file needs to be reloaded.
            if (configurationFilesChanged())
            {
                loadScripts();
            }

            csScript = findScript(scriptId);

            if (csScript == null)
            {
                return null;
            }

            if (csScript.isLoaded())
            {
                // Check if the script file has been changed.
                FileTimestamp scriptTimestamp = csScript.getTimestamp();

                if ((scriptTimestamp != null) && scriptTimestamp.hasChanged())
                {
                    // Create a new instance and load the new version.
                    mScriptMap.remove(csScript.getLocator());
                    csScript = ConfiguredScript.createInstance(csScript.getLocator(),
                                                               scriptTimestamp.getFile());
                    mScriptMap.put(csScript.getLocator(), csScript);
                }
            }
        }

        // Check if we need to load the custom properties.
        if (customPropertyFile != null)
        {
            synchronized (customProperties)
            {
                // Check if the script property file needs to be reloaded.
                if (customPropertyFileTimestamp.hasChanged())
                {
                    loadCustomProperties();
                }
            }
        }

        return csScript;
    }

    /**
     * Returns the soapRequestTimeout.
     *
     * @return  Returns the soapRequestTimeout.
     */
    public long getSoapRequestTimeout()
    {
        return soapRequestTimeout;
    }

    /**
     * Returns the transactionSavePath.
     *
     * @return  Returns the transactionSavePath.
     */
    public File getTransactionSavePath()
    {
        return transactionSavePath;
    }

    /**
     * Sets the soapRequestTimeout.
     *
     * @param  soapRequestTimeout  The soapRequestTimeout to be set.
     */
    public void setSoapRequestTimeout(long soapRequestTimeout)
    {
        this.soapRequestTimeout = soapRequestTimeout;
    }

    /**
     * Checks if the configuration files have changed.
     *
     * @return
     */
    private boolean configurationFilesChanged()
    {
        for (FileTimestamp entry : configFileTimestampList)
        {
            if (entry.hasChanged())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to find the script by the given locator. This method will also try the script to find
     * from default namespace or method name. Search order is: &lt;ul&gt; &lt;li&gt;-[method name,
     * namespace]&lt;/li&gt; &lt;li&gt;-[*, namespace]&lt;/li&gt; &lt;li&gt;-[method name, *]&lt;/li&gt;
     * &lt;li&gt;-[*, *]&lt;/li&gt; &lt;/ul&gt;
     *
     * <p>Note! The method call must be synchronized over mScriptMap.</p>
     *
     * @param   scriptId  Script ID to be located.
     *
     * @return  Script object or <code>null</code> if none was found.
     */
    private ConfiguredScript findScript(ScriptLocator scriptId)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Trying to find script with locator: " + scriptId);
        }

        ConfiguredScript csScript;

        if ((csScript = mScriptMap.get(scriptId)) == null)
        {
            // Try [*, namespace]
            ScriptLocator tmpScriptId = new ScriptLocator(null, scriptId.getNamespace());

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Trying to find script with locator: " + tmpScriptId);
            }

            if ((csScript = mScriptMap.get(tmpScriptId)) == null)
            {
                // Try [method name, *]
                tmpScriptId = new ScriptLocator(scriptId.getMethodName(), null);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Trying to find script with locator: " + tmpScriptId);
                }

                if ((csScript = mScriptMap.get(tmpScriptId)) == null)
                {
                    // Try [*, *]
                    tmpScriptId = new ScriptLocator(null, null);

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Trying to find script with locator: " + tmpScriptId);
                    }

                    csScript = mScriptMap.get(tmpScriptId);
                }
            }
        }

        return csScript;
    }

    /**
     * Loads all custom properties from the custom property file. This method must be called from
     * code which is synchronized over customProperties object.
     *
     * @throws  ScriptConnectorException
     */
    private void loadCustomProperties()
                               throws ScriptConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading custom properties from file: " + customPropertyFile);
        }

        Map<String, String> tempMap = new HashMap<String, String>();
        FileInputStream is = null;

        try
        {
            Properties pProps = new Properties();

            is = new FileInputStream(customPropertyFile);
            pProps.load(is);

            for (Iterator<Map.Entry<Object, Object>> iter = pProps.entrySet().iterator();
                     iter.hasNext();)
            {
                Map.Entry<Object, Object> entry = iter.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                tempMap.put(key, value);
            }
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to load the custom properties from file: " +
                                               customPropertyFile, e);
        }
        finally
        {
            FileUtils.closeStream(is);
        }

        customProperties.clear();
        customProperties.putAll(tempMap);
    }

    /**
     * Loads script definitions. It handles the relative paths and property file includes (even
     * recursive ones).
     *
     * @param   fileName   Property file to load.
     * @param   relFolder  Parent folder for relative paths.
     *
     * @return  Loaded properties.
     *
     * @throws  IOException
     * @throws  ScriptConnectorException
     */
    private Collection<ConfiguredScript> loadScriptDefinitions(String fileName,
                                                               String relFolder)
                                                        throws IOException, ScriptConnectorException
    {
        File file = new File(fileName);

        if (!file.isAbsolute())
        {
            file = new File(relFolder, fileName);
        }

        if (!file.exists())
        {
            throw new IOException("Property file does not exist: " + file);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading script definitions from file: " + file);
        }

        Properties pProps = new Properties();
        InputStream in = null;

        try
        {
            in = new FileInputStream(file);
            pProps.load(in);
        }
        finally
        {
            FileUtils.closeStream(in);
        }

        configFileTimestampList.add(new FileTimestamp(file));

        Collection<ConfiguredScript> resList = new ArrayList<ConfiguredScript>(20);
        Map<String, String> preProcessorClassMap = new HashMap<String, String>();
        Map<String, Map<String, String>> preProcessorParamMap = new HashMap<String, Map<String, String>>();
        String defaultNamespace;

        defaultNamespace = pProps.getProperty(PROP_NAME_DEFAULT_NAMESPACE);
        pProps.remove(PROP_NAME_DEFAULT_NAMESPACE);

        for (Iterator<Map.Entry<Object, Object>> iter = pProps.entrySet().iterator();
                 iter.hasNext();)
        {
            Map.Entry<Object, Object> entry = iter.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            Matcher m;

            if (key.equals(PROP_NAME_INCLUDE) || key.startsWith(PROP_NAME_INCLUDE + "."))
            {
                Collection<ConfiguredScript> childList = loadScriptDefinitions(value,
                                                                               file.getParent());

                resList.addAll(childList);
            }
            else if (key.endsWith(PROP_POSTFIX_NAMESPACE))
            {
                continue;
            }
            else if ((m = PROP_PATTERN_PREPROCESSOR.matcher(key)).matches())
            {
                String methodName = m.group(1);

                if ((methodName != null) && (methodName.length() > 0))
                {
                    preProcessorClassMap.put(methodName, value);
                }
            }
            else if ((m = PROP_PATTERN_PREPROCESSOR_PARAMS.matcher(key)).matches())
            {
                String methodName = m.group(1);
                String paramName = m.group(2);

                if ((methodName != null) && (methodName.length() > 0) && (paramName != null) &&
                        (paramName.length() > 0))
                {
                    Map<String, String> paramMap = preProcessorParamMap.get(methodName);

                    if (paramMap == null)
                    {
                        paramMap = new HashMap<String, String>();
                        preProcessorParamMap.put(methodName, paramMap);
                    }

                    paramMap.put(paramName, value);
                }
            }
            else
            {
                String methodName = key;
                String methodNamespace = pProps.getProperty(methodName + PROP_POSTFIX_NAMESPACE);
                File scriptFile = new File(value);

                if (!scriptFile.isAbsolute())
                {
                    scriptFile = new File(file.getParent(), value);
                }

                if (!scriptFile.exists())
                {
                    throw new IOException("Script file does not exist: " + scriptFile);
                }

                if ((methodNamespace == null) || (methodNamespace.length() == 0))
                {
                    methodNamespace = defaultNamespace;
                }

                if (methodName.equals("*"))
                {
                    // This is the default method.
                    methodName = null;
                }

                ConfiguredScript script = ConfiguredScript.createInstance(new ScriptLocator(methodName,
                                                                                            methodNamespace,
                                                                                            scriptFile),
                                                                          scriptFile);

                resList.add(script);
            }
        }

        // Configure pre-processors.
        for (Map.Entry<String, String> entry : preProcessorClassMap.entrySet())
        {
            String methodName = entry.getKey();
            String className = entry.getValue();
            String processorMethodName = "*".equals(methodName) ? null : methodName;
            ScriptLocator locator = new ScriptLocator(processorMethodName, defaultNamespace);
            IScriptPreProcessor processor;

            if ((className != null) && (className.length() > 0))
            {
                try
                {
                    Object obj = Class.forName(className).newInstance();

                    if (!(obj instanceof IScriptPreProcessor))
                    {
                        throw new IllegalStateException("Class does not implement interface: " +
                                                        IScriptPreProcessor.class.getName());
                    }

                    processor = (IScriptPreProcessor) obj;
                }
                catch (Exception e)
                {
                    throw new ScriptConnectorException("Unable to instantiate script pre-processor class: " +
                                                       className, e);
                }

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Initializing script pre-processor");
                }

                Map<String, String> params = preProcessorParamMap.get(methodName);

                if (params == null)
                {
                    params = new HashMap<String, String>();
                }

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Initializing script pre-processor: " +
                              processor.getClass().getName());
                    LOG.debug("Pre-processor parameters are: " + params);
                }

                if (!processor.initialize(connector, params))
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Pre-processor initialization returned false.");
                    }
                    continue;
                }
            }
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Pre-processor is disable for method: " + locator);
                }

                processor = NULL_PREPROCESSOR;
            }

            mPreProcessorMap.put(locator, processor);
        }

        return resList;
    }

	/**
	 * Return the max files per transaction.
	 * 
	 * @return  Return the max files per transaction
	 */
	public int getMaxFilesPerTransaction() 
	{
		return maxFilesPerTransaction;
	}
	
    /**
     * Loads all scripts from the configuration file. This method must be called from code which is
     * synchronized over mScriptMap object.
     *
     * @throws  ScriptConnectorException
     */
    private void loadScripts()
                      throws ScriptConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading scripts. Script root configuration file: " + configFile);
        }

        configFileTimestampList.clear();

        Map<ScriptLocator, ConfiguredScript> tempMap = new HashMap<ScriptLocator, ConfiguredScript>();
        String configFolder = configFile.getParent();

        try
        {
            Collection<ConfiguredScript> list = loadScriptDefinitions(configFile.getPath(),
                                                                      configFolder);

            for (ConfiguredScript script : list)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("Found script definition %s with file %s",
                                            script.getLocator().toString(),
                                            script.getLocator().getScriptFile()));
                }

                tempMap.put(script.getLocator(), script);
            }
        }
        catch (Exception e)
        {
            throw new ScriptConnectorException("Unable to load the configuration from file: " +
                                               configFile, e);
        }

        mScriptMap.clear();
        mScriptMap.putAll(tempMap);
    }

    /**
     * Returns the customPropertyFile configuration value.
     *
     * @return  customPropertyFile value.
     */
    private File getTransactionSaveFolder()
    {
        String path = xpBase.getStringValue("txnSaveFolder");

        if ((path == null) || (path.length() == 0))
        {
            return null;
        }

        return new File(path);
    }

    /**
     * A dummy class which is needed to be able to put <code>null</code> values into the map..
     *
     * @author  mpoyhone
     */
    private static final class NullPreProcessor
        implements IScriptPreProcessor
    {
        /**
         * @see  com.cordys.coe.ac.scriptconnector.processor.IScriptPreProcessor#execute(com.cordys.coe.ac.scriptconnector.ScriptConnector,
         *       com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript,
         *       com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext)
         */
        public boolean execute(ScriptConnector connector, ConfiguredScript script,
                               ISoapRequestContext requestContext)
                        throws ScriptConnectorException
        {
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.scriptconnector.processor.IScriptPreProcessor#initialize(com.cordys.coe.ac.scriptconnector.ScriptConnector,
         *       java.util.Map)
         */
        public boolean initialize(ScriptConnector connector, Map<String, String> params)
                           throws ScriptConnectorException
        {
            return false;
        }
    }
}
