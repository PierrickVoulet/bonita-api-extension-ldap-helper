package com.bonitasoft.apiextension.ldap.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.json.simple.JSONValue;

import com.bonitasoft.apiextension.ldap.helper.Toolbox.StatusOperation;

public class JaasCheck {

    static Logger logger = Logger.getLogger(PropertiesBonitaConnection.class.getName());

    private static final String cstParamJaasFile = "jaasfile";
    private static final String cstParamJaasAuthKey = "jaasauthkey";
    private static final String cstParamJaasUserName = "jaasusername";
    private static final String cstParamJaasPassword = "jaaspassword";

    private static final String cstParamJaasOpeUserProvider = "jaasopuserprovider";
    private static final String cstParamJaasOpeUserFilter = "jaasopuserfilter";
    private static final String cstParamJaasOpeUseSSL = "jaasopusessl";
    private static final String cstParamJaasOpIdentity = "jaasopidentity";
    private static final String cstParamJaasOpUserName = "jaasopusername";
    private static final String cstParamJaasOpPassword = "jaasoppassword";

    private String mJaasContent;
    private String mFileName;
    private String mAuthKey;
    private String mUserName;
    private String mPassWord;

    private String mJaasOpUserProvider;
    private String mJaasOpUserFilter;
    private boolean mJaasOpUseSSL;
    private String mJaasOpIdentity;
    private String mJaasOpUserName;
    private String mJaasOpPassword;

    public class BonitaAuthenticationCallbackHandler implements CallbackHandler {

        private final String name;
        private final String password;

