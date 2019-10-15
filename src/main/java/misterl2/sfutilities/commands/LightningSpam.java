package misterl2.sfutilities.commands;

import misterl2.sfutilities.SFUtilities;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;

public class LightningSpam extends Command {
    private SFUtilities plugin;

    public LightningSpam(Logger logger, SFUtilities plugin, String... aliases) {
        super(logger, aliases); this.plugin = plugin;
    }

    public CommandSpec build() {
        CommandSpec heal = CommandSpec.builder()
                .description(Text.of("Spam target player (or self) with lightnings!"))
                .permission("sfutilities.admin.lightningspam")
                .arguments(
                        GenericArguments.optional(GenericArguments.integer(Text.of("times"))),
                        GenericArguments.optional(GenericArguments.entity(Text.of("targets")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    Optional<Integer> times = args.getOne("times");
                    final int repetitions = times.isPresent() ? times.get() : 15;
                    if(repetitions < 0) {
                        logger.error("Error: Negative amount of times specified!");
                        return CommandResult.empty();
                    }
                    final Collection<Locatable> targets; //args.getAll("targets") Is guaranteed to be a Collection<Entity>, but if I use the commandsource later on, a non-player commandsource such as a commandblock would also be valid

                    if(args.getAll("targets").isEmpty()) {
                        if(!(src instanceof Locatable)) {
                            logger.error("No target specified for LightningSpam and the CommandSource is not Locatable!");
                            return CommandResult.empty();
                        }
                        targets = new HashSet<>();
                        targets.add((Locatable) src); //Only add the commandsource if no other player is specified
                    } else {
                        targets = args.getAll("targets");
                    }

                    System.out.println("Amount of targets: " + targets.size());

                    Task.builder().execute(new Consumer<Task>() {
                        private int remainingRepetitions = repetitions;
                        @Override
                        public void accept(Task task) {
                            if(remainingRepetitions <= 0) {
                                task.cancel();
                                return;
                            }
                            remainingRepetitions--;

                            for (Locatable target : targets) {
                                String command = new StringBuilder().append("summon minecraft:lightning_bolt ")
                                        .append(target.getLocation().getBlockX()).append(" ") //Locations may change while the task is being repeated. In this case, the lightning strikes follow the targets!
                                        .append(target.getLocation().getBlockY()).append(" ")
                                        .append(target.getLocation().getBlockZ()).append(" ").toString();
                                System.out.println(command);
                                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
                            }
                        }
                    }).intervalTicks(5).submit(plugin);


                    log(repetitions, targets.size());
                    return CommandResult.builder()
                            .affectedEntities(targets.size())
                            .successCount(1)
                            .build();
                })
                .build();
        return heal;
    }

    private void log(int repetitions, int playerAmt) {
        logger.info("Lightning spam activated with " + repetitions + " for " + playerAmt + " players!");
    }

}
