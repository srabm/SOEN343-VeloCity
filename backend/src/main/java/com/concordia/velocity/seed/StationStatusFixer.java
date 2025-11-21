// this script fixes station statuses based on their capacity and number of docked bikes
//it checks the numDockedBikes against capacity to determine if the station should be marked as "empty", "full", or "occupied"

// package com.concordia.velocity.seed;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import com.google.cloud.firestore.Firestore;
// import com.google.firebase.cloud.FirestoreClient;

// @Component
// public class StationStatusFixer implements CommandLineRunner {

//     private final Firestore db = FirestoreClient.getFirestore();

//     @Override
//     public void run(String... args) throws Exception {
//         System.out.println("=== Fixing station statuses based on capacity ===");

//         var stations = db.collection("stations").get().get();

//         stations.getDocuments().forEach(doc -> {
//             try {
//                 var data = doc.getData();
//                 if (data == null) return;

//                 long capacity = (long) data.getOrDefault("capacity", 0);
//                 long docked = (long) data.getOrDefault("numDockedBikes", 0);

//                 String correctStatus;
//                 if (docked == 0) correctStatus = "empty";
//                 else if (docked >= capacity) correctStatus = "full";
//                 else correctStatus = "occupied";

//                 String currentStatus = (String) data.getOrDefault("status", "");

//                 if (!currentStatus.equals(correctStatus)) {
//                     System.out.println("Fixing station " + doc.getId() +
//                             " → " + currentStatus + " → " + correctStatus);

//                     doc.getReference().update("status", correctStatus);
//                 }

//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         });

//         System.out.println("=== Station status fix complete ===");
//     }
// }