        public BonitaAuthenticationCallbackHandler(final String name, final String password) {
            this.name = name;
            this.password = password;
        }

        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (final Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    final NameCallback nc = (NameCallback) callback;
                    nc.setName(name);
                } else if (callback instanceof PasswordCallback) {
                    final PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                }
            }
        }
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Read parameters method */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    public static JaasCheck getInstanceFromJsonSt(final String jsonSt)
    {
        logger.info("Receive parametersJson=" + jsonSt);
        final JaasCheck jaasCheck = new JaasCheck();
        final HashMap<String, Object> jsonHash = (HashMap<String, Object>) JSONValue.parse(jsonSt);
        if (jsonHash == null) {
            logger.severe("Can't decode jsonSt " + jsonSt);

            return new JaasCheck();
        }
        logger.info("Receive parametersJson=" + jsonSt);
        jaasCheck.mJaasContent = Toolbox.getString(jsonHash.get("jaascontent"), null);
        jaasCheck.mFileName = Toolbox.getString(jsonHash.get("jaasfile"), null);
        jaasCheck.mAuthKey = Toolbox.getString(jsonHash.get("jaasauthentkey"), null);
        jaasCheck.mUserName = Toolbox.getString(jsonHash.get("jaasusername"), null);
        jaasCheck.mPassWord = Toolbox.getString(jsonHash.get("jasspassword"), null);

        jaasCheck.mJaasOpUserProvider = Toolbox.getString(jsonHash.get("jaasuserprovider"), null);
        jaasCheck.mJaasOpUserFilter = Toolbox.getString(jsonHash.get("jaasuserfilter"), null);
        jaasCheck.mJaasOpUseSSL = Toolbox.getBoolean(jsonHash.get("jaasuseSSL"), Boolean.FALSE);
        jaasCheck.mJaasOpIdentity = Toolbox.getString(jsonHash.get("jaasidentity"), null);
        jaasCheck.mJaasOpUserName = Toolbox.getString(jsonHash.get("jaasopusername"), null);
        jaasCheck.mJaasOpPassword = Toolbox.getString(jsonHash.get("jaasoppassword"), null);

        return jaasCheck;
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Check method */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /***
     * check check the Ldap
     *
     * @param mHostURL
     * @param mAuthType
     * @param mPrincipalDN
     * @param mPassword
     * @param mSearchDN
     * @param mSearchFilter
     * @return
     */
    public StatusOperation checkJaasConnection() {
        final StatusOperation statusOperation = new StatusOperation("jaasCheck");

        logger.info("Start checkJaasConnection fileName[" + mFileName + "] JaasContent[" + mJaasContent + "]");
        String fileNameJaas = mFileName;
        String contentJaas = null;
        File temporaryFile = null;
        try
        {
            if (mJaasContent != null && mJaasContent.trim().length() > 0)
            {
                temporaryFile = File.createTempFile("temp-jaasstandard", ".cfg");
                final BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile));
                writer.write(mJaasContent);
                writer.close();
                logger.info("Get a JaasContent, write it a temporary file " + temporaryFile.getAbsolutePath());

                fileNameJaas = temporaryFile.getAbsolutePath();
                contentJaas = mJaasContent;
            }
            else
            {
                logger.info("load the JaasStandard file" + mFileName);

                contentJaas = loadFile(mFileName, this);
                if (contentJaas != null) {
                    contentJaas = contentJaas.replace("\n", "<br>");
                    logger.info("load the JaasStandard file" + mFileName + " content[" + contentJaas + "]");
                }

            }
            if (contentJaas == null || contentJaas.trim().length() == 0)
            {
                logger.info("no Jaas Content");
                statusOperation.addError("No JAAS content");
                return statusOperation;
            }
            System.setProperty("java.security.auth.login.config", fileNameJaas);

            final CallbackHandler handler = new BonitaAuthenticationCallbackHandler(mUserName, mPassWord);

            // reset the configuration to null to reaload it
            Configuration.setConfiguration(null);

            final LoginContext loginContext = new LoginContext(mAuthKey, handler);
            loginContext.login();
            loginContext.logout();
            statusOperation.setSuccess("Connection with success");
            logger.info("Connection with success");

            /*
             * SUser user; try { user =
             * this.identityService.getUserByUserName(mUserName); } catch
             * (SUserNotFoundException e) {
             * finalResult.append("Error Identity "+e.toString()); } throw new
             * AuthenticationException(); }
             * if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
             * this.logger.log(getClass(), TechnicalLogSeverity.TRACE, LogUtil
             * .getLogAfterMethod(getClass(), "checkUserCredentials")); }
             */

        } catch (final LoginException e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();
            logger.severe("Exception e " + e.toString() + " at " + exceptionDetails);
            statusOperation.addError("Error JAAS " + e.toString());

        } catch (final IOException e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();
            logger.severe("Exception e " + e.toString() + " at " + exceptionDetails);
            statusOperation.addError("Error Temporary file " + e.toString());

        } finally
        {
            if (temporaryFile != null) {
                temporaryFile.delete();
            }

        }
        return statusOperation;
    }

    /**
     * @param userProvider
     * @param userFilter
     * @param useSSL
     * @param userName
     * @param password
     * @param logger
     * @return
     */
    public StatusOperation checkJaasOperation() {
        final StatusOperation statusOperation = new StatusOperation("JaasOperation");

        logger.info("Check LDAP userProvider[" + mJaasOpUserProvider + "] userFilter[" + mJaasOpUserFilter + "] userName[" + mJaasOpUserName + "] password["
                + mJaasOpPassword + "] useSSL["
                + mJaasOpUseSSL + "];<br>");

        SearchControls constraints = null;
        final Hashtable<String, Object> ldapEnvironment = new Hashtable<String, Object>(9);
        LdapContext ctx;
        final Pattern USERNAME_PATTERN = Pattern.compile("\\{USERNAME\\}");

        ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnvironment.put(Context.PROVIDER_URL, mJaasOpUserProvider);
        if (mJaasOpUseSSL) {
            ldapEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
        } else {
            ldapEnvironment.remove(Context.SECURITY_PROTOCOL);
        }
        final Matcher identityMatcher = USERNAME_PATTERN.matcher(mJaasOpIdentity);

        final String id = replaceUsernameToken(identityMatcher, mJaasOpIdentity, mJaasOpUserName);
        logger.info("Match [" + mJaasOpIdentity + "] with userName[" + mJaasOpUserName + "];<br> => Login:[" + id + "];<br>");

        ldapEnvironment.put(Context.SECURITY_CREDENTIALS, mJaasOpPassword);
        ldapEnvironment.put(Context.SECURITY_PRINCIPAL, id);

        try {
            // Connect to the LDAP server (using simple bind)
            ctx = new InitialLdapContext(ldapEnvironment, null);
            statusOperation.addDetails("################## Step 1: Connection successful;");

            constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(new String[0]); // return no
                                                               // attrs
            constraints.setReturningObjFlag(true); // to get the full DN

            final Matcher filterMatcher = USERNAME_PATTERN.matcher(mJaasOpUserFilter);
            final String searchBy = replaceUsernameToken(filterMatcher, mJaasOpUserFilter, mJaasOpUserName);
            statusOperation.addDetails("SearchBy[" + searchBy + "];<br>");

            final NamingEnumeration<SearchResult> results = ctx.search("", searchBy, constraints);
            statusOperation.addDetails("################## Step 2: Search without error;<br>");

            // Extract the distinguished name of the user's entry
            // (Use the first entry if more than one is returned)
            if (results.hasMore()) {
                final SearchResult entry = results.next();

                // %%% - use the SearchResult.getNameInNamespace method
                // available in JDK 1.5 and later.
                // (can remove call to constraints.setReturningObjFlag)
                final String userDN = ((Context) entry.getObject()).getNameInNamespace();

                statusOperation.addDetails("################### Step 3 : found entry: " + userDN + "; SUCCESS<p>");
                final StringBuffer finalResult = new StringBuffer();
                finalResult.append("BonitaAuthentication-1 {<br>");
                finalResult.append("&nbsp;&nbsp;&nbsp;com.sun.security.auth.module.LdapLoginModule REQUIRED<br>");
                finalResult.append("&nbsp;&nbsp;&nbsp;userProvider=\"" + mJaasOpUserProvider + "\"<br>");
                finalResult.append("&nbsp;&nbsp;&nbsp;userFilter=\"" + mJaasOpUserFilter + "\"<br>");
                finalResult.append("&nbsp;&nbsp;&nbsp;authIdentity=\"" + mJaasOpIdentity + "\"<br>");
                finalResult.append("&nbsp;&nbsp;&nbsp;debug=true<br>");
                finalResult.append("&nbsp;&nbsp;&nbsp;useSSL=" + (mJaasOpUseSSL ? "true" : "false") + "<br>");
                finalResult.append(" }");
                statusOperation.mStatusResultJson.put("jaascontent", finalResult.toString());

            } else {
                statusOperation.addDetails("################# Step 3 : No result found !;");
                statusOperation.addError("No result found");

            }
        } catch (final Exception e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            final String exceptionDetails = sw.toString();
            statusOperation.addError("Exception " + e.toString() + "<br>" + exceptionDetails);

            logger.severe("Exception " + e.toString());
        }
        return statusOperation;
    }

    /* ******************************************************************************** */
    /*                                                                                  */
    /* Toolbox */
    /*                                                                                  */
    /*                                                                                  */
    /* ******************************************************************************** */

    /**
     * @param matcher
     * @param string
     * @param userName
     * @return
     */
    private String replaceUsernameToken(final Matcher matcher, final String string, final String userName) {
        return matcher != null ? matcher.replaceAll(userName) : string;
    }

    /**
     * load the file, and retrun the content of in a String. When an error occure,
     * the result is null.
     */
    public String loadFile(final String fileName,
            final Object caller)
    {
        final StringBuffer result = new StringBuffer();
        try
        {
            final FileReader fileReader = new FileReader(fileName);
            int nbRead;
            final char[] buffer = new char[50000];
            while ((nbRead = fileReader.read(buffer, 0, 50000)) > 0) {
                result.append(new String(buffer).substring(0, nbRead));
            }
            fileReader.close();
            return result.toString();
        } catch (final Exception e)
        {
            return null;
        }
    }
}
