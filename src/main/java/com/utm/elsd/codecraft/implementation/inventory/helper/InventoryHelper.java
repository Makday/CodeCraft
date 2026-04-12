package com.utm.elsd.codecraft.implementation.inventory.helper;

public final class InventoryHelper {

    private InventoryHelper() {}

    /**
     * Maps (row, col) to the slot index used by PlayerScreenHandler.
     *
     * PlayerScreenHandler layout:
     *   0        – crafting output
     *   1–4      – crafting grid
     *   5–8      – armor
     *   9–35     – main inventory (rows 0–2, left-to-right, top-to-bottom)
     *   36–44    – hotbar (row 3)
     *   45       – off-hand
     */
    public static int calculateScreenSlot(int row, int col) {
        if (row == 3) {
            return 36 + col;
        }
        return 9 + row * 9 + col;
    }

    public static boolean isValidSlot(int row, int col) {
        return row >= 0 && row <= 3 && col >= 0 && col <= 8;
    }
}