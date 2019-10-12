package misterl2.sfutilities.database;

import java.util.List;

public interface DBHelper {
    void logBlockBreak(String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId);
    void logBlockPlace(String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId);
    void logChestInteraction(String playerUUID, String action, String block, int amount, int x, int y, int z, long unixTime, char dimensionId);
    List<String> getBlockBreakLog(int x, int y, int z, char dimension);
    List<String> getBlockPlaceLog(int x, int y, int z, char dimension);
    List<String> getChestLog(int x, int y, int z, char dimension);
    void setupDatabase();
}
