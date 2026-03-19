// Room.java
// Represents a single hotel room with all its details
// Rooms have types (Standard, Deluxe, Suite, Presidential)
// and track their availability status

import java.util.List;
import java.util.Arrays;

public class Room {

    public enum Type {
        STANDARD     ("Standard",      1499.00, "🛏️"),
        DELUXE       ("Deluxe",        2999.00, "🛏️✨"),
        SUITE        ("Suite",         5999.00, "🛏️👑"),
        PRESIDENTIAL ("Presidential", 12999.00, "🏆");

        public final String label;
        public final double basePrice;
        public final String icon;

        Type(String label, double basePrice, String icon) {
            this.label     = label;
            this.basePrice = basePrice;
            this.icon      = icon;
        }
    }

    public enum Status { AVAILABLE, OCCUPIED, MAINTENANCE }

    private int          roomNumber;
    private Type         type;
    private int          floor;
    private int          maxGuests;
    private double       pricePerNight;
    private Status       status;
    private String       view;
    private List<String> amenities;
    private String       description;

    public Room(int roomNumber, Type type, int floor) {
        this.roomNumber    = roomNumber;
        this.type          = type;
        this.floor         = floor;
        this.status        = Status.AVAILABLE;
        this.pricePerNight = type.basePrice;
        setupByType();
    }

    // Sets amenities, view, guests and description based on room type
    private void setupByType() {
        switch (type) {
            case STANDARD -> {
                this.maxGuests   = 2;
                this.view        = floor > 5 ? "City View" : "Garden View";
                this.amenities   = Arrays.asList(
                    "Free WiFi", "AC", "TV", "Hot Water",
                    "Room Service", "Daily Housekeeping"
                );
                this.description = "Comfortable and cozy room perfect for solo travelers or couples.";
            }
            case DELUXE -> {
                this.maxGuests   = 3;
                this.view        = floor > 5 ? "Sea View" : "Pool View";
                this.amenities   = Arrays.asList(
                    "Free WiFi", "AC", "Smart TV", "Mini Bar",
                    "King Bed", "Room Service 24/7", "Bathtub",
                    "Premium Toiletries", "Coffee Maker"
                );
                this.description = "Spacious deluxe room with premium furnishings.";
            }
            case SUITE -> {
                this.maxGuests   = 4;
                this.view        = "Sea View";
                this.amenities   = Arrays.asList(
                    "Free WiFi", "AC", "65\" Smart TV", "Full Mini Bar",
                    "King Bed + Sofa Bed", "Jacuzzi", "Dining Area",
                    "Living Room", "Premium Toiletries", "Butler Service",
                    "Free Breakfast", "Lounge Access"
                );
                this.description = "Luxurious suite with separate living and sleeping areas.";
            }
            case PRESIDENTIAL -> {
                this.maxGuests   = 6;
                this.view        = "Panoramic View";
                this.amenities   = Arrays.asList(
                    "Dedicated Butler", "Free WiFi (Fiber)", "AC/Heating",
                    "85\" Smart TV", "Full Bar", "2 King Beds",
                    "Private Pool", "Private Dining", "Jacuzzi + Steam",
                    "Limousine Service", "Personal Chef",
                    "Free All Meals", "Airport Pickup", "Concierge 24/7"
                );
                this.description = "The ultimate luxury experience with every amenity included.";
            }
        }
    }

    // File format: roomNumber|type|floor|status
    public String toFileString() {
        return roomNumber + "|" + type.name() + "|" + floor + "|" + status.name();
    }

    public static Room fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 4) return null;
        try {
            int    num    = Integer.parseInt(parts[0].trim());
            Type   type   = Type.valueOf(parts[1].trim());
            int    floor  = Integer.parseInt(parts[2].trim());
            Status status = Status.valueOf(parts[3].trim());
            Room room = new Room(num, type, floor);
            room.setStatus(status);
            return room;
        } catch (Exception e) {
            return null;
        }
    }

    public int          getRoomNumber()    { return roomNumber; }
    public Type         getType()          { return type; }
    public int          getFloor()         { return floor; }
    public int          getMaxGuests()     { return maxGuests; }
    public double       getPricePerNight() { return pricePerNight; }
    public Status       getStatus()        { return status; }
    public String       getView()          { return view; }
    public List<String> getAmenities()     { return amenities; }
    public String       getDescription()   { return description; }
    public boolean      isAvailable()      { return status == Status.AVAILABLE; }

    public void setStatus(Status status)   { this.status = status; }

    @Override
    public String toString() {
        return String.format("Room %d | %s | Floor %d | Rs.%.0f/night | %s",
            roomNumber, type.label, floor, pricePerNight, status.name());
    }
}