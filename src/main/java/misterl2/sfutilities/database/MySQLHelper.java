package misterl2.sfutilities.database;

//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.
//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.
//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.

import java.util.List;

public class MySQLHelper implements DBHelper {

    @Override
    public void logBlockBreak(String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId) {

    }

    @Override
    public void logBlockPlace(String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId) {

    }

    @Override
    public void logChestInteraction(String playerUUID, char action, String block, int amount, int x, int y, int z, long unixTime, char dimensionId) {

    }


    @Override
    public List<String> getBlockBreakLog(int x, int y, int z, char dimension) {
        return null;
    }

    @Override
    public List<String> getBlockPlaceLog(int x, int y, int z, char dimension) {
        return null;
    }

    @Override
    public List<String> getChestLog(int x, int y, int z, char dimension) {
        return null;
    }

    @Override
    public void setupDatabase() {

    }

}
