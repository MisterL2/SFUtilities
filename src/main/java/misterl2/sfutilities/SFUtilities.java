package misterl2.sfutilities;

import com.google.inject.Inject;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import misterl2.sfutilities.commands.*;
import misterl2.sfutilities.database.DBHelper;
import misterl2.sfutilities.logging.BlockEventListener;
import misterl2.sfutilities.database.SQLiteHelper;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

//    @Inject
//    @ConfigDir(sharedRoot = true)
//    private Path configDir;

    private Path configFile = Paths.get( "config/sfutilities.conf");
    private ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();




    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }

    @Listener
    public void init(GameInitializationEvent event) {
        logger.info("SFUtilities loading...");
        logger.info("Loading config...");
        try {
            //CommentedConfigurationNode load = configLoader.load();
            //configLoader.save(load);
            //CommentedConfigurationNode emptyNode = configLoader.createEmptyNode(ConfigurationOptions.defaults());

            ConfigurationNode rootNode = configLoader.load(); //Creates config correctly
            List<? extends ConfigurationNode> childrenList = rootNode.getChildrenList();
            ConfigurationNode appendedNode = rootNode.getAppendedNode();
            logger.info(appendedNode.getString()); //Returns null
            logger.info("YOLO");
            for (ConfigurationNode node: childrenList) { //empty list
                logger.info(node.getString());
            }
            ConfigurationNode commands = rootNode.getNode("commands");
            ConfigurationNode feed = commands.getNode("feed");
            boolean feedEnabled = feed.getBoolean();
            System.out.println(feedEnabled);
            System.out.println(feedEnabled);
            System.out.println(feedEnabled);
            feed.setValue(true);
            ConfigurationNode heal = commands.getNode("heal");
            heal.setValue(true);

            ConfigurationNode logging = rootNode.getNode("logging");
            logging.setValue(true);
            System.out.println(configLoader.canSave());
            configLoader.save(rootNode);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        logger.info("Finished loading config!");
        DBHelper dbHelper = new SQLiteHelper(logger);
        buildCommands(dbHelper);
        Sponge.getEventManager().registerListeners(this, new BlockEventListener(logger,dbHelper));
        logger.info("SFUtilities loaded!");

        //IF it doesnt exist yet! -> TBD Check if it does

        boolean databaseExists = new File("SFUtil/blocklogs.db").exists();
        if(!databaseExists) {
            logger.warn("No SQLite database found! Creating a new one.");
            new File("SFUtil").mkdir(); //Attempts to make a new folder for the database. If it already exists, returns false and nothing else happens.
            dbHelper.setupDatabase();
        }



    }

    private void buildCommands(DBHelper dbHelper) {
        ArrayList<Command> activatedCommands = new ArrayList<>();
        //Select commands based on config
        activatedCommands.add(new Feed(logger,"feed","restorehunger"));
        activatedCommands.add(new SetFood(logger,"setfood","foodset"));

        activatedCommands.add(new Heal(logger,"heal","restorehealth"));
        activatedCommands.add(new SetHealth(logger,"sethealth","healthset"));

        //If logging is enabled
        activatedCommands.add(new GetBlockBreakLog(dbHelper,logger,"blog","breaklog","blockbreaklog"));
        activatedCommands.add(new GetBlockPlaceLog(dbHelper,logger,"plog","placelog","blockplacelog"));

        //Create, log and register the selected commands
        for (Command command: activatedCommands) {
            logger.info("Command " + command.getClass().getSimpleName() + " is now being loaded!");
            CommandSpec c = command.build();
            Sponge.getCommandManager().register(this,c,command.getAliases());
        }
    }
}