package model;

import util.DBConnection;
import util.DBQuery;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User
{
    private Connection conn = null;

    private int userId;
    private String userName;
    private String password;
    private int active = 0;
    private LocalDateTime createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdateBy;

    // Creates local variable User
    public User(int userId, String userName, String password)
    {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }

    public int getUserId() { return userId;}
    public String getUserName() { return userName;}
    public String getPassword() { return password;}
    public int getActive() { return active;}

    // Checks to see if given start and end times overlap with an existing appointment
    // Return true if any overlap
    public boolean checkOverlappingTimes(Connection conn, int appointmentId, LocalDateTime ss, LocalDateTime ee) throws SQLException {
        this.conn = conn;

        String findAppointmentsString = "SELECT start, end FROM user u " +
                "JOIN appointment a ON (u.userId=a.userId AND u.userId=?) WHERE appointmentId!=?";
        DBQuery.setPreparedStatement(conn, findAppointmentsString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setInt(1, userId);
        ps.setInt(2, appointmentId);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        while(rs.next())
        {
            LocalDateTime s = rs.getTimestamp(1).toLocalDateTime();
            LocalDateTime e = rs.getTimestamp(2).toLocalDateTime();
            LocalDate date = s.toLocalDate();

            if(date.isEqual(ss.toLocalDate()))
            {
                if((ss.isAfter(s) && ss.isBefore(e)) ||
                        (ss.isBefore(s) && ee.isAfter(s)) ||
                        ss.isEqual(s))
                {
                    return true;
                }
            }
        }
        return false;
    }

}

