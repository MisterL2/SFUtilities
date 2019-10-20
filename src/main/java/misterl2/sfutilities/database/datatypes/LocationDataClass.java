package misterl2.sfutilities.database.datatypes;

import java.util.UUID;

public class LocationDataClass { //Probably redundant, but decoupled from sponge
    private UUID worldId;
    private char dimensionId;
    private int x;
    private int y;
    private int z;

    public LocationDataClass(UUID worldId, char dimensionId, int x, int y, int z) {
        this.worldId = worldId; this.dimensionId = dimensionId; this.x = x; this.y = y; this.z = z;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public char getDimensionId() {
        return dimensionId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
