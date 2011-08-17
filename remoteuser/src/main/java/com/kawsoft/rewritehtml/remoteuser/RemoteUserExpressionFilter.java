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
   
package com.kawsoft.rewritehtml.remoteuser;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.mvel2.MVEL;

public class RemoteUserExpressionFilter implements Filter {
    private static final Logger log = Logger.getLogger(RemoteUserExpressionFilter.class.getName());
    
    private String remoteUserExpressionValue;
    private Serializable remoteUserExpression;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String remoteUserExpression = filterConfig.getInitParameter("remote-user-expression");
        if (remoteUserExpression != null) {
            // compile the MVEL expression
            try {
                this.remoteUserExpression = MVEL.compileExpression(remoteUserExpression);
                log.info("Using remote user expression: " + remoteUserExpression);
                this.remoteUserExpressionValue = remoteUserExpression;
            } catch(RuntimeException e) {
                log.log(Level.WARNING, "Could not compile remote user expression: " + remoteUserExpression, e);
                throw new ServletException("Could not compile remote user expression: " + remoteUserExpression, e);
            }
        } else {
            throw new ServletException("The session-expression variable is requried for RemoteUserExpressionFilter"); 
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        // Create servlet request which provides remote user value via expression evaluation.
        final HttpServletRequest servletRequest = ((HttpServletRequest)request);
        if (log.isLoggable(Level.FINEST)) {
            log.fine("Handling request: " + servletRequest.getRequestURI());
        }
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
            
            @Override
            public String toString() {
                return servletRequest.toString();
            }

            @Override
            public String getRemoteUser() {
                if (remoteUserExpression != null) {
                    
                    try {
                        // Use MVEL to evaluate expression.
                        Map<String,Object> vars = new HashMap<String,Object>();
                        vars.put("request", servletRequest);
                        vars.put("session", servletRequest.getSession(false));
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Evaluating URI " + servletRequest.getRequestURI() + " for remote user using expression " + remoteUserExpressionValue + " using vars: " + vars);
                        }
                        Object result = MVEL.executeExpression(remoteUserExpression, vars);
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Evaluated URI " + servletRequest.getRequestURI() + " remote user request to: " + result);
                        }
                        return result == null ? null : result.toString();
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Unexpected exception occurred while evaluating remote user expression: " + remoteUserExpressionValue, e);
                    }
                    return null;
                } else {
                    return super.getRemoteUser();
                }
            }};

        chain.doFilter(wrapper, response);
    }

    @Override
    public void destroy() {
    }
}
