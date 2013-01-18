/**
 * 
 */

package org.nuxeo.ecm.platform.oauth2.openid;

import java.util.Collection;

/**
 * @author Nelson Silva <nelson.silva@inevo.pt>
 */
public interface OpenIDConnectProviderRegistry {

	Collection<OpenIDConnectProvider> getProviders();
	OpenIDConnectProvider getProvider(String name);

	
}
