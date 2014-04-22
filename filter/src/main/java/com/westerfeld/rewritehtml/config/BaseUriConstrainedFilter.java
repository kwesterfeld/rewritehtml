package com.westerfeld.rewritehtml.config;

import javax.xml.bind.annotation.XmlAttribute;

public abstract class BaseUriConstrainedFilter extends BaseFilter {

    protected String uriMatch;

    @XmlAttribute
    public String getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(String uriMatch) {
        this.uriMatch = uriMatch;
    }

}
