# Java Rewrite HTML Filter (rewritehtml)
===

## Introduction

This project contains a java edition of the well-known pattern to proxy
backend resources using Apache mod_proxy/mod_rewrite/mod_rewrite_html 
(which does content adjustment on-the-fly).  To achieve this, a reverse 
proxy servlet is used (see below), and a content rewrite filter makes 
adjustments to content on the fly using the servlet filter chain.

The code contained here is reusable in a variety of settings to proxy to
backend resources.  One nuance implemented is the ability to send HTTP
header information containing the currently logged in user, similar to 
how SSO gateway products many times work.

In most settings, Apache HTTPD should be used.  However, in the case 
where authentication is handled by a java application not front-ended 
by Apache, and the application has to proxy to backend resources, this
project can help.  

## Running the Example

The example can easily be run by doing the following:

    mvn clean install
    cd example
    mvn jetty:run

Point your browser to `http://localhost:8888` and you should reach a working
404.  See below for how to customize.  Press `Ctrl+C` to end the example.

The example is going to need to be adjusted for your system.  First, the
example makes a proxied resource available as the root webapp context; 
this is unlikely to be useful in most cases.  The example proxies to a
backend localhost:8080 web server.  If you have one running, great, otherwise
see `example/src/main/webapp/WEB-INF/web.xml` and adjust accordingly.
To adjust the filter settings, edit the file contained in 
`example/src/main/webapp/WEB-INF/filters.xml`.  The example assumes a copy of
liferay is running on port 8080, and has been mapped to the webapp context
`rportal`.  The example filters are set according to its needs.

## Configuration

A basic configuration is supplied in the example.  The configuration is a 
simple one.  Here is an example:

	<config>
	  <contentFilters>
		<contentFilter mimeTypeMatch="text/html" uriMatch=".*">
		  <replacements>
			<replacement from="http://localhost:8080/" to="/" />
		  </replacements>
		</contentFilter>
	  </contentFilters>
  
	  <uriFilters>
		<uriFilter uriMatch=".*lightbulb.gif">
		  <replacements>
			<replacement to='request.session.setAttribute("foo", "bar");return request.requestURI + "?foo=bar&amp;boo=baz;"' type="mvel" />
		  </replacements>
		</uriFilter>
	  </uriFilters>
	
	  <responseHeaderFilters>
		<responseHeaderFilter header="Set-Cookie">
		  <replacements>
			<replacement from=".*" to="" type="replace-all-regex" />
		  </replacements>
		</responseHeaderFilter>
	  </responseHeaderFilters>
  
	  <uriFilters>
		<uriFilter uriMatch=".*secured.jsp">
		  <replacements>
			<replacement to='request.requestURI + "?username=" + session.getAttribute("username")' type="mvel" />
		  </replacements>
		</uriFilter>
	  </uriFilters>
	</config>

As implied by the xml elements, you can either filter content, request or 
response headers, or perform URI redirection.  Replacement types can
be one of the following replace-all, replace-first, replace-all-regex,
replace-first-regex, and mvel.  The MVEL expression language is used
to do arbitrary access to request context.  The name "value" is passed
to the value being translated.

For example: a location header is typically sent when receiving a 
redirect response to a POST, so this must be translated typically.  
Currently a match specification for content is required for both 
mime type (ie. Content-Type) and the URI. 

Using this filter to setup a new project, the workflow is to setup 
the example, use firefox/firebug to watch traffic, and look for 404
responses.  Evaluate the 404 responses and look at source content, adjusting
along the way the xml configuration to suit.

### uriFilter

Specify a uri match (uriMatch) and a replacement constant or MVEL expression
to alter the inbound URI and/or query parameters.  The type= attribute of the 
uriFilter XML attribute can be set to forward, filter, or redirect.

An MVEL expression is passed the following context parameters: uri, request,
session, response.

### contentFilter

A content filter specifies simple replacements (from/to) or complex MVEL
expressions.  The uriMatch= xml attribute is required to match the URI of the
content requested.

An MVEL expression is passed the following context parameters: content, 
request, session, response.

### requestHeaderFilter/responseHeaderFilter

A header filter specifies a simple replacement (from/to) or complex MVEL
expression.  The header= attribute specifies the exact matching header which
should be processed.

An MVEL expression is passed the following context parameters: header,
request, session, response.

### 

## Configuration Reload

Currently, the configuration contained in the filters.xml is reloaded 
on the fly.  However, the reload is delayed and can take up to 30 seconds.

## Usage

To use this project in another project, one would need to construct a similar
mapping for proxy-servlet (com.woonoz.proxy.servlet.ProxyServlet) and 
Rewrite HTML Filter (com.kawsoft.rewritehtml.HtmlTranslationFilter) in another 
web application's web.xml.  See the example on how this is done.  Another helpful
filter is provided (com.kawsoft.rewritehtml.RemoteUserExpressionFilter)
which allows a MVEL expression to be used to supply the value of 
HttpServletRequest.getRemoteUser() using session data, or a constant,
to a proxied backend application.  *DO NOT* map this as a constant as the 
example has, unless you really know what you're doing!  

## Notes 

As of August 14, 2011, this project depends on a forked copy of the 
"proxy-servlet" found on github here: [https://github.com/mbaechler/proxy-servlet].
The fork can be found here: [https://github.com/kwesterfeld/proxy-servlet]

The fork contains a couple minor fixes and a new feature for sending
the current "remote user" (ie. the currently logged in user) from the proxy
to the back-end server using an HTTP header injection technique.

Thus, to build this project, you must pull the fork, build it, and then
you can build the rest of this project.

At a later date the jproxy project should accept the upstream changes, 
and should appear in the Apache maven repo.  When that happens, this 
project can be updated accordingly.

## TODO

Various things needed:

  - A more formal rule system for determining whether to filter or not
  - If not filtering, completely pass-through response stream capture
  - Being able to set/remove request/response headers
