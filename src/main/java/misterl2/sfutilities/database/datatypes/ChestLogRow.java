package misterl2.sfutilities.database.datatypes;

import misterl2.sfutilities.util.TimeConverter;

import java.util.UUID;

public class ChestLogRow extends LogRow {
    private int amount;

    public ChestLogRow(String blockName, int amount, long timeSince, UUID playerUUID, LocationDataClass location) {
        super(blockName, amount > 0 ? 'I' : 'R', timeSince, playerUUID, location);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        String timeString = TimeConverter.secondsToTimeString(getUnixTimeSinceRelease());
        String action;
        if(getAction() == 'I') {
            action = "inserted";
        } else if(getAction() == 'R') {
            action = "removed";
        } else {
            action ="invalid_action";
            System.out.println("Unsupported action found in database!"); //Cbb including a logger in this class
        }
        String player = getPlayerName().isPresent() ? getPlayerName().get() : getPlayerUUID().toString();
        return player + " " + action + " x" + Math.abs(getAmount()) + " " + getBlockName() + " " + timeString + " ago!";
    }
}
