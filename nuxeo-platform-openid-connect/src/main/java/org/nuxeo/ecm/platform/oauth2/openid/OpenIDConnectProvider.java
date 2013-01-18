package org.nuxeo.ecm.platform.oauth2.openid;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.platform.oauth2.openid.auth.UserInfo;
import org.nuxeo.ecm.platform.oauth2.providers.NuxeoOAuth2ServiceProvider;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

public class OpenIDConnectProvider {

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	NuxeoOAuth2ServiceProvider oauth2Provider;

	private String userInfoURL;

	private String icon;

	public OpenIDConnectProvider(NuxeoOAuth2ServiceProvider oauth2Provider, String userInfoURL, String icon) {
		this.oauth2Provider = oauth2Provider;
		this.userInfoURL = userInfoURL;
		this.icon = icon;
	}

	public String getRedirectUri() {
		return "http://localhost:8080/nuxeo/nxstartup.faces?provider="+oauth2Provider.getServiceName();
	}

	public String getAuthenticationUrl(String requestedUrl) {
		// redirect to the authorization flow
		AuthorizationCodeFlow flow = oauth2Provider.getAuthorizationCodeFlow(HTTP_TRANSPORT, JSON_FACTORY);
		AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl(); //.setResponseTypes("token");
		authorizationUrl.setRedirectUri(getRedirectUri());

		return authorizationUrl.build();
	}

	public String getName() {
		return oauth2Provider.getServiceName();
	}

	public String getIcon() {
		return icon;
	}

	public String getAccessToken(String code) {
		String accessToken = null;

		HttpResponse response = null;

		try {
			AuthorizationCodeFlow flow = oauth2Provider.getAuthorizationCodeFlow(HTTP_TRANSPORT, JSON_FACTORY);

			String redirectUri = getRedirectUri();

			response = flow.newTokenRequest(code).setRedirectUri(redirectUri).executeUnparsed();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String type = response.getContentType();

		try {
			if (type.contains("text/plain")) {
				String str = response.parseAsString();
				String[] params = str.split("&");
				for (String param : params) {
					String[] kv = param.split("=");
					if (kv[0].equals("access_token")) {
						accessToken = kv[1]; // get the token
						break;
					}
				}
			} else { // try to parse as JSON

				TokenResponse tokenResponse = response.parseAs(TokenResponse.class);
				accessToken = tokenResponse.getAccessToken();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return accessToken;
	}

	public UserInfo getUserInfo(String accessToken) {
		UserInfo userInfo = null;

		HttpRequestFactory requestFactory = 
				HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) throws IOException {
						request.setParser(new JsonObjectParser(JSON_FACTORY));
					}
				});




		GenericUrl url = new GenericUrl(userInfoURL);
		url.set("access_token", accessToken);

		try {
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse response = request.execute();
			userInfo = response.parseAs(UserInfo.class);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return userInfo;
	}
}
