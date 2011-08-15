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
   
package com.kawsoft.rewritehtml.example;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class MockRemoteUserFilter implements javax.servlet.Filter {
    private static final Logger log = Logger.getLogger(MockRemoteUserFilter.class.getName());
    
    private String userName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String userName = filterConfig.getInitParameter("user-name");
        if (userName != null) {
            this.userName = userName;
            log.info("Using mock login user: " + userName);
        }
    }

    @Override
    public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        // Create mock servlet request which provides remote user setting.
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
            
            @Override
            public String toString() {
                return request.toString();
            }

            @Override
            public String getRemoteUser() {
                if (userName != null) {
                    return userName;
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
