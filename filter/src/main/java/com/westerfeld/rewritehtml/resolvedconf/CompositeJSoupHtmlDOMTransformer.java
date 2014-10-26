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

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.westerfeld.rewritehtml.JSoupHtmlDOMTransformer;

public class CompositeJSoupHtmlDOMTransformer implements JSoupHtmlDOMTransformer {

	private static final Logger log = LoggerFactory.getLogger(CompositeJSoupHtmlDOMTransformer.class.getName());
	
	private final List<JSoupHtmlDOMTransformer> transformers;

	public CompositeJSoupHtmlDOMTransformer(List<JSoupHtmlDOMTransformer> transformers) {
		this.transformers = transformers;
	}
	
	@Override
    public String toString() {
        return "CompositeJSoupHtmlDOMTransformer";
    }

	@Override
	public Document transform(Document htmlDOMContent, HttpServletRequest request, HttpServletResponse response) {
		Document res = htmlDOMContent;
		for(JSoupHtmlDOMTransformer transformer : transformers) {
			try {
	    		res = transformer.transform(res, request, response);
	    	} catch(Exception ex) {
	    		log.warn("JSoupHtmlDOMTransformer failed! " + transformer + " ... ignore, no rethrow!, ex:" + ex.getMessage());
	    	}
		}
    	return res;
	}

	
}
