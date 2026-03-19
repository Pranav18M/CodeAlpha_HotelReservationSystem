// SearchEngine.java
// Handles searching and filtering rooms based on user criteria

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SearchEngine {

    public static class SearchCriteria {
        public Room.Type  roomType;
        public LocalDate  checkIn;
        public LocalDate  checkOut;
        public int        guests;
        public double     maxPrice;

        public SearchCriteria(Room.Type type, LocalDate checkIn,
                               LocalDate checkOut, int guests, double maxPrice) {
            this.roomType  = type;
            this.checkIn   = checkIn;
            this.checkOut  = checkOut;
            this.guests    = guests;
            this.maxPrice  = maxPrice;
        }
    }

    private List<Room>        allRooms;
    private List<Reservation> allReservations;

    public SearchEngine(List<Room> rooms, List<Reservation> reservations) {
        this.allRooms        = rooms;
        this.allReservations = reservations;
    }

    // Main search — applies all filters and returns sorted results
    public List<Room> search(SearchCriteria criteria) {
        return allRooms.stream()
            .filter(Room::isAvailable)
            .filter(r -> criteria.roomType == null || r.getType() == criteria.roomType)
            .filter(r -> r.getMaxGuests() >= criteria.guests)
            .filter(r -> criteria.maxPrice == 0 || r.getPricePerNight() <= criteria.maxPrice)
            .filter(r -> isRoomFreeForDates(r, criteria.checkIn, criteria.checkOut))
            .sorted((a, b) -> Double.compare(a.getPricePerNight(), b.getPricePerNight()))
            .collect(Collectors.toList());
    }

    // Checks if a room has no conflicting reservations for the given dates
    public boolean isRoomFreeForDates(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation r : allReservations) {
            if (r.getRoom().getRoomNumber() != room.getRoomNumber()) continue;
            if (r.getStatus() == Reservation.Status.CANCELLED) continue;
            boolean overlaps = checkIn.isBefore(r.getCheckOut()) &&
                               checkOut.isAfter(r.getCheckIn());
            if (overlaps) return false;
        }
        return true;
    }

    public List<Room> getRoomsByType(Room.Type type) {
        return allRooms.stream()
            .filter(r -> r.getType() == type)
            .collect(Collectors.toList());
    }

    public int countAvailableByType(Room.Type type) {
        return (int) allRooms.stream()
            .filter(r -> r.getType() == type && r.isAvailable())
            .count();
    }

    public int getTotalRooms()    { return allRooms.size(); }

    public int getOccupiedCount() {
        return (int) allRooms.stream()
            .filter(r -> r.getStatus() == Room.Status.OCCUPIED)
            .count();
    }
}