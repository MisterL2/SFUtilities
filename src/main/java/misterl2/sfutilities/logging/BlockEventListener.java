package misterl2.sfutilities.logging;

import misterl2.sfutilities.SFUtilities;
import misterl2.sfutilities.database.DBHelper;
import misterl2.sfutilities.database.datatypes.ChestLogRow;
import misterl2.sfutilities.database.datatypes.ItemTransfer;
import misterl2.sfutilities.database.datatypes.LocationDataClass;
import misterl2.sfutilities.database.datatypes.LogRow;
import misterl2.sfutilities.util.TimeConverter;
import org.slf4j.Logger;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.MultiBlockCarrier;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockEventListener {
    private SFUtilities plugin;
    private Logger logger;
    private DBHelper dbHelper;

    public BlockEventListener(SFUtilities plugin, Logger logger, DBHelper dbHelper) {
        this.logger = logger;
        this.dbHelper = dbHelper;
        this.plugin = plugin;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Task.builder().execute(() -> {
            for (Transaction<BlockSnapshot> t : transactions) {

                BlockSnapshot brokenBlock = t.getOriginal();

                if (!brokenBlock.getLocation().isPresent()) {
                    logger.error("Wtf why is there no location for the block that was placed?!");
                    return;
                }

                Location<World> blockLocation = brokenBlock.getLocation().get();
                int blockX = blockLocation.getBlockX();
                int blockY = blockLocation.getBlockY();
                int blockZ = blockLocation.getBlockZ();

                LocationDataClass locationDataClass = new LocationDataClass(blockLocation.getExtent().getUniqueId(), getDimensionChar(blockLocation), blockX, blockY, blockZ);
                LogRow logRow = new LogRow(getNiceBlockName(brokenBlock.getState().getType()),'B', TimeConverter.getUnixTimeSinceRelease(), player.getUniqueId(), locationDataClass);
                dbHelper.logBlockBreak(logRow);
            }
        }).async().submit(plugin);
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player) {
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Task.builder().execute(() -> {
            for (Transaction<BlockSnapshot> t : transactions) {

                BlockSnapshot placedBlock = t.getFinal();
//            logger.info(placedBlock.getLocation().toString());


                if (!placedBlock.getLocation().isPresent()) {
                    logger.error("Wtf why is there no location for the block that was placed?!");
                    return;
                }

                Location<World> blockLocation = placedBlock.getLocation().get();
                int blockX = blockLocation.getBlockX();
                int blockY = blockLocation.getBlockY();
                int blockZ = blockLocation.getBlockZ();

//            logger.info("should call dbhelper now!");
                LocationDataClass locationDataClass = new LocationDataClass(blockLocation.getExtent().getUniqueId(), getDimensionChar(blockLocation), blockX, blockY, blockZ);
                LogRow logRow = new LogRow(getNiceBlockName(placedBlock.getState().getType()), 'P', TimeConverter.getUnixTimeSinceRelease(), player.getUniqueId(), locationDataClass);
                dbHelper.logBlockPlace(logRow);
            }
        }).async().submit(plugin);
    }


    @Listener
    public void onItemChange(ClickInventoryEvent event, @Root Player player) { //Despite the unintuitive name, ClickInventoryName manages transactions between containers by players
//        System.out.println("########ON ITEM CHANGE#########");
        List<SlotTransaction> transactions = event.getTransactions();
        if (transactions.isEmpty()) {
            return;
        }


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
//        System.out.println("Transaction noted!");
        Predicate<SlotTransaction> filter = slotTransaction -> {
            if (slotTransaction.getOriginal().getType() == ItemTypes.NONE && slotTransaction.getFinal().getType() == ItemTypes.NONE) { //Empty transaction, random click
//                System.out.println("Empty slot transfer!");
                return false;
            }
            int index = getIndex(slotTransaction);
            if (index < 0 || index >= capacity) {
//                System.out.println("Transfer outside of container!");
                return false;
            }
            return true;
        };

        List<SlotTransaction> validTransactions = transactions.stream().filter(filter).collect(Collectors.toList());
        if(validTransactions.isEmpty()) {
            return;
        }
        Location<World> chestLocation = carrier.getLocation();

        List<ItemTransfer> combinedItemTransfers = new ArrayList<>();

        long timeSinceRelease = TimeConverter.getUnixTimeSinceRelease();

        for(SlotTransaction validTransaction : validTransactions) {
            if(carrier instanceof  MultiBlockCarrier) { //If it is a doublechest, overwrite the chestLocation value with the value of the specific sub-chest being targeted
                chestLocation = getMultiBlockCarrierLocation((MultiBlockCarrier) carrier, getIndex(validTransaction), capacity);
            }

            int quantityChange = validTransaction.getFinal().getQuantity() - validTransaction.getOriginal().getQuantity();
            char action = quantityChange > 0 ? 'I' : 'R';
            ItemType itemtype = action == 'I' ? validTransaction.getFinal().getType() : validTransaction.getOriginal().getType();
            boolean alreadyInList = false;

            for(ItemTransfer itemTransfer : combinedItemTransfers) {
                if(itemTransfer.getAction() != action) {
                    continue;
                }

                if(!itemTransfer.getBlockName().equals(getNiceBlockName(itemtype))) {
                    continue;
                }

                itemTransfer.addQuantityChange(quantityChange);
                alreadyInList = true;
            }

            if(!alreadyInList) {
                ItemTransfer itemTransfer = new ItemTransfer(getNiceBlockName(itemtype), quantityChange);
                combinedItemTransfers.add(itemTransfer);
            }
        }

        //Now save into DB

        UUID playerUUID = player.getUniqueId();
        int x = chestLocation.getBlockX(); int y = chestLocation.getBlockY(); int z = chestLocation.getBlockZ();
        UUID worldId = chestLocation.getExtent().getUniqueId();
        char dimensionChar = getDimensionChar(chestLocation);
        Task.builder().execute(() -> {
            for(ItemTransfer itemTransfer : combinedItemTransfers) {
                LocationDataClass locationDataClass = new LocationDataClass(worldId, dimensionChar, x, y, z);
                ChestLogRow chestLogRow = new ChestLogRow(itemTransfer.getBlockName(), itemTransfer.getAmount(), timeSinceRelease, playerUUID, locationDataClass);
                dbHelper.logChestInteraction(chestLogRow);
            }
        }).async().submit(plugin);
    }


    private char getDimensionChar(Location<World> location) {
        return location.getExtent().getDimension().getType().toString().charAt(0);
    }

    private int getIndex(SlotTransaction transaction) {
        return transaction.getSlot().getInventoryProperty(SlotIndex.class).map(SlotIndex::getValue).orElse(-1);
    }

    private Location<World> getMultiBlockCarrierLocation(MultiBlockCarrier multiBlockCarrier, int slotIndex, int capacity) {
//        System.out.println("Multiblocky boi");
        Location<World> initialLocation = multiBlockCarrier.getLocation(); // Base location, used as fallback for unsupported e.g. TripleChest
        List<Location<World>> locations = multiBlockCarrier.getLocations();

        //Chest with the highest X / Z value out of the two connected chests is at the BOTTOM of the doublechest.
        //"capacity - slotIndex*2 - 1" -> NEGATIVE for bottom, POSITIVE for upper half. This, combined with 'min', always returns the correct chest based on the slotIndex
        Optional<Location<World>> maybeChestCoords = locations.stream().min(Comparator.comparingInt(o -> (capacity - slotIndex*2 - 1) * (o.getBlockX() + o.getBlockZ())));

        if(!maybeChestCoords.isPresent()) {
            logger.error("Attempting to log chest transaction: Targeted MultiBlockCarrier does not have ANY locations!");
            return initialLocation; //Fallback
        }

        return maybeChestCoords.get();
    }

    private String getNiceBlockName(CatalogType type) {
        String blockName = type.getName();
        if (blockName.contains(":")) { //Remove prefix
            return blockName.split(":")[1]; //The part after the prefix
        }
        return blockName;
    }

}
