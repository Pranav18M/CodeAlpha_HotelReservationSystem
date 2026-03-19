// Hotel.java
// Central controller — manages all rooms, reservations and business logic

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Hotel {

    private String            hotelName;
    private List<Room>        rooms;
    private List<Reservation> reservations;
    private SearchEngine      searchEngine;
    private PaymentProcessor  paymentProcessor;

    public Hotel() {
        this.hotelName       = "The Grand Horizon";
        this.rooms           = new ArrayList<>();
        this.reservations    = new ArrayList<>();
        this.paymentProcessor = new PaymentProcessor();

        initializeRooms();
        loadSavedData();
        this.searchEngine = new SearchEngine(rooms, reservations);
    }

    // Creates all 30 rooms across 4 types and 8 floors
    private void initializeRooms() {
        // Floor 1-3: Standard rooms
        for (int floor = 1; floor <= 3; floor++)
            for (int num = 1; num <= 4; num++)
                rooms.add(new Room(floor * 100 + num, Room.Type.STANDARD, floor));

        // Floor 4-5: Deluxe rooms
        for (int floor = 4; floor <= 5; floor++)
            for (int num = 1; num <= 5; num++)
                rooms.add(new Room(floor * 100 + num, Room.Type.DELUXE, floor));

        // Floor 6-7: Suites
        for (int floor = 6; floor <= 7; floor++)
            for (int num = 1; num <= 3; num++)
                rooms.add(new Room(floor * 100 + num, Room.Type.SUITE, floor));

        // Floor 8: Presidential
        rooms.add(new Room(801, Room.Type.PRESIDENTIAL, 8));
        rooms.add(new Room(802, Room.Type.PRESIDENTIAL, 8));
    }

    private void loadSavedData() {
        reservations = FileManager.loadReservations(rooms);

        // Apply saved room statuses
        for (String line : FileManager.loadRoomStatuses()) {
            Room saved = Room.fromFileString(line);
            if (saved != null) {
                rooms.stream()
                    .filter(r -> r.getRoomNumber() == saved.getRoomNumber())
                    .findFirst()
                    .ifPresent(r -> r.setStatus(saved.getStatus()));
            }
        }
    }

    // ── BOOKING ───────────────────────────────────────────────────────────

    public Reservation createBooking(Guest guest, Room room,
                                      LocalDate checkIn, LocalDate checkOut,
                                      int guests,
                                      Reservation.PaymentMethod paymentMethod,
                                      String specialRequests) {
        String bookingId = "BK" + System.currentTimeMillis() % 100000;
        Reservation res  = new Reservation(bookingId, guest, room,
            checkIn, checkOut, guests, paymentMethod, specialRequests);

        reservations.add(res);
        room.setStatus(Room.Status.OCCUPIED);

        FileManager.saveReservations(reservations);
        FileManager.saveRooms(rooms);
        return res;
    }

    // ── CANCELLATION ──────────────────────────────────────────────────────

    public double cancelBooking(String bookingId) {
        Reservation res = findReservationById(bookingId);
        if (res == null || res.getStatus() == Reservation.Status.CANCELLED) return -1;

        double refund = res.calculateRefund();
        res.setStatus(Reservation.Status.CANCELLED);
        res.getRoom().setStatus(Room.Status.AVAILABLE);

        FileManager.saveReservations(reservations);
        FileManager.saveRooms(rooms);
        return refund;
    }

    // ── SEARCH ────────────────────────────────────────────────────────────

    public List<Room> searchRooms(SearchEngine.SearchCriteria criteria) {
        return searchEngine.search(criteria);
    }

    public List<Room> getAllRooms()       { return rooms; }

    public List<Room> getRoomsByType(Room.Type type) {
        return rooms.stream()
            .filter(r -> r.getType() == type)
            .collect(Collectors.toList());
    }

    // ── RESERVATIONS ──────────────────────────────────────────────────────

    public List<Reservation> getAllReservations() { return reservations; }

    public Reservation findReservationById(String id) {
        return reservations.stream()
            .filter(r -> r.getBookingId().equalsIgnoreCase(id))
            .findFirst().orElse(null);
    }

    // ── STATS ─────────────────────────────────────────────────────────────

    public int getTotalRooms()     { return rooms.size(); }

    public int getAvailableCount() {
        return (int) rooms.stream().filter(Room::isAvailable).count();
    }

    public int getOccupiedCount()  {
        return (int) rooms.stream()
            .filter(r -> r.getStatus() == Room.Status.OCCUPIED).count();
    }

    public double getTotalRevenue() {
        return reservations.stream()
            .filter(r -> r.getStatus() != Reservation.Status.CANCELLED)
            .mapToDouble(Reservation::getTotalAmount)
            .sum();
    }

    public int getAvailableByType(Room.Type type) {
        return (int) rooms.stream()
            .filter(r -> r.getType() == type && r.isAvailable())
            .count();
    }

    public String           getHotelName()  { return hotelName; }
    public PaymentProcessor getPayment()    { return paymentProcessor; }
    public SearchEngine     getSearchEngine(){ return searchEngine; }
}