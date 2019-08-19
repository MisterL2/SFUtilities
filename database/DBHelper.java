package misterl2.sfutilities.database;

import java.util.List;

public interface DBHelper {
    void logBlockBreak(String playerUUID, String block, int x, int y, int z, long unixTime, char dimensionId);
    List<String> getBlockBreakLog(int x, int y, int z, char dimension);
    //default List<String> getBlockBreakLog(int x, int y, int z) {        return getBlockBreakLog(x,y,z,'O');    }
    void setupDatabase();
}
