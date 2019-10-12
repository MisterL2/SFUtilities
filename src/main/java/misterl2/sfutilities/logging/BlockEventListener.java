package misterl2.sfutilities.logging;

import misterl2.sfutilities.database.DBHelper;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.*;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockEventListener {
    private Logger logger;
    private DBHelper dbHelper;

    public BlockEventListener(Logger logger, DBHelper dbHelper) {
        this.logger = logger;
        this.dbHelper = dbHelper;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player) {

//        logger.info("TRANSACTIONS");
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for (Transaction<BlockSnapshot> t : transactions) {

            BlockSnapshot original = t.getOriginal();
//            logger.info(original.getLocation().toString());
//            logger.info("Location");


            String blockName = original.getState().getType().getName();
            if (blockName.contains(":")) { //Remove prefix
                String[] splitBlockName = blockName.split(":");
                blockName = splitBlockName[1]; //The part after the prefix
            }

            int blockX = 0;
            int blockY = 0;
            int blockZ = 0;
            char dimensionId = 'O'; //Dimension default is "O" for OVERWORLD. I don't know in which absurd case these defaults would ever be used
            if (!original.getLocation().isPresent()) {
                logger.error("Wtf why is there no location for the block that broke?!");
            } else {
                Location<World> blockLocation = original.getLocation().get();
                blockX = blockLocation.getBlockX();
                blockY = blockLocation.getBlockY();
                blockZ = blockLocation.getBlockZ();
                dimensionId = getDimensionChar(blockLocation);
            }

//            logger.info("should call dbhelper now!");
            dbHelper.logBlockBreak(player.getUniqueId().toString(), blockName, blockX, blockY, blockZ, getUnixTime(), dimensionId);
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        for (Transaction<BlockSnapshot> t : transactions) {

            BlockSnapshot placedBlock = t.getFinal();
//            logger.info(placedBlock.getLocation().toString());
            String blockName = placedBlock.getState().getType().getName();
            if (blockName.contains(":")) { //Remove prefix
                String[] splitBlockName = blockName.split(":");
                blockName = splitBlockName[1]; //The part after the prefix
            }

            int blockX = 0;
            int blockY = 0;
            int blockZ = 0;
            char dimensionId = 'O'; //Dimension default is "O" for OVERWORLD. I don't know in which absurd case these defaults would ever be used
            if (!placedBlock.getLocation().isPresent()) {
                logger.error("Wtf why is there no location for the block that was placed?!");
            } else {
                Location<World> blockLocation = placedBlock.getLocation().get();
                blockX = blockLocation.getBlockX();
                blockY = blockLocation.getBlockY();
                blockZ = blockLocation.getBlockZ();
                dimensionId = getDimensionChar(blockLocation);
            }


//            logger.info("should call dbhelper now!");
            dbHelper.logBlockPlace(player.getUniqueId().toString(), blockName, blockX, blockY, blockZ, getUnixTime(), dimensionId);
        }
    }

    @Listener
    public void onItemChange(ClickInventoryEvent event, @Root Player player) { //Despite the unintuitive name, ClickInventoryName manages transactions between containers by players
        System.out.println("########ON ITEM CHANGE#########");
        List<SlotTransaction> transactions = event.getTransactions();
        if (transactions.isEmpty()) {
            return;
        } // Return immediately to avoid wasting performance on players spam-clicking their inventories


        Inventory parent = transactions.get(0).getSlot().parent(); //Get(0) is guaranteed to exist, as the list is not empty. Parent also necessarily exists, as a slot cannot exist without parent
        if (!(parent instanceof CarriedInventory)) { //The CarriedInventory class also refers to entities that are not exactly "carried", such as stationary chests
            return;
        }

        CarriedInventory containerInv = (CarriedInventory) parent;
        if (!containerInv.getCarrier().isPresent() || !(containerInv.getCarrier().get() instanceof BlockCarrier)) { //No idea when this is the case, but ignore those cases
            return;
        }
        BlockCarrier carrier = (BlockCarrier) containerInv.getCarrier().get();
        int capacity = containerInv.first().capacity();

        Predicate<SlotTransaction> filter = slotTransaction -> {
            if (slotTransaction.getOriginal().getType() == ItemTypes.NONE && slotTransaction.getFinal().getType() == ItemTypes.NONE) { //Empty transaction, random click
                System.out.println("Empty slot transfer!");
                return false;
            }
            int index = getIndex(slotTransaction);
            if (index < 0 || index >= capacity) {
                System.out.println("Transfer outside of container!");
                return false;
            }
            return true;
        };

        List<SlotTransaction> validTransactions = transactions.stream().filter(filter).collect(Collectors.toList());

        Location<World> chestLocation = carrier.getLocation();



        for(SlotTransaction validTransaction : validTransactions) {
            if(carrier instanceof  MultiBlockCarrier) { //If it is a doublechest, overwrite the chestLocation value with the value of the specific sub-chest being targeted
                chestLocation = getMultiBlockCarrierLocation((MultiBlockCarrier) carrier, getIndex(validTransaction), capacity);
            }
            char action = 'I'; // I = Insert, R = Remove
            String blockName = validTransaction.getOriginal().getType().getName();
            System.out.println(blockName);
            System.out.println("??");
            System.out.println(validTransaction.getFinal().getQuantity());
            System.out.println(validTransaction.getOriginal().getQuantity());
            int quantityChange = validTransaction.getFinal().getQuantity() - validTransaction.getOriginal().getQuantity();
            if(quantityChange < 0) { //Itemstack quantity is lower after the transaction than it was before.
                action = 'R';
                blockName = validTransaction.getFinal().getType().getName();
            }
            dbHelper.logChestInteraction(player.getUniqueId().toString(),action, blockName, quantityChange, chestLocation.getBlockX(), chestLocation.getBlockY(), chestLocation.getBlockZ(), getUnixTime(), getDimensionChar(chestLocation));
        }

    }

    private long getUnixTime() {
        return new Date().getTime() / 100L; //in 0.1s
    }

    private char getDimensionChar(Location<World> location) {
        return location.getExtent().getDimension().getType().toString().charAt(0);
    }

    private int getIndex(SlotTransaction transaction) {
        return transaction.getSlot().getInventoryProperty(SlotIndex.class).map(SlotIndex::getValue).orElse(-1);
    }

    private Location<World> getMultiBlockCarrierLocation(MultiBlockCarrier multiBlockCarrier, int slotIndex, int capacity) {
        System.out.println("Multiblocky boi");
        Location<World> initialLocation = multiBlockCarrier.getLocation(); // Base location, used as fallback for unsupported e.g. TripleChest
        List<Location<World>> locations = multiBlockCarrier.getLocations();
        if (locations.size() > 2) {
            logger.warn("Logging does not work correctly for multi-block-containers consisting of more than 2 blocks (e.g. TripleChests)!");
            return initialLocation;
        }


        //Chest with the highest X / Z value out of the two connected chests is at the BOTTOM of the doublechest.
        //"capacity - slotIndex*2 - 1" -> NEGATIVE for bottom, POSITIVE for upper half. This, combined with 'min', always returns the correct chest based on the slotIndex
        Optional<Location<World>> maybeChestCoords = locations.stream().min(Comparator.comparingInt(o -> (capacity - slotIndex*2 - 1) * (o.getBlockX() + o.getBlockZ())));

        if(!maybeChestCoords.isPresent()) {
            logger.error("Attempting to log chest transaction: Targeted MultiBlockCarrier does not have ANY locations!");
            return initialLocation; //Fallback
        }

        return maybeChestCoords.get();
    }



}

//        if(carrier instanceof MultiBlockCarrier) {
//            MultiBlockCarrier c = (MultiBlockCarrier) carrier;
//            List<Location<World>> locations = c.getLocations();
//            for (Location<World> location: locations) {
//                System.out.println(location.getBlockX());
//                System.out.println(location.getBlockY());
//                System.out.println(location.getBlockZ());
//                System.out.println("xxxxxxxxxxxxxxxx");
//                BlockState block = location.getBlock();
//                System.out.println(block);
//                System.out.println(block.getType().getName());
//
////                if (block instanceof Chest) {
////                    System.out.println("IT's a chest boy!");
////                }
//                if (block instanceof CarriedInventory) {
//                    System.out.println("Carried inventory");
//                    CarriedInventory carriedBlock = (CarriedInventory) block;
//                    System.out.println(carriedBlock.getName().get());
//                    System.out.println(carriedBlock.getCarrier().get().toString());
//                    Object curry = carriedBlock.getCarrier().get();
//                    if (curry instanceof Chest) {
//                        Chest chestCurry = (Chest) curry;
//                        System.out.println(".....................ddddddddddddd");
//                        System.out.println(chestCurry.getInventory());
//                        System.out.println(chestCurry.getDoubleChestInventory());
//
//                    } else {
//                        System.out.println("Not a chest WTF");
//                    }
//                }
//                Optional<Inventory> inventory = c.getInventory(location);
//                if(inventory.isPresent()) {
//                    Inventory inventory1 = inventory.get();
//                    System.out.println(inventory1);
//                } else {
//                    System.out.println("Not present 23124892!");
//                }
//            }
//
//        } else {
//            System.out.println("NOT A MultiBlockCarrier");
//        }
//        System.out.println(carrier);
//        System.out.println(carrier.hashCode());
//        System.out.println(carrier.getClass());
//        Location<World> location = carrier.getLocation();
//        int blockX = location.getBlockX();
//        int blockY = location.getBlockY();
//        int blockZ = location.getBlockZ();
//        System.out.println(blockX);
//        System.out.println(blockY);
//        System.out.println(blockZ);
//        System.out.println(player.getName());
//        System.out.println("######------#######");
////        System.out.println(player.getInventory());
//        System.out.println(player.getInventory().root());
////        System.out.println(carrier.getInventory());
//        System.out.println(carrier.getInventory().root());
//        System.out.println("----d--------d-");
//        for (SlotTransaction transaction: transactions) {
//            System.out.println(transaction.getSlot().parent());
//            Inventory parent1 = transaction.getSlot().parent();
//            System.out.println(parent1.hasChildren());
//            System.out.println(parent1.capacity());
//            ItemStackSnapshot original = transaction.getOriginal();
//            System.out.println("Original: " + original.getQuantity() + " : " + original.getType());
//            ItemStackSnapshot aFinal = transaction.getFinal();
//            System.out.println("Final: " + aFinal.getQuantity() + " : " + aFinal.getType());
//
//        }
//        System.out.println(event.getClass().toString());
//        System.out.println("-------------------");
//
//    }
//
//}
//        Transaction<ItemStackSnapshot> cursorTransaction = event.getCursorTransaction();
//        Container targetInventory = event.getTargetInventory();
//        System.out.println("Container:");
//        System.out.println(targetInventory.getName().get());
//
//        int i = targetInventory.totalItems();
//        System.out.println("Total items in the container: " + i);
//
//        Map<EventContextKey<?>, Object> eventContextKeyObjectMap = event.getContext().asMap();
//        System.out.println("----------------------");
//        for (EventContextKey key: eventContextKeyObjectMap.keySet()) {
//            System.out.println("Key");
//            System.out.println(key.getAllowedType());
//            System.out.println(key.getName());
//            System.out.println("Value");
//            System.out.println(eventContextKeyObjectMap.get(key));
//        }
//        System.out.println("-----------------------------------------------");
//        ItemStackSnapshot original = cursorTransaction.getOriginal();
//        System.out.println("Original");
//        int quantity = original.getQuantity();
//        System.out.println(quantity);
//        ItemType type = original.getType();
//        System.out.println(type);
//        System.out.println("Afterwards");
//        ItemStackSnapshot aFinal = cursorTransaction.getFinal();
//        int quantity1 = aFinal.getQuantity();
//        System.out.println(quantity1);
//        ItemType type1 = aFinal.getType();
//        System.out.println(type1);