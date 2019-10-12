package misterl2.sfutilities.database;

//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.
//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.
//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.

import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MySQLHelper extends DBHelper {


    public MySQLHelper(Logger logger, int logLimit) {
        super(logger, logLimit);
    }

    @Override
    public void logBlockBreak(String playerUUID, String block, int x, int y, int z, long unixTime, UUID world, char dimension) {

    }

    @Override
    public void logBlockPlace(String playerUUID, String block, int x, int y, int z, long unixTime, UUID world, char dimension) {

    }

    @Override
    public void logChestInteraction(String playerUUID, char action, String block, int amount, int x, int y, int z, long unixTime, UUID world, char dimension) {

    }

    @Override
    public List<String> getBlockBreakLog(int x, int y, int z, UUID world, char dimension) {
        return null;
    }

    @Override
    public List<String> getBlockPlaceLog(int x, int y, int z, UUID world, char dimension) {
        return null;
    }

    @Override
    public Map<String, Long> getChestLog(int x, int y, int z, UUID world, char dimension) {
        return null;
    }

    @Override
    public void setupDatabase() {

    }
}
