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

import javax.xml.bind.annotation.XmlAttribute;


public class URIFilter extends BaseUriConstrainedFilter {
    
    private URIFilterType type = URIFilterType.Forward;
    
    public URIFilter() {}
    
    @Override
    public String toString() {
        return String.format("(uri filter uri %s, type %s)%s", this.uriMatch, this.getType(), super.toString());
    }

    @XmlAttribute
    public void setType(URIFilterType type) {
        this.type = type;
    }

    public URIFilterType getType() {
        return type;
    }
}
