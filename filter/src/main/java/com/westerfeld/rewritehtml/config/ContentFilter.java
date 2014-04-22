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

package com.westerfeld.rewritehtml.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="contentFilter")
public class ContentFilter extends BaseUriConstrainedFilter {
    
    private String mimeTypeMatch;
	private ContentMap contentMap;
    
    public ContentFilter() {}
    
    @Override
    public String toString() {
        return String.format("(content filter uri %s, mime type %s%s)%s", this.uriMatch, this.mimeTypeMatch, this.contentMap != null ? ", " + this.contentMap.toString() : "", super.toString());
    }

    @XmlAttribute
    public String getMimeTypeMatch() {
        return mimeTypeMatch;
    }

    public void setMimeTypeMatch(String mimeTypeMatch) {
        this.mimeTypeMatch = mimeTypeMatch;
    }
    
    public ContentMap getContentMap() {
    	return this.contentMap;
    }

	public void setContentMap(ContentMap contentMap) {
		this.contentMap = contentMap;
	}

	@Override
	public List<Replacement> getReplacements() {
		List<Replacement> result = super.getReplacements();
		
		if (this.contentMap != null) {
			result = new ArrayList<Replacement>(result);
			
			// Add pre-defined content filter replacements.
			String from = this.contentMap.getFrom();
			String to = this.contentMap.getTo();
			if (from == null) {
				from = "";
			}
			if (to == null) {
				to = "";
			}
			result.add(new Replacement("href=(['\"])/([^/])" + from, "href=$1" + to + "$2", ReplaceType.ReplaceAllRegex));
			result.add(new Replacement("src=(['\"])/([^/])" + from, "src=$1" + to + "$2", ReplaceType.ReplaceAllRegex));
			result.add(new Replacement("url\\((['\"])/([^/])" + from, "url($1" + to + "$2", ReplaceType.ReplaceAllRegex));
			result.add(new Replacement("url\\(/([^/])" + from, "url(/$1" + to, ReplaceType.ReplaceAllRegex));
		}
		
		return result;
	}
}
