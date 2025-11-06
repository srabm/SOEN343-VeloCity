package com.concordia.velocity.config;

/*
// @Component
public class FirestoreDataSeederOld {

    @Bean
    CommandLineRunner seedFirestoreData() {
        return args -> {
            Firestore db = FirestoreClient.getFirestore();

            try {
                // --- STATIONS ---
                Station station1 = new Station(
                        "S001",
                        "Downtown Ste-Catherine Station",
                        "occupied", 
                        "45.5017",
                        "-73.5673",
                        "300 Ste-Catherine St, Montreal",
                        10,
                        8,
                        10,
                        Arrays.asList("D001","D002","D003","D004","D005","D006","D007","D008","D009","D010"),
                        Arrays.asList("B001","B002","B003","B004","B005","B006","B007","B008")
                );

                Station station2 = new Station(
                        "S002",
                        "Old Port Station",
                        "empty", 
                        "45.5070",
                        "-73.5540",
                        "456 St-Paul St, Montreal",
                        10,
                        0,
                        10,
                        Arrays.asList("D011","D012","D013","D014","D015","D016","D017","D018","D019","D020"),
                        Arrays.asList()
                );

                Station station3 = new Station(
                        "S003",
                        "McGill University Station",
                        "full", 
                        "45.5048",
                        "-73.5772",
                        "845 Sherbrooke St W, Montreal",
                        10,
                        10,
                        10,
                        Arrays.asList("D021","D022","D023","D024","D025","D026","D027","D028","D029","D030"),
                        Arrays.asList("B014","B015","B016","B017","B018","B019","B020","B021","B022","B023")
                );

                Station station4 = new Station(
                        "S004",
                        "Atwater Market Station",
                        "occupied", 
                        "45.4790",
                        "-73.5750",
                        "138 Atwater Ave, Montreal",
                        10,
                        7,
                        10,
                        Arrays.asList("D031","D032","D033","D034","D035","D036","D037","D038","D039","D040"),
                        Arrays.asList("B024","B025","B026","B027","B028","B029","B030")
                );

                Station station5 = new Station(
                        "S005",
                        "Plateau Mont-Royal Station",
                        "out_of_service", 
                        "45.5272",
                        "-73.5791",
                        "1200 Mont-Royal Ave E, Montreal",
                        10,
                        4,
                        10,
                        Arrays.asList("D041","D042","D043","D044","D045","D046","D047","D048","D049","D050"),
                        Arrays.asList("B031","B032","B033","B034")
                );

                List<Station> stations = Arrays.asList(station1, station2, station3, station4, station5);
                for (Station s : stations) {
                    db.collection("stations").document(s.getStationId()).set(s).get();
                }
                System.out.println("5 sample stations added to Firestore.");

                // --- DOCKS ---
                List<Dock> docks = Arrays.asList(
                        // Station 1 (8 bikes, 2 empty)
                        new Dock("D001","occupied","B001","S001"),
                        new Dock("D002","occupied","B002","S001"),
                        new Dock("D003","occupied","B003","S001"),
                        new Dock("D004","occupied","B004","S001"),
                        new Dock("D005","occupied","B005","S001"),
                        new Dock("D006","occupied","B006","S001"),
                        new Dock("D007","occupied","B007","S001"),
                        new Dock("D008","occupied","B008","S001"),
                        new Dock("D009","available","B009","S001"),
                        new Dock("D010","on_trip","B010","S001"),
                    

                        // Station 2 (empty)
                        new Dock("D011","empty",null,"S002"),
                        new Dock("D012","empty",null,"S002"),
                        new Dock("D013","empty",null,"S002"),
                        new Dock("D014","empty",null,"S002"),
                        new Dock("D015","empty",null,"S002"),
                        new Dock("D016","empty",null,"S002"),
                        new Dock("D017","empty",null,"S002"),
                        new Dock("D018","empty",null,"S002"),
                        new Dock("D019","empty",null,"S002"),
                        new Dock("D020","empty",null,"S002"),

                        // Station 3 (full)
                        new Dock("D021","occupied","B014","S003"),
                        new Dock("D022","occupied","B015","S003"),
                        new Dock("D023","occupied","B016","S003"),
                        new Dock("D024","occupied","B017","S003"),
                        new Dock("D025","occupied","B018","S003"),
                        new Dock("D026","occupied","B019","S003"),
                        new Dock("D027","occupied","B020","S003"),
                        new Dock("D028","occupied","B021","S003"),
                        new Dock("D029","occupied","B022","S003"),
                        new Dock("D030","occupied","B023","S003"),

                        // Station 4 (7 bikes)
                        new Dock("D031","occupied","B024","S004"),
                        new Dock("D032","occupied","B025","S004"),
                        new Dock("D033","occupied","B026","S004"),
                        new Dock("D034","occupied","B027","S004"),
                        new Dock("D035","occupied","B028","S004"),
                        new Dock("D036","occupied","B029","S004"),
                        new Dock("D037","occupied","B030","S004"),
                        new Dock("D038","empty",null,"S004"),
                        new Dock("D039","empty",null,"S004"),
                        new Dock("D040","empty",null,"S004"),

                        // Station 5 (out of service)
                        new Dock("D041","occupied","B031","S005"),
                        new Dock("D042","occupied","B032","S005"),
                        new Dock("D043","occupied","B033","S005"),
                        new Dock("D044","occupied","B034","S005"),
                        new Dock("D045","out_of_service",null,"S005"),
                        new Dock("D046","out_of_service",null,"S005"),
                        new Dock("D047","out_of_service",null,"S005"),
                        new Dock("D048","out_of_service",null,"S005"),
                        new Dock("D049","out_of_service",null,"S005"),
                        new Dock("D050","out_of_service",null,"S005")
                );

                for (Dock d : docks) {
                    db.collection("docks").document(d.getDockId()).set(d).get();
                }
                System.out.println("Docks added to Firestore.");

                // --- BIKES ---
                List<Bike> bikes = Arrays.asList(
                        // S001
                        new Bike("B001","available","standard","D001","S001"),
                        new Bike("B002","available","standard","D002","S001"),
                        new Bike("B003","reserved","e-bike","D003","S001"),
                        new Bike("B004","maintenance","standard","D004","S001"),
                        new Bike("B005","available","standard","D005","S001"),
                        new Bike("B006","available","e-bike","D006","S001"),
                        new Bike("B007","available","standard","D007","S001"),
                        new Bike("B008","available","standard","D008","S001"),
                        new Bike("B009","available","standard","D009","S001"),
                        new Bike("B010","available","e-bike","D010","S001"),

                        // S003 (full)
                        new Bike("B014","available","standard","D021","S003"),
                        new Bike("B015","available","e-bike","D022","S003"),
                        new Bike("B016","available","standard","D023","S003"),
                        new Bike("B017","available","standard","D024","S003"),
                        new Bike("B018","available","standard","D025","S003"),
                        new Bike("B019","maintenance","e-bike","D026","S003"),
                        new Bike("B020","available","standard","D027","S003"),
                        new Bike("B021","available","standard","D028","S003"),
                        new Bike("B022","available","standard","D029","S003"),
                        new Bike("B023","available","standard","D030","S003"),

                        // S004 (7 bikes)
                        new Bike("B024","reserved","standard","D031","S004"),
                        new Bike("B025","reserved","standard","D032","S004"),
                        new Bike("B026","maintenance","e-bike","D033","S004"),
                        new Bike("B027","available","standard","D034","S004"),
                        new Bike("B028","available","standard","D035","S004"),
                        new Bike("B029","available","e-bike","D036","S004"),
                        new Bike("B030","available","standard","D037","S004"),

                        // S005 (out_of_service)
                        new Bike("B031","available","standard","D041","S005"),
                        new Bike("B032","available","e-bike","D042","S005"),
                        new Bike("B033","maintenance","standard","D043","S005"),
                        new Bike("B034","available","standard","D044","S005")
                );

                for (Bike b : bikes) {
                    db.collection("bikes").document(b.getBikeId()).set(b).get();
                }
                System.out.println("Bikes added to Firestore.");

                System.out.println("Firestore data seeding complete!");

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
    }
}
*/