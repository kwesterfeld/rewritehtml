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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
public class Config {

    private List<ContentFilter> contentFilters = new ArrayList<ContentFilter>();
    private List<HtmlDOMContentFilter> htmlDOMContentFilters = new ArrayList<HtmlDOMContentFilter>();
	private List<URIFilter> uriFilters = new ArrayList<URIFilter>();
    private List<ResponseHeaderFilter> responseHeaderFilters = new ArrayList<ResponseHeaderFilter>();
    private List<RequestHeaderFilter> requestHeaderFilters = new ArrayList<RequestHeaderFilter>();
    
    public Config() {}
    
    @Override
    public String toString() {
        return String.format("(content filters %s)\n(request header filters %s)\n(response header filters %s)\n(uri filters %s)", this.contentFilters.toString(), this.getRequestHeaderFilters().toString(), this.responseHeaderFilters.toString(), this.uriFilters.toString());
    }

    @XmlElementWrapper(name="contentFilters")
    @XmlElement(name="contentFilter")
    public List<ContentFilter> getContentFilters() {
        return contentFilters;
    }

    public void setContentFilters(List<ContentFilter> contentFilters) {
        this.contentFilters = contentFilters;
    }

    @XmlElementWrapper(name="htmlDOMContentFilters")
    @XmlElement(name="htmlDOMContentFilter")
    public List<HtmlDOMContentFilter> getHtmlDOMContentFilters() {
        return htmlDOMContentFilters;
    }

    public void setHtmlDOMContentFilters(List<HtmlDOMContentFilter> htmlDOMContentFilters) {
        this.htmlDOMContentFilters = htmlDOMContentFilters;
    }

    @XmlElementWrapper(name="responseHeaderFilters")
    @XmlElement(name="responseHeaderFilter")
    public void setResponseHeaderFilters(List<ResponseHeaderFilter> responseHeaderFilters) {
        this.responseHeaderFilters = responseHeaderFilters;
    }

    public List<ResponseHeaderFilter> getResponseHeaderFilters() {
        return responseHeaderFilters;
    }

    @XmlElementWrapper(name="requestHeaderFilters")
    @XmlElement(name="requestHeaderFilter")
    public void setRequestHeaderFilters(List<RequestHeaderFilter> requestHeaderFilters) {
        this.requestHeaderFilters = requestHeaderFilters;
    }

    public List<RequestHeaderFilter> getRequestHeaderFilters() {
        return requestHeaderFilters;
    }

    @XmlElementWrapper(name="uriFilters")
    @XmlElement(name="uriFilter")
    public void setUriFilters(List<URIFilter> uriFilters) {
        this.uriFilters = uriFilters;
    }

    public List<URIFilter> getUriFilters() {
        return uriFilters;
    }
}
