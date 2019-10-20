package misterl2.sfutilities.database;

import misterl2.sfutilities.database.datatypes.ChestLogRow;
import misterl2.sfutilities.database.datatypes.LocationDataClass;
import misterl2.sfutilities.database.datatypes.LogRow;
import misterl2.sfutilities.util.TimeConverter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.sql.*;
import java.util.*;

public class H2Helper extends DBHelper {

    public H2Helper(Logger logger, int logLimit, String path) {
        super(logger, logLimit, path);
    }

    //The dimensionId is the first character of the dimension, so O for OVERWORLD. This saves storage space.
    //changeType refers to the event action, so for Block.BREAK it would be "BREAK", for Block.PLACE it would be "PLACE", etc
    @Override
    public void logBlockBreak(LogRow logRow) {
//        logger.info("Logging block!");
//        logger.info("BREAK" + " " + playerUUID + " " + block + " " + x + " " + y + " "  + z + " " + unixTime + " " + dimension);
        logBlockChange("BREAK", logRow);
    }

    @Override
    public void logBlockPlace(LogRow logRow) {
//        logger.info("Logging block!");
//        logger.info("PLACE" + " " + playerUUID + " " + block + " " + x + " " + y + " "  + z + " " + unixTime + " " + dimension);
        logBlockChange("PLACE", logRow);
    }

