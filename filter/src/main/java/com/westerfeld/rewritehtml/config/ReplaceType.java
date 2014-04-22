package com.westerfeld.rewritehtml.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum ReplaceType {
    
    @XmlEnumValue(value="replace-all")
    ReplaceAll,
    
    @XmlEnumValue(value="replace-first")
    ReplaceFirst,
    
    @XmlEnumValue(value="replace-all-regex")
    ReplaceAllRegex, 
    
    @XmlEnumValue(value="replace-first-regex")
    ReplaceFirstRegex,

    @XmlEnumValue(value="mvel")
    MVEL
    
    
}
