/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.scriptconnector.config;

import java.io.File;

/**
 * Holds a file last modification timestamp. This is used to check if the file has been modified
 * (but not with every access).
 *
 * <p>This class is not thread-safe and access to it synchronized.</p>
 *
 * @author  mpoyhone
 */
public class FileTimestamp
{
    /**
     * File status is checked only if last check was done after this time.
     */
    private static final long SCAN_INTERVAL = 300L;
    /**
     * File object.
     */
    private File file;
    /**
     * Last modification check.
     */
    private long lastCheck;
    /**
     * File's last modification time.
     */
    private long lastModified;

    /**
     * Constructor for FileTimestamp.
     *
     * @param  file
     */
    public FileTimestamp(File file)
    {
        super();
        this.file = file;
        this.lastModified = file.lastModified();
        this.lastCheck = System.currentTimeMillis();
    }

    /**
     * Checks if the file has been modified.
     *
     * @return  <code>true</code> if the file has been modified.
     */
    public boolean hasChanged()
    {
        long now = System.currentTimeMillis();

        if ((now - lastCheck) < SCAN_INTERVAL)
        {
            return false;
        }

        long fileTimestamp = file.lastModified();
        boolean changed = fileTimestamp != lastModified;

        lastCheck = now;
        lastModified = fileTimestamp;

        return changed;
    }

    /**
     * Returns the file.
     *
     * @return  Returns the file.
     */
    public File getFile()
    {
        return file;
    }
}
