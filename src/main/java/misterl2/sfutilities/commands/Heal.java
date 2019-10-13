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

import static org.spongepowered.api.command.args.GenericArguments.player;

public class Heal extends Command {


    public Heal(Logger logger, String... aliases) {
        super(logger, aliases);
    }

    public CommandSpec build() {
        CommandSpec heal = CommandSpec.builder()
                .description(Text.of("Restore all hunger on a player"))
                .permission("sfutilities.heal")
                .arguments(
                        player(Text.of("player")),
                        GenericArguments.optional(GenericArguments.doubleNum(Text.of("healthAmount")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    final Collection<Player> players = args.getAll("player");
                    Optional<Double> maybeHealthAmount = args.getOne("healthAmount");
                    int playerAmt = 0;
                    for (Player player : players) {
                        MutableBoundedValue<Double> playerHealth = player.health();
                        if(playerHealth.get()!=playerHealth.getMaxValue()) { //If not already full

                            if(maybeHealthAmount.isPresent()) { //If second parameter is supplied, only restore that much health
                                playerHealth.set(Math.min(maybeHealthAmount.get()+playerHealth.get(),playerHealth.getMaxValue()));
                            } else {
                                playerHealth.set(playerHealth.getMaxValue());
                            }

                            player.offer(playerHealth);
                        }

                        if(players.size()==1) {
                            if(maybeHealthAmount.isPresent()) {
                                log(player,maybeHealthAmount.get().intValue());
                            } else {
                                log(player);
                            }
                        }
                        playerAmt++;
                    }
                    if(players.size()!=1) {
                        if(maybeHealthAmount.isPresent()) {
                            log(playerAmt, maybeHealthAmount.get().intValue());
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
        return heal;
    }

    private void log(Player player) {
        logger.info("Restored full health for " + player.getName());
    }

    private void log(Player player, int healthAmount) {
        logger.info("Restored " + healthAmount + " health for " + player.getName());
    }

    private void log(int playerAmt) {
        logger.info("Restored full health for " + playerAmt + " players");
    }
    private void log(int playerAmt, int healthAmount) {
        logger.info("Restored " + healthAmount + " health for " + playerAmt + " players");
    }

}
