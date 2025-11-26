package com.concordia.velocity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable Spring's scheduled task execution
 * This allows the @Scheduled annotation to work in services like AbandonedTripService
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // This class enables scheduled tasks across the application
    // No additional configuration needed - the annotation does all the work
}