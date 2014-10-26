package com.westerfeld.rewritehtml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface BodyRequestTransformer {

	String transformContent(String content, HttpServletRequest httpReq,
			HttpServletResponse httpResp);

}
