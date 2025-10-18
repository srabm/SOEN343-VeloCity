// This file is supposed to handle business logic related to stations. 
// It's the service layer that keeps the logic testable and reusable, so that it keeps the controller clean.

package service;

import model.Station;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class StationService {

    private static final String COLLECTION_NAME = "stations";

    // Create or Update
    public String saveStation(Station station) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(station.getStationId()).set(station).get();
        return "Station " + station.getStationId() + " saved successfully.";
    }

    // Read (Get by ID)
    public Station getStationById(String stationId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(stationId).get().get();
        return snapshot.exists() ? snapshot.toObject(Station.class) : null;
    }

    // Read (All stations)
    public List<Station> getAllStations() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Station> stations = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            stations.add(doc.toObject(Station.class));
        }
        return stations;
    }

    // Delete
    public String deleteStation(String stationId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(stationId).delete().get();
        return "Station " + stationId + " deleted successfully.";
    }

    // Update Station Status
    public String updateStationStatus(String stationId, String newStatus)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(stationId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        docRef.update(updates).get();
        return "Station " + stationId + " status updated to " + newStatus;
    }
}