    @Override
    public void logChestInteraction(ChestLogRow chestLogRow) {
//        System.out.println("Logging chest change!");
//        logger.info("CHESTINTERACT" + " " + playerUUID + " " + action + " " + block + " " + amount + " " + x + " " + y + " "  + z + " " + unixTime + " " + world.toString() + " " + dimension);
        try (Connection conn = getDataSource().getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO chest VALUES (?,?,?,?,?,?,?,?,?)");){
//            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO chest VALUES (?,?,?,?,?,?,?,?,?,?)");
            pstmt.setString(1,chestLogRow.getPlayerUUID().toString());
//            pstmt.setString(2,String.valueOf(chestLogRow.getAction()));
            pstmt.setString(2,chestLogRow.getBlockName());
            pstmt.setInt(3,chestLogRow.getAmount());
            pstmt.setInt(4,chestLogRow.getLocation().getX());
            pstmt.setInt(5,chestLogRow.getLocation().getY());
            pstmt.setInt(6,chestLogRow.getLocation().getZ());
            pstmt.setLong(7,chestLogRow.getUnixTimeSinceRelease());
            pstmt.setString(8,chestLogRow.getLocation().getWorldId().toString());
            pstmt.setString(9,String.valueOf(chestLogRow.getLocation().getDimensionId()));

            safeInsert(pstmt);
            //Database is in auto-commit mode, so no need to commit

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void logBlockChange(String tableName, LogRow logRow) {
        try (Connection conn = getDataSource().getConnection(); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?,?)");){
            //PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + tableName + " VALUES (?,?,?,?,?,?,?,?)");
            pstmt.setString(1,logRow.getPlayerUUID().toString());
            pstmt.setString(2,logRow.getBlockName());
            pstmt.setInt(3,logRow.getLocation().getX());
            pstmt.setInt(4,logRow.getLocation().getY());
            pstmt.setInt(5,logRow.getLocation().getZ());
            pstmt.setLong(6,logRow.getUnixTimeSinceRelease());
            pstmt.setString(7,logRow.getLocation().getWorldId().toString());
            pstmt.setString(8,String.valueOf(logRow.getLocation().getDimensionId()));

            safeInsert(pstmt);
            //Database is in auto-commit mode, so no need to commit

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<LogRow> getBlockBreakLog(LocationDataClass location) {
        return getBlockChangedLog("break", location);
    }

    @Override
    public List<LogRow> getBlockPlaceLog(LocationDataClass location) {
        return getBlockChangedLog("place", location);
    }

    @Override
    public List<ChestLogRow> getChestLog(LocationDataClass location) {
//        System.out.println("Getting chest LOG!!!");
//        System.out.println("x: " + x);
//        System.out.println("y: " + y);
//        System.out.println("z: " + z);
//        System.out.println("world: " + world);
//        System.out.println("dimension: " + dimension);
        List<ChestLogRow> logs = new ArrayList<>();
//        System.out.println(world.toString());
        try (Connection conn = getDataSource().getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT player, item, amount, unixtime FROM chest WHERE x=? and y=? and z=? and world=? and dimension=? ORDER BY unixtime DESC LIMIT " + getLogLimit());) {
            //PreparedStatement pstmt = conn.prepareStatement("SELECT player, action, item, amount, unixtime FROM chest WHERE x=? and y=? and z=? and world=? and dimension=? ORDER BY unixtime DESC LIMIT " + getLogLimit());
            pstmt.setInt(1,location.getX());
            pstmt.setInt(2,location.getY());
            pstmt.setInt(3,location.getZ());
            pstmt.setString(4,location.getWorldId().toString());
            pstmt.setString(5,String.valueOf(location.getDimensionId()));
            ResultSet resultSet = pstmt.executeQuery();
            long currentUnixtime = TimeConverter.getUnixTimeSinceRelease();

            while(resultSet.next()) {
//                System.out.println("LOGROW WIRD GELESEN BRUDA");
                String playerUUIDString = resultSet.getString("player");

                String blockName = resultSet.getString("item");
                int amount = resultSet.getInt("amount");
                long unixtime = resultSet.getLong("unixtime");

                System.out.println(blockName);
                System.out.println(amount);
                System.out.println(unixtime);

                long timeSince = currentUnixtime - unixtime; //In seconds
                if(timeSince<0) {
                    logger.warn("The timestamp of the blocklog is in the future!");
                    continue;
                }

                UUID playerUUID = UUID.fromString(playerUUIDString);
                ChestLogRow chestLogRow = new ChestLogRow(blockName, amount, timeSince, playerUUID, location);

                setPlayerName(chestLogRow, playerUUID);

                logs.add(chestLogRow);
            }
            resultSet.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
//        System.out.println("Log rows: " + logs.size());
        return logs; //Unsorted! But contains the timeSince for sorting
    }


    private List<LogRow> getBlockChangedLog(String tableName, LocationDataClass location) {
        List<LogRow> logs = new ArrayList<>();
        try (Connection conn = getDataSource().getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT player, block, unixtime FROM " + tableName + " WHERE x=? and y=? and z=? and world=? and dimension=? ORDER BY unixtime DESC LIMIT " + getLogLimit());) {
            //PreparedStatement pstmt = conn.prepareStatement("SELECT player, block, unixtime FROM " + tableName + " WHERE x=? and y=? and z=? and world=? and dimension=? ORDER BY unixtime DESC LIMIT " + getLogLimit());
            pstmt.setInt(1,location.getX());
            pstmt.setInt(2,location.getY());
            pstmt.setInt(3,location.getZ());
            pstmt.setString(4,location.getWorldId().toString());
            pstmt.setString(5,String.valueOf(location.getDimensionId()));
            ResultSet resultSet = pstmt.executeQuery();

            long unixTimeSinceRelease = TimeConverter.getUnixTimeSinceRelease();

            while(resultSet.next()) {
                String playerUUIDString = resultSet.getString("player");
                String blockName = resultSet.getString("block");
                long unixtime = resultSet.getLong("unixtime");

                long timeSince = unixTimeSinceRelease - unixtime; //In seconds
                if(timeSince<0) {
                    logger.warn("The timestamp of the blocklog is in the future!");
                    continue;
                }

                UUID playerUUID = UUID.fromString(playerUUIDString);
                LogRow logRow = new LogRow(blockName, tableName.toUpperCase().charAt(0), timeSince, playerUUID, location); //Getting the action char from tablename is very coupled

                setPlayerName(logRow, playerUUID);

                logs.add(logRow);
            }
            resultSet.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        Collections.reverse(logs); //So that the newest ones are furthest down
        return logs;
    }

    private void setPlayerName(LogRow logRow, UUID playerUUID) {
        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> maybePlayer = uss.get(playerUUID);
        if(!maybePlayer.isPresent()) {
            logger.warn("The player with UUID " + playerUUID + " cannot be found in playercache, but appears in the blocklogs! Using an outdated database?");
        } else {
            logRow.setPlayerName(maybePlayer.get().getName());
        }
    }


    @Override
    public void setupDatabase() { //To-Do: Improve storage efficiency by creating a "player" "world" table which maps playerId/worldId to UUID, and then use worldId in the different tables. No need to repeat a lengthy ID millions of times
//        String url = "jdbc:sqlite:SFUtil/blocklogs.db";
//        try (Connection conn = DriverManager.getConnection(url)) {
        try (Connection conn = getDataSource().getConnection()) {
            if (conn != null) {
                Statement statement = conn.createStatement();
                String createBreak = "CREATE TABLE break (\n" +
                        "player TEXT NOT NULL,\n" +
                        "block TEXT NOT NULL,\n" +
                        "x INT NOT NULL,\n" +
                        "y INT NOT NULL,\n" +
                        "z INT NOT NULL,\n" +
                        "unixtime INT NOT NULL,\n" +
                        "world TEXT NOT NULL,\n" +
                        "dimension CHAR(1) NOT NULL\n" +
                        ");";
                String createPlace =  "CREATE TABLE place (\n" +
                        "player TEXT NOT NULL,\n" +
                        "block TEXT NOT NULL,\n" +
                        "x INT NOT NULL,\n" +
                        "y INT NOT NULL,\n" +
                        "z INT NOT NULL,\n" +
                        "unixtime INT NOT NULL,\n" +
                        "world TEXT NOT NULL,\n" +
                        "dimension CHAR(1) NOT NULL\n" +
                        ");";
                String createChest =  "CREATE TABLE chest (\n" +
                        "player TEXT NOT NULL,\n" +
                        "item TEXT NOT NULL,\n" +
                        "amount INT NOT NULL,\n" +
                        "x INT NOT NULL,\n" +
                        "y INT NOT NULL,\n" +
                        "z INT NOT NULL,\n" +
                        "unixtime INT NOT NULL,\n" +
                        "world TEXT NOT NULL,\n" +
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
