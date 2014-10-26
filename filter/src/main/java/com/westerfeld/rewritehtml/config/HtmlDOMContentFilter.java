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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="htmlDOMContentFilter")
public class HtmlDOMContentFilter {

    protected String uriMatch;
    private String mimeTypeMatch;
    
    protected List<HtmlDOMTransformer> htmlDOMTransformers = new ArrayList<HtmlDOMTransformer>(); 
    
    public HtmlDOMContentFilter() {}
    
    @XmlAttribute
    public String getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(String uriMatch) {
        this.uriMatch = uriMatch;
    }

    @XmlAttribute
    public String getMimeTypeMatch() {
        return mimeTypeMatch;
    }

    public void setMimeTypeMatch(String mimeTypeMatch) {
        this.mimeTypeMatch = mimeTypeMatch;
    }

    @XmlElementWrapper(name="htmlDOMTransformers")
    @XmlElement(name="htmlDOMTransformer")
    public List<HtmlDOMTransformer> getHtmlDOMTransformers() {
        return htmlDOMTransformers;
    }

    public void setHtmlDOMTransformers(List<HtmlDOMTransformer> htmlDOMTransformers) {
        this.htmlDOMTransformers = htmlDOMTransformers;
    }
    
    @Override
    public String toString() {
        return String.format("(Html DOM content filter uri %s, mime type %s)", this.uriMatch, this.mimeTypeMatch);
    }
}
