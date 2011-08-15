Java Rewrite HTML Filter
(rewritehtml)
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
	
	  <responseHeaderFilters>
		<responseHeaderFilter header="Location">
		  <replacements>
			<replacement from="http://localhost:8080/" to="/" />
		  </replacements>
		</responseHeaderFilter>
	  </responseHeaderFilters>
	</config>

As implied by the xml elements, you can either filter content or headers.
A location header is typically sent when receiving a redirect response to
a POST, so this must be translated typically.  Currently a match
specification for content is required for both mime type (ie. Content-Type)
and the URI. 

Using this filter to setup a new project, the workflow is to setup 
the example, use firefox/firebug to watch traffic, and look for 404
responses.  Evaluate the 404 responses and look at source content, adjusting
along the way the xml configuration to suit.

## Configuration Reload

Currently, the configuration contained in the filters.xml is reloaded 
on the fly.  However, the reload is delayed and can take up to 30 seconds.

## Usage

To use this project in another project, one would need to construct a similar
mapping for JProxy and Rewrite HTML Filter in another web application's web.xml.
See example on how this is done.  NOTE: the mock remote user filter is only
used for convenience in testing; it should *NEVER* be used for any other purpose.

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
 * a more formal rule system for determining whether to filter or not
 * if not filtering, completely pass-through response stream capture
 * response filter "dropping"; if header becomes blank in replacement, should
   we drop it?
 * make reload interval configurable by filter init parameter