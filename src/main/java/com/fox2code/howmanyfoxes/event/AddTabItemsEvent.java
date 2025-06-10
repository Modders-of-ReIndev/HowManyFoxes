package com.fox2code.howmanyfoxes.event;

import com.fox2code.foxevents.Event;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.Ingredient;

import java.util.ArrayList;

public final class AddTabItemsEvent extends Event {
    private final ArrayList<ItemStack> itemList;

    public AddTabItemsEvent(ArrayList<ItemStack> itemList) {
        this.itemList = itemList;
    }

    public ArrayList<ItemStack> getItemList() {
        return this.itemList;
    }

    public void addItem(ItemStack itemStack) {
        this.itemList.add(itemStack);
    }

    public void removeItems(Ingredient ingredient) {
        this.itemList.removeIf(ingredient::matchIngredient);
    }
}
