package misterl2.sfutilities.commands;

import misterl2.sfutilities.database.DBHelper;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class GetBlockBreakLog extends DBCommand {


    public GetBlockBreakLog(DBHelper dbHelper, Logger logger, String... aliases) {
        super(dbHelper, logger, aliases);
    }

    @Override
    public CommandSpec build() {
        CommandSpec getBreakLog = CommandSpec.builder()
                .description(Text.of("Gets the log of all blocks broken at the specified coordinates"))
                .permission("sfutilities.logging")
                .arguments(
                        GenericArguments.integer(Text.of("x")),
                        GenericArguments.integer(Text.of("y")),
                        GenericArguments.integer(Text.of("z")),
                        GenericArguments.optional(GenericArguments.world(Text.of("world"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("dimension")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    //These one might be optionals, but they are required arguments so they can't be empty
                    int x = (int) args.getOne("x").get();
                    int y = (int) args.getOne("y").get();
                    int z = (int) args.getOne("z").get();

                    Optional<World> maybeWorld = getWorld(src, args);
                    if(!maybeWorld.isPresent()) {
                        logger.error("World must be specified for this command if the player executing it is not ingame!");
                        return CommandResult.empty();
                    }
                    World world = maybeWorld.get();
                    List<String> blockBreakLog = dbHelper.getBlockBreakLog(x, y, z, world.getUniqueId(), getDimensionId(src, args));
                    src.sendMessage(Text.of("=============================="));
                    if(blockBreakLog.isEmpty()) {
                        src.sendMessage(Text.of("There are no break logs for these coordinates in this dimension!"));
                    } else {
                        for (String logRow: blockBreakLog) {
                            src.sendMessage(Text.of(logRow));
                        }
                    }
                    src.sendMessage(Text.of("=============================="));
                    return CommandResult.success();
                })
                .build();


        return getBreakLog;
    }

}
