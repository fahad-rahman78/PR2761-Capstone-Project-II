package com.capstone.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the whole application.
 *
 * The @SpringBootApplication annotation does three things for us:
 *   1. Marks this as a configuration class.
 *   2. Turns on Spring Boot auto-configuration (it sets up the web server,
 *      the database connection pool, etc. based on what is on the classpath).
 *   3. Tells Spring to scan this package (and sub-packages) for our
 *      controllers, services and repositories.
 *
 * When you run `mvn spring-boot:run`, Spring starts an embedded Tomcat web
 * server on port 8080 and our REST API becomes available.
 */
@SpringBootApplication
public class BookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }
}
