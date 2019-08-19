package misterl2.sfutilities.commands;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Optional;

public class SetFood extends Command {


    public SetFood(Logger logger, String... aliases) {
        super(logger, aliases);
    }

    public CommandSpec build() {
        CommandSpec setFood = CommandSpec.builder()
                .description(Text.of("Set the food level of a player the specified amount"))
                .arguments(
                        GenericArguments.player(Text.of("player")),
                        GenericArguments.integer(Text.of("foodAmount"))
                )
                .permission("sfutilities.feed")
                .executor((CommandSource src, CommandContext args) -> {
                    Collection<Player> players = args.getAll("player");
                    Optional<Integer> foodAmount = args.getOne("foodAmount");
                    if(!foodAmount.isPresent()) {
                        logError("The amount of food wasn't specified! Why is this value an \"Optional\" for a required argument anyways?");
                        return CommandResult.empty();
                    }
                    int playerAmt = 0;
                    for (Player player : players) {
                        MutableBoundedValue<Integer> foodLevel = player.foodLevel();
                        foodLevel.set(Math.min(foodAmount.get(),foodLevel.getMaxValue()));
                        player.offer(foodLevel);
                        playerAmt++;
                    }
                    return CommandResult.builder()
                            .successCount(1)
                            .affectedEntities(playerAmt)
                            .build();
                })
                .build();
        return setFood;
    }

    private void logError(String msg) {
        logger.error("An error occurred in SetFood: " + msg);
    }
}
