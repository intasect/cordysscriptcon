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
