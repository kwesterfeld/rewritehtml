package com.westerfeld.rewritehtml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum URIFilterType {
    
    @XmlEnumValue("forward")
    Forward,
    
    @XmlEnumValue("filter")
    Filter,
    
    @XmlEnumValue("redirect")
    Redirect
}
