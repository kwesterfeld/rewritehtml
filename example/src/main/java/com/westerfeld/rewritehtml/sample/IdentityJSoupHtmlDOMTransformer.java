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

package com.westerfeld.rewritehtml.sample;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import com.westerfeld.rewritehtml.JSoupHtmlDOMTransformer;

public class IdentityJSoupHtmlDOMTransformer implements JSoupHtmlDOMTransformer {

	public IdentityJSoupHtmlDOMTransformer() {
	}
	
	@Override
	public Document transform(Document document, HttpServletRequest request, HttpServletResponse response) {
		return document;
	}

	@Override
	public String toString() {
		return "IdentityJSoupHtmlDOMTransformer";
	}
	
}
