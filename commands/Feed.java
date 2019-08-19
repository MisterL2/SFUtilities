package misterl2.sfutilities.commands;

import com.google.inject.Inject;
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

import static org.spongepowered.api.command.args.GenericArguments.player;

public class Feed extends Command {


    public Feed(Logger logger, String... aliases) {
        super(logger, aliases);
    }

    public CommandSpec build() {
        CommandSpec feed = CommandSpec.builder()
                .description(Text.of("Restore all hunger on a player"))
                .permission("sfutilities.feed")
                .arguments(
                        player(Text.of("player")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("foodAmount")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    final Collection<Player> players = args.getAll("player");
                    Optional<Integer> maybeFoodAmount = args.getOne("foodAmount");
                    int playerAmt = 0;
                    for (Player player : players) {
                        MutableBoundedValue<Integer> foodLevel = player.foodLevel();
                        if(foodLevel.get()!=foodLevel.getMaxValue()) { //If not already full
                            MutableBoundedValue<Double> saturation = player.saturation();
                            saturation.set(5D);

                            if(maybeFoodAmount.isPresent()) { //If second parameter is supplied, only restore that much food
                                foodLevel.set(Math.min(maybeFoodAmount.get()+foodLevel.get(),foodLevel.getMaxValue()));
                            } else {
                                foodLevel.set(foodLevel.getMaxValue());
                            }

                            player.offer(saturation);
                            player.offer(foodLevel);
                        }

                        if(players.size()==1) {
                            if(maybeFoodAmount.isPresent()) {
                                log(player,maybeFoodAmount.get());
                            } else {
                                log(player);
                            }
                        }
                        playerAmt++;
                    }
                    if(players.size()!=1) {
                        if(maybeFoodAmount.isPresent()) {
                            log(playerAmt, maybeFoodAmount.get());
                        } else {
                            log(playerAmt);
                        }
                    }
                    return CommandResult.builder()
                            .affectedEntities(playerAmt)
                            .successCount(1)
                            .build();
                })
                .build();
        return feed;
    }

    private void log(Player player) {
        logger.info("Restored full hunger for " + player.getName());
    }

    private void log(Player player, int foodAmount) {
        logger.info("Restored " + foodAmount + " hunger for " + player.getName());
    }

    private void log(int playerAmt) {
        logger.info("Restored full hunger for " + playerAmt + " players");
    }
    private void log(int playerAmt, int foodAmount) {
        logger.info("Restored " + foodAmount + " hunger for " + playerAmt + " players");
    }

}
