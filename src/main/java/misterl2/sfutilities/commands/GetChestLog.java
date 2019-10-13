package misterl2.sfutilities.commands;

import misterl2.sfutilities.database.DBHelper;
import misterl2.sfutilities.util.TimeConverter;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.item.inventory.MultiBlockCarrier;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GetChestLog extends DBCommand {

    public GetChestLog(DBHelper dbHelper, Logger logger, String... aliases) {
        super(dbHelper, logger, aliases);
    }

    @Override
    public CommandSpec build() {
        CommandSpec getChestLog = CommandSpec.builder()
                .description(Text.of("Find out who took something from this chest"))
                .permission("sfutilities.logging")
                .arguments(
                        GenericArguments.integer(Text.of("x")),
                        GenericArguments.integer(Text.of("y")),
                        GenericArguments.integer(Text.of("z")),
                        GenericArguments.optional(GenericArguments.world(Text.of("world"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("dimension")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    int x = (int) args.getOne("x").get();
                    int y = (int) args.getOne("y").get();
                    int z = (int) args.getOne("z").get();

                    Optional<World> maybeWorld = getWorld(src, args);
                    if(!maybeWorld.isPresent()) {
                        logger.error("World must be specified for this command if the player executing it is not ingame!");
                        return CommandResult.empty();
                    }
                    World world = maybeWorld.get();

                    BlockState block = world.getBlock(x, y, z);
                    Map<String,Long> chestLog = new HashMap<>();
                    if(block instanceof MultiBlockCarrier) {
                        List<Location<World>> locations = ((MultiBlockCarrier) block).getLocations();
                        for(Location<World> location : locations) {
                            chestLog.putAll(dbHelper.getChestLog(location.getBlockX(), location.getBlockY(), location.getBlockZ(), world.getUniqueId(), getDimensionId(src, args)));
                        }
                    } else {
                        chestLog = dbHelper.getChestLog(x, y, z, world.getUniqueId(), getDimensionId(src, args));
                    }

                    System.out.println("Amount in getChestLog " + chestLog.size());

                    String chestLogString = chestLog.entrySet().stream().sorted((e1,e2) -> ((int) (e2.getValue() - e1.getValue()))).limit(dbHelper.getLogLimit()).map(e -> (e.getKey() + TimeConverter.secondsToTimeString(e.getValue()) + " ago!")).collect(Collectors.joining("\n"));

                    System.out.println("Log limit: " + dbHelper.getLogLimit());

                    src.sendMessage(Text.of("=============================="));
                    if(chestLogString.isEmpty()) {
                        src.sendMessage(Text.of("There are no chest logs for this location!"));
                    } else {
                        src.sendMessage(Text.of(chestLogString));
                    }

                    src.sendMessage(Text.of("=============================="));
                    return CommandResult.success();
                })
                .build();
        return getChestLog;
    }
}
