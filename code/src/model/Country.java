package model;

import util.DBConnection;
import util.DBQuery;

import java.sql.*;

public class Country
{
    private Connection conn = null;

    private int countryId;
    private String countryName;
    private Timestamp createDate; //not used yet
    private String createdBy; // userName
    private Timestamp lastUpdate; //not used yet
    private String lastUpdateBy;

    // Creates new country in database if a country with that name doesn't already exist
    // Creates a local country object
    public Country(Connection conn, String countryName, String createdBy) throws SQLException {
        this.countryName = countryName;

        String getCountry =
                "SELECT countryId FROM country WHERE country=?";
        DBQuery.setPreparedStatement(conn, getCountry);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setString(1, countryName);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        if(rs.next())
        {
            this.countryId = (int)rs.getLong("countryId");
        }
        else
        {
            this.createdBy = createdBy;
            this.lastUpdateBy = createdBy;

            String addCountryString =
                    "INSERT INTO country(country, createDate, createdBy, lastUpdateBy) " +
                            "VALUES(?, now(), ?, ?);";
            DBQuery.setPreparedStatement(conn, addCountryString);
            PreparedStatement ps1 = DBQuery.getPreparedStatement();
            ps1.setString(1, countryName);
            ps1.setString(2, createdBy);
            ps1.setString(3, lastUpdateBy);
            ps1.execute();
            ResultSet rs1 = ps1.getGeneratedKeys();
            if (rs1.next())
            {
                this.countryId = (int) rs1.getLong(1);
            }
        }
    }

    public int getCountryId() { return countryId; }
    public String getCountryName() { return countryName; }
}

