/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.util;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 *  //TODO - After upgrading to javax.servlet version API 3.0, add response status code logging
 *  //TODO - Consider using Java SIMON API for measuring request times
 *
 *  In this filter, all incoming requests are captured and we measure server response times (using System.nanoTime() for now),
 *  this may be later adjusted using Java SIMON API (but this API is based on System.nanoTime() as well).
 *
 *  Right now, we are logging this request/response information
 *      Requested URL
 *      Request method (GET/POST)
 *      Request session id
 *
 *  Requests for .css or various image files are filtered and not recorded.
 *
 *  @author lazyman
 *  @author shood
 */
public class MidPointProfilingServletFilter implements Filter {

    /* Class Variables */
    private static final Trace LOGGER = TraceManager.getTrace(MidPointProfilingServletFilter.class);
    private static DecimalFormat df = new DecimalFormat("0.00");

    /* Attributes */
    protected FilterConfig config;

    /* Behavior */
    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        long startTime = System.nanoTime();
        chain.doFilter(request, response);
        long elapsedTime = System.nanoTime() - startTime;

        if(request instanceof HttpServletRequest){
            String uri = ((HttpServletRequest)request).getRequestURI();
            String info = ((HttpServletRequest)request).getMethod();
            String sessionId = ((HttpServletRequest)request).getRequestedSessionId();


            if(uri.startsWith("/midpoint/admin")){
                LOGGER.trace(info + " " + uri + " " + sessionId + " " + df.format(((double)elapsedTime)/1000000) + " (ms).");
            }
        }
    }   //doFilter


}
