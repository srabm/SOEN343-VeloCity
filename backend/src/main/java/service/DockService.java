package service;

import com.concordia.velocity.backend.model.Dock;
import com.concordia.velocity.backend.model.Bike;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class DockService {

    private static final String COLLECTION_NAME = "docks";

    private Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    // Add or update dock
    public String saveDock(Dock dock) throws ExecutionException, InterruptedException {
        getDb().collection(COLLECTION_NAME)
                .document(dock.getDockId())
                .set(dock)
                .get();
        return "Dock " + dock.getDockId() + " saved successfully.";
    }

    // Get dock by ID
    public Dock getDockById(String dockId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = getDb()
                .collection(COLLECTION_NAME)
                .document(dockId)
                .get()
                .get();
        return snapshot.exists() ? snapshot.toObject(Dock.class) : null;
    }

    // Get all docks
    public List<Dock> getAllDocks() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = getDb()
                .collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments();
        List<Dock> docks = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            docks.add(doc.toObject(Dock.class));
        }
        return docks;
    }

    // Delete dock
    public String deleteDock(String dockId) throws ExecutionException, InterruptedException {
        getDb().collection(COLLECTION_NAME).document(dockId).delete().get();
        return "Dock " + dockId + " deleted successfully.";
    }

    // Update dock status (with reserved bike handling)
    public String updateDockStatus(String dockId, String newStatus)
            throws ExecutionException, InterruptedException {

        Firestore db = getDb();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(dockId);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Dock not found");
        }

        Dock dock = snapshot.toObject(Dock.class);

        // Check if a bike is linked to this dock
        if (dock.getBikeId() != null) {
            DocumentSnapshot bikeSnap = db.collection("bikes").document(dock.getBikeId()).get().get();
            if (bikeSnap.exists()) {
                Bike bike = bikeSnap.toObject(Bike.class);

                // If reserved, terminate reservation
                if ("reserved".equalsIgnoreCase(bike.getStatus())) {
                    bike.setStatus("available");
                    bike.setReservationExpiry(null);
                    db.collection("bikes").document(bike.getBikeId()).set(bike).get();
                    System.out.println("Reservation terminated for bike " + bike.getBikeId());
                    System.out.println("Rider notified: Reservation cancelled due to dock status change.");
                }
            }
        }

        dock.setState(newStatus.toLowerCase());
        docRef.set(dock).get();

        System.out.println("Dock " + dockId + " status changed to " + newStatus.toUpperCase() +
                " at " + LocalDateTime.now());

        return "Dock " + dockId + " updated successfully.";
    }
}
