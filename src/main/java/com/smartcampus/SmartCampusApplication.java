package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 *
 * The @ApplicationPath annotation defines the base URI for all JAX-RS resources.
 * When deployed on Tomcat, this works together with the servlet mapping in web.xml.
 * The full base URL becomes: http://localhost:8080/smart-campus-api/api/v1/
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Jersey auto-discovers resources via package scanning defined in web.xml
}