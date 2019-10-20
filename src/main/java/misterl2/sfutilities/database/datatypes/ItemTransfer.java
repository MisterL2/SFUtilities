package misterl2.sfutilities.database.datatypes;

import org.spongepowered.api.item.ItemType;

public class ItemTransfer {
    private String blockName;
    private int amount;
    private char action;


    public ItemTransfer(String blockName, int amount) {
        this.blockName=blockName; this.amount=amount;
        this.action = amount > 0 ? 'I' : 'R';
    }

    public ItemTransfer(String blockName, int amount, char action) {
        this.blockName=blockName; this.amount=amount; this.action = action;
    }

    public char getAction() {
        return this.action;
    }

    public String getBlockName() {
        return this.blockName;
    }

    public int getAmount() {
        return this.amount;
    }

    public void addQuantityChange(int amount) {
        this.amount += amount;
    }


}
