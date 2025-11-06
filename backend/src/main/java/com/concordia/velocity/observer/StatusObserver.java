package com.concordia.velocity.observer;

import com.google.cloud.Timestamp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StatusObserver implements Observer {
    @Override
    public void update(String message) {
        // Log the issue to a local file
        String logEntry = String.format(
                "[%s] TripID: %s\n",
                Timestamp.now(),
                message
        );

        Path logFilePath = Paths.get("status_changes.log");
        try {
            Files.write(logFilePath, logEntry.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Status updates: " + message);
    }
}