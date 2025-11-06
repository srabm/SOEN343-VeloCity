package com.concordia.velocity.config;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Dock;
import com.concordia.velocity.model.Station;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Enhanced Firestore Data Seeder
 * Updated to include dockCode and maintain existing station structure
 *
 * To run: Uncomment @Component annotation above
 * Or manually call seedFirestoreData() bean
 */
// @Component  // Uncomment to run on startup
public class FirestoreDataSeeder {

    private final Random random = new Random();

    @Bean
    CommandLineRunner seedFirestoreData() {
        return args -> {
            // Check if --seed flag is present
            if (Arrays.asList(args).contains("--seed")) {
                System.out.println("🌱 Starting Firestore data seeding...");
                Firestore db = FirestoreClient.getFirestore();
                seedDatabase(db);
                System.out.println("✅ Firestore data seeding complete!");
            }
        };
    }

    /**
     * Seeds the database with all data
     */
    private void seedDatabase(Firestore db) {
        try {
            // Optional: Clear existing data
            // clearDatabase(db);

            // Seed stations, docks, and bikes
            seedStations(db);
            seedDocks(db);
            seedBikes(db);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error during seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all collections EXCEPT riders (optional)
     * Preserves: riders, users (if you have them)
     */
    private void clearDatabase(Firestore db) throws ExecutionException, InterruptedException {
        System.out.println("🗑️  Clearing existing data (preserving riders)...");

        // Only clear these collections - riders will be preserved
        String[] collections = {"stations", "docks", "bikes", "trips", "bills"};

        for (String collection : collections) {
            var docs = db.collection(collection).listDocuments();
            int count = 0;
            for (var doc : docs) {
                doc.delete().get();
                count++;
            }
            System.out.println("   Cleared " + count + " documents from: " + collection);
        }

        System.out.println("   ✓ Riders collection preserved");
    }

    /**
     * Seeds stations with original data from your seeder + 6 new downtown stations
     */
    private void seedStations(Firestore db) throws ExecutionException, InterruptedException {
        System.out.println("📍 Seeding stations...");

        // Station 1: Downtown Ste-Catherine (occupied - 8 bikes)
        Station station1 = new Station(
                "S001",
                "Downtown Ste-Catherine Station",
                "occupied",
                "45.5017",
                "-73.5673",
                "300 Ste-Catherine St, Montreal",
                10,  // capacity
                8,   // numDockedBikes
                15,  // reservationHoldTime (minutes)
                Arrays.asList("D001","D002","D003","D004","D005","D006","D007","D008","D009","D010"),
                Arrays.asList("B001","B002","B003","B004","B005","B006","B007","B008")
        );

        // Station 2: Old Port (empty - 0 bikes)
        Station station2 = new Station(
                "S002",
                "Old Port Station",
                "empty",
                "45.5070",
                "-73.5540",
                "456 St-Paul St, Montreal",
                10,
                0,
                15,
                Arrays.asList("D011","D012","D013","D014","D015","D016","D017","D018","D019","D020"),
                Arrays.asList()
        );

        // Station 3: McGill University (full - 10 bikes)
        Station station3 = new Station(
                "S003",
                "McGill University Station",
                "full",
                "45.5048",
                "-73.5772",
                "845 Sherbrooke St W, Montreal",
                10,
                10,
                15,
                Arrays.asList("D021","D022","D023","D024","D025","D026","D027","D028","D029","D030"),
                Arrays.asList("B014","B015","B016","B017","B018","B019","B020","B021","B022","B023")
        );

        // Station 4: Atwater Market (occupied - 7 bikes)
        Station station4 = new Station(
                "S004",
                "Atwater Market Station",
                "occupied",
                "45.4790",
                "-73.5750",
                "138 Atwater Ave, Montreal",
                10,
                7,
                15,
                Arrays.asList("D031","D032","D033","D034","D035","D036","D037","D038","D039","D040"),
                Arrays.asList("B024","B025","B026","B027","B028","B029","B030")
        );

        // Station 5: Plateau Mont-Royal (out_of_service - 4 bikes)
        Station station5 = new Station(
                "S005",
                "Plateau Mont-Royal Station",
                "out_of_service",
                "45.5272",
                "-73.5791",
                "1200 Mont-Royal Ave E, Montreal",
                10,
                4,
                15,
                Arrays.asList("D041","D042","D043","D044","D045","D046","D047","D048","D049","D050"),
                Arrays.asList("B031","B032","B033","B034")
        );

        // ===== NEW DOWNTOWN STATIONS =====

        // Station 6: Place des Arts (occupied - 6 bikes)
        Station station6 = new Station(
                "S006",
                "Place des Arts Station",
                "occupied",
                "45.5081",
                "-73.5673",
                "175 Rue Sainte-Catherine O, Montreal",
                12,
                6,
                15,
                Arrays.asList("D051","D052","D053","D054","D055","D056","D057","D058","D059","D060","D061","D062"),
                Arrays.asList("B035","B036","B037","B038","B039","B040")
        );

        // Station 7: Quartier des Spectacles (occupied - 8 bikes)
        Station station7 = new Station(
                "S007",
                "Quartier des Spectacles Station",
                "occupied",
                "45.5089",
                "-73.5617",
                "1435 Rue Saint-Laurent, Montreal",
                15,
                8,
                15,
                Arrays.asList("D063","D064","D065","D066","D067","D068","D069","D070","D071","D072","D073","D074","D075","D076","D077"),
                Arrays.asList("B041","B042","B043","B044","B045","B046","B047","B048")
        );

        // Station 8: Complexe Desjardins (occupied - 9 bikes)
        Station station8 = new Station(
                "S008",
                "Complexe Desjardins Station",
                "occupied",
                "45.5092",
                "-73.5630",
                "150 Rue Sainte-Catherine O, Montreal",
                12,
                9,
                15,
                Arrays.asList("D078","D079","D080","D081","D082","D083","D084","D085","D086","D087","D088","D089"),
                Arrays.asList("B049","B050","B051","B052","B053","B054","B055","B056","B057")
        );

        // Station 9: Place Ville Marie (occupied - 11 bikes)
        Station station9 = new Station(
                "S009",
                "Place Ville Marie Station",
                "occupied",
                "45.5020",
                "-73.5698",
                "1 Place Ville Marie, Montreal",
                15,
                11,
                15,
                Arrays.asList("D090","D091","D092","D093","D094","D095","D096","D097","D098","D099","D100","D101","D102","D103","D104"),
                Arrays.asList("B058","B059","B060","B061","B062","B063","B064","B065","B066","B067","B068")
        );

        // Station 10: Square Victoria (occupied - 5 bikes)
        Station station10 = new Station(
                "S010",
                "Square Victoria Station",
                "occupied",
                "45.5010",
                "-73.5633",
                "800 Rue du Square-Victoria, Montreal",
                10,
                5,
                15,
                Arrays.asList("D105","D106","D107","D108","D109","D110","D111","D112","D113","D114"),
                Arrays.asList("B069","B070","B071","B072","B073")
        );

        // Station 11: Chinatown (occupied - 7 bikes)
        Station station11 = new Station(
                "S011",
                "Chinatown Station",
                "occupied",
                "45.5074",
                "-73.5598",
                "1001 Rue Saint-Urbain, Montreal",
                12,
                7,
                15,
                Arrays.asList("D115","D116","D117","D118","D119","D120","D121","D122","D123","D124","D125","D126"),
                Arrays.asList("B074","B075","B076","B077","B078","B079","B080")
        );

        List<Station> stations = Arrays.asList(
                station1, station2, station3, station4, station5,
                station6, station7, station8, station9, station10, station11
        );

        for (Station s : stations) {
            db.collection("stations").document(s.getStationId()).set(s).get();
        }

        System.out.println("   ✓ Added 11 stations to Firestore (5 original + 6 new downtown)");
    }

    /**
     * Seeds docks with original structure + dockCode + new stations
     */
    private void seedDocks(Firestore db) throws ExecutionException, InterruptedException {
        System.out.println("🔌 Seeding docks...");

        List<Dock> docks = Arrays.asList(
                // Station 1: Downtown Ste-Catherine (8 bikes, 2 empty)
                new Dock("D001", "occupied", "B001", "S001", generateDockCode()),
                new Dock("D002", "occupied", "B002", "S001", generateDockCode()),
                new Dock("D003", "occupied", "B003", "S001", generateDockCode()),
                new Dock("D004", "occupied", "B004", "S001", generateDockCode()),
                new Dock("D005", "occupied", "B005", "S001", generateDockCode()),
                new Dock("D006", "occupied", "B006", "S001", generateDockCode()),
                new Dock("D007", "occupied", "B007", "S001", generateDockCode()),
                new Dock("D008", "occupied", "B008", "S001", generateDockCode()),
                new Dock("D009", "empty", null, "S001", generateDockCode()),
                new Dock("D010", "empty", null, "S001", generateDockCode()),

                // Station 2: Old Port (empty - all docks available)
                new Dock("D011", "empty", null, "S002", generateDockCode()),
                new Dock("D012", "empty", null, "S002", generateDockCode()),
                new Dock("D013", "empty", null, "S002", generateDockCode()),
                new Dock("D014", "empty", null, "S002", generateDockCode()),
                new Dock("D015", "empty", null, "S002", generateDockCode()),
                new Dock("D016", "empty", null, "S002", generateDockCode()),
                new Dock("D017", "empty", null, "S002", generateDockCode()),
                new Dock("D018", "empty", null, "S002", generateDockCode()),
                new Dock("D019", "empty", null, "S002", generateDockCode()),
                new Dock("D020", "empty", null, "S002", generateDockCode()),

                // Station 3: McGill University (full - all docks occupied)
                new Dock("D021", "occupied", "B014", "S003", generateDockCode()),
                new Dock("D022", "occupied", "B015", "S003", generateDockCode()),
                new Dock("D023", "occupied", "B016", "S003", generateDockCode()),
                new Dock("D024", "occupied", "B017", "S003", generateDockCode()),
                new Dock("D025", "occupied", "B018", "S003", generateDockCode()),
                new Dock("D026", "occupied", "B019", "S003", generateDockCode()),
                new Dock("D027", "occupied", "B020", "S003", generateDockCode()),
                new Dock("D028", "occupied", "B021", "S003", generateDockCode()),
                new Dock("D029", "occupied", "B022", "S003", generateDockCode()),
                new Dock("D030", "occupied", "B023", "S003", generateDockCode()),

                // Station 4: Atwater Market (7 bikes, 3 empty)
                new Dock("D031", "occupied", "B024", "S004", generateDockCode()),
                new Dock("D032", "occupied", "B025", "S004", generateDockCode()),
                new Dock("D033", "occupied", "B026", "S004", generateDockCode()),
                new Dock("D034", "occupied", "B027", "S004", generateDockCode()),
                new Dock("D035", "occupied", "B028", "S004", generateDockCode()),
                new Dock("D036", "occupied", "B029", "S004", generateDockCode()),
                new Dock("D037", "occupied", "B030", "S004", generateDockCode()),
                new Dock("D038", "empty", null, "S004", generateDockCode()),
                new Dock("D039", "empty", null, "S004", generateDockCode()),
                new Dock("D040", "empty", null, "S004", generateDockCode()),

                // Station 5: Plateau Mont-Royal (out_of_service - 4 bikes, 6 out of service)
                new Dock("D041", "occupied", "B031", "S005", generateDockCode()),
                new Dock("D042", "occupied", "B032", "S005", generateDockCode()),
                new Dock("D043", "occupied", "B033", "S005", generateDockCode()),
                new Dock("D044", "occupied", "B034", "S005", generateDockCode()),
                new Dock("D045", "out_of_service", null, "S005", generateDockCode()),
                new Dock("D046", "out_of_service", null, "S005", generateDockCode()),
                new Dock("D047", "out_of_service", null, "S005", generateDockCode()),
                new Dock("D048", "out_of_service", null, "S005", generateDockCode()),
                new Dock("D049", "out_of_service", null, "S005", generateDockCode()),
                new Dock("D050", "out_of_service", null, "S005", generateDockCode()),

                // ===== NEW STATIONS =====

                // Station 6: Place des Arts (6 bikes, 6 empty)
                new Dock("D051", "occupied", "B035", "S006", generateDockCode()),
                new Dock("D052", "occupied", "B036", "S006", generateDockCode()),
                new Dock("D053", "occupied", "B037", "S006", generateDockCode()),
                new Dock("D054", "occupied", "B038", "S006", generateDockCode()),
                new Dock("D055", "occupied", "B039", "S006", generateDockCode()),
                new Dock("D056", "occupied", "B040", "S006", generateDockCode()),
                new Dock("D057", "empty", null, "S006", generateDockCode()),
                new Dock("D058", "empty", null, "S006", generateDockCode()),
                new Dock("D059", "empty", null, "S006", generateDockCode()),
                new Dock("D060", "empty", null, "S006", generateDockCode()),
                new Dock("D061", "empty", null, "S006", generateDockCode()),
                new Dock("D062", "empty", null, "S006", generateDockCode()),

                // Station 7: Quartier des Spectacles (8 bikes, 7 empty)
                new Dock("D063", "occupied", "B041", "S007", generateDockCode()),
                new Dock("D064", "occupied", "B042", "S007", generateDockCode()),
                new Dock("D065", "occupied", "B043", "S007", generateDockCode()),
                new Dock("D066", "occupied", "B044", "S007", generateDockCode()),
                new Dock("D067", "occupied", "B045", "S007", generateDockCode()),
                new Dock("D068", "occupied", "B046", "S007", generateDockCode()),
                new Dock("D069", "occupied", "B047", "S007", generateDockCode()),
                new Dock("D070", "occupied", "B048", "S007", generateDockCode()),
                new Dock("D071", "empty", null, "S007", generateDockCode()),
                new Dock("D072", "empty", null, "S007", generateDockCode()),
                new Dock("D073", "empty", null, "S007", generateDockCode()),
                new Dock("D074", "empty", null, "S007", generateDockCode()),
                new Dock("D075", "empty", null, "S007", generateDockCode()),
                new Dock("D076", "empty", null, "S007", generateDockCode()),
                new Dock("D077", "empty", null, "S007", generateDockCode()),

                // Station 8: Complexe Desjardins (9 bikes, 3 empty)
                new Dock("D078", "occupied", "B049", "S008", generateDockCode()),
                new Dock("D079", "occupied", "B050", "S008", generateDockCode()),
                new Dock("D080", "occupied", "B051", "S008", generateDockCode()),
                new Dock("D081", "occupied", "B052", "S008", generateDockCode()),
                new Dock("D082", "occupied", "B053", "S008", generateDockCode()),
                new Dock("D083", "occupied", "B054", "S008", generateDockCode()),
                new Dock("D084", "occupied", "B055", "S008", generateDockCode()),
                new Dock("D085", "occupied", "B056", "S008", generateDockCode()),
                new Dock("D086", "occupied", "B057", "S008", generateDockCode()),
                new Dock("D087", "empty", null, "S008", generateDockCode()),
                new Dock("D088", "empty", null, "S008", generateDockCode()),
                new Dock("D089", "empty", null, "S008", generateDockCode()),

                // Station 9: Place Ville Marie (11 bikes, 4 empty)
                new Dock("D090", "occupied", "B058", "S009", generateDockCode()),
                new Dock("D091", "occupied", "B059", "S009", generateDockCode()),
                new Dock("D092", "occupied", "B060", "S009", generateDockCode()),
                new Dock("D093", "occupied", "B061", "S009", generateDockCode()),
                new Dock("D094", "occupied", "B062", "S009", generateDockCode()),
                new Dock("D095", "occupied", "B063", "S009", generateDockCode()),
                new Dock("D096", "occupied", "B064", "S009", generateDockCode()),
                new Dock("D097", "occupied", "B065", "S009", generateDockCode()),
                new Dock("D098", "occupied", "B066", "S009", generateDockCode()),
                new Dock("D099", "occupied", "B067", "S009", generateDockCode()),
                new Dock("D100", "occupied", "B068", "S009", generateDockCode()),
                new Dock("D101", "empty", null, "S009", generateDockCode()),
                new Dock("D102", "empty", null, "S009", generateDockCode()),
                new Dock("D103", "empty", null, "S009", generateDockCode()),
                new Dock("D104", "empty", null, "S009", generateDockCode()),

                // Station 10: Square Victoria (5 bikes, 5 empty)
                new Dock("D105", "occupied", "B069", "S010", generateDockCode()),
                new Dock("D106", "occupied", "B070", "S010", generateDockCode()),
                new Dock("D107", "occupied", "B071", "S010", generateDockCode()),
                new Dock("D108", "occupied", "B072", "S010", generateDockCode()),
                new Dock("D109", "occupied", "B073", "S010", generateDockCode()),
                new Dock("D110", "empty", null, "S010", generateDockCode()),
                new Dock("D111", "empty", null, "S010", generateDockCode()),
                new Dock("D112", "empty", null, "S010", generateDockCode()),
                new Dock("D113", "empty", null, "S010", generateDockCode()),
                new Dock("D114", "empty", null, "S010", generateDockCode()),

                // Station 11: Chinatown (7 bikes, 5 empty)
                new Dock("D115", "occupied", "B074", "S011", generateDockCode()),
                new Dock("D116", "occupied", "B075", "S011", generateDockCode()),
                new Dock("D117", "occupied", "B076", "S011", generateDockCode()),
                new Dock("D118", "occupied", "B077", "S011", generateDockCode()),
                new Dock("D119", "occupied", "B078", "S011", generateDockCode()),
                new Dock("D120", "occupied", "B079", "S011", generateDockCode()),
                new Dock("D121", "occupied", "B080", "S011", generateDockCode()),
                new Dock("D122", "empty", null, "S011", generateDockCode()),
                new Dock("D123", "empty", null, "S011", generateDockCode()),
                new Dock("D124", "empty", null, "S011", generateDockCode()),
                new Dock("D125", "empty", null, "S011", generateDockCode()),
                new Dock("D126", "empty", null, "S011", generateDockCode())
        );

        for (Dock d : docks) {
            db.collection("docks").document(d.getDockId()).set(d).get();
        }

        System.out.println("   ✓ Added 126 docks to Firestore (all with unique dock codes)");
    }

    /**
     * Seeds bikes with original structure + new stations
     */
    private void seedBikes(Firestore db) throws ExecutionException, InterruptedException {
        System.out.println("🚲 Seeding bikes...");

        List<Bike> bikes = Arrays.asList(
                // Station 1: Downtown Ste-Catherine (8 bikes)
                new Bike("B001", "available", "standard", "D001", "S001"),
                new Bike("B002", "available", "standard", "D002", "S001"),
                new Bike("B003", "reserved", "electric", "D003", "S001"),
                new Bike("B004", "maintenance", "standard", "D004", "S001"),
                new Bike("B005", "available", "standard", "D005", "S001"),
                new Bike("B006", "available", "electric", "D006", "S001"),
                new Bike("B007", "available", "standard", "D007", "S001"),
                new Bike("B008", "available", "standard", "D008", "S001"),

                // Station 3: McGill University (10 bikes - full)
                new Bike("B014", "available", "standard", "D021", "S003"),
                new Bike("B015", "available", "electric", "D022", "S003"),
                new Bike("B016", "available", "standard", "D023", "S003"),
                new Bike("B017", "available", "standard", "D024", "S003"),
                new Bike("B018", "available", "standard", "D025", "S003"),
                new Bike("B019", "maintenance", "electric", "D026", "S003"),
                new Bike("B020", "available", "standard", "D027", "S003"),
                new Bike("B021", "available", "standard", "D028", "S003"),
                new Bike("B022", "available", "standard", "D029", "S003"),
                new Bike("B023", "available", "standard", "D030", "S003"),

                // Station 4: Atwater Market (7 bikes)
                new Bike("B024", "reserved", "standard", "D031", "S004"),
                new Bike("B025", "reserved", "standard", "D032", "S004"),
                new Bike("B026", "maintenance", "electric", "D033", "S004"),
                new Bike("B027", "available", "standard", "D034", "S004"),
                new Bike("B028", "available", "standard", "D035", "S004"),
                new Bike("B029", "available", "electric", "D036", "S004"),
                new Bike("B030", "available", "standard", "D037", "S004"),

                // Station 5: Plateau Mont-Royal (4 bikes - out_of_service station)
                new Bike("B031", "available", "standard", "D041", "S005"),
                new Bike("B032", "available", "electric", "D042", "S005"),
                new Bike("B033", "maintenance", "standard", "D043", "S005"),
                new Bike("B034", "available", "standard", "D044", "S005"),

                // ===== NEW STATIONS =====

                // Station 6: Place des Arts (6 bikes)
                new Bike("B035", "available", "standard", "D051", "S006"),
                new Bike("B036", "available", "electric", "D052", "S006"),
                new Bike("B037", "available", "standard", "D053", "S006"),
                new Bike("B038", "available", "electric", "D054", "S006"),
                new Bike("B039", "available", "standard", "D055", "S006"),
                new Bike("B040", "available", "standard", "D056", "S006"),

                // Station 7: Quartier des Spectacles (8 bikes)
                new Bike("B041", "available", "standard", "D063", "S007"),
                new Bike("B042", "available", "electric", "D064", "S007"),
                new Bike("B043", "available", "standard", "D065", "S007"),
                new Bike("B044", "available", "electric", "D066", "S007"),
                new Bike("B045", "available", "standard", "D067", "S007"),
                new Bike("B046", "available", "standard", "D068", "S007"),
                new Bike("B047", "available", "electric", "D069", "S007"),
                new Bike("B048", "available", "standard", "D070", "S007"),

                // Station 8: Complexe Desjardins (9 bikes)
                new Bike("B049", "available", "standard", "D078", "S008"),
                new Bike("B050", "available", "electric", "D079", "S008"),
                new Bike("B051", "available", "standard", "D080", "S008"),
                new Bike("B052", "available", "electric", "D081", "S008"),
                new Bike("B053", "available", "standard", "D082", "S008"),
                new Bike("B054", "available", "standard", "D083", "S008"),
                new Bike("B055", "maintenance", "electric", "D084", "S008"),
                new Bike("B056", "available", "standard", "D085", "S008"),
                new Bike("B057", "available", "standard", "D086", "S008"),

                // Station 9: Place Ville Marie (11 bikes)
                new Bike("B058", "available", "standard", "D090", "S009"),
                new Bike("B059", "available", "electric", "D091", "S009"),
                new Bike("B060", "available", "standard", "D092", "S009"),
                new Bike("B061", "available", "electric", "D093", "S009"),
                new Bike("B062", "available", "standard", "D094", "S009"),
                new Bike("B063", "available", "standard", "D095", "S009"),
                new Bike("B064", "available", "electric", "D096", "S009"),
                new Bike("B065", "available", "standard", "D097", "S009"),
                new Bike("B066", "reserved", "standard", "D098", "S009"),
                new Bike("B067", "available", "electric", "D099", "S009"),
                new Bike("B068", "available", "standard", "D100", "S009"),

                // Station 10: Square Victoria (5 bikes)
                new Bike("B069", "available", "standard", "D105", "S010"),
                new Bike("B070", "available", "electric", "D106", "S010"),
                new Bike("B071", "available", "standard", "D107", "S010"),
                new Bike("B072", "available", "standard", "D108", "S010"),
                new Bike("B073", "available", "electric", "D109", "S010"),

                // Station 11: Chinatown (7 bikes)
                new Bike("B074", "available", "standard", "D115", "S011"),
                new Bike("B075", "available", "electric", "D116", "S011"),
                new Bike("B076", "available", "standard", "D117", "S011"),
                new Bike("B077", "available", "electric", "D118", "S011"),
                new Bike("B078", "available", "standard", "D119", "S011"),
                new Bike("B079", "available", "standard", "D120", "S011"),
                new Bike("B080", "maintenance", "electric", "D121", "S011")
        );

        for (Bike b : bikes) {
            db.collection("bikes").document(b.getBikeId()).set(b).get();
        }

        System.out.println("   ✓ Added 75 bikes to Firestore");
        System.out.println("\n📊 Summary:");
        System.out.println("   • 11 stations (5 original + 6 new downtown Montreal)");
        System.out.println("   • 126 docks (all with unique 4-digit codes)");
        System.out.println("   • 75 bikes (mix of standard and electric)");
        System.out.println("   • Riders collection preserved (not modified)");
    }

    /**
     * Generates a unique 4-digit dock code
     */
    private String generateDockCode() {
        return String.format("%04d", random.nextInt(10000));
    }
}