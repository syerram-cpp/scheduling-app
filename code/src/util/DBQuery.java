package util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class DBQuery
{
    private static PreparedStatement statement;

    public static void setPreparedStatement(Connection conn, String sql) throws SQLException
    {
        statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    public static PreparedStatement getPreparedStatement()
    {
        return statement;
    }
}
