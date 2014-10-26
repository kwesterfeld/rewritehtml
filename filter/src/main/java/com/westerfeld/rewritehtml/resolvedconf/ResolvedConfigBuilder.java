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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.westerfeld.rewritehtml.JSoupHtmlDOMTransformer;
import com.westerfeld.rewritehtml.config.Config;
import com.westerfeld.rewritehtml.config.HtmlDOMContentFilter;
import com.westerfeld.rewritehtml.config.HtmlDOMTransformer;

public class ResolvedConfigBuilder {

	private static final Logger log = LoggerFactory.getLogger(ResolvedConfigBuilder.class.getName());
	
	public ResolvedConfig build(Config src) {
		List<ResolvedHtmlDOMContentFilter> htmlDOMContentFilters = new ArrayList<ResolvedHtmlDOMContentFilter>();
		if (src.getHtmlDOMContentFilters() != null) {
			for(HtmlDOMContentFilter rawHtmlDOMContentFilter : src.getHtmlDOMContentFilters()) {
				htmlDOMContentFilters.add(buildResolvedHtmlDOMContentFilter(rawHtmlDOMContentFilter));
			}
		}
		
        ResolvedConfig config = new ResolvedConfig(src, htmlDOMContentFilters);
		return config;
	}

	private ResolvedHtmlDOMContentFilter buildResolvedHtmlDOMContentFilter(
			HtmlDOMContentFilter src) {
		List<JSoupHtmlDOMTransformer> resolvedHtmlDOMReplacements = new ArrayList<JSoupHtmlDOMTransformer>();
		if (src.getHtmlDOMTransformers() != null) {
			for(HtmlDOMTransformer srcElt : src.getHtmlDOMTransformers()) {
				resolvedHtmlDOMReplacements.add(buildJSoupHtmlDOMTransformer(srcElt));
			}
		}
		return new ResolvedHtmlDOMContentFilter(src, resolvedHtmlDOMReplacements);
	}

	public JSoupHtmlDOMTransformer buildJSoupHtmlDOMTransformer(HtmlDOMTransformer src) {
    	String className = src.getClassName();
    	Class<?> clazz = null;
    	try {
    		clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			log.error("Class not found! " + className);
		}
    	Object htmlDOMReplacementInstance = null;
    	try {
			htmlDOMReplacementInstance = clazz.newInstance();
		} catch (InstantiationException e) {
			log.error("Class instance creation failed! " + className);
		} catch (IllegalAccessException e) {
			log.error("Class instance creation illegal access! " + className);
		}
    	return (JSoupHtmlDOMTransformer) htmlDOMReplacementInstance;    	
	}
	
}
