package misterl2.sfutilities.database;

public interface DBHelper {
    void logBlock(String changeType, String playerName, String block, int x, int y, int z, long unixTime, char dimensionId);
    void setupDatabase();
}
