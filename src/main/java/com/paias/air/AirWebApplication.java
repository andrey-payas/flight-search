package com.paias.air;

import com.paias.air.config.AppConfig;
import com.paias.air.config.Neo4jConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.*;

public class AirWebApplication {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(AppConfig.class, Neo4jConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(webContext);
        context.addServlet(new ServletHolder(dispatcherServlet), "/");

        server.setHandler(context);

        server.start();
        System.out.println("Spring app started on port 8080...");
        server.join();
    }
}
