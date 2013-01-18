/**
 * 
 */

package org.nuxeo.ecm.platform.oauth2.openid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.oauth2.providers.NuxeoOAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistryImpl;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.osgi.framework.Bundle;


/**
 * @author Nelson Silva <nelson.silva@inevo.pt>
 */
public class OpenIDConnectProviderRegistryImpl extends DefaultComponent implements OpenIDConnectProviderRegistry {

	protected static final Log log = LogFactory.getLog(OpenIDConnectProviderRegistryImpl.class);
	
	public static final String PROVIDER_EP = "providers";
	
	protected Map<String, OpenIDConnectProvider> providers = new HashMap<String, OpenIDConnectProvider>();

	
	protected OAuth2ServiceProviderRegistry getOAuth2ServiceProviderRegistry() throws Exception {
		return Framework.getService(OAuth2ServiceProviderRegistry.class);
	}
	
    @Override
	public void registerContribution(Object contribution,
			String extensionPoint, ComponentInstance contributor)
					throws Exception {
		if (PROVIDER_EP.equals(extensionPoint)) {
			OpenIDConnectProviderDescriptor provider = (OpenIDConnectProviderDescriptor) contribution;
			
			OAuth2ServiceProviderRegistry oauth2ProviderRegistry = getOAuth2ServiceProviderRegistry();
			
			NuxeoOAuth2ServiceProvider oauth2Provider = oauth2ProviderRegistry.getProvider(provider.getName());
			
			if (oauth2Provider == null) {
				oauth2Provider = oauth2ProviderRegistry.addProvider(
						provider.getName(), 
						provider.getTokenServerURL(), 
						provider.getAuthorizationServerURL(), 
						provider.getClientId(), 
						provider.getClientSecret(),
						Arrays.asList(provider.getScopes()));
			}
			
			providers.put(provider.getName(), new OpenIDConnectProvider(oauth2Provider, provider.getUserInfoURL(), provider.getIcon()));
		}
	}

	@Override
	public Collection<OpenIDConnectProvider> getProviders() {
		return providers.values();
	}

	@Override
	public OpenIDConnectProvider getProvider(String name) {
		return providers.get(name);
	}
    
    
}
