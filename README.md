# 🏨 The Grand Horizon — Hotel Reservation System

> **CodeAlpha Java Internship — Task 4**
> A full-featured hotel reservation desktop application built with Java Swing.
> Search rooms, make bookings, simulate payments, and manage reservations — all in a clean, professional GUI.

---

## 💡 About This Project

The Grand Horizon is a hotel reservation system that works like a real booking platform. You can browse 30 rooms across 8 floors, filter by your dates and budget, book a room with guest details, pay using card or UPI, and get a full booking confirmation — all from a desktop window built entirely in Java.

I built this as Task 4 of my CodeAlpha Java internship. The goal was to practice real-world OOP design, GUI development with Swing, and File I/O for data persistence. I wanted it to feel like an actual product — not just a college project — so I spent extra time on the UI design, business logic like GST calculation and refund policies, and making the data persist across sessions.

---

## ✨ Features

### 🔍 Search & Book
- Filter rooms by **type, check-in/check-out dates, number of guests, and max price**
- Smart **date conflict detection** — the same room can never be double-booked
- Each room card shows amenities, floor, view, price per night, and total cost
- **"Book Now"** button takes you straight to the guest form

### 🏷️ Room Categories
| Type | Price/Night | Capacity | Highlights |
|---|---|---|---|
| Standard | ₹1,499 | 2 guests | WiFi, AC, TV, Room Service |
| Deluxe | ₹2,999 | 3 guests | King Bed, Bathtub, Mini Bar, Sea View |
| Suite | ₹5,999 | 4 guests | Jacuzzi, Butler Service, Free Breakfast |
| Presidential | ₹12,999 | 6 guests | Private Pool, Personal Chef, Limousine |

### 💳 Payment Simulation
- Supports **Credit Card, Debit Card, UPI, and Net Banking**
- Card validation — checks 16-digit number, MM/YY expiry, and CVV
- UPI ID format validation
- Runs on a **background thread** so the UI stays smooth during processing
- Every successful payment gets a unique **Transaction ID**

### 📋 Booking Management
- View all reservations with **status badges** (Confirmed / Cancelled)
- Full booking receipt showing guest info, room details, GST breakdown, transaction ID
- **Cancel any booking** by entering the Booking ID
- Smart **refund policy**: 80% refund (2+ days before) / 50% (1 day before) / No refund (same day)

### 💰 Pricing & Tax
- **18% GST** automatically calculated on every booking
- Clean price breakdown — base amount + tax = total
- Revenue tracker on the dashboard

### 💾 Data Persistence
- All bookings saved to `data/bookings.txt`
- Room statuses saved to `data/rooms.txt`
- Everything reloads automatically when you restart the app — nothing is lost

---

## 🗂️ Project Structure

```
CodeAlpha_HotelReservationSystem/
│
├── src/
│   ├── Main.java                ← Entry point, launches the GUI
│   ├── HotelGUI.java            ← Main window, header bar, sidebar navigation
│   ├── BookingPanel.java        ← Search, booking form, payment, confirmation screens
│   ├── ManagementPanel.java     ← Dashboard, all rooms, reservations, cancel, about
│   ├── UITheme.java             ← All colors, fonts and borders in one place
│   ├── Hotel.java               ← Central controller — all business logic
│   ├── Room.java                ← Room model (type, amenities, pricing, status)
│   ├── Guest.java               ← Guest model (name, contact, ID details)
│   ├── Reservation.java         ← Booking model (dates, payment, refund logic)
│   ├── PaymentProcessor.java    ← Simulates card, UPI and net banking payments
│   ├── SearchEngine.java        ← Filters rooms by type, dates, guests, price
│   └── FileManager.java         ← Saves and loads all data using File I/O
│
├── data/
│   ├── rooms.txt                ← Auto-saved room availability statuses
│   └── bookings.txt             ← Auto-saved reservation records
│
├── out/                         ← Compiled .class files
│
└── README.md
```

---

## 🏗️ OOP Design

| Class | Responsibility |
|---|---|
| `Room` | Stores room number, type, floor, amenities, price and status |
| `Guest` | Stores guest name, email, phone and ID verification details |
| `Reservation` | Complete booking record — links guest + room + dates + payment |
| `PaymentProcessor` | Validates and simulates card, UPI and net banking transactions |
| `SearchEngine` | Filters available rooms based on user criteria and checks date conflicts |
| `Hotel` | Central controller — creates bookings, cancellations, stats and queries |
| `FileManager` | Reads and writes all data to `data/` folder using BufferedWriter/Reader |
| `UITheme` | Design system — all colors, fonts and border styles in one place |
| `BookingPanel` | Swing panel handling search, guest form, payment and confirmation |
| `ManagementPanel` | Swing panel handling dashboard, room grid, bookings list and cancel |
| `HotelGUI` | Main JFrame — builds the window, header and sidebar navigation |
| `Main` | Sets rendering hints and launches the GUI on the Event Dispatch Thread |

