/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
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
