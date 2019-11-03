package misterl2.sfutilities.database;

import misterl2.sfutilities.database.datatypes.ChestLogRow;
import misterl2.sfutilities.database.datatypes.LocationDataClass;
import misterl2.sfutilities.database.datatypes.LogRow;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class DBHelper {
    protected final Logger logger;
    protected final int logLimit;
    private final DataSource dataSource;

    public DBHelper(Logger logger, int logLimit, String path) {
        this.logger = logger; this.logLimit = logLimit;

        boolean databaseExists = new File("SFUtil/blocklogs.mv.db").exists();
        if(!databaseExists) {
            this.logger.warn("No SQLite database found! Creating a new one.");
            new File("SFUtil").mkdir(); //Attempts to make a new folder for the database. If it already exists, returns false and nothing else happens.
        }
        try {
            dataSource = Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource(path);
        } catch (SQLException e) {
            this.logger.error(e.getMessage());
            throw new IllegalArgumentException("SQL Error in setup! ALL LOGGING WILL FAIL!");
        }

        this.setupDatabase();
    }

    public abstract void logBlockBreak(LogRow logRow);
    public abstract void logBlockPlace(LogRow logRow);
    public abstract void logChestInteraction(ChestLogRow chestLogRow); // I = Insert, R = Remove; B = Break, P = Place;
    public abstract List<LogRow> getBlockBreakLog(LocationDataClass location);
    public abstract List<LogRow> getBlockPlaceLog(LocationDataClass location);
    public abstract List<ChestLogRow> getChestLog(LocationDataClass location);
    public abstract void setupDatabase();

    public int getLogLimit() {
        return logLimit;
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }
    protected void safeInsert(PreparedStatement pstmt) {
        try {
            pstmt.execute();
        } catch (SQLException e) {
            logger.warn("Database transaction failed: " + e.getMessage());
        }
    }
}
