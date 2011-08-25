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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kawsoft.rewritehtml.config.Config;

public class ConfigManager {
    
    private static final long UPDATE_CHECK_INTERVAL = 30L * 1000L;
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class.getName());
    private long updateCheckInterval = UPDATE_CHECK_INTERVAL;
    private final URL configLocationUrl;
    private final AtomicReference<Config> config = new AtomicReference<Config>();
    private final AtomicBoolean processingUpdate = new AtomicBoolean();
    private long nextUpdateTimeCheck;
    private long configLastModifiedTime;
    private JAXBContext context;

    public ConfigManager(FilterConfig filterConfig) throws ServletException {
        
        // Process the config location.
        String configLocation = filterConfig.getInitParameter("filter-xml");
        if (configLocation == null) {
            throw new ServletException("Required filter-xml init parameter missing");
        }
        log.info("Filter instance '{}' loading filter configuration: {}", filterConfig.getFilterName(), configLocation);
        
        // Process the reload interval.
        String updateCheckInterval = filterConfig.getInitParameter("update-check-interval");
        if (updateCheckInterval != null) {
            this.updateCheckInterval = Long.parseLong(updateCheckInterval) * 1000L;
        }
        log.info("Filter instance '{}' re-load filter interval: {} ms", filterConfig.getFilterName(), this.updateCheckInterval);
        
        // Determine the location url.
        ServletContext servletContext = filterConfig.getServletContext();
        try {
            URL configLocationUrl = null;
            
            // Does the location contain a ':'?  If so, it's a URL
            if (configLocation.indexOf(':') > 0) {
                
                configLocationUrl = new URL(configLocation);
                log.info("Loading filter configuration from supplied URL: {}", configLocationUrl);
                
            } else {
                // Get by resource.
                configLocationUrl = servletContext.getResource(configLocation);
                if (configLocationUrl != null) {
                    log.debug("Loaded filter configuration from servlet context: {}", configLocationUrl);
                } else {
                    
                    // Get by classpath.
                    configLocationUrl = ConfigManager.class.getResource(configLocation);
                    if (configLocationUrl != null) {
                        log.debug("Loaded filter configuration from class path: {}", configLocationUrl);
                    }
                }
            }
            
            // Set it (final).
            this.configLocationUrl = configLocationUrl;
            
        } catch (Exception e) {
            log.warn("Could not load configuration: {}", configLocation, e);
            throw new ServletException("Could not load configuration: " + configLocation, e);
        }
        if (this.configLocationUrl == null) {
            throw new ServletException("Missing configuration: " + configLocation);
        }
        
        // Do initial load.
        try {
            updateCheck();
        } catch (Exception e) {
            throw new ServletException("Unexpected exception occurred during load: " + configLocation, e); 
        }
    }
    
    public Config getConfig() {
        try {
            updateCheck();
        } catch (Exception e) {
            log.warn("Unexpected exception occurred reloading configuration: {}", this.configLocationUrl, e);
            
            // After succeeding on initial load, if we get a reload exception we would still
            // have a valid configuration, so we can proceed.
        }
        return this.config.get();
    }

    private void updateCheck() throws Exception {
        
        // If we are using a file-based URL, we can locate the file and manage the update time.
        if (this.configLocationUrl.getProtocol().equals("file")) {
            
            // Only process the update from one thread; 
            // we use atomicity of assignment to deal with updating the configuration singleton. 
            // Other threads can continue to use the configuration while we reload, and we only
            // need one thread to load it.
            if (this.processingUpdate.compareAndSet(false, true)) {
                try {

                    // Configuration file update check happens once per interval.
                    if (this.nextUpdateTimeCheck < System.currentTimeMillis()) {
                        
                        // Our next time check will be now + interval.
                        this.nextUpdateTimeCheck = System.currentTimeMillis() + this.updateCheckInterval; 
                        
                        // Get the file from URL.
                        File configFile = new File(this.configLocationUrl.getFile());
                        if (!configFile.exists()) {
                            throw new Exception("Supplied configuration does not exist: " + this.configLocationUrl + " - " + configFile.getAbsolutePath());
                        }
                        if (!configFile.canRead()) {
                            throw new Exception("Supplied configuration is not readable: " + this.configLocationUrl + " - " + configFile.getAbsolutePath());
                        }
                        if (this.configLastModifiedTime != configFile.lastModified()) {
                            this.configLastModifiedTime = configFile.lastModified();
                            load();
                        }
                    }
                } finally {
                    this.processingUpdate.set(false);
                }
            }
        } else if (this.config.get() == null) {
            load();
        }
    }

    private void load() throws Exception {
        log.debug("Loading filter configuration: {}", this.configLocationUrl);

        // Unmarshall the stream.
        if (this.context == null) {
            this.context = JAXBContext.newInstance(Config.class);
        }
        InputStream ins = this.configLocationUrl.openConnection().getInputStream();
        try {
            this.config.set((Config) this.context.createUnmarshaller().unmarshal(ins));
        } finally {
            ins.close();
        }
        log.debug("Loaded {} content replacements and {} header replacements", this.config.get().getContentFilters().size(), this.config.get().getResponseHeaderFilters().size());
    }
}
 