---

## 🖥️ Screens

**1. Dashboard**
Shows total rooms, available count, occupied count and total revenue at a glance. Below that, each room type has an availability progress bar. Quick action buttons let you jump to any screen in one click.

**2. Search Rooms**
Form with room type dropdown, date fields, guest count and max price filter. Results appear as room cards with amenities preview, capacity and a Book Now button.

**3. Booking Form**
Shows a summary of the selected room and price breakdown. Guest fills in name, email, phone, ID type and payment method. Validation happens before moving to payment.

**4. Payment Screen**
Displays the total amount to pay. Input fields change based on payment method — card fields for credit/debit, UPI ID field for UPI, bank name for net banking. Processing runs in the background.

**5. Booking Confirmation**
Big green tick with the booking ID. Full receipt showing every detail — guest info, room, dates, GST, total paid and transaction ID.

**6. My Bookings**
All reservations listed newest first, each showing status badge, room info, guest name, dates and amount paid.

**7. Cancel Booking**
Enter a Booking ID to look up the reservation. Shows refund amount based on policy. One click to confirm cancellation.

---

## 🚀 How to Run

### Requirements
- Java 11 or higher
- No external libraries — pure Java only

### Step 1 — Create folders
```cmd
mkdir out
mkdir data
```

### Step 2 — Compile all source files
```cmd
javac -d out src\Room.java src\Guest.java src\Reservation.java src\PaymentProcessor.java src\SearchEngine.java src\FileManager.java src\Hotel.java src\UITheme.java src\BookingPanel.java src\ManagementPanel.java src\HotelGUI.java src\Main.java
```

### Step 3 — Run
```cmd
java -cp out Main
```

The hotel window opens immediately. Start from the Dashboard!

---

## 🎮 Quick Start Guide

```
1. Open the app  →  Dashboard loads with hotel stats

2. Click "Search Rooms"
   →  Select room type, enter dates, choose guests
   →  Click SEARCH AVAILABLE ROOMS

3. Pick a room  →  Click "Book Now"
   →  Fill in your name, email, phone, ID details
   →  Choose payment method  →  Click PROCEED TO PAYMENT

4. On payment screen
   →  Enter card number / UPI ID / bank name
   →  Click PAY — wait 1-2 seconds for processing
   →  Booking Confirmation appears with your Booking ID

5. To cancel  →  Click "Cancel Booking" in sidebar
   →  Enter your Booking ID  →  See refund amount
   →  Click Confirm Cancellation
```

---

## 📊 Business Logic

**GST Calculation**
```
Base Amount  =  Room Price × Number of Nights
GST (18%)    =  Base Amount × 0.18
Total        =  Base Amount + GST
```

**Refund Policy**
```
Cancel 2+ days before check-in  →  80% refund
Cancel 1 day before check-in    →  50% refund
Cancel on check-in day          →  No refund
```

**Date Conflict Check**
```
A room is blocked if:
  new check-in  <  existing check-out
  AND
  new check-out  >  existing check-in
```

---

## 🔮 Future Improvements

- [ ] Add room photos using actual image rendering
- [ ] Email confirmation sent to guest after booking
- [ ] Admin login to manage room prices and availability
- [ ] Filter bookings by date range or guest name
- [ ] Print booking receipt as PDF
- [ ] Add check-in and check-out workflow
- [ ] Loyalty points system for repeat guests

---

## 🛠️ Technologies Used

- **Java 11+** — core language
- **Java Swing** — GUI framework (JFrame, JPanel, custom painted components)
- **OOP** — 12 classes with clean separation of responsibilities
- **File I/O** — `BufferedWriter` and `BufferedReader` for data persistence
- **java.time** — `LocalDate` and `ChronoUnit` for date handling
- **Multithreading** — background thread for payment processing so UI stays responsive
- **Collections** — `ArrayList`, `List`, streams for room filtering

---

## 👨‍💻 About This Project

Built as part of the **CodeAlpha Java Development Internship — Task 4**.

The most interesting part of building this was the search and booking logic. Making sure rooms can't be double-booked required comparing date ranges properly — a new booking overlaps an existing one if the new check-in is before the existing check-out AND the new check-out is after the existing check-in. Getting that logic right took some careful thinking.

The GUI was the most time-consuming part. Java Swing doesn't have modern-looking components out of the box, so I used custom `paintComponent()` overrides to draw rounded cards, colored buttons, and progress bars. The result is a clean, professional interface that looks nothing like a typical Swing app.

Overall this project taught me a lot about real-world application design — separating concerns across classes, handling edge cases in business logic, and making a user interface that actually feels good to use.

---

## 📄 License

Open source — free to use, learn from, and build upon.

---

*Made with ☕ Java | CodeAlpha Internship 2025*