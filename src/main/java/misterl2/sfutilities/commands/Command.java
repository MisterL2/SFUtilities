package misterl2.sfutilities.commands;

import org.slf4j.Logger;
import org.spongepowered.api.command.spec.CommandSpec;

import java.util.Arrays;
import java.util.List;

public abstract class Command {
    protected Logger logger;
    protected final List<String> aliases;

    public Command(Logger logger, String... aliases) {
        this.logger=logger;
        this.aliases = Arrays.asList(aliases);
        if(this.aliases.isEmpty()) {
            logger.error("The command " + this.getClass().getSimpleName() + " has no in-game command aliases and will not work!");
        }
    }

    public List<String> getAliases() {
        return this.aliases;
    }
    public abstract CommandSpec build();
}
