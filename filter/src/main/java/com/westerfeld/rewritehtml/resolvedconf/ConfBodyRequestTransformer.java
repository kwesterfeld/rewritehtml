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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.westerfeld.rewritehtml.BodyRequestTransformer;
import com.westerfeld.rewritehtml.ConfigManager;
import com.westerfeld.rewritehtml.config.Replacement;

public class ConfBodyRequestTransformer implements BodyRequestTransformer {

	private ConfigManager configManager;
	private List<Replacement> replacements;
	
	public ConfBodyRequestTransformer(ConfigManager configManager, List<Replacement> replacements) {
		this.configManager = configManager;
		this.replacements = replacements;
	}
	
	public String transformContent(String content, HttpServletRequest httpReq,
			HttpServletResponse httpResp) {
		ResolvedConfig config = configManager.getConfig();
		String res = config.replace(
            "content", content, 
            replacements,
            httpReq,
            httpResp,
            null, // filterChain
            null); // context
		return res;
	}
	
}
