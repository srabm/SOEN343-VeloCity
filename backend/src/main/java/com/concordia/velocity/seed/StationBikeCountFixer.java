//this cleanup seeder thingy fixes the bike counts for all stations in the database
// we noticed the numDockedBikes does not match the actual number of bikes present at each station
//so this script goes through all stations, counts the bikes based on bikeIds array, and updates the counts accordingly

// package com.concordia.velocity.seed;

// import java.util.List;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import com.google.cloud.firestore.Firestore;
// import com.google.firebase.cloud.FirestoreClient;

// @Component
// public class StationBikeCountFixer implements CommandLineRunner {

//     private final Firestore db = FirestoreClient.getFirestore();

//     @Override
//     public void run(String... args) throws Exception {

//         System.out.println("=== Fixing station bike counts ===");

//         var stations = db.collection("stations").get().get();
//         var bikes = db.collection("bikes").get().get();

//         // load bikes into memory
//         var bikeMap = bikes.getDocuments().stream()
//             .collect(java.util.stream.Collectors.toMap(
//                     d -> d.getId(),
//                     d -> d.getData()
//             ));

//         for (var doc : stations.getDocuments()) {
//             var data = doc.getData();
//             if (data == null) continue;

//             List<String> bikeIds = (List<String>) data.getOrDefault("bikeIds", List.of());
//             long capacity = (long) data.getOrDefault("capacity", 0);

//             int total = 0;
//             int electric = 0;
//             int standard = 0;

//             for (String id : bikeIds) {
//                 var bike = bikeMap.get(id);
//                 if (bike == null) continue;

//                 total++;

//                 String type = (String) bike.getOrDefault("type", "standard");
//                 if (type.equals("e-bike") || type.equals("electric"))
//                     electric++;
//                 else
//                     standard++;
//             }

//             String correctStatus =
//                 (total == 0) ? "empty" :
//                 (total >= capacity) ? "full" :
//                 "occupied";

//             doc.getReference().update(
//                     "numDockedBikes", total,
//                     "numElectricBikes", electric,
//                     "numStandardBikes", standard,
//                     "status", correctStatus
//             );

//             System.out.println(
//                 "Updated " + doc.getId() +
//                 " → docked=" + total +
//                 ", electric=" + electric +
//                 ", standard=" + standard +
//                 ", status=" + correctStatus
//             );
//         }

//         System.out.println("=== Station bike count fix complete ===");
//     }
// }
