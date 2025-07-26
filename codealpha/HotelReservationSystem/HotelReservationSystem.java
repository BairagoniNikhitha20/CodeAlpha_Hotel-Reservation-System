import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class HotelReservationSystem extends JFrame {
    static class Room implements Serializable {
        private int roomNo;
        private String category;
        private double price;
        private boolean isBooked;

        public Room(int roomNo, String category, double price) {
            this.roomNo = roomNo;
            this.category = category;
            this.price = price;
            this.isBooked = false;
        }

        public int getRoomNo() { return roomNo; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public boolean isBooked() { return isBooked; }
        public void setBooked(boolean booked) { this.isBooked = booked; }

        @Override
        public String toString() {
            return "Room " + roomNo + " (" + category + "), $" + price + (isBooked ? " [Booked]" : " [Available]");
        }
    }

    static class Reservation implements Serializable {
        private static int counter = 1;
        private int reservationId;
        private String customerName;
        private Room room;
        private String checkIn;
        private String checkOut;
        private boolean paymentDone;

        public Reservation(String customerName, Room room, String checkIn, String checkOut) {
            this.reservationId = counter++;
            this.customerName = customerName;
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.paymentDone = false;
        }

        public int getReservationId() { return reservationId; }
        public String getCustomerName() { return customerName; }
        public Room getRoom() { return room; }
        public String getCheckIn() { return checkIn; }
        public String getCheckOut() { return checkOut; }
        public boolean isPaymentDone() { return paymentDone; }
        public void setPaymentDone(boolean paymentDone) { this.paymentDone = paymentDone; }

        @Override
        public String toString() {
            return "Res#" + reservationId
                    + " | " + customerName
                    + " | Room " + room.getRoomNo() + " (" + room.getCategory() + ")"
                    + " | " + checkIn + " to " + checkOut
                    + (paymentDone ? " | Paid" : " | Not Paid");
        }
    }

    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Reservation> reservations = new ArrayList<>();
    private final String ROOMS_FILE = "rooms.dat";
    private final String RESERVATIONS_FILE = "reservations.dat";

    private JTextArea outputArea;
    private JComboBox<String> categoryBox;
    private JTextField nameField, roomField, checkInField, checkOutField;
    private JTextField cancelIdField, payIdField;

    public HotelReservationSystem() {
        setTitle("Hotel Reservation System");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel();
        categoryBox = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite"});
        JButton searchBtn = new JButton("Search Rooms");
        topPanel.add(new JLabel("Category:"));
        topPanel.add(categoryBox);
        topPanel.add(searchBtn);

        outputArea = new JTextArea(12, 58);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel bookPanel = new JPanel(new GridLayout(7,2,5,2));
        nameField = new JTextField();
        roomField = new JTextField();
        checkInField = new JTextField("2025-07-01");
        checkOutField = new JTextField("2025-07-02");
        JButton bookBtn = new JButton("Book Room");
        payIdField = new JTextField();
        JButton payBtn = new JButton("Simulate Payment");
        bookPanel.setBorder(BorderFactory.createTitledBorder("Book a Room"));
        bookPanel.add(new JLabel("Name:")); bookPanel.add(nameField);
        bookPanel.add(new JLabel("Room #:")); bookPanel.add(roomField);
        bookPanel.add(new JLabel("Check-in (yyyy-mm-dd):")); bookPanel.add(checkInField);
        bookPanel.add(new JLabel("Check-out (yyyy-mm-dd):")); bookPanel.add(checkOutField);
        bookPanel.add(bookBtn); bookPanel.add(new JLabel(""));
        bookPanel.add(new JLabel("Reservation # for Payment:")); bookPanel.add(payIdField);
        bookPanel.add(payBtn); bookPanel.add(new JLabel(""));

        JPanel bottomPanel = new JPanel();
        JButton viewBtn = new JButton("View Reservations");
        cancelIdField = new JTextField(6);
        JButton cancelBtn = new JButton("Cancel Reservation");
        JButton saveBtn = new JButton("Save Data");
        JButton loadBtn = new JButton("Load Data");
        bottomPanel.add(viewBtn);
        bottomPanel.add(new JLabel("Reservation #:"));
        bottomPanel.add(cancelIdField);
        bottomPanel.add(cancelBtn);
        bottomPanel.add(saveBtn);
        bottomPanel.add(loadBtn);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bookPanel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData();

        searchBtn.addActionListener(e -> {
            String cat = (String)categoryBox.getSelectedItem();
            outputArea.setText("Available " + cat + " rooms:\n");
            boolean found = false;
            for (Room r : rooms) {
                if (!r.isBooked() && r.getCategory().equalsIgnoreCase(cat)) {
                    outputArea.append(r + "\n");
                    found = true;
                }
            }
            if (!found) outputArea.append("No rooms available in this category.\n");
        });

        bookBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String roomNoStr = roomField.getText().trim();
            String in = checkInField.getText().trim(), out = checkOutField.getText().trim();
            if (name.isEmpty() || roomNoStr.isEmpty() || in.isEmpty() || out.isEmpty()) {
                outputArea.setText("Please fill all fields.");
                return;
            }
            int roomNo;
            try { roomNo = Integer.parseInt(roomNoStr); }
            catch (Exception ex) { outputArea.setText("Room number must be a number."); return; }

            Room room = getRoomByNo(roomNo);
            if (room == null) {
                outputArea.setText("Room not found.");
                return;
            }
            if (room.isBooked()) {
                outputArea.setText("Room is already booked.");
                return;
            }

            Reservation res = new Reservation(name, room, in, out);
            room.setBooked(true);
            reservations.add(res);
            outputArea.setText("Reservation successful! Your reservation #: " + res.getReservationId());
        });

        payBtn.addActionListener(e -> {
            String idStr = payIdField.getText().trim();
            if (idStr.isEmpty()) {
                outputArea.setText("Enter reservation number.");
                return;
            }
            int id;
            try { id = Integer.parseInt(idStr); }
            catch (Exception ex) { outputArea.setText("Invalid reservation number."); return; }
            for (Reservation r : reservations) {
                if (r.getReservationId() == id) {
                    r.setPaymentDone(true);
                    outputArea.setText("Payment simulated! Reservation #" + id + " is now marked as paid.");
                    return;
                }
            }
            outputArea.setText("Reservation not found.");
        });

        viewBtn.addActionListener(e -> {
            outputArea.setText("All Reservations:\n");
            if (reservations.isEmpty()) outputArea.append("No reservations.\n");
            for (Reservation r : reservations) outputArea.append(r + "\n");
        });

        cancelBtn.addActionListener(e -> {
            String idStr = cancelIdField.getText().trim();
            if (idStr.isEmpty()) {
                outputArea.setText("Enter reservation number to cancel.");
                return;
            }
            int id;
            try { id = Integer.parseInt(idStr); }
            catch (Exception ex) { outputArea.setText("Invalid reservation number."); return; }
            for (int i = 0; i < reservations.size(); i++) {
                Reservation r = reservations.get(i);
                if (r.getReservationId() == id) {
                    r.getRoom().setBooked(false);
                    reservations.remove(i);
                    outputArea.setText("Reservation #" + id + " cancelled.");
                    return;
                }
            }
            outputArea.setText("Reservation not found.");
        });

        saveBtn.addActionListener(e -> {
            if (saveData()) outputArea.setText("Data saved!");
            else outputArea.setText("Error saving data.");
        });

        loadBtn.addActionListener(e -> {
            if (loadData()) outputArea.setText("Data loaded!");
            else outputArea.setText("Error loading data.");
        });
    }

    private Room getRoomByNo(int roomNo) {
        for (Room room : rooms) if (room.getRoomNo() == roomNo) return room;
        return null;
    }

    private void initDefaultRooms() {
        rooms.clear();
        rooms.add(new Room(101, "Standard", 100));
        rooms.add(new Room(102, "Standard", 100));
        rooms.add(new Room(201, "Deluxe", 150));
        rooms.add(new Room(202, "Deluxe", 150));
        rooms.add(new Room(301, "Suite", 300));
    }

    private boolean saveData() {
        try (ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE));
             ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(RESERVATIONS_FILE))) {
            oos1.writeObject(rooms);
            oos2.writeObject(reservations);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean loadData() {
        boolean loaded = false;
        File rf = new File(ROOMS_FILE), bf = new File(RESERVATIONS_FILE);
        try {
            if (rf.exists() && bf.exists()) {
                try (ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream(rf));
                     ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(bf))) {
                    rooms = (ArrayList<Room>) ois1.readObject();
                    reservations = (ArrayList<Reservation>) ois2.readObject();
                    int maxId = 1;
                    for (Reservation r : reservations)
                        if (r.getReservationId() >= maxId) maxId = r.getReservationId() + 1;
                    Reservation.counter = maxId;
                    loaded = true;
                }
            } else {
                initDefaultRooms();
                reservations.clear();
                loaded = true;
            }
        } catch (Exception e) {
            initDefaultRooms();
            reservations.clear();
            loaded = false;
        }
        return loaded;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReservationSystem().setVisible(true));
    }
}