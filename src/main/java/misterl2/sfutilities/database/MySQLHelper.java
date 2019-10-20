package misterl2.sfutilities.database;

//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.
//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.
//TBD sometime in the future, low priority. File is just included so that the concept of the interface is clear.

import misterl2.sfutilities.database.datatypes.ChestLogRow;
import misterl2.sfutilities.database.datatypes.LocationDataClass;
import misterl2.sfutilities.database.datatypes.LogRow;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

public class MySQLHelper extends DBHelper {

    /*
    H2 HAS COMPATIBILITY MODE FOR MYSQL!
    H2 HAS COMPATIBILITY MODE FOR MYSQL!
    H2 HAS COMPATIBILITY MODE FOR MYSQL!
    H2 HAS COMPATIBILITY MODE FOR MYSQL!
    H2 HAS COMPATIBILITY MODE FOR MYSQL!
     */
    public MySQLHelper(Logger logger, int logLimit, String path) {
        super(logger, logLimit, path);
    }

    @Override
    public void logBlockBreak(LogRow logRow) {

    }

    @Override
    public void logBlockPlace(LogRow logRow) {

    }

    @Override
    public void logChestInteraction(ChestLogRow chestLogRow) {

    }


    @Override
    public List<LogRow> getBlockBreakLog(LocationDataClass location) {
        return null;
    }

    @Override
    public List<LogRow> getBlockPlaceLog(LocationDataClass location) {
        return null;
    }

    @Override
    public List<ChestLogRow> getChestLog(LocationDataClass location) {
        return null;
    }


    @Override
    public void setupDatabase() {

    }
}
