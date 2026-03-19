// Guest.java
// Represents a hotel guest with personal and contact details

public class Guest {

    private String name;
    private String email;
    private String phone;
    private String idType;
    private String idNumber;
    private int    totalBookings;

    public Guest(String name, String email, String phone,
                 String idType, String idNumber) {
        this.name          = name;
        this.email         = email;
        this.phone         = phone;
        this.idType        = idType;
        this.idNumber      = idNumber;
        this.totalBookings = 0;
    }

    // File format: name|email|phone|idType|idNumber
    public String toFileString() {
        return name + "|" + email + "|" + phone + "|" + idType + "|" + idNumber;
    }

    public static Guest fromFileString(String line) {
        String[] p = line.split("\\|");
        if (p.length < 5) return null;
        return new Guest(p[0], p[1], p[2], p[3], p[4]);
    }

    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }
    public String getIdType()        { return idType; }
    public String getIdNumber()      { return idNumber; }
    public int    getTotalBookings() { return totalBookings; }

    public void incrementBookings()  { totalBookings++; }

    @Override
    public String toString() {
        return String.format("%s  |  %s  |  %s", name, email, phone);
    }
}