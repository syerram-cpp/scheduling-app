package model;

import util.DBConnection;
import util.DBQuery;

import java.sql.*;
import java.time.LocalDateTime;

public class Address
{
    City city = null; // contains cityId, cityName, countryId, countryName
    int addressId;
    String address1;
    String address2;
    String postalCode;
    String phone;
    LocalDateTime createDate; //not used yet
    String createdBy; // userName
    Timestamp lastUpdate; //not used yet
    String lastUpdateBy;

    // CITY FIELDS
    private int cityId;
    private String cityName;

    // COUNTRY FIELDS
    private int countryId;
    private String countryName;

    public Address(Connection conn, String countryName, String cityName, String address1, String address2, String postalCode, String phone, String createdBy) throws SQLException {
        this.city = new City(conn, countryName, cityName, createdBy);
        this.address1 = address1;
        this.address2 = address2;
        this.postalCode = postalCode;
        this.phone = phone;
        this.createdBy = createdBy;
        this.lastUpdateBy = createdBy;

        String getAddressString =
                "SELECT addressId FROM city ci JOIN address a ON (a.cityId=ci.cityId AND ci.cityId=?) " +
                        "WHERE address=? AND postalCode=? AND phone=?";
        DBQuery.setPreparedStatement(conn, getAddressString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setInt(1, city.getCityId());
        ps.setString(2, address1);
        ps.setString(3, postalCode);
        ps.setString(4, phone);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        if(rs.next())
        { this.addressId = (int)rs.getLong("addressId");}

        else
        {
            String addAddressString =
                    "INSERT INTO address(address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdateBy)" +
                            "VALUES(?, ?, ?, ?, ?, now(), ?, ?)";
            DBQuery.setPreparedStatement(conn, addAddressString);
            PreparedStatement ps1 = DBQuery.getPreparedStatement();
            ps1.setString(1, address1);
            ps1.setString(2, address2);
            ps1.setInt(3, city.getCityId());
            ps1.setString(4, postalCode);
            ps1.setString(5, phone);
            ps1.setString(6, createdBy);
            ps1.setString(7, lastUpdateBy);
            ps1.execute();
            ResultSet rs1 = ps1.getGeneratedKeys();
            if (rs1.next()) {
                this.addressId = (int) rs1.getLong(1); }
        }
    }

    public int getAddressId() { return addressId; }
    public String getAddress1() { return address1; }
    public String getAddress2() { return address2; }
    public String getPostalCode() { return postalCode; }
    public String getPhone() { return phone; }
    //GET CITY FIELDS
    public int getCityId() { return city.getCityId(); }
    public String getCityName() { return city.getCityName(); }
    //GET COUNTRY FIELDS
    public int getCountryId() { return city.getCountryId(); }
    public String getCountryName() { return city.getCountryName(); }

    public void setAddress1(String address1) { this.address1=address1;}
    public void setPhone(String phone) { this.phone=phone;}


}

