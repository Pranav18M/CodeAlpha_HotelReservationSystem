// FileManager.java
// Handles all file I/O — saves and loads rooms and reservations

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String ROOMS_FILE    = "data/rooms.txt";
    private static final String BOOKINGS_FILE = "data/bookings.txt";
    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ── ROOMS ─────────────────────────────────────────────────────────────

    public static void saveRooms(List<Room> rooms) {
        new File("data").mkdirs();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            for (Room room : rooms) {
                w.write(room.toFileString());
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not save room data.");
        }
    }

    public static List<String> loadRoomStatuses() {
        List<String> lines = new ArrayList<>();
        File file = new File(ROOMS_FILE);
        if (!file.exists()) return lines;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null)
                if (!line.trim().isEmpty()) lines.add(line.trim());
        } catch (IOException e) {
            System.err.println("Warning: Could not load room data.");
        }
        return lines;
    }

    // ── RESERVATIONS ──────────────────────────────────────────────────────

    public static void saveReservations(List<Reservation> reservations) {
        new File("data").mkdirs();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Reservation res : reservations) {
                w.write(res.toFileString());
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not save booking data.");
        }
    }

    public static List<Reservation> loadReservations(List<Room> rooms) {
        List<Reservation> reservations = new ArrayList<>();
        File file = new File(BOOKINGS_FILE);
        if (!file.exists() || file.length() == 0) return reservations;

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Format: bookingId~guestData~roomNum~checkIn~checkOut~
                //         guests~paymentMethod~specialReq~status~total~bookingDate
                String[] p = line.split("~", 11);
                if (p.length < 11) continue;

                try {
                    String   bookingId  = p[0];
                    Guest    guest      = Guest.fromFileString(p[1]);
                    int      roomNum    = Integer.parseInt(p[2]);
                    LocalDate checkIn   = LocalDate.parse(p[3], DATE_FMT);
                    LocalDate checkOut  = LocalDate.parse(p[4], DATE_FMT);
                    int      numGuests  = Integer.parseInt(p[5]);
                    Reservation.PaymentMethod method =
                        Reservation.PaymentMethod.valueOf(p[6]);
                    String   specialReq = p[7].equals("NONE") ? "" : p[7];
                    Reservation.Status status =
                        Reservation.Status.valueOf(p[8]);
                    double   total      = Double.parseDouble(p[9]);
                    String   bookDate   = p[10];

                    Room room = rooms.stream()
                        .filter(rm -> rm.getRoomNumber() == roomNum)
                        .findFirst().orElse(null);

                    if (room != null && guest != null) {
                        reservations.add(new Reservation(
                            bookingId, guest, room, checkIn, checkOut,
                            numGuests, method, specialReq,
                            status, total, bookDate));
                    }
                } catch (Exception ex) {
                    // Skip malformed lines silently
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load bookings.");
        }
        return reservations;
    }
}