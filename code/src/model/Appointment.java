package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import util.DBConnection;
import util.DBQuery;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Appointment
{
    Connection conn = null;

    private int appointmentId;
    private String title = "";
    private String description = "";
    private String location = "";
    private String contact = "";
    private String type;
    private String url = "";
    private Timestamp start;
    private Timestamp end;
    private LocalDateTime createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdateBy;
    // CUSTOMER AND ITS FIELDS
    private Customer customer = null;
    private int customerId;
    private String customerName;
    // ADDRESS FIELDS
    String address1;
    String phone;
    //USER AND ITS FIELDS
    private User user = null;
    private int userId;
    private String userName;
    //FOR REPORT
    String monthName;
    int noOfTypes;
    static int initialNoOfUpcomingApp = 0;

    public Appointment(Connection conn, Customer customer, User user, String title, String description, String location, String contact, String url, String type, Timestamp start, Timestamp end) throws SQLException
    {
        this.customer = customer;
        this.user = user;
        this.customerId = customer.getCustomerId();
        this.customerName = customer.getCustomerName();
        this.userId = user.getUserId();
        this.userName = user.getUserName();

        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.url = url;

        this.type = type;
        this.start = start;
        this.end = end;
        this.createdBy = userName;
        this.lastUpdateBy = userName;

        String addAppointmentString =
                "INSERT INTO appointment(customerId, userId, title, description, " +
                        "location, contact, type, url, start, end, createDate, createdBy, lastUpdateBy)" +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?)";
        DBQuery.setPreparedStatement(conn, addAppointmentString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setInt(1, customerId);
        ps.setInt(2, userId);
        ps.setString(3, title);
        ps.setString(4, description);
        ps.setString(5, location);
        ps.setString(6, contact);
        ps.setString(7, type);
        ps.setString(8, url);
        ps.setTimestamp(9, start);
        ps.setTimestamp(10, end);
        ps.setString(11, createdBy);
        ps.setString(12, lastUpdateBy);
        ps.execute();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next())
        {
            this.appointmentId = (int) rs.getLong(1);
        }
    }

    // CREATE APPOINTMENT WITHOUT INSERTING INTO DATABASE
    // Used for Appointment.getAllAppointments()
    public Appointment(Customer customer, User user, int appointmentId, String type, Timestamp start, Timestamp end) throws SQLException {
        this.customer = customer;
        this.user = user;
        this.customerId = customer.getCustomerId();
        this.customerName = customer.getCustomerName();
        this.userId = user.getUserId();
        this.appointmentId = appointmentId;
        this.userName = user.getUserName();
        this.type = type;
        this.start = start;
        this.end = end;
    }

    // Used in the List class to create an observable list of appointments
    // that stores the number of appointment types by month
    public Appointment(String monthName, int noOfTypes)
    {
        this.monthName = monthName;
        this.noOfTypes = noOfTypes;
    }

    //USER GETTERS
    public int getUserId() { return userId; }
    public String getUserName() { return userName;}
    //APPOINTMENT GETTERS
    public int getAppointmentId() { return appointmentId;}
    public Customer getCustomer() { return customer;}
    public String getCustomerName() { return customerName;}
    public String getType() { return type;}
    public Timestamp getStart() { return start;}
    public Timestamp getEnd() { return end;}
    //CUSTOMER GETTERS
    public int getCustomerId() { return customer.getCustomerId();}
    //REPORT CONTROLLER GETTERS
    public String getMonthName() { return monthName;}
    public int getNoOfTypes() { return noOfTypes;}
    public static int getInitialNoOfUpcomingApp()
    {
        try { return initialNoOfUpcomingApp; }
        catch(Exception e) { }
        return 0;
    }

    //SETTERS
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerAddress(String address1) { this.address1 = address1;}
    public void setCustomerPhone(String phone) { this.phone = phone;}

    // Updates an appointment's customerId, type, start, and end in the database
    // Sets the local variable appointment's fields equal to the inputted fields.
    public void updateAppointment(Connection conn, Customer customer, String type, Timestamp start, Timestamp end) throws SQLException {
        int customerId = customer.getCustomerId();

        String updateAppointmentString = "UPDATE appointment a JOIN customer c ON (a.customerId=c.customerId) " +
                "SET a.customerId=?, a.type=?, a.start=?, a.end=? " +
                "WHERE appointmentId=?";
        DBQuery.setPreparedStatement(conn, updateAppointmentString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setInt(1, customerId);
        ps.setString(2, type);
        ps.setTimestamp(3, start);
        ps.setTimestamp(4, end);
        ps.setInt(5, appointmentId);
        ps.execute();

        this.customer = customer;
        this.customerName = customer.getCustomerName();
        this.type = type;
        this.start = start;
        this.end = end;
    }

    // Gets all appointments from the database and creates a local object appointment, populating its fields
    public static ObservableList<Appointment> getAllAppointments(Connection conn) throws SQLException, IOException {

        ObservableList<Appointment> appointmentList = javafx.collections.FXCollections.observableArrayList();
        LocalDateTime currentDateTime = LocalDateTime.now();
        int noOfUpcomingApp = 0;

        String getAllAppointmentsString = "SELECT u.userId, u.userName, u.password, co.country, ci.city, ad.address, ad.address2," +
                "ad.postalCode, ad.phone, c.createdBy, c.customerId, c.customerName, c.active, a.appointmentId, a.type, a.start, a.end " +
                "FROM user u JOIN appointment a ON (u.userId=a.userId)" +
                "JOIN customer c ON (a.customerId=c.customerId) " +
                "JOIN address ad ON (c.addressId=ad.addressId) " +
                "JOIN city ci ON (ad.cityId=ci.cityId) " +
                "JOIN country co ON (ci.countryId=co.countryId) ";
        DBQuery.setPreparedStatement(conn, getAllAppointmentsString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();

        ResultSet rs = ps.getResultSet();
        while(rs.next())
        {
            User user = new User((int)rs.getLong("userId"),
                    rs.getString("userName"),
                    rs.getString("password"));
            Customer customer = new Customer(conn,
                    (int)rs.getLong("customerId"),
                    rs.getString("country"),
                    rs.getString("city"),
                    rs.getString("address"),
                    rs.getString("address2"),
                    rs.getString("postalCode"),
                    rs.getString("phone"),
                    rs.getString("createdBy"),
                    rs.getString("customerName"));
            Timestamp timestamp = rs.getTimestamp("start");
            appointmentList.add(new Appointment(
                    customer,
                    user,
                    ((int)rs.getLong("appointmentId")),
                    rs.getString("type"),
                    timestamp,
                    rs.getTimestamp("end")));
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            if(dateTime.isAfter(currentDateTime))
            {   noOfUpcomingApp++;}
        }
        initialNoOfUpcomingApp = noOfUpcomingApp;
        return appointmentList;
    }

    // Deletes an appointment from the database
    public void deleteAppointment(Connection conn) throws SQLException {
        String deleteAppointmentString = "DELETE FROM appointment WHERE customerId=?";
        DBQuery.setPreparedStatement(conn, deleteAppointmentString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setInt(1, customer.getCustomerId());
        ps.execute();
    }

}

