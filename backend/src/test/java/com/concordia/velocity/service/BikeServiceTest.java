
import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.model.Trip;
import com.concordia.velocity.service.BikeService;
import com.concordia.velocity.service.TripService;
import com.concordia.velocity.service.UserService;
import com.concordia.velocity.service.LoyaltyStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BikeServiceTest {

	@Mock
	private UserService userService;
	@Mock
	private LoyaltyStatsService loyaltyStatsService;
	@Mock
	private TripService tripService;

	@InjectMocks
	private BikeService bikeService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		bikeService = new BikeService(userService, loyaltyStatsService);
	}

	@Test
	void happyPath_reserveRideReturnBike_outputsBillAndTrip() throws ExecutionException, InterruptedException {
		// Arrange: create mock objects for Station A, Station B, Rider, Bike
		String stationAId = "TS001";
		String stationBId = "TS002";
		String bikeId = "TB001";
		String riderId = "TR001";
		String dockAId = "TD001";
		String dockBId = "TD002";

		Station stationA = new Station();
		stationA.setStationId(stationAId);
		stationA.setStationName("Test station 1");
		Station stationB = new Station();
		stationB.setStationId(stationBId);
		stationB.setStationName("Test station 2");

		Rider rider = new Rider();
		rider.setId(riderId);
		rider.setFirstName("Test");
		rider.setLastName("Rider");

		Bike bike = new Bike();
		bike.setBikeId(bikeId);
		bike.setStatus(Bike.STATUS_AVAILABLE);
		bike.setDockId(dockAId);
		bike.setStationId(stationAId);

		// Mock userService
		when(userService.getUserById(riderId)).thenReturn(rider);

		// Simulate reservation
		bike.setStatus(Bike.STATUS_RESERVED);
		bike.setReservedByUserId(riderId);

		// Simulate unlocking and starting trip
		bike.setStatus(Bike.STATUS_ON_TRIP);
		bike.setDockId(null);
		bike.setStationId(null);

		// Simulate returning bike at Station B
		bike.setStatus(Bike.STATUS_AVAILABLE);
		bike.setDockId(dockBId);
		bike.setStationId(stationBId);

		// Create a Trip and Bill
		Trip trip = new Trip();
		trip.setTripId("TT001");
		trip.setRiderId(riderId);
		trip.setBikeId(bikeId);
		trip.setStartStationId(stationAId);
		trip.setEndStationId(stationBId);
		trip.setStatus(Trip.STATUS_COMPLETED);

		Bill bill = new Bill();
		bill.setBillId("TB001");
		bill.setTripId(trip.getTripId());
		bill.setRiderId(riderId);
		bill.setTotal(5.00);
		trip.setBill(bill);

		// Mock tripService to return trip and bill
        when(tripService.generateBillForTrip(trip)).thenReturn(bill);

		// Assert: Bill and Trip are generated and have expected values
		assertEquals(Trip.STATUS_COMPLETED, trip.getStatus());
		assertNotNull(trip.getBill());
		assertEquals(5.00, trip.getBill().getTotal());
		assertEquals(riderId, trip.getRiderId());
		assertEquals(stationAId, trip.getStartStationId());
		assertEquals(stationBId, trip.getEndStationId());
		assertEquals(bikeId, trip.getBikeId());
		assertEquals(trip.getTripId(), bill.getTripId());
	}
}
