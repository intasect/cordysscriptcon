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

import com.cordys.coe.ac.scriptconnector.ScriptConnector;
import com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext;
import com.cordys.coe.ac.scriptconnector.aclib.NomConnectorStub;
import com.cordys.coe.ac.scriptconnector.config.ScriptConnectorConfiguration;
import com.cordys.coe.ac.scriptconnector.config.SoapMethodInfo;

/**
 * A stub for the ScriptConnector test cases.
 *
 * @author  mpoyhone
 */
public class ScriptConnectorStub extends ScriptConnector
{
    /**
     * Creates a new ScriptConnectorStub object.
     *
     * @param  config        Configuration object.
     * @param  nomConnector  NOM connector stub.
     */
    public ScriptConnectorStub(ScriptConnectorConfiguration config, NomConnectorStub nomConnector)
    {
        this.acConfiguration = config;
        this.nomConnector = nomConnector;
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.ScriptConnector#getOrganizationDn()
     */
    @Override
    public String getOrganizationDn()
    {
        return "dummyorg";
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.ScriptConnector#getScriptConfig()
     */
    @Override
    public ScriptConnectorConfiguration getScriptConfig()
    {
        return super.getScriptConfig();
    }

    /**
     * @see  com.cordys.coe.ac.scriptconnector.ScriptConnector#getSoapMethodInfo(com.cordys.coe.ac.scriptconnector.aclib.ISoapRequestContext)
     */
    @Override
    public SoapMethodInfo getSoapMethodInfo(ISoapRequestContext requestContext)
    {
        return super.getSoapMethodInfo(requestContext);
    }
}
