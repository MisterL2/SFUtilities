package misterl2.sfutilities.commands;

import com.flowpowered.math.vector.Vector3i;
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

import java.util.*;
import java.util.function.Consumer;

public class LightningCircle extends Command {
    private SFUtilities plugin;

    public LightningCircle(Logger logger, SFUtilities plugin, String... aliases) {
        super(logger, aliases); this.plugin = plugin;
    }

    public CommandSpec build() {
        CommandSpec heal = CommandSpec.builder()
                .description(Text.of("Spam target player (or self) with lightnings!"))
                .permission("sfutilities.admin.lightningcircle")
                .arguments(
                        GenericArguments.optional(GenericArguments.integer(Text.of("radius"))),
                        GenericArguments.optional(GenericArguments.integer(Text.of("times"))),
                        GenericArguments.optional(GenericArguments.entity(Text.of("targets")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    Optional<Integer> times = args.getOne("times");
                    final int repetitions = times.isPresent() ? times.get() : 3;

                    if(repetitions < 1) {
                        logger.error("Error: Repetitions needs to be >= 1!");
                        return CommandResult.empty();
                    }

                    Optional<Integer> maybeRadius = args.getOne("radius");
                    final int radius = maybeRadius.isPresent() ? maybeRadius.get() : 12;

                    if(radius < 1) {
                        logger.error("Error: Radius needs to be >= 1!");
                        return CommandResult.empty();
                    }

                    Collection<Locatable> selectedTargets = args.getAll("targets");

                    Set<Locatable> targets = new HashSet<>();
                    if(selectedTargets.isEmpty()) {
                        if(!(src instanceof Locatable)) {
                            logger.error("No target specified and commandsource is not locatable!");
                            return CommandResult.empty();

                        }
                        targets.add((Locatable) src);
                    } else {
                        targets.addAll(selectedTargets);
                    }


                    Task.builder().execute(new Consumer<Task>() {
                        private int remainingRepetitions = 5*repetitions;

                        @Override
                        public void accept(Task task) {
                            if(remainingRepetitions <= 0) {
                                task.cancel();
                                return;
                            }
                            remainingRepetitions--;

                            int valid_lightning_index = remainingRepetitions % 5;
                            for (Locatable target : targets) {
                                List<Vector3i> lightning_locations = generateCircle(target.getLocation().getBlockX(), Math.min(1,target.getLocation().getBlockY()-5), target.getLocation().getBlockZ(), radius);
                                for(int i = 0; i < lightning_locations.size(); i++) {
                                    if(i%5 != valid_lightning_index) {
                                        continue;
                                    }
                                    Vector3i location = lightning_locations.get(i);
                                    String command = new StringBuilder().append("summon minecraft:lightning_bolt ")
                                        .append(location.getX()).append(" ") //Locations may change while the task is being repeated. In this case, the lightning strikes follow the targets!
                                        .append(location.getY()).append(" ")
                                        .append(location.getZ()).append(" ").toString();
                                    //System.out.println(command);
                                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
                                }

                            }
                        }
                    }).intervalTicks(10).submit(plugin);


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


    private List<Vector3i> generateCircle(int centerX, int centerY, int centerZ, int r) {
//        System.out.println("Started circle generation!");

        List<Vector3i> sectorOne = new ArrayList<>();
        List<Vector3i> inverseSectorTwo = new ArrayList<>();
        List<Vector3i> sectorThree = new ArrayList<>();
        List<Vector3i> inverseSectorFour = new ArrayList<>();
        List<Vector3i> sectorFive = new ArrayList<>();
        List<Vector3i> inverseSectorSix = new ArrayList<>();
        List<Vector3i> sectorSeven = new ArrayList<>();
        List<Vector3i> inverseSectorEight = new ArrayList<>();
        //x² + z² = r²
        //z² = r² - x²
        //For the initial value, x = 0, it does not matter if it is +x or -x, as x=-x. Adding them duplicate would produce unexpected behaviour.

        sectorOne.add(new Vector3i(centerX,centerY,centerZ+r));
        sectorThree.add(new Vector3i(centerX+r,centerY,centerZ));
        sectorFive.add(new Vector3i(centerX,centerY,centerZ-r));
        sectorSeven.add(new Vector3i(centerX-r,centerY,centerZ));

        int z = r;
        int x = 1;
        while(x<z) { //z value is initially r, but decreases as you follow along the circle shape. You can think of it as a cartesian grid, where x is the x-axis, and z is the y-axis.
            z = (int) Math.round(Math.sqrt(r*r - x*x));
            //8 way symmetry
            sectorOne.add(new Vector3i(centerX+x,centerY,centerZ+z));
            sectorThree.add(new Vector3i(centerX+z,centerY,centerZ-x));
            sectorFive.add(new Vector3i(centerX-x,centerY,centerZ-z));
            sectorSeven.add(new Vector3i(centerX-z,centerY,centerZ+x));

            inverseSectorTwo.add(new Vector3i(centerX+z,centerY,centerZ+x));
            inverseSectorSix.add(new Vector3i(centerX-z,centerY,centerZ-x));
            inverseSectorFour.add(new Vector3i(centerX+x,centerY,centerZ-z));
            inverseSectorEight.add(new Vector3i(centerX-x,centerY,centerZ+z));

            x++;
        }

        if(x==z) { //If there is a shared block (line of symmetry going through the block), then it should be added only ONCE!
            sectorOne.add(new Vector3i(centerX+x,centerY,centerZ+z));
            sectorThree.add(new Vector3i(centerX+x,centerY,centerZ-z));
            sectorFive.add(new Vector3i(centerX-x,centerY,centerZ-z));
            sectorSeven.add(new Vector3i(centerX-x,centerY,centerZ+z));
        }
        Collections.reverse(inverseSectorTwo);
        Collections.reverse(inverseSectorFour);
        Collections.reverse(inverseSectorSix);
        Collections.reverse(inverseSectorEight);
        sectorOne.addAll(inverseSectorTwo);
        sectorOne.addAll(sectorThree);
        sectorOne.addAll(inverseSectorFour);
        sectorOne.addAll(sectorFive);
        sectorOne.addAll(inverseSectorSix);
        sectorOne.addAll(sectorSeven);
        sectorOne.addAll(inverseSectorEight);

//        System.out.println("Finished circle generation!");
        return sectorOne;
    }
}