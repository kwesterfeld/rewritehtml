package com.westerfeld.rewritehtml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ResponseHeaderTransformer {

	String transformHeaderValue(String name, String value,
			HttpServletRequest httpReq, HttpServletResponse httpResp);

}
