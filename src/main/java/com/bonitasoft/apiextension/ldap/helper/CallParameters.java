package com.bonitasoft.apiextension.ldap.helper;

import java.util.HashMap;
import java.util.logging.Logger;

import org.json.simple.JSONValue;

public class CallParameters {
    static Logger logger = Logger.getLogger(CallParameters.class.getName());

    String ldapSynchronizerPath;
    String domain;

    public static CallParameters getInstanceFromJsonSt(final String jsonSt) {
        if (jsonSt == null) {
            return new CallParameters();
        }
        logger.info("LdapConnectionParam: JsonSt[" + jsonSt + "]");
        final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);
        if (jsonHash == null) {
            return new CallParameters();
        }
        final CallParameters craneTruckParam = new CallParameters();
        craneTruckParam.ldapSynchronizerPath = Toolbox.getString(jsonHash.get("ldapSynchronizerPath"), null);
        craneTruckParam.domain = Toolbox.getString(jsonHash.get("domain"), PropertiesBonitaConnection.DEFAULT_TENANT);

        return craneTruckParam;
    }

    @Override
    public String toString()
    {
        return "Path[" + ldapSynchronizerPath + "] domain[" + domain + "]";
    }
}