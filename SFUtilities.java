package misterl2.sfutilities;

import com.google.inject.Inject;
import misterl2.sfutilities.commands.*;
import misterl2.sfutilities.database.DBHelper;
import misterl2.sfutilities.logging.BlockEventListener;
import misterl2.sfutilities.database.SQLiteHelper;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

import java.util.ArrayList;
@Plugin(
        id = "sfutilities",
        name = "SFUtilities",
        description = "Implements a few utility features missing in Essentials",
        authors = {
                "MisterL2"
        },
        version = "0.1-ALPHA"
)
public class SFUtilities {

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }

    @Listener
    public void init(GameInitializationEvent event) {
        logger.info("SFUtilities loading...");
        DBHelper dbHelper = new SQLiteHelper(logger);
        buildCommands();
        Sponge.getEventManager().registerListeners(this, new BlockEventListener(logger,dbHelper));
        logger.info("SFUtilities loaded!");

        //IF it doesnt exist yet! -> TBD Check if it does
        dbHelper.setupDatabase();

    }

    private void buildCommands() {
        ArrayList<Command> activatedCommands = new ArrayList<>();
        //Select commands based on config
        activatedCommands.add(new Feed(logger,"feed","restorehunger"));
        activatedCommands.add(new SetFood(logger,"setfood","foodset"));

        activatedCommands.add(new Heal(logger,"heal","restorehealth"));
        activatedCommands.add(new SetHealth(logger,"sethealth","healthset"));
        //Create, log and register the selected commands
        for (Command command: activatedCommands) {
            logger.info("Command " + command.getClass().getSimpleName() + " is now being loaded!");
            CommandSpec c = command.build();
            Sponge.getCommandManager().register(this,c,command.getAliases());
        }
    }
}