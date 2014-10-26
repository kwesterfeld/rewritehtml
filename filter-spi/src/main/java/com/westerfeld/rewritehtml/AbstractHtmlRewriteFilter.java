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
   
package com.westerfeld.rewritehtml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractHtmlRewriteFilter implements javax.servlet.Filter {

	private static final Logger log = LoggerFactory.getLogger(AbstractHtmlRewriteFilter.class.getName());
    
	private URIRequestTransformer uriRequestTransformer;
	private RequestHeaderTransformer requestHeaderTransformer;
	private ResponseHeaderTransformer responseHeaderTransformer; 
	private BodyRequestTransformerSelector bodyRequestTransformerSelector;
	
	private Map<String,Object> context;

    public AbstractHtmlRewriteFilter() {
        super();
    }

    public Map<String,Object> getContext() {
		return context;
	}

	public void setContext(Map<String,Object> context) {
		this.context = context;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		// Save current context class loader.
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			// Swizzle to our classloader.
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			
			// Do the init.
			doInit(filterConfig);
			
		} finally {
			// Restore context class loader.
			Thread.currentThread().setContextClassLoader(cl);
		}
    }

	public abstract void doInit(FilterConfig filterConfig) throws ServletException;

		
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {

        // Get the uri for possible rewrite.
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) res;
        
        String uri = httpReq.getRequestURI();
        String uriOriginal = uri;
        log.debug("Processing request for URI: {}", uri);
        
        // Make URI replacements; if it was altered, forward it along.
        URIFilterType type = null;
        URIRequestTransformer.URITransformResult uriTransformRes = new URIRequestTransformer.URITransformResult();  

        // *** do delegate transform URI by find first matching URI *** 
        uri = uriRequestTransformer.transformFirstMatchingURI(uriTransformRes, uri, httpReq, httpResp, filterChain, context);
        type = uriTransformRes.type;
        
        if (uri != uriOriginal && uri != null && !uriOriginal.equals(uri)) {
            
            // Translate 
            String translatedURI = null;
            String translatedQueryString = null;
            StringBuffer translatedURL = new StringBuffer();
            try {
                log.debug("Replacement URI is returned as: {}", uri);
                URI translated = new URI(uri);
                translatedURI = translated.getPath();
                translatedQueryString = translated.getQuery();
                
                // Did they return a scheme and host for redirect?
                if (translated.getScheme() != null && translated.getHost() != null) {
                    translatedURL.append(translated.toString());
                }
                else {
                    translatedURL.append(httpReq.getRequestURL().toString().replace(uriOriginal, translatedURI));
                    if (translatedQueryString != null && translatedQueryString.length() > 0) {
                        if (!translatedQueryString.startsWith("?")) {
                            translatedURL.append("?");
                        }
                        translatedURL.append(translatedQueryString);
                    }
                }
                log.debug("URI translation from: {} {} {} to {} {} {}", httpReq.getRequestURI(), httpReq.getQueryString(), httpReq.getRequestURL().toString(), translatedURI, translatedQueryString, translatedURL.toString());
            } catch (URISyntaxException e) {
                throw new ServletException("Unexpected exception translating URI: " + uri, e);
            }
            
            log.debug("Performing uri rewrite dispatch by {}", type);
            if (type == URIFilterType.Forward) {
                req.getRequestDispatcher(translatedURI).forward(req, res);
            } else  if (type == URIFilterType.Redirect) {
                ((HttpServletResponse) res).sendRedirect(translatedURL.toString());
            } else {
                // Do the filtering/translation
                log.debug("Doing filtering");
                ResponseWrapper responseWrapper = new ResponseWrapper(httpReq, (HttpServletResponse) res);
                filterChain.doFilter(new RequestWrapper(httpReq, translatedURL, translatedURI, translatedQueryString), responseWrapper);
                responseWrapper.commit();
            }
        } else {
            // Do the filtering/translation
            log.debug("Doing filtering");
            ResponseWrapper responseWrapper = new ResponseWrapper(httpReq, (HttpServletResponse) res);
            filterChain.doFilter(new RequestWrapper(httpReq, null, null, null), responseWrapper);
            responseWrapper.commit();
        }
    }

    public void destroy() {
    }
    

	
    private class RequestWrapper extends HttpServletRequestWrapper {

        private final StringBuffer requestURL;
        private final String requestURI;
        private final String queryString;

        public RequestWrapper(HttpServletRequest request, StringBuffer requestURL, String requestURI, String queryString) {
            super(request);
            this.requestURL = requestURL;
            this.requestURI = requestURI;
            this.queryString = queryString;
        }
        
        @Override
        public String getQueryString() {
            if (this.queryString != null) {
                return this.queryString;
            } else {
                return super.getQueryString();
            }
        }

        @Override
        public String getRequestURI() {
            if (this.requestURI != null) {
                return this.requestURI;
            } else {
                return super.getRequestURI();
            }
        }

        @Override
        public StringBuffer getRequestURL() {
            if (this.requestURL != null) {
                return this.requestURL;
            } else {
                return super.getRequestURL();
            }
        }

        @Override
        public String getHeader(String name) {
            return processHeader(name, super.getHeader(name));
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Enumeration getHeaders(final String name) {
            Enumeration headers = super.getHeaders(name);
            final Enumeration delegate;
            if (!headers.hasMoreElements()) {
            	HttpServletRequest httpReq = (HttpServletRequest)getRequest();
            	boolean isSupplyIfMissing = requestHeaderTransformer != null && requestHeaderTransformer.containsMatchingHeaderSupplyIfMissing(httpReq, name);
                if (isSupplyIfMissing) {
                 	delegate = Collections.enumeration(Collections.singleton(null)); // A null returned by the enumeration to hook processing in process header.
                } else {
                	delegate = headers;
                }
            } else {
            	delegate = headers;
            }
            return new Enumeration() {

                @Override
                public boolean hasMoreElements() {
                    return delegate.hasMoreElements();
                }

                @Override
                public Object nextElement() {
                    return processHeader(name, (String) delegate.nextElement());
                }
            };
        }
        
        @SuppressWarnings("rawtypes")
		@Override
		public Enumeration getHeaderNames() {
        	
        	// Do we have "supply if missing" request headers?
        	Set<String> headers = new LinkedHashSet<String>();
        	for (Enumeration<?> e = super.getHeaderNames(); e.hasMoreElements(); ) {
        		headers.add(e.nextElement().toString());
        	}
        	HttpServletRequest httpReq = (HttpServletRequest)getRequest();
        	if (requestHeaderTransformer != null) {
        		requestHeaderTransformer.fillMatchingHeaderSupplyIfMissing(headers, httpReq);
        	}
        	
        	// Return enumeration of combined.
        	final Iterator<String> iterator = headers.iterator();
        	return new Enumeration<String>() {

				@Override
				public boolean hasMoreElements() {
					return iterator.hasNext();
				}

				@Override
				public String nextElement() {
					return iterator.next();
				}
        	};
		}

		private String processHeader(String name, String value) {
			HttpServletRequest httpReq = (HttpServletRequest)getRequest();
            if (requestHeaderTransformer != null) {
            	value = requestHeaderTransformer.transformHeaderValueSupplyIfMissing(httpReq, name, value);
            }
            return value;
        }
    }

    private class ResponseWrapper extends HttpServletResponseWrapper {
        private WrappedServletOutputStream stream;
        private boolean isFiltered;
        private boolean isWriter;
        private boolean isGzip;
        private boolean isDeflated;
        private boolean isCommitted;
        private BodyRequestTransformer contentFilter;
        private JSoupHtmlDOMTransformer htmlDOMContentFilter;
        private String charset;
        private HttpServletRequest request;
        private Set<String> headers = new HashSet<String>();

        public ResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
            super(response);
            this.request = request;
        }

        public void commit() throws IOException {
        	
        	if (!isCommitted) {
	        	// If we are filtering, and we have a stream...
        		HttpServletRequest httpReq = (HttpServletRequest) this.request;
        		HttpServletResponse httpResp = (HttpServletResponse) getResponse();
        		
	            String requestURI = this.request.getRequestURI();
	            if (this.stream != null && !this.stream.isEmpty()) {
	        		isCommitted = true;
	        		// See if there is any supply default response headers.
					if (requestHeaderTransformer != null) {
						requestHeaderTransformer.supplyDefaultHeadersIfMissing(this, headers);
					}
					

					if (this.isFiltered) {
		                log.debug("Filtering content for: " + requestURI);
		                String content = getAsString();
	
		                // *** transform body as Text ***
		                if (contentFilter != null) {
		                	content = contentFilter.transformContent(content, httpReq, httpResp);
		                }
		                
		                // *** transform body as DOM ***
		                // If we have a Html DOM replacement filter to post-apply after textual replacements 
		                if (htmlDOMContentFilter != null) {
			                // 1) parse HTML text as DOM using jSoup
		                	Document htmlDOMContent;
		                	try {
		                		htmlDOMContent = Jsoup.parse(content);
		                	} catch(Exception ex) {
		                		// Failed to parse?? => ignore error, leave html unmodified!!
		                		htmlDOMContent = null;
		                	}
		                	if (htmlDOMContent != null) {
		                		try {
				                	// 2) replace html DOM
				                	htmlDOMContent = htmlDOMContentFilter.transform(
					                        htmlDOMContent, 
					                        httpReq,
					                        httpResp);
			
				                	// 3) re-render to text  (indentation may be lost!)
				                	content = htmlDOMContent.outerHtml();
		                		} catch(Exception ex) {
		                			// failed to modify or re-render to html?? => => ignore error, leave html unmodified!!
		                		}
		                	}
		                }
		                
		                if (this.isGzip || this.isDeflated) {
		                    log.debug("Returning filtered, compressed content for: {}", requestURI);
		                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		                    OutputStream wrapped = this.isGzip ? new GZIPOutputStream(bos) : new DeflaterOutputStream(bos);
		                    wrapped.write(this.charset != null ? content.getBytes(Charset.forName(this.charset)) : content.getBytes());
		                    wrapped.close();
		
		                    byte[] data = bos.toByteArray();
		                    writeResponseData(data);
		                } else {
		
		                    log.debug("Returning filtered content for: {}", requestURI);
		                    // TOCHECK..reuse same charset as while decoding ... Charset.forName(this.charset)
		                    this.getResponse().setContentLength(content.length());
		                    PrintWriter out = this.getResponse().getWriter();
		                    out.write(content);
		                    try {
		                        out.close();
		                    } catch (Exception e) {
		                        log.warn("Unexpected exception occurred during close", e);
		                    }
		                }
		            } else {
		                log.debug("Returning unmodified content for: {}", requestURI);
		                byte[] data = this.stream.toByteArray();
		                writeResponseData(data);
		            }
	            }
        	}
        }

		private void writeResponseData(byte[] data) throws IOException {
			this.getResponse().setContentLength(data.length);
			ServletOutputStream out = this.getResponse().getOutputStream();
			out.write(data);
			try {
			    out.close();
			} catch (Exception e) {
			    log.warn("Unexpected exception occurred during close", e);
			}
		}

		@Override
        public void setHeader(String name, String value) {
            String originalValue = value;
            value = processHeader(name, value);
            if (value == null || value.trim().length() == 0) {
                log.debug("Dropping empty response header {} translated from {} for {}", name, originalValue, this.request.getRequestURI());
                return;
            }
            super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            String originalValue = value;
            value = processHeader(name, value);
            if (value == null || value.trim().length() == 0) {
                log.debug("Dropping empty response header {} translated from {} for {}", name, originalValue, this.request.getRequestURI());
                return;
            }
            super.addHeader(name, value);
        }

        private String processHeader(String name, String value) {
        	headers.add(name);
        	
        	String requestURI = request.getRequestURI();

        	if (name.equals("Content-Type")) {
                String contentType = value.replaceAll("([^;]*).*", "$1");
                if (value.contains("charset")) {
                    this.charset = value.replaceAll(".*charset\\s*=\\s*([^;]*).*", "$1");
                }
                
                this.contentFilter = bodyRequestTransformerSelector.findMatchingBodyRequestTransformer(contentType, requestURI);
                if (this.contentFilter != null) {
                    isFiltered = true;
                    log.debug("Configured to filter ({}/{}) content for: {}", contentType, charset, requestURI);
                }
                
                this.htmlDOMContentFilter = bodyRequestTransformerSelector.findMatchingDOMBodyRequestTransformer(contentType, requestURI);
                if (this.htmlDOMContentFilter != null) {
                    isFiltered = true;
                    log.debug("Configured to filter ({}/{}) DOM content for: {}", contentType, charset, requestURI);
                }
                
            } else if (name.equals("Content-Encoding")) {
            	String contentEncoding = value.toLowerCase();
				if (contentEncoding.contains("gzip")) {
            		isGzip = true;
            	} else if (contentEncoding.contains("deflate")) {
            		isDeflated = true;
            	}
            } else {
            	if (responseHeaderTransformer != null) {
            		HttpServletRequest httpReq = (HttpServletRequest) this.request;
            		HttpServletResponse httpResp = (HttpServletResponse) getResponse();
            		String originalValue = value;
            		
            		// *** transform header value ***
            		value = responseHeaderTransformer.transformHeaderValue(name, value, httpReq, httpResp);

            		if (!value.equals(originalValue)) {
                        log.debug("Returning updated response header {} translated from {} to {} for {}", name, originalValue, value, requestURI);
            		}
            	}
                
            }

            return value;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (this.isWriter) {
                throw new IllegalStateException("Cannot call getOutputStream() if getWriter() has been called"); // Per servlet spec!
            }
            if (this.stream == null) {
                this.stream = new WrappedServletOutputStream(this);
            }
            return this.stream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (!isFiltered) {
                return super.getWriter();
            }
            
            if (this.stream != null) {
                throw new IllegalStateException("Cannot call getWriter() if getOutputStream() has been called"); // Per servlet spec!
            }
            isWriter = true;
            return super.getWriter();
        }

        public String getAsString() {
            if (this.stream == null) {
                throw new IllegalStateException("Cannot get string content without delegate");
            }

            // Handle gzip/deflation.
            byte[] data = this.stream.toByteArray();
            if (data.length > 0) {
                try {
                    if (isGzip) {
                        
                        // Double check gzip magic # here.
                        int header_magic = ((int) data[0] & 0xff) | ((data[1] << 8) & 0xff00);
                        if (header_magic == GZIPInputStream.GZIP_MAGIC) {
                            data = capture(new GZIPInputStream(new ByteArrayInputStream(data)));
                        }
                    } else if (isDeflated) {
                        data = capture(new DeflaterInputStream(new ByteArrayInputStream(data)));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected i/o exception occurred unwrapping compressed content", e);
                }
            }

            return this.charset != null ? new String(data, Charset.forName(this.charset)) : new String(data);
        }

        private byte[] capture(InputStream ins) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384];
            while (true) {
                int len = ins.read(buffer);
                if (len <= 0) {
                    break;
                }
                bos.write(buffer, 0, len);
            }
            ins.close();
            bos.close();
            return bos.toByteArray();
        }
    }

    private static class WrappedServletOutputStream extends ServletOutputStream {

		private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();
		private ResponseWrapper wrapper;
		private boolean empty = true;
		
		public WrappedServletOutputStream(ResponseWrapper wrapper) {
			this.wrapper = wrapper;
		}

        public boolean isEmpty() {
			return empty;
		}

		@Override
        public void write(int b) throws IOException {
            this.delegate.write(b);
            empty = false;
        }

        public byte[] toByteArray() {
            return this.delegate.toByteArray();
        }
        
        @Override
		public void close() throws IOException {
			super.close();
			this.wrapper.commit();
		}
    }
}

