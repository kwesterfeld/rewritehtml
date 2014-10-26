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

import com.westerfeld.rewritehtml.BodyRequestTransformer;
import com.westerfeld.rewritehtml.BodyRequestTransformerSelector;
import com.westerfeld.rewritehtml.ConfigManager;
import com.westerfeld.rewritehtml.JSoupHtmlDOMTransformer;
import com.westerfeld.rewritehtml.config.ContentFilter;

public class ConfBodyRequestTransformerHeaderSelector implements BodyRequestTransformerSelector {
	
	private ConfigManager configManager;
	
	public ConfBodyRequestTransformerHeaderSelector(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public BodyRequestTransformer findMatchingBodyRequestTransformer(
			String contentType, String requestURI) {
		BodyRequestTransformer res = null;
		for (ContentFilter contentFilter : configManager.getConfig().getContentFilters()) {
            if (contentType.matches(contentFilter.getMimeTypeMatch()) &&
                requestURI.matches(contentFilter.getUriMatch())) {
                res = new ConfBodyRequestTransformer(configManager, contentFilter.getReplacements());
                break;
            }
        }
		return res;
	}

	@Override
	public JSoupHtmlDOMTransformer findMatchingDOMBodyRequestTransformer(
			String contentType, String requestURI) {
		JSoupHtmlDOMTransformer res = null;
		for (ResolvedHtmlDOMContentFilter htmlDOMContentFilter : configManager.getConfig().getHtmlDOMContentFilters()) {
            if (htmlDOMContentFilter.matchRequest(requestURI, contentType)) {
                res = new CompositeJSoupHtmlDOMTransformer(htmlDOMContentFilter.getResolvedHtmlDOMReplacements());
                break;
            }
        }
		return res;
	}

	
}
