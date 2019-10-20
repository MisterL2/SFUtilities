package misterl2.sfutilities.database.datatypes;

import misterl2.sfutilities.util.TimeConverter;
import org.spongepowered.api.item.ItemType;

import java.util.Optional;
import java.util.UUID;

public class LogRow {
    private long timeSince;
    private UUID playerUUID;
    private LocationDataClass location;
    private String blockName;
    private char action;

    private String playerName; //Nullable


    public LogRow(String blockName, char action, long timeSince, UUID playerUUID, LocationDataClass location) {
        this.blockName = blockName;
        this.action = action;
        this.timeSince = timeSince;
        this.playerUUID = playerUUID;
        this.location = location;
    }

    public long getUnixTimeSinceRelease() {
        return this.timeSince;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public LocationDataClass getLocation() {
        return location;
    }

    public char getAction() {
        return this.action;
    }

    public String getBlockName() {
        return this.blockName;
    }

    public Optional<String> getPlayerName() {
        return Optional.ofNullable(playerName);
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String toString() {
        String timeString = TimeConverter.secondsToTimeString(getUnixTimeSinceRelease());
        String action;
        if(getAction() == 'B') {
            action = "broke";
        } else if(getAction() == 'P') {
            action = "placed";
        } else {
            action ="invalid_action";
            System.out.println("Unsupported action found in database!"); //Can't be bothered including a logger in this class
        }
        String player = getPlayerName().isPresent() ? getPlayerName().get() : getPlayerUUID().toString();
        return player + " " + action + " " + getBlockName() + " " + timeString + " ago!";
    }
}
