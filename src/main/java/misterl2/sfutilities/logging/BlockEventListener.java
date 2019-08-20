package misterl2.sfutilities.logging;

import misterl2.sfutilities.database.DBHelper;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Date;
import java.util.List;

public class BlockEventListener {
    private Logger logger;
    private DBHelper dbHelper;

    public BlockEventListener(Logger logger, DBHelper dbHelper) {
        this.logger=logger;
        this.dbHelper = dbHelper;
    }

    @Listener
    public void onBlockBreak (ChangeBlockEvent.Break event, @Root Player player) {

        logger.info("TRANSACTIONS");
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for(Transaction<BlockSnapshot> t : transactions) {

            BlockSnapshot original = t.getOriginal();
            logger.info(original.getLocation().toString());

            logger.info("Location");


            String blockName = original.getState().getType().getName();
            if(blockName.contains(":")) { //Remove prefix
                String[] splitBlockName = blockName.split(":");
                blockName = splitBlockName[1]; //The part after the prefix
            }

            int blockX=0; int blockY=0; int blockZ=0; char dimensionId='O'; //Dimension default is "O" for OVERWORLD. I don't know in which absurd case these defaults would ever be used
            if(!original.getLocation().isPresent()) {
                logger.error("Wtf why is there no location for the block that broke?!");
            } else {
                Location<World> blockLocation = original.getLocation().get();
                blockX = blockLocation.getBlockX();
                blockY = blockLocation.getBlockY();
                blockZ = blockLocation.getBlockZ();
                dimensionId = blockLocation.getExtent().getDimension().getType().toString().charAt(0);
            }

            long unixTime = new Date().getTime() / 100L; //in 0.1s
            logger.info("should call dbhelper now!");
            dbHelper.logBlockBreak(player.getUniqueId().toString(),blockName,blockX,blockY,blockZ,unixTime,dimensionId);
        }
    }

    @Listener
    public void onBlockPlace (ChangeBlockEvent.Place event, @Root Player player) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for(Transaction<BlockSnapshot> t : transactions) {

            BlockSnapshot placedBlock = t.getFinal();
            logger.info(placedBlock.getLocation().toString());
            String blockName = placedBlock.getState().getType().getName();
            if(blockName.contains(":")) { //Remove prefix
                String[] splitBlockName = blockName.split(":");
                blockName = splitBlockName[1]; //The part after the prefix
            }

            int blockX=0; int blockY=0; int blockZ=0; char dimensionId='O'; //Dimension default is "O" for OVERWORLD. I don't know in which absurd case these defaults would ever be used
            if(!placedBlock.getLocation().isPresent()) {
                logger.error("Wtf why is there no location for the block that was placed?!");
            } else {
                Location<World> blockLocation = placedBlock.getLocation().get();
                blockX = blockLocation.getBlockX();
                blockY = blockLocation.getBlockY();
                blockZ = blockLocation.getBlockZ();
                dimensionId = blockLocation.getExtent().getDimension().getType().toString().charAt(0);
            }

            long unixTime = new Date().getTime() / 100L; //in 0.1s
            logger.info("should call dbhelper now!");
            dbHelper.logBlockPlace(player.getUniqueId().toString(),blockName,blockX,blockY,blockZ,unixTime,dimensionId);
        }
    }
}
