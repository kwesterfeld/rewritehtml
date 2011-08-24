package com.csc.tch.rewritehtml.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.kawsoft.rewritehtml.HtmlTranslationFilter;

public class UserPasswordRewriteTest {

    private static final String DEFAULT_URL = "/context/jsp/target.jsp";

    // https://www.somehost.com/context/jsp/target.jsp?report=Some%20Report%20Target&user=test-user&password=test-password
    
    private HttpServletRequest buildReportViewerServletRequest(String url, String queryString) {
        
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getServletPath()).andReturn("/context").anyTimes();
        EasyMock.expect(request.getRequestURI()).andReturn(url).anyTimes();
        EasyMock.expect(request.getContextPath()).andReturn("").anyTimes();
        EasyMock.expect(request.getQueryString()).andReturn(queryString).anyTimes();
        EasyMock.expect(request.getServerName()).andReturn("www.somehost.com").anyTimes();
        StringBuffer requestURL = new StringBuffer();
        requestURL.append(url);
        EasyMock.expect(request.getRequestURL()).andReturn(requestURL).anyTimes();
        EasyMock.expect(request.getServerPort()).andReturn(80).anyTimes();
        HttpSession session = buildSession();
        EasyMock.expect(request.getSession(false)).andReturn(session).anyTimes();
        EasyMock.expect(request.getSession(true)).andReturn(session).anyTimes();
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(request.isSecure()).andReturn(true).anyTimes();
        EasyMock.replay(request);
        return request;
    }

    private HttpSession buildSession() {
        HttpSession session = EasyMock.createMock(HttpSession.class);
        Map<String,Object> APPLICATION = new HashMap<String,Object>();
        Map<String,Object> USERINFO = new HashMap<String,Object>();
        USERINFO.put("USERNAME", "test-user");
        APPLICATION.put("USERINFO", USERINFO);
        EasyMock.expect(session.getAttribute("APPLICATION")).andReturn(APPLICATION).anyTimes();
        EasyMock.replay(session);
        return session;
    }

    private FilterConfig buildConfig() throws MalformedURLException {
        FilterConfig filterConfig = EasyMock.createMock(FilterConfig.class);
        EasyMock.expect(filterConfig.getFilterName()).andReturn("HTML Filter").anyTimes();
        EasyMock.expect(filterConfig.getInitParameter("filter-xml")).andReturn("/" + UserPasswordRewriteTest.class.getSimpleName() + ".xml").anyTimes();
        EasyMock.expect(filterConfig.getInitParameter("update-check-interval")).andReturn("0").anyTimes();
        EasyMock.expect(filterConfig.getServletContext()).andReturn(buildServletContext()).anyTimes();
        EasyMock.replay(filterConfig);
        return filterConfig;
    }

    private ServletContext buildServletContext() throws MalformedURLException {
        ServletContext servletContext = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(servletContext.getResource("/" + UserPasswordRewriteTest.class.getSimpleName() + ".xml")).andReturn(UserPasswordRewriteTest.class.getResource("/" + UserPasswordRewriteTest.class.getSimpleName() + ".xml")).anyTimes();
        EasyMock.replay(servletContext);
        return servletContext;
    }

    private HttpServletResponse buildServletResponse() {
        HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
        EasyMock.replay(servletResponse);
        return servletResponse;
    }

    @Test
    public void testURIFilterNoUsername() throws ServletException, IOException {
        testFilter("report=Some Report Target");
    }
    
    @Test
    public void testURIFilterWithUsername() throws ServletException, IOException {
        testFilter("report=Some Report Target&user=baduser&password=nothing");
    }
    
    @Test
    public void testURIFilterWithUsernameReversed() throws ServletException, IOException {
        testFilter("report=Some Report Target&password=nothing&user=baduser");
    }
    
    @Test
    public void testURIFilterWithUsernameJumbled() throws ServletException, IOException {
        testFilter("password=nothing&user=baduser&report=Some Report Target");
    }
    
    @Test
    public void testURIFilterWithUsernameOnly() throws ServletException, IOException {
        testFilter("user=baduser&report=Some Report Target");
    }
    
    @Test
    public void testURIFilterWithPasswordOnly() throws ServletException, IOException {
        testFilter("password=nothing&report=Some Report Target");
    }
    
    @Test
    public void testURIFilterWithABunchOfArgs() throws ServletException, IOException {
        testFilter("arg1=1&arg2=2&user=baduser&arg3=3&password=nothing&report=Some Report Target");
    }
    
    @Test
    public void testURIFilterWithInterleavedArgs() throws ServletException, IOException {
        testFilter("arg1=1&report=Some Report Target&arg2=2&user=baduser&arg3=3&password=nothing&arg4=4");
    }
    
    @Test
    public void testURIFilterWithMultipleUser() throws ServletException, IOException {
        testFilter("user=baduser&user=baduser&password=nothing&user=baduser&password=nothing&report=Some Report Target");
    }
    
    @Test
    public void testURIFilterReplaceHack() throws ServletException, IOException {
        testFilter("useruser=baduser&baduser&passwordpassword=nothing&=nothing&report=Some Report Target");
    }
    
    @Test
    public void testAlternateURIWithUsername() throws ServletException, IOException {
        testFilter("/context/some/other.htm", false, "report=Some Report Target&user=baduser&password=nothing&user=baduser&password=nothing");
    }
    
    private void testFilter(final String queryParams) throws ServletException, IOException {
        testFilter(DEFAULT_URL, true, queryParams);
    }
    
    private void testFilter(final String url, final boolean testInsert, final String queryParams) throws ServletException, IOException {
        HtmlTranslationFilter filter = new HtmlTranslationFilter();
        filter.init(buildConfig());
        
        HttpServletRequest req = buildReportViewerServletRequest(url, queryParams);
        HttpServletResponse res = buildServletResponse();
        filter.doFilter(req, res, new FilterChain() {
            
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                System.out.println("Translating params: " + queryParams);
                System.out.println("Request URI: " +  ((HttpServletRequest)request).getRequestURI());
                String queryString = ((HttpServletRequest)request).getQueryString();
                System.out.println("Request params: " +  queryString);
                Assert.assertEquals(url, ((HttpServletRequest)request).getRequestURI());
                Assert.assertTrue(queryString.contains("report=" + URLEncoder.encode("Some Report Target", "UTF-8")));
                if (testInsert) {
                    Assert.assertTrue(queryString.contains("user=test-user"));
                    Assert.assertTrue(queryString.contains("password=test-password"));
                }
                Assert.assertFalse(queryString.contains("user=baduser"));
                Assert.assertFalse(queryString.contains("password=nothing"));
            }
        });
    }
    
    @Test 
    public void testReplacements() throws UnsupportedEncodingException {
        testReplacement("report=Some Report Target&password=nothing&user=baduser");
        testReplacement("report=Some Report Target&password=nothing&user%3dbaduser");
        testReplacement("report=Some Report Target&user=baduser&password=nothing");
        testReplacement("password=nothing&user=baduser&report=Some Report Target");
        testReplacement("password=nothing&report=Some Report Target&user=baduser");
        testReplacement("user=baduser&password=nothing&report=Some Report Target");
        testReplacement("user=baduser&report=Some Report Target&password=nothing");
    }
    
    private void testReplacement(String queryString) throws UnsupportedEncodingException {
        // System.out.println(queryString);
        String r0 = URLDecoder.decode(queryString, "UTF-8");
        // System.out.println(r0);
        String r1 = r0.replaceAll("[&]{0,1}(user|password)=[^&]*", "");
        // System.out.println(r1);
        String r2 = r1.replaceAll("^&|&$", "");
        // System.out.println(r2);
        Assert.assertEquals("Report comparision", "report=Some Report Target", r2);
    }
}
