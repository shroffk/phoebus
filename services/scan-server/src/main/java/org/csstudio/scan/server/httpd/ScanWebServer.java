/*******************************************************************************
 * Copyright (c) 2012-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.scan.server.httpd;

import org.csstudio.scan.server.ScanServer;
import org.csstudio.scan.server.ScanServerInstance;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

/** Web server for {@link ScanServer} monitor/control
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScanWebServer
{
    private final Server server;

    public ScanWebServer(final int port)
    {
        // Configure Jetty to use java.util.logging, and don't announce that it's doing that
        System.setProperty("org.eclipse.jetty.util.log.announce", "false");
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.JavaUtilLog");

        server = new Server(port);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY | ServletContextHandler.NO_SESSIONS);

        // Our servlets
        context.addServlet(ServerServlet.class, "/server/*");
        context.addServlet(ScansServlet.class, "/scans/*");
        context.addServlet(ScanServlet.class, "/scan/*");
        context.addServlet(SimulateServlet.class, "/simulate/*");

        // Serve static files from webroot to "/"
        context.setContextPath("/");
        context.setResourceBase(ScanServerInstance.class.getResource("/webroot").toExternalForm());
        context.addServlet(DefaultServlet.class, "/");

        // Example for enabling 'request' log:
        // 0:0:0:0:0:0:0:1 - - [14/Dec/2020:19:48:23 +0000] "GET / HTTP/1.1" 200 5019 "-" "Mozilla/...
        final RequestLogHandler log_handler = new RequestLogHandler();
        final NCSARequestLog log = new NCSARequestLog("/tmp/jetty-requests-yyyy_mm_dd.log");
        log.setRetainDays(30);
        log.setAppend(true);
        log.setExtended(false);
        log.setLogTimeZone("GMT");
        log_handler.setRequestLog(log);

        final HandlerCollection handlers = new HandlerCollection(context, log_handler);
        server.setHandler(handlers);
    }

    public void start() throws Exception
    {
        server.start();
    }

    public void shutdown() throws Exception
    {
        server.stop();
        server.join();
    }
}
