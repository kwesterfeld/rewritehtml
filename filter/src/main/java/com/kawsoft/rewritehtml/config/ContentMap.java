package com.kawsoft.rewritehtml.config;

import javax.xml.bind.annotation.XmlAttribute;

public class ContentMap {

	private String from;
	private String to;
	
    @Override
    public String toString() {
        return String.format("(content map from '%s' to '%s')", this.from, this.to);
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
}
