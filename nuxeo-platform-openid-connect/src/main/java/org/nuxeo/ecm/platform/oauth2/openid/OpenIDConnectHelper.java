package org.nuxeo.ecm.platform.oauth2.openid;

import java.util.Collection;
import java.util.List;

import org.nuxeo.runtime.api.Framework;

public class OpenIDConnectHelper {
	protected static OpenIDConnectProviderRegistry getOpenIDConnectProviderRegistry() throws Exception {
		return Framework.getService(OpenIDConnectProviderRegistry.class);
	}
	
	public static Collection<OpenIDConnectProvider> getProviders() {
		Collection<OpenIDConnectProvider> providers = null;
		try {
			providers =  getOpenIDConnectProviderRegistry().getProviders();
		} catch (Exception e) {
			
		}
		return providers;
	}
}
