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
    
    private Serializable remoteUserExpression;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String remoteUserExpression = filterConfig.getInitParameter("remote-user-expression");
        if (remoteUserExpression != null) {
            // compile the MVEL expression
            this.remoteUserExpression = MVEL.compileExpression(remoteUserExpression);
            log.info("Using remote user expression: " + remoteUserExpression);
        } else {
            throw new ServletException("The session-expression variable is requried for RemoteUserExpressionFilter"); 
        }
    }

    @Override
    public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        // Create servlet request which provides remote user value via expression evaluation.
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
            
            @Override
            public String toString() {
                return request.toString();
            }

            @Override
            public String getRemoteUser() {
                if (remoteUserExpression != null) {
                    
                    // Use MVEL to evaluate expression.
                    Map<String,Object> vars = new HashMap<String,Object>();
                    vars.put("request", request);
                    vars.put("session", ((HttpServletRequest)request).getSession(false));
                    Object result = MVEL.executeExpression(remoteUserExpression, vars);
                    return result == null ? null : result.toString();
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
