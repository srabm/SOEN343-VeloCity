package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Dock;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.Observer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DockService {

    private final Firestore db = FirestoreClient.getFirestore();

    //Updates a dock’s status while validating bike state.

    public String updateDockStatus(String dockId, String newStatus)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot doc = db.collection("docks").document(dockId).get().get();
        Dock dock = doc.toObject(Dock.class);

        if (dock == null) {
            throw new IllegalArgumentException("Dock not found: " + dockId);
        }

        if (!List.of("empty", "occupied", "out_of_service").contains(newStatus)) {
            throw new IllegalArgumentException("Invalid dock status: " + newStatus);
        }

        // If dock has an associated bike, check its status
        if (dock.getBikeId() != null && !dock.getBikeId().isEmpty()) {
            DocumentSnapshot bikeDoc = db.collection("bikes")
                                         .document(dock.getBikeId())
                                         .get().get();
            Bike bike = bikeDoc.toObject(Bike.class);

            if (bike != null && "reserved".equalsIgnoreCase(bike.getStatus())) {
                // terminate reservation before proceeding
                terminateReservation(bike.getBikeId());
                bike.setStatus("available");
                db.collection("bikes").document(bike.getBikeId()).set(bike);
                System.out.println("Reservation terminated for bike " + bike.getBikeId());
            }
        }


        dock.setStatus(newStatus);
        db.collection("docks").document(dockId).set(dock);


        Observer dashboardObserver = new DashboardObserver();
        dock.attach(dashboardObserver);

        dock.notifyObservers();

        return "Dock " + dockId + " updated to status: " + dock.getStatus();
    }

    private void terminateReservation(String bikeId)
            throws ExecutionException, InterruptedException {
        var reservations = db.collection("reservations")
                             .whereEqualTo("bikeId", bikeId)
                             .get()
                             .get()
                             .getDocuments();

        for (DocumentSnapshot res : reservations) {
            db.collection("reservations").document(res.getId()).delete();
        }
        System.out.println("Reservation entries for bike " + bikeId + " cleaned up.");
    }


    public Dock getDockById(String dockId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("docks").document(dockId).get().get();
        return doc.toObject(Dock.class);
    }

    public List<Dock> getAllDocks()
            throws ExecutionException, InterruptedException {
        List<Dock> docks = new ArrayList<>();
        for (DocumentSnapshot doc : db.collection("docks").get().get().getDocuments()) {
            docks.add(doc.toObject(Dock.class));
        }
        return docks;
    }
}
