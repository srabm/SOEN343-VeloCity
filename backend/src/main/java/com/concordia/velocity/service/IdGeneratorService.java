package com.concordia.velocity.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service for generating sequential IDs for Trips and Bills
 * Maintains counters in Firestore to ensure uniqueness across server restarts
 */
@Service
public class IdGeneratorService {

    private final Firestore db = FirestoreClient.getFirestore();

    // Collection to store counters
    private static final String COUNTERS_COLLECTION = "counters";
    private static final String TRIP_COUNTER_DOC = "tripCounter";
    private static final String BILL_COUNTER_DOC = "billCounter";

    /**
     * Generates next trip ID in format: T0001, T0002, etc.
     */
    public String generateTripId() throws ExecutionException, InterruptedException {
        int nextNumber = getAndIncrementCounter(TRIP_COUNTER_DOC);
        return String.format("T%04d", nextNumber);
    }

    /**
     * Generates next bill ID in format: BILL0001, BILL0002, etc.
     */
    public String generateBillId() throws ExecutionException, InterruptedException {
        int nextNumber = getAndIncrementCounter(BILL_COUNTER_DOC);
        return String.format("BILL%04d", nextNumber);
    }

    /**
     * Gets the current counter value and increments it atomically
     */
    private int getAndIncrementCounter(String counterName) throws ExecutionException, InterruptedException {
        // Use Firestore transaction to ensure atomic increment
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(
                    db.collection(COUNTERS_COLLECTION).document(counterName)
            ).get();

            int currentValue;
            if (!snapshot.exists()) {
                // First time - initialize counter
                currentValue = 1;
            } else {
                currentValue = snapshot.getLong("value").intValue();
            }

            // Increment counter
            int nextValue = currentValue + 1;

            Map<String, Object> data = new HashMap<>();
            data.put("value", nextValue);
            data.put("lastUpdated", com.google.cloud.Timestamp.now());

            transaction.set(
                    db.collection(COUNTERS_COLLECTION).document(counterName),
                    data
            );

            return currentValue;
        }).get();
    }

    /**
     * Resets the trip counter to a specific value
     * Useful for testing or migration
     */
    public void resetTripCounter(int value) throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("value", value);
        data.put("lastUpdated", com.google.cloud.Timestamp.now());

        db.collection(COUNTERS_COLLECTION)
                .document(TRIP_COUNTER_DOC)
                .set(data)
                .get();
    }

    /**
     * Resets the bill counter to a specific value
     * Useful for testing or migration
     */
    public void resetBillCounter(int value) throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("value", value);
        data.put("lastUpdated", com.google.cloud.Timestamp.now());

        db.collection(COUNTERS_COLLECTION)
                .document(BILL_COUNTER_DOC)
                .set(data)
                .get();
    }

    /**
     * Gets the current trip counter value without incrementing
     */
    public int getCurrentTripCounter() throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = db.collection(COUNTERS_COLLECTION)
                .document(TRIP_COUNTER_DOC)
                .get()
                .get();

        if (!snapshot.exists()) {
            return 0;
        }

        return snapshot.getLong("value").intValue();
    }

    /**
     * Gets the current bill counter value without incrementing
     */
    public int getCurrentBillCounter() throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = db.collection(COUNTERS_COLLECTION)
                .document(BILL_COUNTER_DOC)
                .get()
                .get();

        if (!snapshot.exists()) {
            return 0;
        }

        return snapshot.getLong("value").intValue();
    }
}