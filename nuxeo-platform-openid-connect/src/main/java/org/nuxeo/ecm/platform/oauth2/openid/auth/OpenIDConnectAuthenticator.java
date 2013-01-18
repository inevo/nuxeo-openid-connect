package org.nuxeo.ecm.platform.oauth2.openid.auth;

import static org.nuxeo.ecm.platform.ui.web.auth.NXAuthConstants.LOGIN_ERROR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.ecm.platform.oauth2.openid.OpenIDConnectProvider;
import org.nuxeo.ecm.platform.oauth2.openid.OpenIDConnectProviderRegistry;
import org.nuxeo.ecm.platform.ui.web.auth.interfaces.NuxeoAuthenticationPlugin;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

public class OpenIDConnectAuthenticator implements NuxeoAuthenticationPlugin {

	private static final Log log = LogFactory.getLog(OpenIDConnectAuthenticator.class);

	public static final String CODE_URL_PARAM_NAME = "code";
	public static final String ERROR_URL_PARAM_NAME = "error";
	public static final String PROVIDER_URL_PARAM_NAME = "provider";

	private OpenIDConnectProviderRegistry registry;

	private UserManager userManager;

	protected void sendError(HttpServletRequest req, String msg) {
		req.setAttribute(LOGIN_ERROR, msg);
	}

	public UserIdentificationInfo retrieveIdentityFromOAuth(
			HttpServletRequest req, HttpServletResponse resp) {

		// Getting the "error" URL parameter
		String error = req.getParameter(ERROR_URL_PARAM_NAME);

		/// Checking if there was an error such as the user denied access
		if (error != null && error.length() > 0) {
			sendError(req, "There was an error: \""+error+"\".");
			return null;
		}

		// Getting the "code" URL parameter
		String code = req.getParameter(CODE_URL_PARAM_NAME);

		// Checking conditions on the "code" URL parameter
		if (code == null || code.isEmpty()) {
			sendError(req, "There was an error: \""+error+"\".");
			return null;
		}
		
		// Getting the "provider" URL parameter
		String serviceProviderName = req.getParameter(PROVIDER_URL_PARAM_NAME);
		
		// Checking conditions on the "provider" URL parameter
		if (serviceProviderName == null || serviceProviderName.isEmpty()) {
			sendError(req, "Missing OpenID Connect Provider ID.");
			return null;
		}

		OpenIDConnectProvider provider;
		try {
			provider = getProviderRegistry().getProvider(serviceProviderName);

			if (provider == null) {
				sendError(req, "No service provider called: \""+ serviceProviderName +"\".");
				return null;
			}

			// Validate the token 
			String accessToken = provider.getAccessToken(code);

			if (accessToken == null) {
				return null;
			}
			
			UserInfo info = provider.getUserInfo(accessToken);

			//TODO - get the email
			String email = info.email;

			String userId = findUser(email);

			if (userId == null) {
				sendError(req, "No user found with email: \""+ email +"\".");
				return null;
			}


			return new UserIdentificationInfo(userId, userId);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		return null;
	}

	protected String findUser(String email) {

		String username = null;

		try {
			Map<String, Serializable> query = new HashMap<String, Serializable>();
			query.put(getUserManager().getUserEmailField(), email);

			DocumentModelList users = getUserManager().searchUsers(query, null);

			if (users.isEmpty()) {
				return null;
			}

			DocumentModel user = users.get(0);
			username = (String) user.getPropertyValue(getUserManager().getUserIdField());

		} catch (ClientException e) {}

		return username;
	}

	public OpenIDConnectProviderRegistry getProviderRegistry() {
		if (registry == null) {
			try {
				registry = Framework.getService(OpenIDConnectProviderRegistry.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return registry;
	}
	
	public UserManager getUserManager() {
		if (userManager == null) {
			try {
				userManager = Framework.getService(UserManager.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return userManager;
	}

	public List<String> getUnAuthenticatedURLPrefix() {
		return new ArrayList<String>();
	}


	@Override
	public UserIdentificationInfo handleRetrieveIdentity(
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		//if (httpRequest.getParameter(FORM_SUBMITTED_MARKER) != null) {
			//return super.handleRetrieveIdentity(httpRequest, httpResponse);
		//} else {
			String error = httpRequest.getParameter(ERROR_URL_PARAM_NAME);
			String code = httpRequest.getParameter(CODE_URL_PARAM_NAME);
			String serviceProviderName = httpRequest.getParameter(PROVIDER_URL_PARAM_NAME);
			if (serviceProviderName == null) {
				return null;
			}
			if (code == null && error == null) {
				return null;
			}
			UserIdentificationInfo userIdent = retrieveIdentityFromOAuth(httpRequest, httpResponse);
			if (userIdent != null) {
				userIdent.setAuthPluginName("TRUSTED_LM");
			}
			return userIdent;
		//}
	}

	@Override
	public Boolean handleLoginPrompt(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, String baseURL) {
		return false;
	}

	@Override
	public Boolean needLoginPrompt(HttpServletRequest httpRequest) {
		return false;
	}

	@Override
	public void initPlugin(Map<String, String> parameters) {
		// TODO Auto-generated method stub
		
	}
}
