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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.westerfeld.rewritehtml.ConfigManager;
import com.westerfeld.rewritehtml.ResponseHeaderTransformer;
import com.westerfeld.rewritehtml.config.HeaderFilter;

public class ConfResponseHeaderTransformer implements ResponseHeaderTransformer {

	private ConfigManager configManager;
	
	public ConfResponseHeaderTransformer(ConfigManager configManager) {
		this.configManager = configManager;
	}

	@Override
	public String transformHeaderValue(String name, String value,
			HttpServletRequest httpReq, HttpServletResponse httpResp) {
		ResolvedConfig config = configManager.getConfig();
		String nameValueStr = "response header '" + name + "'";
		Map<String,Object> context = new HashMap<String,Object>();
		context.put("header", name);
		for (HeaderFilter responseHeaderFilter: config.getResponseHeaderFilters()) {
            if (name.equals(responseHeaderFilter.getHeader())) {
				value = config.replace(nameValueStr, value, 
                        responseHeaderFilter.getReplacements(),
                        httpReq, httpResp, null, context);
            }
        }
		return value;
	}
	
	    
}
