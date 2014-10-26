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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.westerfeld.rewritehtml.ConfigManager;
import com.westerfeld.rewritehtml.RequestHeaderTransformer;
import com.westerfeld.rewritehtml.config.HeaderFilter;
import com.westerfeld.rewritehtml.config.RequestHeaderFilter;

public class ConfRequestHeaderTransformer implements RequestHeaderTransformer {

	private static final Logger log = LoggerFactory.getLogger(ConfRequestHeaderTransformer.class.getName());
	
	private ConfigManager configManager;
	
	public ConfRequestHeaderTransformer(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public boolean containsMatchingHeaderSupplyIfMissing(HttpServletRequest httpReq, String name) {
		boolean isSupplyIfMissing = false;
        ResolvedConfig config = configManager.getConfig();
	    for (RequestHeaderFilter requestHeaderFilter: config.getRequestHeaderFilters()) {
	    	if (name.equals(requestHeaderFilter.getHeader()) && requestHeaderFilter.isSupplyIfMissing()) {
	    		isSupplyIfMissing = true;
	    		break;
	    	}
	    }
	    return isSupplyIfMissing;
	}
	
	@Override
	public void fillMatchingHeaderSupplyIfMissing(Set<String> headers, HttpServletRequest httpReq) {
        ResolvedConfig config = configManager.getConfig();
		for (RequestHeaderFilter requestHeaderFilter: config.getRequestHeaderFilters()) {
        	if (!headers.contains(requestHeaderFilter.getHeader()) && requestHeaderFilter.isSupplyIfMissing()) {
        		headers.add(requestHeaderFilter.getHeader());
        	}
        }
	}

	@Override
	public String transformHeaderValueSupplyIfMissing(
			HttpServletRequest httpReq, String name, String value) {
        ResolvedConfig config = configManager.getConfig();
		for (RequestHeaderFilter requestHeaderFilter: config.getRequestHeaderFilters()) {
            if (name.equals(requestHeaderFilter.getHeader())) {
                if (value != null || requestHeaderFilter.isSupplyIfMissing()) {
                    String originalValue = value;
                    value = config.replace(
                            "request header '" + name + "'",
                            value, 
                            requestHeaderFilter.getReplacements(),
                            httpReq, null, null, // httpResponse not available here?!
                            null);
                    log.debug("Returning updated request header {} translated from {} to {} for {}", name, originalValue, value, httpReq.getRequestURI());
                }
            }
        }
		return value;
	}

	@Override
	public void supplyDefaultHeadersIfMissing(HttpServletResponseWrapper httpResp, Set<String> headers) {
		for (HeaderFilter responseHeaderFilter: configManager.getConfig().getResponseHeaderFilters()) {
			if (responseHeaderFilter.isSupplyIfMissing() && !headers.contains(responseHeaderFilter.getHeader())) {
				httpResp.addHeader(responseHeaderFilter.getHeader(), "");
			}
		}

	}
	
    
}
