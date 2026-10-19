package com.gamezone.model;

import java.util.List;

/**
 * Memory accessory sold by the store. On top of the data inherited from
 * Accessory it keeps its storage capacity in gigabytes and its memory type.
 */
public class Memory extends Accessory {
    private int capacityInGb;
    private String memoryType;

    /**
     * Creates a memory accessory.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param compatibleConsoleIds ids of the compatible consoles
     * @param capacityInGb storage capacity in gigabytes
     * @param memoryType type of memory, such as SD or microSD
     */
    public Memory(String id, String title, double price, int stock,
                  List<String> compatibleConsoleIds, int capacityInGb, String memoryType) {
        super(id, title, price, stock, compatibleConsoleIds);
        this.capacityInGb = capacityInGb;
        this.memoryType = memoryType;
    }

    /**
     * Returns the type marker of this accessory.
     * @return MEMORY
     */
    @Override
    public String getAccessoryType() {
        return "MEMORY";
    }

    /**
     * Returns the columns that only a memory card has, in the order the accessory
     * file expects them: how much it holds and of what kind it is. A null is
     * written as an empty string, so the word {@code null} never reaches the
     * file.
     * @return a two element list with the capacity and the memory type
     */
    @Override
    public List<String> getExtraFields() {
        return List.of(String.valueOf(capacityInGb),
                memoryType == null ? "" : memoryType);
    }

    /**
     * Builds the description of this memory by adding its capacity and its type
     * to the description of the common accessory data.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return super.getDescription()
                + " | Capacidad: " + capacityInGb + " GB"
                + " | Tipo: " + memoryType;
    }

    /**
     * Returns the storage capacity of this memory.
     * @return capacity in gigabytes
     */
    public int getCapacityInGb() {
        return capacityInGb;
    }

    /**
     * Changes the storage capacity of this memory.
     * @param capacityInGb new capacity in gigabytes
     */
    public void setCapacityInGb(int capacityInGb) {
        this.capacityInGb = capacityInGb;
    }

    /**
     * Returns the type of this memory.
     * @return memory type
     */
    public String getMemoryType() {
        return memoryType;
    }

    /**
     * Changes the type of this memory.
     * @param memoryType new memory type
     */
    public void setMemoryType(String memoryType) {
        this.memoryType = memoryType;
    }
}
