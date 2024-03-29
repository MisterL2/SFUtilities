package misterl2.sfutilities;

import com.google.inject.Inject;
import misterl2.sfutilities.commands.*;
import misterl2.sfutilities.database.DBHelper;
import misterl2.sfutilities.database.H2Helper;
import misterl2.sfutilities.logging.BlockEventListener;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ValueType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

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

    @Inject
    @DefaultConfig  (sharedRoot = true)
    private Path configDir;

    private Path configFile = Paths.get( "config/sfutilities.conf");
    private ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();


    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }

    @Listener
    public void init(GameInitializationEvent event) {
        logger.info("SFUtilities loading...");
        logger.info("Loading config...");
        ConfigurationNode rootNode = null;
        try {
            rootNode = configLoader.load();
            List<? extends ConfigurationNode> childrenList = rootNode.getChildrenList();
            if(rootNode.getValueType() == ValueType.NULL) { //Creates config, if it doesn't exist yet
                logger.info("No config found, creating new config");
                ConfigurationNode commands = rootNode.getNode("commands");
                ConfigurationNode feed = commands.getNode("feed");

                feed.setValue(true);
                ConfigurationNode heal = commands.getNode("heal");
                heal.setValue(true);

                ConfigurationNode logging = rootNode.getNode("logging");
                logging.setValue(true);

                ConfigurationNode logLimit = logging.getNode("max-logs-shown");
                logLimit.setValue(20);

                ConfigurationNode adminfun = commands.getNode("fun");
                ConfigurationNode lightningSpam = adminfun.getNode("lightning-spam");
                lightningSpam.setValue(true);

                ConfigurationNode lightningCircle = adminfun.getNode("lightning-circle");
                lightningCircle.setValue(true);

                System.out.println(configLoader.canSave());
                configLoader.save(rootNode);
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        logger.info("Finished loading config!");
        Integer logLimit = (Integer) rootNode.getNode("logging", "max-logs-shown").getValue();
        DBHelper dbHelper = new H2Helper(logger,logLimit, "jdbc:h2:./SFUtil/blocklogs");
        buildCommands(dbHelper,rootNode);
        Sponge.getEventManager().registerListeners(this, new BlockEventListener(this, logger,dbHelper));
        logger.info("SFUtilities loaded!");


    }

    private void buildCommands(DBHelper dbHelper, ConfigurationNode rootNode) {
        /* TBD

        -> GetBlockPlace by player (in case they placed TNT / lava / water and its hard to pinpoint a specific block), then you can check the blocks they placed.
        ^^^^ Allow filtering by block and
        ->


        */
        if(rootNode==null) {
            logger.warn("Config could not be loaded, using defaults!");
        }
        ArrayList<Command> activatedCommands = new ArrayList<>();

        //Select commands based on config

        if(rootNode == null || rootNode.getNode("commands","feed").getBoolean(true)) {
            activatedCommands.add(new Feed(logger,"feed","restorehunger"));
            activatedCommands.add(new SetFood(logger,"setfood","foodset"));

        }

        if(rootNode == null || rootNode.getNode("commands","heal").getBoolean(true)) {
            activatedCommands.add(new Heal(logger, "heal", "restorehealth"));
            activatedCommands.add(new SetHealth(logger, "sethealth", "healthset"));
        }

        if(rootNode == null || rootNode.getNode("commands","fun","lightning-spam").getBoolean(true)) {
            activatedCommands.add(new LightningSpam(logger, this,"ls", "lspam", "lightningspam"));
        }

        if(rootNode == null || rootNode.getNode("commands","fun","lightning-circle").getBoolean(true)) {
            activatedCommands.add(new LightningCircle(logger, this,"lc", "lcircle", "lightningcircle"));
        }

        //If logging is enabled
        if(rootNode == null || rootNode.getNode("logging").getBoolean(true)) {
            activatedCommands.add(new GetBlockBreakLog(dbHelper, logger, "blog", "breaklog", "blockbreaklog"));
            activatedCommands.add(new GetBlockPlaceLog(dbHelper, logger, "plog", "placelog", "blockplacelog"));
            activatedCommands.add(new GetChestLog(dbHelper, logger, "clog", "chestlog"));
        }
        //Create, log and register the selected commands
        for (Command command: activatedCommands) {
            logger.info("Command " + command.getClass().getSimpleName() + " is now being loaded!");
            CommandSpec c = command.build();
            Sponge.getCommandManager().register(this,c,command.getAliases());
        }
    }
}