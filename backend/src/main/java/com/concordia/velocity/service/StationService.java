// This file is supposed to handle business logic related to updating station status

package com.concordia.velocity.service;

import com.concordia.velocity.model.Station;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.NotificationObserver;
import com.concordia.velocity.observer.Observer;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class StationService {

    private final Firestore db = FirestoreClient.getFirestore();

    public String updateStationStatus(String stationId, String newStatus) throws ExecutionException, InterruptedException {
        Station station = db.collection("stations").document(stationId)
                .get().get().toObject(Station.class);

        if (station == null) {
            return "Station not found.";
        }

        // Attaches observers
        Observer dashboardObserver = new DashboardObserver();
        Observer notificationObserver = new NotificationObserver();

        station.attach(dashboardObserver);
        station.attach(notificationObserver);

        
        if (newStatus.equals("out_of_service")) {
            station.setStatus("out_of_service");
        } else {
            int docked = station.getNumDockedBikes();
            int capacity = station.getCapacity();

            if (docked == 0)
                station.setStatus("empty");
            else if (docked == capacity)
                station.setStatus("full");
            else
                station.setStatus("occupied");  
        }
        station.notifyObservers();
        db.collection("stations").document(stationId).set(station);
        return "Station " + stationId + " updated to status:  " + station.getStatus() + ".\n";
    }

    public Object getStationById(String stationId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        return  db.collection("stations").document(stationId)
                .get().get().toObject(Station.class);
    }

    public void decreaseBikeCount(String stationId) throws ExecutionException, InterruptedException  {

    }
}
