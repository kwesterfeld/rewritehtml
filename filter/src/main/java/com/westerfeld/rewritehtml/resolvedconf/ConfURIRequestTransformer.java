/**
 *  Copyright 2011 Kurt Westerfeld
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.westerfeld.rewritehtml.resolvedconf;

import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.westerfeld.rewritehtml.ConfigManager;
import com.westerfeld.rewritehtml.URIFilterType;
import com.westerfeld.rewritehtml.URIRequestTransformer;
import com.westerfeld.rewritehtml.config.URIFilter;

public class ConfURIRequestTransformer implements URIRequestTransformer {

	private ConfigManager configManager;
	
	public ConfURIRequestTransformer(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	public String transformFirstMatchingURI(URITransformResult returnRes, String uri, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			FilterChain filterChain,
			Map<String,Object> context) {
		URIFilterType type = null;
		ResolvedConfig config = configManager.getConfig();
	    for (URIFilter uriFilter: config.getUriFilters()) {
	        if (uri.matches(uriFilter.getUriMatch())) {
				uri = config.replace("uri", uri, 
	                    uriFilter.getReplacements(),
	                    httpReq, httpResp, filterChain, context);
	            type = uriFilter.getType();
	            break;
	        }
	    }
	    returnRes.type = type;
	    return uri;
	}
}
