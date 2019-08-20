package misterl2.sfutilities.commands;

import misterl2.sfutilities.database.DBHelper;
import org.slf4j.Logger;

public abstract class DBCommand extends Command {
    protected DBHelper dbHelper;

    public DBCommand(DBHelper dbHelper, Logger logger, String... aliases) {
        super(logger, aliases);
        this.dbHelper = dbHelper;
    }
}
