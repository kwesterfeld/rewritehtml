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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.westerfeld.rewritehtml.JSoupHtmlDOMTransformer;

public class PrependDivJsoupTransformer implements JSoupHtmlDOMTransformer {

	public PrependDivJsoupTransformer() {
	}
	
	@Override
	public Document transform(Document document, HttpServletRequest request, HttpServletResponse response) {
		
		Element newDiv = document.createElement("div");
		newDiv.attr("class", "rewritehtml-injected-body-header-class");
		newDiv.text("**** rewritehtml-inject *****");
		document.body().prependChild(newDiv);

		// document.getElementById(id)"#logo > a:nth-child(1) > span:nth-child(1)"
		Elements slashdotElts = document.getElementsMatchingOwnText("Slashdot");
		for(Element s : slashdotElts) {
			String oldText = s.text();
			String newText = oldText.replaceAll("Slashdot", "Annoted-Slashdot");
			s.text(newText);
		}
		
		return document;
	}

	@Override
	public String toString() {
		return "IdentityJSoupHtmlDOMTransformer";
	}
	
}
