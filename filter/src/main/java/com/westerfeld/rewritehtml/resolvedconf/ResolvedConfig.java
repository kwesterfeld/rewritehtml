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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.westerfeld.rewritehtml.config.Config;
import com.westerfeld.rewritehtml.config.ContentFilter;
import com.westerfeld.rewritehtml.config.Replacement;
import com.westerfeld.rewritehtml.config.RequestHeaderFilter;
import com.westerfeld.rewritehtml.config.ResponseHeaderFilter;
import com.westerfeld.rewritehtml.config.URIFilter;

public class ResolvedConfig {

	private static final Logger log = LoggerFactory.getLogger(ResolvedConfig.class.getName());
	
	private final Config config;
	
//    private List<ContentFilter> contentFilters = new ArrayList<ContentFilter>();
    private List<ResolvedHtmlDOMContentFilter> htmlDOMContentFilters;
//	private List<URIFilter> uriFilters = new ArrayList<URIFilter>();
//    private List<ResponseHeaderFilter> responseHeaderFilters = new ArrayList<ResponseHeaderFilter>();
//    private List<RequestHeaderFilter> requestHeaderFilters = new ArrayList<RequestHeaderFilter>();
    
    private Map<Replacement,Serializable> mvelExpressionCache = Collections.synchronizedMap(new WeakHashMap<Replacement, Serializable>());

    
    public ResolvedConfig(Config config,
    		List<ResolvedHtmlDOMContentFilter> htmlDOMContentFilters
    		) {
    	this.config = config;
    	this.htmlDOMContentFilters = htmlDOMContentFilters;
    }
    
    @Override
    public String toString() {
        return String.format("(resolved config for %s)", this.config.toString());
    }

    public List<ContentFilter> getContentFilters() {
        return config.getContentFilters();
    }

    public List<ResolvedHtmlDOMContentFilter> getHtmlDOMContentFilters() {
        return htmlDOMContentFilters;
    }

    public List<ResponseHeaderFilter> getResponseHeaderFilters() {
        return config.getResponseHeaderFilters();
    }

    public List<RequestHeaderFilter> getRequestHeaderFilters() {
        return config.getRequestHeaderFilters();
    }

    public List<URIFilter> getUriFilters() {
        return config.getUriFilters();
    }
    
    
    
    public String replace(String what, String value, List<Replacement> replacements,
    		HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			FilterChain filterChain,
			Map<String,Object> context) {
        String originalValue = value;
        for (Replacement replacement: replacements) {
            switch (replacement.getType()) {
            case ReplaceAll:
                value = value.replace(replacement.getFrom(), replacement.getEffectiveTo());
                break;
            case ReplaceFirst:
                value = value.replaceFirst(Pattern.quote(replacement.getFrom()), replacement.getEffectiveTo());
                break;
            case ReplaceAllRegex:
                value = value.replaceAll(replacement.getFrom(), replacement.getEffectiveTo());
                break;
            case ReplaceFirstRegex:
                value = value.replaceFirst(replacement.getFrom(), replacement.getEffectiveTo());
                break;
            case MVEL: 
                try {
                    Map<String,Object> vars = new HashMap<String,Object>();
                    vars.put("request", httpReq);
                    vars.put("session", httpReq.getSession(false));
                    vars.put("response", httpResp);
                    
                    if (context != null) {
                    	vars.put("context", context);
                    }
                    vars.put("value", value);
                    
                    // TODO ... use ResolvedReplacement instead of HashMap cache ...
                    Serializable expression = mvelExpressionCache.get(replacement);
                    if (expression == null) {
                        mvelExpressionCache.put(replacement, expression = MVEL.compileExpression(replacement.getEffectiveTo()));
                    }
                    log.debug("Evaluating {} for replacement using expression {} and vars {}: {} {}", replacement.getEffectiveTo(), expression, vars, what, value);
                    Object rvalue = MVEL.executeExpression(expression, vars);
                    if (rvalue == null) {
                        return null;
                    }
                    value = rvalue.toString();
                } catch (Exception e) {
                    log.warn("Unexpected exception occurred while evaluating {} with replacement expression: {}", what, replacement.getEffectiveTo(), e);
                }
 
                break;
            }
        }
        
        if (originalValue == null && value != null) {
            log.debug("Provided initial value of {} as {}", what, value);
        }
        else if (!originalValue.equals(value)) {
            log.debug("Performed replacement of {} from {} to {}", what, originalValue, value);
        }
        
        return value;
    }

}
