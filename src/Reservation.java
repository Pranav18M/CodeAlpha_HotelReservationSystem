// Reservation.java
// Represents a complete hotel booking
// Stores all details: guest, room, dates, payment, status

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Reservation {

    public enum Status        { CONFIRMED, CANCELLED, CHECKED_IN, CHECKED_OUT }
    public enum PaymentMethod { CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final double TAX_RATE = 0.18;  // 18% GST

    private String        bookingId;
    private Guest         guest;
    private Room          room;
    private LocalDate     checkIn;
    private LocalDate     checkOut;
    private int           guests;
    private Status        status;
    private PaymentMethod paymentMethod;
    private double        totalAmount;
    private double        taxAmount;
    private double        paidAmount;
    private String        bookingDate;
    private String        specialRequests;

    // Constructor for new bookings
    public Reservation(String bookingId, Guest guest, Room room,
                       LocalDate checkIn, LocalDate checkOut, int guests,
                       PaymentMethod paymentMethod, String specialRequests) {
        this.bookingId       = bookingId;
        this.guest           = guest;
        this.room            = room;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.guests          = guests;
        this.paymentMethod   = paymentMethod;
        this.specialRequests = specialRequests;
        this.status          = Status.CONFIRMED;
        this.bookingDate     = LocalDate.now().format(DATE_FMT);

        long   nights    = ChronoUnit.DAYS.between(checkIn, checkOut);
        double base      = room.getPricePerNight() * nights;
        this.taxAmount   = base * TAX_RATE;
        this.totalAmount = base + taxAmount;
        this.paidAmount  = totalAmount;
    }

    // Constructor for loading from file
    public Reservation(String bookingId, Guest guest, Room room,
                       LocalDate checkIn, LocalDate checkOut, int guests,
                       PaymentMethod paymentMethod, String specialRequests,
                       Status status, double totalAmount, String bookingDate) {
        this.bookingId       = bookingId;
        this.guest           = guest;
        this.room            = room;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.guests          = guests;
        this.paymentMethod   = paymentMethod;
        this.specialRequests = specialRequests;
        this.status          = status;
        this.totalAmount     = totalAmount;
        this.taxAmount       = totalAmount - (totalAmount / (1 + TAX_RATE));
        this.paidAmount      = totalAmount;
        this.bookingDate     = bookingDate;
    }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double getBaseAmount() {
        return totalAmount - taxAmount;
    }

    // Refund policy: 80% if 2+ days before, 50% if 1 day, 0% same day
    public double calculateRefund() {
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
        if (daysUntil >= 2) return paidAmount * 0.80;
        if (daysUntil >= 1) return paidAmount * 0.50;
        return 0;
    }

    // File save format
    public String toFileString() {
        return bookingId + "~" +
               guest.toFileString() + "~" +
               room.getRoomNumber() + "~" +
               checkIn.format(DATE_FMT) + "~" +
               checkOut.format(DATE_FMT) + "~" +
               guests + "~" +
               paymentMethod.name() + "~" +
               (specialRequests.isEmpty() ? "NONE" : specialRequests.replace("~", " ")) + "~" +
               status.name() + "~" +
               totalAmount + "~" +
               bookingDate;
    }

    public String        getBookingId()       { return bookingId; }
    public Guest         getGuest()           { return guest; }
    public Room          getRoom()            { return room; }
    public LocalDate     getCheckIn()         { return checkIn; }
    public LocalDate     getCheckOut()        { return checkOut; }
    public int           getGuests()          { return guests; }
    public Status        getStatus()          { return status; }
    public PaymentMethod getPaymentMethod()   { return paymentMethod; }
    public double        getTotalAmount()     { return totalAmount; }
    public double        getTaxAmount()       { return taxAmount; }
    public double        getPaidAmount()      { return paidAmount; }
    public String        getBookingDate()     { return bookingDate; }
    public String        getSpecialRequests() { return specialRequests; }

    public void setStatus(Status s) { this.status = s; }

    public String getCheckInFormatted()  { return checkIn.format(DATE_FMT); }
    public String getCheckOutFormatted() { return checkOut.format(DATE_FMT); }
}