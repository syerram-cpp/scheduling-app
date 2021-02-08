package util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLException;

public class DBConnection
{
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String ipAddress = "//3.227.166.251/U07ohv";
    private static final String jdbcURL = protocol + vendorName + ipAddress;

    private static Connection conn = null;

    private static final String username = "U07ohv";
    private static final String password = "53689087605";

    public static Connection startConnection()
    {
        try {
            conn = DriverManager.getConnection(jdbcURL, username, password);
            System.out.println("Connection Started");
        }
        catch(Exception e) {
            System.out.println("Connection Failed");
        }
        return conn;
    }

    public static void closeConnection()
    {
        try {
            conn.close();
            System.out.println("Connection closed");
        }
        catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}
