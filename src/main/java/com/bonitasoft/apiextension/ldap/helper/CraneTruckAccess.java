package com.bonitasoft.apiextension.ldap.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONValue;

import com.bonitasoft.apiextension.ldap.helper.ToolFileProperties.PropertiesParam;
import com.bonitasoft.apiextension.ldap.helper.Toolbox.StatusOperation;

public class CraneTruckAccess {
    static Logger logger = Logger.getLogger(CraneTruckAccess.class.getName());

    /**
     * @param ldapSynchronizerPath
     * @param mDomain
     * @return
     */
    public static HashMap<String, Object> readFromProperties(final CallParameters craneTruckParam) {
        logger.info("CraneTruck.readFromProperties: Start ------------------------------- ");
        final HashMap<String, Object> result = new HashMap<String, Object>();

        final StatusOperation statusOperationCollector = new StatusOperation("Read Properties");

        PropertiesParam propertiesParam;
        propertiesParam = new PropertiesLdapConnection();
        ToolFileProperties.readPropertiesFile(propertiesParam, craneTruckParam.ldapSynchronizerPath, craneTruckParam.domain);
        statusOperationCollector.addStatusOperation(propertiesParam.getStatusOperation());
        result.put("ldap", propertiesParam.toMap());

        propertiesParam = new PropertiesBonitaConnection();
        ToolFileProperties.readPropertiesFile(propertiesParam, craneTruckParam.ldapSynchronizerPath, craneTruckParam.domain);
        statusOperationCollector.addStatusOperation(propertiesParam.getStatusOperation());
        result.put("bonita", propertiesParam.toMap());

        propertiesParam = new PropertiesLogger();
        ToolFileProperties.readPropertiesFile(propertiesParam, craneTruckParam.ldapSynchronizerPath, craneTruckParam.domain);
        statusOperationCollector.addStatusOperation(propertiesParam.getStatusOperation());
        result.put("logger", propertiesParam.toMap());

        propertiesParam = new PropertiesSynchronize();
        ToolFileProperties.readPropertiesFile(propertiesParam, craneTruckParam.ldapSynchronizerPath, craneTruckParam.domain);
        statusOperationCollector.addStatusOperation(propertiesParam.getStatusOperation());
        result.put("sync", propertiesParam.toMap());

        propertiesParam = new PropertiesMapper();
        ToolFileProperties.readPropertiesFile(propertiesParam, craneTruckParam.ldapSynchronizerPath, craneTruckParam.domain);
        statusOperationCollector.addStatusOperation(propertiesParam.getStatusOperation());
        result.put("mapper", propertiesParam.toMap());

        result.put("statuserror", statusOperationCollector.mStatusError);
        if (statusOperationCollector.mStatusError.length() == 0 && statusOperationCollector.mStatusinfo.length() == 0) {
            statusOperationCollector.mStatusinfo = "OK";
        }
        result.putAll(statusOperationCollector.toMap());

        logger.info("CraneTruck.readFromProperties: ---------------------------------  status=[" + statusOperationCollector.mStatusinfo + "] statusError=["
                + statusOperationCollector.mStatusError + "] result=[" + result + "]");
        return result;

    }

    /**
     * @param jsonSt
     * @param ldapSynchronizerPath
     * @param domain
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> writeToProperties(final String jsonSt, final CallParameters craneTruckParam)
    {
        final StatusOperation statusOperationCollector = new StatusOperation("WriteProperties");
        final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);
        if (jsonSt == null || jsonHash == null)
        {
            statusOperationCollector.addError("No JsonParameters");
            return statusOperationCollector.toMap();
        }
        if (craneTruckParam == null)
        {
            statusOperationCollector.addError("No Parameters (directory to save)");
            return statusOperationCollector.toMap();
        }
        logger.info("CraneTruckAccess writeProperties from [" + jsonSt + "] param=" + craneTruckParam.toString() + "]");

        //-------------------------------- Ldap connection
        PropertiesParam propertiesParam;

        propertiesParam = PropertiesLdapConnection.getInstanceFromMap((Map<String, Object>) jsonHash.get("ldap"));
        statusOperationCollector.addStatusOperation(ToolFileProperties.writePropertiesFile(propertiesParam, craneTruckParam.ldapSynchronizerPath,
                craneTruckParam.domain));

        //------------------------------------- bonita connection
        propertiesParam = PropertiesBonitaConnection.getInstanceFromMap((Map<String, Object>) jsonHash.get("bonita"));
        statusOperationCollector.addStatusOperation(ToolFileProperties.writePropertiesFile(propertiesParam,
                craneTruckParam.ldapSynchronizerPath,
                craneTruckParam.domain));

        //------------------------------------- Logger connection
        propertiesParam = PropertiesLogger.getInstanceFromMap((Map<String, Object>) jsonHash.get("logger"));
        statusOperationCollector.addStatusOperation(ToolFileProperties.writePropertiesFile(propertiesParam,
                craneTruckParam.ldapSynchronizerPath,
                craneTruckParam.domain));
        //------------------------------------- Sync connection
        propertiesParam = PropertiesSynchronize.getInstanceFromMap((Map<String, Object>) jsonHash.get("sync"));
        statusOperationCollector.addStatusOperation(ToolFileProperties.writePropertiesFile(propertiesParam,
                craneTruckParam.ldapSynchronizerPath,
                craneTruckParam.domain));
        //------------------------------------- mapper connection
        propertiesParam = PropertiesMapper.getInstanceFromMap((Map<String, Object>) jsonHash.get("mapper"));
        statusOperationCollector.addStatusOperation(ToolFileProperties.writePropertiesFile(propertiesParam,
                craneTruckParam.ldapSynchronizerPath,
                craneTruckParam.domain));

        logger.info("CraneTruck.writeFromProperties: status=[" + statusOperationCollector.mStatusinfo + "] statusError=["
                + statusOperationCollector.mStatusError
                + "] result=[" + statusOperationCollector.toMap() + "]");

        return statusOperationCollector.toMap();

    }
}
