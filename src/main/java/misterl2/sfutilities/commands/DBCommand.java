package misterl2.sfutilities.commands;

import misterl2.sfutilities.database.DBHelper;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

import java.util.Optional;

public abstract class DBCommand extends Command {
    protected DBHelper dbHelper;

    public DBCommand(DBHelper dbHelper, Logger logger, String... aliases) {
        super(logger, aliases);
        this.dbHelper = dbHelper;
    }

    protected char getDimensionId(CommandSource src, CommandContext args) {
        Optional<String> maybeDimension = args.getOne("dimension");
        if(maybeDimension.isPresent()) {
            return maybeDimension.get().charAt(0);
        }

        if(src instanceof Locatable) { //If src is a player or something else that can be located, substitute in their current dimension, otherwise assume 'O' for OVERWORLD
            return ((Locatable) src).getLocation().getExtent().getDimension().getType().toString().charAt(0);
        }

        return 'O';
    }

    protected Optional<World> getWorld(CommandSource src, CommandContext args) {
        Optional<World> maybeWorld = args.getOne("world");
        if (maybeWorld.isPresent()) {
            return maybeWorld;
        }

        if(src instanceof Locatable) {
            return Optional.of(((Locatable) src).getWorld());
        }

        return Optional.empty();
    }
}
