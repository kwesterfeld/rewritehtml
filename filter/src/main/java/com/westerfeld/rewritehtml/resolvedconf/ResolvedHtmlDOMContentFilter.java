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

import java.util.List;

import com.westerfeld.rewritehtml.JSoupHtmlDOMTransformer;
import com.westerfeld.rewritehtml.config.HtmlDOMContentFilter;

/**
 * resolved class for config HtmlDOMContentFilter 
 */
public class ResolvedHtmlDOMContentFilter {
	
	protected final HtmlDOMContentFilter config;
    
    protected final List<JSoupHtmlDOMTransformer> resolvedHtmlDOMReplacements; 
    
    public ResolvedHtmlDOMContentFilter(HtmlDOMContentFilter config, List<JSoupHtmlDOMTransformer> resolvedHtmlDOMReplacements) {
    	this.config = config;
    	this.resolvedHtmlDOMReplacements = resolvedHtmlDOMReplacements;
    }
    
    public List<JSoupHtmlDOMTransformer> getResolvedHtmlDOMReplacements() {
		return resolvedHtmlDOMReplacements;
	}

	public HtmlDOMContentFilter getConfig() {
		return config;
	}

	@Override
    public String toString() {
        return String.format("(Resolved Html DOM content filter for %s)", this.config.toString());
    }

	public boolean matchRequest(String requestURI, String contentType) {
		return contentType.matches(config.getMimeTypeMatch())
				&& requestURI.matches(config.getUriMatch());
	}

}
