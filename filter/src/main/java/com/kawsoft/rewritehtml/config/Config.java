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

package com.kawsoft.rewritehtml.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
public class Config {

    private List<ContentFilter> contentFilters = new ArrayList<ContentFilter>();
    private List<ResponseHeaderFilter> responseHeaderFilters = new ArrayList<ResponseHeaderFilter>();
    
    public Config() {}
    
    @Override
    public String toString() {
        return String.format("(content filters %s)\n(header filters %s)", this.contentFilters.toString(), this.responseHeaderFilters.toString());
    }

    @XmlElementWrapper(name="contentFilters")
    @XmlElement(name="contentFilter")
    public List<ContentFilter> getContentFilters() {
        return contentFilters;
    }

    public void setContentFilters(List<ContentFilter> contentFilters) {
        this.contentFilters = contentFilters;
    }

    @XmlElementWrapper(name="responseHeaderFilters")
    @XmlElement(name="responseHeaderFilter")
    public void setResponseHeaderFilters(List<ResponseHeaderFilter> responseHeaderFilters) {
        this.responseHeaderFilters = responseHeaderFilters;
    }

    public List<ResponseHeaderFilter> getResponseHeaderFilters() {
        return responseHeaderFilters;
    }
}
