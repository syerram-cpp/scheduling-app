package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.DBConnection;
import util.DBQuery;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class Customer
{
    private Address address = null;
    private int customerId;
    private String customerName;
    private int active = 0;
    private LocalDateTime createDate; //not used yet
    private String createdBy;
    private Timestamp lastUpdate; //not used yet
    private String lastUpdateBy;
    // ADDRESS FIELDS
    int addressId;
    String address1;
    String address2;
    String postalCode;
    String phone;
    // CITY FIELDS
    private int cityId;
    private String cityName;
    // COUNTRY FIELDS
    private int countryId;
    private String countryName;

    // Creates customer locally and adds the customer to the database
    public Customer(Connection conn, String countryName, String cityName, String address1, String address2, String postalCode, String phone, String createdBy, String customerName, int active) throws SQLException {
        this.address = new Address(conn, countryName, cityName, address1, address2, postalCode, phone, createdBy);
        this.addressId = address.getAddressId();
        this.customerName = customerName;
        this.active = active;
        this.createdBy = createdBy;
        this.lastUpdateBy = createdBy;

        String addCustomerString =
                "INSERT INTO customer(customerName, addressId, active, createDate, createdBy, lastUpdateBy)" +
                        "VALUES(?, ?, ?, now(), ?, ?)";
        DBQuery.setPreparedStatement(conn, addCustomerString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setString(1, customerName);
        ps.setInt(2, addressId);
        ps.setInt(3, active);
        ps.setString(4, createdBy);
        ps.setString(5, lastUpdateBy);
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next()) { this.customerId = (int) rs.getLong(1); }
    }

    //Creates customer without inserting it into the database
    public Customer(Connection conn, int customerId, String countryName, String cityName, String address1, String address2, String postalCode, String phone, String createdBy, String customerName) throws IOException, SQLException {
        this.address = new Address(conn, countryName, cityName, address1, address2, postalCode, phone, createdBy);
        this.customerId = customerId;
        this.addressId = address.getAddressId();
        this.cityId = address.getCityId();
        this.countryId = address.getCountryId();

        this.countryName = countryName;
        this.cityName = address.getCityName();
        this.address1 = address.getAddress1();
        this.address2 = address.getAddress2();
        this.postalCode = address.getPostalCode();
        this.phone = address.getPhone();
        this.createdBy = createdBy;
        this.customerName = customerName;
    }

    //ALL GETTERS
    public int getCustomerId() { return customerId;}
    public String getCustomerName() { return customerName;}
    public int getActive() { return active;}
    //GET ADDRESS FIELDS
    public int getAddressId() { return address.getAddressId();}
    public String getAddress1() { return address.getAddress1();
    }
    public String getAddress2() { return address.getAddress2();}
    public String getPostalCode() { return address.getPostalCode();}
    public String getPhone() { return address.getPhone();
    }
    //GET CITY FIELDS
    public int getCityId() { return address.getCityId();}
    public String getCityName() { return address.getCityName();}
    //GET COUNTRY FIELDS
    public int getCountryId() { return address.getCountryId();}
    public String getCountryName() { return address.getCountryName();}

    public void setCustomerName(Connection conn, String customerName) throws SQLException {

        String updateCustomerName = "UPDATE customer SET customerName = ? " +
                "WHERE customerId = ?";
        DBQuery.setPreparedStatement(conn, updateCustomerName);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setString(1, customerName);
        ps.setInt(2, getCustomerId());
        ps.execute();

        this.customerName = customerName;

    }

    public void setAddress1(Connection conn, String address1) throws SQLException {
        String updateCustomerAddress = "UPDATE address SET address.address=? " +
                "WHERE address.addressId=?";
        DBQuery.setPreparedStatement(conn, updateCustomerAddress);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setString(1, address1);
        ps.setInt(2, address.getAddressId());
        ps.execute();

        this.address.setAddress1(address1);
        this.address1=address1;
    }

    public void setPhone(Connection conn, String phone) throws SQLException {
        String updateCustomerPhone = "UPDATE address SET address.phone = ? " +
                "WHERE address.addressId = ?";
        DBQuery.setPreparedStatement(conn, updateCustomerPhone);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setString(1, phone);
        ps.setInt(2, getAddressId());
        ps.execute();

        this.address.setPhone(phone);
        this.phone=phone;
    }

    public static ObservableList<Customer> getAllCustomers(Connection conn) throws SQLException, IOException {

        ObservableList<Customer> customerList = javafx.collections.FXCollections.observableArrayList();
        String getAllCustomersString = "SELECT c.customerId, co.country, ci.city, a.address, " +
                "a.address2, a.postalCode, a.phone, c.createdBy, c.customerName FROM " +
                "customer c JOIN address a ON c.addressId=a.addressId " +
                "JOIN city ci ON a.cityId=ci.cityId " +
                "JOIN country co ON ci.countryId=co.countryId";
        DBQuery.setPreparedStatement(conn, getAllCustomersString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();

        ResultSet rs = ps.getResultSet();
        while(rs.next())
        {
            customerList.add(new Customer(conn,
                    ((int)rs.getLong(1)),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getString(7),
                    rs.getString(8),
                    rs.getString(9)));
        }
        return customerList;
    }

    public void deleteCustomer(Connection conn) throws SQLException {

        String deleteCustomerString = "DELETE FROM customer WHERE customerId=?";
        DBQuery.setPreparedStatement(conn, deleteCustomerString);
        PreparedStatement ps1 = DBQuery.getPreparedStatement();
        ps1.setInt(1, getCustomerId());
        ps1.execute();

        String deleteAddressString = "DELETE FROM address WHERE addressId=?";
        DBQuery.setPreparedStatement(conn, deleteAddressString);
        PreparedStatement ps2 = DBQuery.getPreparedStatement();
        ps2.setInt(1, getAddressId());
        ps2.execute();

    }


}

