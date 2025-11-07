package com.concordia.velocity.service;

import com.concordia.velocity.model.Rider;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;    
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.SetOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private final Firestore db = FirestoreClient.getFirestore();

    //Fetch user info by ID (document ID or email) 
    public Rider getUserById(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("users").document(userId).get().get();

        if (!doc.exists()) return null;

        Rider rider = doc.toObject(Rider.class);
        if (rider != null) rider.setId(doc.getId());
        return rider;
    }

    //Check if an email already exists (for uniqueness) 
    public boolean emailExists(String email) throws ExecutionException, InterruptedException {
        var query = db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();
        return !query.isEmpty();
    }

    // Create a new rider (only if email is unique)
    public Map<String, Object> createUser(Rider rider) throws ExecutionException, InterruptedException {
        Map<String, Object> response = new HashMap<>();

        if (emailExists(rider.getEmail())) {
            response.put("error", "Email already exists: " + rider.getEmail());
            return response;
        }

        db.collection("users").document(rider.getEmail()).set(rider).get();
        response.put("message", "Rider created successfully");
        response.put("email", rider.getEmail());
        return response;
    }

    public Map<String, Object> updateUser(String userId, Map<String, Object> updates) throws ExecutionException, InterruptedException {
        Map<String, Object> response = new HashMap<>();

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.set(updates, SetOptions.merge()).get();

        response.put("message", "Rider updated successfully");
        response.put("userId", userId);
        return response;
    }

    public Map<String, Object> deleteUser(String userId) throws ExecutionException, InterruptedException {
        Map<String, Object> response = new HashMap<>();

        db.collection("users").document(userId).delete().get();
        response.put("message", "Rider deleted successfully");
        response.put("userId", userId);
        return response;
    }


}

