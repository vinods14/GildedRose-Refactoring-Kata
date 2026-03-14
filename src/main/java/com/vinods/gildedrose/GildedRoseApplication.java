package com.vinods.gildedrose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Gilded Rose Kata application.
 *
 * <p>Bootstraps the application context and enables component scanning
 * across the {@code com.vinods.gildedrose} package tree, which includes:
 * <ul>
 *   <li>{@code com.vinods.gildedrose.config} — Spring wiring of domain objects</li>
 *   <li>{@code com.vinods.gildedrose.adapter.in.web} — REST controller adapter</li>
 * </ul>
 *
 * <p>Domain and application-service classes ({@code domain.*}, {@code application.*})
 * are intentionally free of Spring annotations; they are wired via
 * {@link com.vinods.gildedrose.config.GildedRoseConfiguration}.
 */
@SpringBootApplication
public class GildedRoseApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments forwarded to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(GildedRoseApplication.class, args);
    }
}
