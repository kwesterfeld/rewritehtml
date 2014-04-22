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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="responseHeaderFilter")
public class ResponseHeaderFilter extends HeaderFilter {
	
	private boolean supplyIfMissing;
    
    public ResponseHeaderFilter() {}

    @XmlAttribute
	public boolean isSupplyIfMissing() {
		return supplyIfMissing;
	}

	public void setSupplyIfMissing(boolean supplyIfMissing) {
		this.supplyIfMissing = supplyIfMissing;
	}
}
