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
//        Cause cause = event.getCause();
//        logger.info("CAUSE");
//        logger.info(cause.root().toString());
//        for (Object o : cause.all()) {
//            logger.info(o.toString());
//        }
//
//
//        logger.info("CONTEXT");
//        EventContext context = event.getContext();
//
//        Set<EventContextKey<?>> keySet = context.keySet();
//        Map<EventContextKey<?>, Object> map = context.asMap();
//
//        for (EventContextKey key : keySet) {
//            logger.info("Key");
//            logger.info(key.getName());
//            logger.info(key.getAllowedType().getSimpleName());
//            logger.info(key.getId());
//            logger.info(key.toString());
//            logger.info("Value");
//            Object o = map.get(key);
//            logger.info(o.toString());
//        }
//
//
//
//        logger.info("SOURCE");
//        Object source = event.getSource();
//        logger.info(source.toString());

        logger.info("TRANSACTIONS");
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for(Transaction<BlockSnapshot> t : transactions) {

            BlockSnapshot original = t.getOriginal();
            logger.info(original.getLocation().toString());
//            logger.info(original.getCreator().toString());
//            logger.info(original.getExtendedState().toString());
//            logger.info(original.getState().toString());
//            logger.info(original.getNotifier().toString());
//
//            BlockSnapshot tDefault = t.getDefault();
//
//            logger.info(tDefault.getCreator().toString());
//            logger.info(tDefault.getExtendedState().toString());
//            logger.info(tDefault.getState().toString());
//            logger.info(tDefault.getNotifier().toString());

//            BlockSnapshot aFinal = t.getFinal();

            logger.info("Location");


//            logger.info(aFinal.getCreator().toString());
//            logger.info(aFinal.getExtendedState().toString());
//            logger.info(aFinal.getState().toString());
//            logger.info(aFinal.getNotifier().toString());


            //The below should go in a seperate thread for performance

            //logger.info(original.getState().getName());

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

            long unixTime = new Date().getTime() / 1000L;
            logger.info("should call dbhelper now!");
            dbHelper.logBlockBreak(player.getUniqueId().toString(),blockName,blockX,blockY,blockZ,unixTime,dimensionId);
        }
    }
}
