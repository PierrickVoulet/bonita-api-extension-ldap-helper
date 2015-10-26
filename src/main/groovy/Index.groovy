import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Locale;
import java.util.logging.Logger;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.page.PageController;
import org.bonitasoft.console.common.server.page.PageResourceProvider;
import org.bonitasoft.console.common.server.page.PageContext;
import org.bonitasoft.console.common.server.page.RestApiController
import org.bonitasoft.console.common.server.page.RestApiResponse;
import org.bonitasoft.console.common.server.page.RestApiResponseBuilder;
import org.bonitasoft.console.common.server.page.RestApiUtil;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.apiextension.ldap.helper.CallParameters
import com.bonitasoft.apiextension.ldap.helper.JaasCheck
import com.bonitasoft.apiextension.ldap.helper.PropertiesBonitaConnection
import com.bonitasoft.apiextension.ldap.helper.PropertiesLdapConnection
import com.bonitasoft.apiextension.ldap.helper.CraneTruckAccess
import com.bonitasoft.apiextension.ldap.helper.PropertiesSynchronize.PropertiesSynchronizeTest
import com.bonitasoft.apiextension.ldap.helper.PropertiesSynchronize
import com.bonitasoft.engine.api.PlatformMonitoringAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.api.TenantAPIAccessor;

import org.json.simple.JSONValue

public class Index implements RestApiController {

	@Override
	RestApiResponse doHandle(HttpServletRequest request, PageResourceProvider pageResourceProvider, PageContext pageContext, RestApiResponseBuilder apiResponseBuilder, RestApiUtil restApiUtil) {
		try {
			//get action name
			String action = request.getParameter("action")
			String json = request.getParameter("json");
			
			//if no action parameter
			if (action == null) {
				return buildErrorResponse(apiResponseBuilder, "the parameter action is missing", restApiUtil.logger)
			}
			
			APISession session = pageContext.getApiSession()
			ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
			PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI(session);
			IdentityAPI identityApi = TenantAPIAccessor.getIdentityAPI(session);
	
			HashMap<String,Object> answer = null;
			if ("readfromproperties".equals(action)) {
				CallParameters craneTruckParam = CallParameters.getInstanceFromJsonSt( json );
				answer = CraneTruckAccess.readFromProperties(craneTruckParam);
			} else if ("writetoproperties".equals(action)) {
				CallParameters craneTruckParam = CallParameters.getInstanceFromJsonSt( json );
				answer = CraneTruckAccess.writeToProperties(json,craneTruckParam);
			} else if ("testsynchronize".equals(action)) {
				PropertiesSynchronizeTest synchronizeTest = PropertiesSynchronize.PropertiesSynchronizeTest.getInstanceFromJsonSt( json );
				answer = PropertiesSynchronize.checkSynchronize( synchronizeTest ).toMap();
			} else if ("testldapconnection".equals(action)) {
				PropertiesLdapConnection ldapConnection = PropertiesLdapConnection.getInstanceFromJsonSt( json );
				answer = ldapConnection.checkLdapConnection().toMap();
			}
			else if ("getdefaultbonitaconnection".equals(action)) {
				answer = PropertiesBonitaConnection.getDefaultValue(session, identityApi).toMap();
			}
			else if ("testbonitaconnection".equals(action)) {
				PropertiesBonitaConnection bonitaConnection = PropertiesBonitaConnection.getInstanceFromJsonSt( json );
				answer = bonitaConnection.checkBonitaConnection().toMap();
			} else if ("testjaasconnection".equals(action)) {
				String jsonStReplace = json.replace("_£", "&");
				JaasCheck jaasCheck = JaasCheck.getInstanceFromJsonSt( jsonStReplace );
				answer = jaasCheck.checkJaasConnection().toMap();
			}
	
			if (answer != null) {
				String jsonDetailsSt = JSONValue.toJSONString( answer );
				return buildResponse(apiResponseBuilder, jsonDetailsSt);
			} else {
				return buildErrorResponse(apiResponseBuilder, "the parameter action is not recognized", restApiUtil.logger)
			}
		} catch(Exception e) {
			return buildErrorResponse(apiResponseBuilder, e.getMessage(), restApiUtil.logger)
		}
	}
	
	protected RestApiResponse buildErrorResponse(RestApiResponseBuilder apiResponseBuilder, String message, Logger logger ) {
		logger.severe message

		Map<String, String> result = [:]
		result.put "error", message
		apiResponseBuilder.withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
		buildResponse apiResponseBuilder, result
	}
	
	protected RestApiResponse buildResponse(RestApiResponseBuilder apiResponseBuilder, Serializable result) {
		apiResponseBuilder.with {
			withResponse(result)
			build()
		}
	}
}
