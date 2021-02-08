package model;

import util.DBConnection;
import util.DBQuery;

import java.sql.*;
import java.time.LocalDateTime;

public class City
{
    Connection conn = null;

    private Country country = null; //contains countryId and countryName
    private int cityId;
    private String cityName;
    private Timestamp createDate; //not used yet
    private String createdBy; // userName
    private Timestamp lastUpdate; //not used yet
    private String lastUpdateBy;

    // COUNTRY FIELDS
    private int countryId;
    private String countryName;

    //Creates a city if that city does not already exist
    public City(Connection conn, String countryName, String cityName, String createdBy) throws SQLException
    {
        this.country = new Country(conn, countryName, createdBy);
        this.cityName = cityName;

        String getCityString =
                "SELECT cityId FROM country co JOIN city ci ON (co.countryId=ci.countryId AND co.countryId=?) WHERE city=?";
        DBQuery.setPreparedStatement(conn, getCityString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setInt(1, getCountryId());
        ps.setString(2, cityName);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        if(rs.next())
        {
            this.cityId = (int)rs.getLong("cityId");
        }

        else
        {
            this.createdBy = createdBy;
            this.lastUpdateBy = createdBy;

            this.conn = conn;

            String addCityString =
                    "INSERT INTO city(city, countryId, createDate, createdBy, lastUpdateBy)" +
                            "VALUES(?, ?, now(), ?, ?)";
            DBQuery.setPreparedStatement(conn, addCityString);
            PreparedStatement ps1 = DBQuery.getPreparedStatement();
            ps1.setString(1, cityName);
            ps1.setInt(2, getCountryId());
            ps1.setString(3, createdBy);
            ps1.setString(4, lastUpdateBy);
            ps1.execute();
            ResultSet rs1 = ps1.getGeneratedKeys();
            if (rs1.next())
            {
                this.cityId = (int) rs1.getLong(1);
            }
        }
    }


    public int getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    //GET COUNTRY FIELDS
    public int getCountryId() { return country.getCountryId(); }
    public String getCountryName() { return country.getCountryName(); }
}

