package misterl2.sfutilities.commands;

import misterl2.sfutilities.database.DBHelper;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.Optional;

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
                        GenericArguments.optional(GenericArguments.string(Text.of("dimension")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    int x = (int) args.getOne("x").get();
                    int y = (int) args.getOne("y").get();
                    int z = (int) args.getOne("z").get();
                    char dimensionId = 'O';
                    Optional<String> maybeDimension = args.getOne("dimension");
                    if (maybeDimension.isPresent()) {
                        dimensionId = maybeDimension.get().charAt(0);
                    }
                    /* TBD



                    */
                    return null;
                })
                .build();
        return getChestLog;
    }
}
