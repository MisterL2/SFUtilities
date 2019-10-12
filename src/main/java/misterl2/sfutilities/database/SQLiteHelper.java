package misterl2.sfutilities.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import misterl2.sfutilities.util.TimeConverter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.sql.*;
import java.util.*;
import java.util.Date;

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
    public void logBlockBreak(String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId) {
        logger.info("Logging block!");
        logger.info("BREAK" + " " + playerUUID + " " + block + " " + x + " " + y + " "  + z + " " + unixTime + " " + dimensionId);
        logBlockChange("BREAK",playerUUID,block,x,y,z,unixTime,dimensionId);
    }

    @Override
    public void logBlockPlace(String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId) {
        logger.info("Logging block!");
        logger.info("BREAK" + " " + playerUUID + " " + block + " " + x + " " + y + " "  + z + " " + unixTime + " " + dimensionId);
        logBlockChange("PLACE",playerUUID,block,x,y,z,unixTime,dimensionId);
    }

    @Override
    public void logChestInteraction(String playerUUID, char action, String block, int amount, int x, int y, int z, long unixTime, char dimensionId) {
        try (Connection conn = cpds.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO chest VALUES (?,?,?,?,?,?,?,?,?)");
            pstmt.setString(1,playerUUID);
            pstmt.setString(2,String.valueOf(action));
            pstmt.setString(3,block);
            pstmt.setInt(4,amount);
            pstmt.setInt(5,x);
            pstmt.setInt(6,y);
            pstmt.setInt(7,z);
            pstmt.setLong(8,unixTime);
            pstmt.setString(9,String.valueOf(dimensionId));

            logger.info(pstmt.toString());
            pstmt.execute();
            //Database is in auto-commit mode, so no need to commit

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void logBlockChange(String tableName, String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId) {
        try (Connection conn = cpds.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?)");
            pstmt.setString(1,playerUUID);
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
    public List<String> getBlockBreakLog(int x, int y, int z, char dimension) {
        return getBlockChangedLog("break",x,y,z,dimension);
    }

    @Override
    public List<String> getBlockPlaceLog(int x, int y, int z, char dimension) {
        return getBlockChangedLog("place",x,y,z,dimension);
    }

    @Override
    public List<String> getChestLog(int x, int y, int z, char dimension) {
        return null;
    }

    private List<String> getBlockChangedLog(String tableName, int x, int y, int z, char dimension) {
        List<String> logs = new ArrayList<>();
        try (Connection conn = cpds.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT player, block, unixtime FROM " + tableName + " WHERE x=? and y=? and z=? and dimension=? ORDER BY unixtime DESC LIMIT 20");
            pstmt.setInt(1,x);
            pstmt.setInt(2,y);
            pstmt.setInt(3,z);
            pstmt.setString(4,String.valueOf(dimension));
            ResultSet resultSet = pstmt.executeQuery();

            long currentUnixtime = new Date().getTime() / 100L; //in 0.1s

            while(resultSet.next()) {
                String playerUUID = resultSet.getString("player");
                String block = resultSet.getString("block");
                long unixtime = resultSet.getLong("unixtime");

                long timeSince = currentUnixtime - unixtime; //In seconds
                if(timeSince<0) {
                    logger.warn("The timestamp of the blocklog is in the future!");
                    continue;
                }
                String timeSinceString = TimeConverter.secondsToTimeString(timeSince);

                UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
                Optional<User> maybePlayer = uss.get(UUID.fromString(playerUUID));
                String playerName = playerUUID; //Fallback to UUID if name cannot be resolved
                if(!maybePlayer.isPresent()) {
                    logger.warn("The player with UUID " + playerUUID + " cannot be found in playercache, but appears in the blocklogs! Using an outdated database?");
                } else {
                    playerName = maybePlayer.get().getName();
                }
                String logRow;
                if(tableName.toLowerCase().equals("break")) {
                    logRow = playerName + " broke \"" + block + "\" " + timeSinceString + " ago!";
                } else {
                    logRow = playerName + " placed \"" + block + "\" " + timeSinceString + " ago!";
                }
                logs.add(logRow);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        Collections.reverse(logs); //So that the newest ones are furthest down
        return logs;
    }


    @Override
    public void setupDatabase() {
        String url = "jdbc:sqlite:SFUtil/blocklogs.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                Statement statement = conn.createStatement();
                String createBreak = "CREATE TABLE break (\n" +
                        "player TEXT NOT NULL,\n" +
                        "block TEXT NOT NULL,\n" +
                        "x INT NOT NULL,\n" +
                        "y INT NOT NULL,\n" +
                        "z INT NOT NULL,\n" +
                        "unixtime INT NOT NULL,\n" +
                        "dimension CHAR(1) NOT NULL\n" +
                        ");";
                String createPlace =  "CREATE TABLE place (\n" +
                        "player TEXT NOT NULL,\n" +
                        "block TEXT NOT NULL,\n" +
                        "x INT NOT NULL,\n" +
                        "y INT NOT NULL,\n" +
                        "z INT NOT NULL,\n" +
                        "unixtime INT NOT NULL,\n" +
                        "dimension CHAR(1) NOT NULL\n" +
                        ");";
                String createChest =  "CREATE TABLE chest (\n" +
                        "player TEXT NOT NULL,\n" +
                        "action CHAR(1) NOT NULL,\n" +
                        "item TEXT NOT NULL,\n" +
                        "amount INT NOT NULL,\n" +
                        "x INT NOT NULL,\n" +
                        "y INT NOT NULL,\n" +
                        "z INT NOT NULL,\n" +
                        "unixtime INT NOT NULL,\n" +
                        "dimension CHAR(1) NOT NULL\n" +
                        ");";
                statement.execute(createBreak);
                statement.execute(createPlace);
                statement.execute(createChest);
                statement.close();
//                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (SQLException e) {
            logger.warn("DB could not be set up! " + e.getMessage());
        }
    }




}
