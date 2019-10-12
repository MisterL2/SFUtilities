package misterl2.sfutilities.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class DBHelper {
    protected final Logger logger;
    protected final ComboPooledDataSource cpds;
    protected final int logLimit;

    public DBHelper(Logger logger, int logLimit) {
        this.logger = logger; this.logLimit = logLimit;
        cpds = new ComboPooledDataSource();
    }

    public abstract void logBlockBreak(String playerUUID, String block, int x, int y, int z, long unixTime, UUID world, char dimension);
    public abstract void logBlockPlace(String playerUUID, String block, int x, int y, int z, long unixTime, UUID world, char dimension);
    public abstract void logChestInteraction(String playerUUID, char action, String block, int amount, int x, int y, int z, long unixTime, UUID world, char dimension); // I = Insert, R = REMOVE
    public abstract List<String> getBlockBreakLog(int x, int y, int z, UUID world, char dimension);
    public abstract List<String> getBlockPlaceLog(int x, int y, int z, UUID world, char dimension);
    public abstract Map<String,Long> getChestLog(int x, int y, int z, UUID world, char dimension); //The LONG in the map specifies timeSince. It is NOT sorted by time!
    public abstract void setupDatabase();

    public int getLogLimit() {
        return logLimit;
    }
}
