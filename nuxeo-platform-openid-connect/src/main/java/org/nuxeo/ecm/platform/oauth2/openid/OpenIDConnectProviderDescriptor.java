package org.nuxeo.ecm.platform.oauth2.openid;

import java.io.Serializable;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("provider")
public class OpenIDConnectProviderDescriptor implements Serializable {
	private static final long serialVersionUID = 1L;

	@XNode("name")
	private String name;

	@XNode("tokenServerURL")
	private String tokenServerURL;

	@XNode("authorizationServerURL")
	private String authorizationServerURL;

	@XNode("userInfoURL")
	private String userInfoURL;
	
	@XNode("clientId")
	private String clientId;

	@XNode("clientSecret")
	private String clientSecret;

	@XNodeList(value = "scope", type = String[].class, componentType = String.class)
	public String[] scopes;

	@XNode("icon")
	private String icon;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getName() {
		return name;
	}

	public String getTokenServerURL() {
		return tokenServerURL;
	}

	public String getAuthorizationServerURL() {
		return authorizationServerURL;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String[] getScopes() {
		return scopes;
	}

	public String getUserInfoURL() {
		return userInfoURL;
	}

	public String getIcon() {
		return icon;
	}

	
	
}

