package com.fox2code.howmanyfoxes.hmi.tabs;

import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.recipe.RefridgifreezerRecipes;

import java.util.ArrayList;

public class TabFreezing extends TabSmelting {
    public TabFreezing(String tabCreator) {
        super(tabCreator, RefridgifreezerRecipes.instance.getSmeltingList(),
                new ArrayList<>(), "/textures/gui/container/refridgifreezer.png", Blocks.REFRIDGIFREEZER_IDLE);
        for(Item item : Items.ITEMS_LIST) {
            if (item != null && this.getItemBurnTime(new ItemStack(item)) > 0) {
                this.getFuels().add(new ItemStack(item));
            }
        }
    }

    @Override
    protected int getItemBurnTime(ItemStack itemStack) {
        return Items.ITEMS_LIST[itemStack.getItem().itemID].getBurnTimeViaType(2);
    }
}
