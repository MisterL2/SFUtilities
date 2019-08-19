package misterl2.sfutilities.database;

import org.slf4j.Logger;

import java.sql.*;

public class SQLiteHelper implements DBHelper {
    private Logger logger;

    public SQLiteHelper(Logger logger) {
        this.logger=logger;
    }



    //The dimensionId is the first character of the dimension, so O for OVERWORLD. This saves storage space.
    //changeType refers to the event action, so for Block.BREAK it would be "BREAK", for Block.PLACE it would be "PLACE", etc
    @Override
    public void logBlock(String changeType, String playerName, String block, int x, int y, int z, long unixTime, char dimensionId) {

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
