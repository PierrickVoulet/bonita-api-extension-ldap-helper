package com.bonitasoft.apiextension.ldap.helper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Toolbox {

    static Logger logger = Logger.getLogger(Toolbox.class.getName());

    static Integer getInteger(final Object parameter, final Integer defaultValue)
    {
        if (parameter == null) {
            return defaultValue;
        }
        try
        {
            return Integer.valueOf(parameter.toString());
        } catch (final Exception e)
        {
            PropertiesLdapConnection.logger.severe("Can't decode integer [" + parameter + "]");
            return defaultValue;
        }
    }

    static Boolean getBoolean(final Object parameter, final Boolean defaultValue)
    {
        if (parameter == null) {
            return defaultValue;
        }
        try
        {
            return Boolean.valueOf(parameter.toString());
        } catch (final Exception e)
        {
            PropertiesLdapConnection.logger.severe("Can't decode boolean [" + parameter + "]");
            return defaultValue;
        }
    }

    static String getString(final Object parameter, final String defaultValue)
    {
        if (parameter == null) {
            return defaultValue;
        }
        try
        {
            return parameter.toString();
        } catch (final Exception e)
        {
            return defaultValue;
        }
    }

    static List<Map<String, String>> getList(final Object parameter, final List<Map<String, String>> defaultValue)
    {
        if (parameter == null) {
            return defaultValue;
        }
        try
        {
            return (List<Map<String, String>>) parameter;
        } catch (final Exception e)
        {
            return defaultValue;
        }
    }
    /**
     * calculate the file name
     *
     * @param directory
     * @param domain
     * @param configFileName
     * @return
     */
    public static String getConfigFileName(final String ldapSynchronizerPath, final String domain, final String configFileName)
    {
        final String fileName = ldapSynchronizerPath + File.separator + domain + File.separator + configFileName;
        // logger.info("CraneTruck.Toolbox: configuration [" + configFileName + "] file is [" + fileName + "]");
        return fileName;
    }

    /**
     * this class is available to be used in different element
     *
     * @author pierre-yves
     */
    public static class StatusOperation
    {

        public String mStatusTitle;
        public String mStatusinfo = "";
        public String mStatusError = "";
        public String mStatusDetails = "";

        public Map<String, Object> mStatusResultJson = new HashMap<String, Object>();

        public StatusOperation(final String title)
        {
            mStatusTitle = title;
        }

        /**
         * if one error message is set, then the status are in error
         *
         * @return
         */
        public boolean isError()
        {
            return mStatusError.length() > 0;
        }

        public void addError(final String error)
        {
            mStatusError += error + ";";
            mStatusResultJson.put("error", mStatusError);
        }

        public void addDetails(final String error)
        {
            mStatusDetails += mStatusDetails + ";";
            mStatusResultJson.put("details", mStatusDetails);
        }

        public void setSuccess(final String success)
        {
            mStatusinfo = success;
            mStatusResultJson.put("status", success);

        }
        public void addStatusOperation(final StatusOperation statusOperation)
        {
            mStatusTitle += statusOperation.mStatusTitle + ";";
            mStatusinfo += statusOperation.mStatusinfo.length() > 0 ? statusOperation.mStatusinfo + ";" : "";
            mStatusError += statusOperation.mStatusError.length() > 0 ? statusOperation.mStatusError + ";" : "";
            mStatusDetails += statusOperation.mStatusDetails.length() > 0 ? statusOperation.mStatusDetails + ";" : "";
            if (statusOperation.mStatusError.length() > 0) {
                logger.info(statusOperation.mStatusError);
            }
        }

        public Map<String, Object> toMap()
        {
            // status.properties.error}
            final Map<String, Object> result = new HashMap<String, Object>();
            result.put("title", mStatusTitle);
            result.put("info", mStatusinfo);
            result.put("error", mStatusError);
            result.put("details", mStatusDetails);
            result.put("detailsjsonmap", mStatusResultJson);

            return result;

        }
    }

}
