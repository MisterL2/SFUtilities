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

public class SetHealth extends Command {


    public SetHealth(Logger logger, String... aliases) {
        super(logger, aliases);
    }

    public CommandSpec build() {
        CommandSpec setHealth = CommandSpec.builder()
                .description(Text.of("Set the food level of a player the specified amount"))
                .arguments(
                        GenericArguments.player(Text.of("player")),
                        GenericArguments.doubleNum(Text.of("healthAmount"))
                )
                .permission("sfutilities.heal")
                .executor((CommandSource src, CommandContext args) -> {
                    Collection<Player> players = args.getAll("player");
                    Optional<Double> healthAmount = args.getOne("healthAmount");
                    if(!healthAmount.isPresent()) {
                        logError("The amount of health wasn't specified! Why is this value an \"Optional\" for a required argument anyways?");
                        return CommandResult.empty();
                    }
                    int playerAmt = 0;
                    for (Player player : players) {
                        MutableBoundedValue<Double> healthLevel = player.health();
                        healthLevel.set(Math.min(healthAmount.get(),healthLevel.getMaxValue()));
                        player.offer(healthLevel);
                        playerAmt++;
                    }
                    return CommandResult.builder()
                            .successCount(1)
                            .affectedEntities(playerAmt)
                            .build();
                })
                .build();
        return setHealth;
    }

    private void logError(String msg) {
        logger.error("An error occurred in SetHealth: " + msg);
    }
}
