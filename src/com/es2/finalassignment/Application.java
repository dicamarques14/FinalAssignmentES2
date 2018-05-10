package com.es2.finalassignment;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class Application {
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

        ServletContextHandler ctx = configureServletHandler();
        ContextHandler staticContextHandler = configureStaticHandler();
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { staticContextHandler, ctx });
        server.setHandler(handlers);

        ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/*");
        serHol.setInitOrder(1);
        serHol.setInitParameter("jersey.config.server.provider.packages", "com.es2.finalassignment");

        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {

            server.destroy();
        }
    }

	private static ContextHandler configureStaticHandler() {
		ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase("./web/");
        //resource_handler.setWelcomeFiles(new String[]{ "pom.xml" });
        ContextHandler staticContextHandler = new ContextHandler("/static");
        staticContextHandler.setHandler(resource_handler);
		return staticContextHandler;
	}

	private static ServletContextHandler configureServletHandler() {
		ServletContextHandler ctx = 
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
                
        ctx.setContextPath("/");
		return ctx;
	}
}

