package org.nuxeo.ecm.platform.oauth2.openid.auth;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class UserInfo extends GenericJson {        
	@Key("email")
	public String email;
}
