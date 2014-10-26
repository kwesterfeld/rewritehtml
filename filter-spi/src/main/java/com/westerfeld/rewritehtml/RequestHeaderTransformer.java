package com.westerfeld.rewritehtml;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponseWrapper;

public interface RequestHeaderTransformer {

	public boolean containsMatchingHeaderSupplyIfMissing(HttpServletRequest httpReq, String name);

	public void fillMatchingHeaderSupplyIfMissing(Set<String> headers, HttpServletRequest httpReq);

	public String transformHeaderValueSupplyIfMissing(
			HttpServletRequest httpReq, String name, String value);

	public void supplyDefaultHeadersIfMissing(HttpServletResponseWrapper httpResp, Set<String> headers);
	
}
