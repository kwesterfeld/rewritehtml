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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="replacement")
public class Replacement {

    private String from;
    private String to;
    private String toValue;
    private ReplaceType type = ReplaceType.ReplaceAll;
    
    public Replacement() {}

    public Replacement(String from, String to, ReplaceType type) {
		this.from = from;
		this.to = to;
		this.type = type;
	}

	@Override
    public String toString() {
        return String.format("(from %s to %s via type %s)", this.from, this.to, this.getType());
    }

    @XmlAttribute
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @XmlAttribute
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @XmlAttribute
    public void setType(ReplaceType type) {
        this.type = type;
    }

    public ReplaceType getType() {
        return type;
    }

    @XmlValue
    public void setToValue(String toValue) {
        this.toValue = toValue;
    }

    public String getToValue() {
        return toValue;
    }
    
    @XmlTransient
    public String getEffectiveTo() {
        return this.toValue != null && this.toValue.trim().length() > 0 ? this.toValue : this.to;
    }
}
