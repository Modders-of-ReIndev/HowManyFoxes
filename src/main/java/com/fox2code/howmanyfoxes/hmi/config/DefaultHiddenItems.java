package com.fox2code.howmanyfoxes.hmi.config;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;

import java.util.ArrayList;

public final class DefaultHiddenItems {
    public static final ArrayList<ItemStack> DEFAULT_HIDDEN_ITEMS = new ArrayList<>();

    static {
        short[] simpleIDs = new short[]{
                26, 34, 59, 63, 64, 68, 71, 75, 10, 8, 28,
                23, 22, 36, 51, 69, 76, 119, 120, 121,
                147, 148, 162, 163, 165, 164, 185, 181,
                182, 198, 356, 357, 331, 381, 360, 1019,
                1002, 1001, 183, 184, 109, 112,
                83, 156, 157, 212, 1048, 1058, 1084,
                1085, 1086, 1162, 1164, 1165, 1172, 1174,
                1175, 1294, 1296, 1297, 1411, 1413, 1414,
                1697, 1698, 1699, 1700, 1701, 1702, 1703,
                1704, 1705, 1706, 1707, 1708, 1709, 1710,
                1711, 1712, 1713, 1714, 1715, 1716, 1729,
                1738, 1748, 1756, 1757, 1758, 1759, 1760,
                1761, 1771,
        };

        for (short n : simpleIDs) {
            DEFAULT_HIDDEN_ITEMS.add(n < 256 ?
                    new ItemStack(Blocks.BLOCKS_LIST[n], 1, -1) :
                    new ItemStack(Items.ITEMS_LIST[n], 1, -1));
        }

        short[][] comboIDMeta = new short[][]{
                {360, 0}, {100, 2}, {116, 3}, {130, 4}, {178, 2}, {130, 3},
                {135, 0}, {135, 1}, {135, 2}, {98, 2}, {44, 0}, {44, 1}, {44, 2}
        };

        for (short[] n2 : comboIDMeta) {
            DEFAULT_HIDDEN_ITEMS.add(n2[0] < 256 ?
                    new ItemStack(Blocks.BLOCKS_LIST[n2[0]], 1, n2[1]) :
                    new ItemStack(Items.ITEMS_LIST[n2[0]], 1, n2[1]));
        }
    }
}
