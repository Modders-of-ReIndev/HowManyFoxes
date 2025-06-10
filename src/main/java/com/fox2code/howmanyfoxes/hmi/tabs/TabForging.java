package com.fox2code.howmanyfoxes.hmi.tabs;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.recipe.BlastFurnaceRecipes;

public class TabForging extends TabSmelting {
    public TabForging(String tabCreator, TabSmelting tabSmelting) {
        super(tabCreator, BlastFurnaceRecipes.instance.getSmeltingList(),
                tabSmelting.getFuels(), "/textures/gui/container/furnace.png", Blocks.FORGE_IDLE);
    }
}
