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
   
package com.kawsoft.rewritehtml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.kawsoft.rewritehtml.config.ContentFilter;
import com.kawsoft.rewritehtml.config.Replacement;
import com.kawsoft.rewritehtml.config.ResponseHeaderFilter;


public class HtmlTranslationFilter implements javax.servlet.Filter {
    private static final Logger log = Logger.getLogger(HtmlTranslationFilter.class.getName());
    private ConfigManager configManager;

    public HtmlTranslationFilter() {
        super();
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        String filterXml = filterConfig.getInitParameter("filter-xml");
        if (filterXml == null) {
            throw new ServletException("Required filter-xml init parameter missing");
        }
        this.configManager = new ConfigManager(filterXml, filterConfig.getServletContext());
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {

        ResponseWrapper wrapper = new ResponseWrapper((HttpServletRequest) req, (HttpServletResponse) res);
        filterChain.doFilter(req, wrapper);
        wrapper.commit();
    }

    public void destroy() {
    }

    private class ResponseWrapper extends HttpServletResponseWrapper {
        private WrappedServletOutputStream stream;
        private boolean isFiltered;
        private boolean isWriter;
        private boolean isGzip;
        private boolean isDeflated;
        private ContentFilter contentFilter;
        private String charset;
        private HttpServletRequest request;

        public ResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
            super(response);
            this.request = request;
        }

        public void commit() throws IOException {
            if (this.isFiltered) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Filtering content for: " + this.request.getRequestURI());
                }
                String content = replace(getAsString(), this.contentFilter.getReplacements());

                if (this.isGzip || this.isDeflated) {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("Returning filtered, compressed content for: " + this.request.getRequestURI());
                    }
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    OutputStream wrapped = this.isGzip ? new GZIPOutputStream(bos) : new DeflaterOutputStream(bos);
                    wrapped.write(this.charset != null ? content.getBytes(Charset.forName(this.charset)) : content.getBytes());
                    wrapped.close();

                    byte[] data = bos.toByteArray();
                    this.getResponse().setContentLength(data.length);
                    ServletOutputStream out = this.getResponse().getOutputStream();
                    out.write(data);
                    out.close();
                } else {

                    if (log.isLoggable(Level.FINE)) {
                        log.fine("Returning filtered content for: " + this.request.getRequestURI());
                    }
                    this.getResponse().setContentLength(content.length());
                    PrintWriter out = this.getResponse().getWriter();
                    out.write(content);
                    out.close();
                }
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Returning unmodified content for: " + this.request.getRequestURI());
                }
                byte[] data = this.stream.toByteArray();
                this.getResponse().setContentLength(data.length);
                ServletOutputStream out = this.getResponse().getOutputStream();
                out.write(data);
                out.close();
            }
        }

        @Override
        public void setHeader(String name, String value) {
            value = processHeader(name, value);
            super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            value = processHeader(name, value);
            super.addHeader(name, value);
        }

        private String processHeader(String name, String value) {
            if (name.equals("Content-Type")) {
                
                String contentType = value.replaceAll("([^;]*).*", "$1");
                if (value.contains("charset")) {
                    this.charset = value.replaceAll(".*charset\\s*=\\s*([^;]*).*", "$1");
                }

                for (ContentFilter contentFilter : configManager.getConfig().getContentFilters()) {
                    if (contentType.matches(contentFilter.getMimeTypeMatch()) &&
                        request.getRequestURI().matches(contentFilter.getUriMatch())) {
                        isFiltered = true;
                        this.contentFilter = contentFilter;
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Configured to filter (" + contentType + "/" + charset + ") content for: " + this.request.getRequestURI());
                        }
                        break;
                    }
                }
            } else if (name.equals("Content-Encoding") && value.toLowerCase().contains("gzip")) {
                isGzip = true;
            } else if (name.equals("Content-Encoding") && value.toLowerCase().contains("deflate")) {
                isDeflated = true;
            } else {
                for (ResponseHeaderFilter responseHeaderFilter: configManager.getConfig().getResponseHeaderFilters()) {
                    if (name.equals(responseHeaderFilter.getHeader())) {
                        value = replace(value, responseHeaderFilter.getReplacements());
                    }
                }
            }

            return value;
        }

        private String replace(String value, List<Replacement> replacements) {
            for (Replacement replacement: replacements) {
                value = value.replace(replacement.getFrom(), replacement.getTo());
            }
            return value;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            // TODO: we might be able optimize if we can assume header has been set for content-type.  This 
            //       would allow us to pass-through for non-filtered content.
            if (this.isWriter) {
                throw new IllegalStateException("Cannot call getOutputStream() if getWriter() has been called"); // Per servlet spec!
            }
            if (this.stream == null) {
                this.stream = new WrappedServletOutputStream();
            }
            return this.stream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
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

        @Override
        public void write(int b) throws IOException {
            this.delegate.write(b);
        }

        public byte[] toByteArray() {
            return this.delegate.toByteArray();
        }
    }
}

