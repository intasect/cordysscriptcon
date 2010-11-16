/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.scripting;

import com.cordys.coe.ac.scriptconnector.config.FileTimestamp;
import com.cordys.coe.ac.scriptconnector.config.ScriptLocator;
import com.cordys.coe.ac.scriptconnector.exception.ScriptConnectorException;

import java.io.File;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * Contains script information.
 *
 * @author  mpoyhone
 */
public abstract class ConfiguredScript
{
    /**
     * Indicates whether the script file has been loaded.
     */
    protected volatile boolean loaded;
    /**
     * Contains the script locator (name, namespace) for this script.
     */
    protected ScriptLocator locator;
    /**
     * Script name. This is taken from the file name.
     */
    protected String scriptName;
    /**
     * Script type. This is current the file extension.
     */
    protected String scriptType;
    /**
     * Script file timestamp object.
     */
    protected FileTimestamp timestamp;

    /**
     * Creates a handler instance.
     *
     * @return
     */
    public abstract IScriptHandler createHandler();

    /**
     * Factory method for creating a new ConfiguredScript instance. The script is not loaded by this
     * method.
     *
     * @param   locator  Script locator object.
     * @param   fFile    Script File.
     *
     * @return  New script instance.
     *
     * @throws  ScriptConnectorException  Thrown if the file path is invalid.
     */
    public static ConfiguredScript createInstance(ScriptLocator locator, File fFile)
                                           throws ScriptConnectorException
    {
        String fileName = fFile.getName();
        int extPos = fileName.lastIndexOf('.');
        String ext = ((extPos > 0) && (extPos < (fileName.length() - 1)))
                     ? fileName.substring(extPos + 1) : null;

        if ((ext == null) || (ext.length() == 0))
        {
            throw new ScriptConnectorException("Unable to determine script type from file name: " +
                                               fileName);
        }

        ConfiguredScript script;

        if (ext.equals("js"))
        {
            script = new E4XScript();
        }
        else
        {
            script = new GenericScript();
        }

        script.scriptType = ext;
        script.locator = locator;
        script.timestamp = new FileTimestamp(fFile);
        script.scriptName = fFile.getName();

        return script;
    }

    /**
     * Returns the locator.
     *
     * @return  Returns the locator.
     */
    public ScriptLocator getLocator()
    {
        return locator;
    }

    /**
     * Returns the scriptName.
     *
     * @return  Returns the scriptName.
     */
    public String getScriptName()
    {
        return scriptName;
    }

    /**
     * Returns the scriptType.
     *
     * @return  Returns the scriptType.
     */
    public String getScriptType()
    {
        return scriptType;
    }

    /**
     * Returns the timestamp.
     *
     * @return  Returns the timestamp.
     */
    public FileTimestamp getTimestamp()
    {
        return timestamp;
    }

    /**
     * Returns the loaded.
     *
     * @return  Returns the loaded.
     */
    public boolean isLoaded()
    {
        return loaded;
    }

    /**
     * Sets the loaded.
     *
     * @param  loaded  The loaded to be set.
     */
    public void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }

    /**
     * Sets the scriptName.
     *
     * @param  scriptName  The scriptName to be set.
     */
    public void setScriptName(String scriptName)
    {
        this.scriptName = scriptName;
    }

    /**
     * Script holder for E4X Javascripts.
     *
     * @author  mpoyhone
     */
    public static class E4XScript extends ConfiguredScript
    {
        /**
         * Contains a compiled script.
         */
        public Script sScript;
        /**
         * Contains the script scope object.
         */
        public ScriptableObject sSharedScope;

        /**
         * @see  com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript#createHandler()
         */
        @Override
        public IScriptHandler createHandler()
        {
            return new E4XHandler();
        }
    }

    /**
     * Script holder for Java scripting API scripts.
     *
     * @author  mpoyhone
     */
    public static class GenericScript extends ConfiguredScript
    {
        /**
         * Contains cached script text from the file.
         */
        public String scriptText;

        /**
         * @see  com.cordys.coe.ac.scriptconnector.scripting.ConfiguredScript#createHandler()
         */
        @Override
        public IScriptHandler createHandler()
        {
            return new GenericScriptHandler();
        }
    }
}
