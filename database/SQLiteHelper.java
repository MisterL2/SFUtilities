package misterl2.sfutilities.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;

import java.sql.*;

public class SQLiteHelper implements DBHelper {
    private Logger logger;
    private ComboPooledDataSource cpds;

    public SQLiteHelper(Logger logger) {
        this.logger=logger;
        cpds = new ComboPooledDataSource();

        cpds.setJdbcUrl("jdbc:sqlite:SFUtil/blocklogs.db");

    }

    //The dimensionId is the first character of the dimension, so O for OVERWORLD. This saves storage space.
    //changeType refers to the event action, so for Block.BREAK it would be "BREAK", for Block.PLACE it would be "PLACE", etc
    @Override
    public void logBlockBreak(String playerName, String block, int x, int y, int z, long unixTime, char dimensionId) {
        logger.info("Logging block!");
        logger.info("BREAK" + " " + playerName + " " + block + " " + x + " " + y + " "  + z + " " + unixTime + " " + dimensionId);
        try (Connection conn = cpds.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO BREAK VALUES (?,?,?,?,?,?,?)");
            pstmt.setString(1,playerName);
            pstmt.setString(2,block);
            pstmt.setInt(3,x);
            pstmt.setInt(4,y);
            pstmt.setInt(5,z);
            pstmt.setLong(6,unixTime);
            pstmt.setString(7,String.valueOf(dimensionId));
            logger.info(pstmt.toString());
            pstmt.execute();
            //Database is in auto-commit mode, so no need to commit

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void setupDatabase() {
        String url = "jdbc:sqlite:SFUtil/blocklogs.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                Statement statement = conn.createStatement();
                String sql = "CREATE TABLE break (\n" +
                        "player TEXT NOT NULL,\n" +
                        "block TEXT NOT NULL,\n" +
                        "x INT NOT NULL,\n" +
                        "y INT NOT NULL,\n" +
                        "z INT NOT NULL,\n" +
                        "unixtime INT NOT NULL,\n" +
                        "dimension CHAR(1) NOT NULL\n" +
                        ");";
                statement.execute(sql);
                statement.close();
//                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (SQLException e) {
            logger.warn("DB could not be set up! " + e.getMessage());
        }
    }




}
