package com.fox2code.howmanyfoxes.hmi.tabs;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.container.ContainerCarpentryTable;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.CarpentryTableRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class TabCarpentry extends TabWithTexture {
    public final static byte WIDTH = ContainerCarpentryTable.WIDTH;
    public final static byte HEIGHT = ContainerCarpentryTable.HEIGHT;
    private static final int TOTAL_SLOTS = ContainerCarpentryTable.TOTAL_SLOTS;
    private final ArrayList<CarpentryRecipe> allRecipes;
    private ArrayList<CarpentryRecipe> recipes;
    private final Block tabBlock;

    public TabCarpentry(String tabCreator) {
        this(tabCreator, CarpentryTableRecipes.instance.getRecipeList(), Blocks.CARPENTRY_TABLE);
    }

    public TabCarpentry(String tabCreator, Int2ObjectMap<ItemStack[]> recipes, Block tabBlock) {
        super(tabCreator, TOTAL_SLOTS + 1, "/textures/gui/container/carpentry_table.png", 163, 74, 16, 0, 6, 36);
        this.allRecipes = new ArrayList<>();
        for (Int2ObjectMap.Entry<ItemStack[]> entry : recipes.int2ObjectEntrySet()) {
            int num = entry.getIntKey();
            int id = num & 0xFFF;
            int metadata = (num >> 30 > 0 ? num >> 13 : 0) & 0xFFFF;
            ItemStack[] results = entry.getValue();
            if (results.length > TOTAL_SLOTS) {
                results = Arrays.copyOf(results, TOTAL_SLOTS);
            }
            this.allRecipes.add(new CarpentryRecipe(new ItemStack(id, 1, metadata), results));
        }
        this.recipes = this.allRecipes;
        this.tabBlock = tabBlock;
        this.slots[0] = new Integer[]{2, 5};
        for (int w = 0; w < WIDTH; w++) {
            for (int h = 0; h < HEIGHT; h++) {
                this.slots[1 + w + (h * WIDTH)] = new Integer[]{56 + w * 18, 5 + h * 18};
            }
        }
        this.equivalentCraftingStations.add(this.getTabItem());
    }

    @Override
    public @NotNull ItemStack getTabItem() {
        return new ItemStack(this.tabBlock);
    }

    @Override
    public ItemStack[][] getItems(int index, ItemStack filter) {
        ItemStack[][] items = new ItemStack[this.recipesPerPage][];
        for(int j = 0; j < this.recipesPerPage; ++j) {
            ItemStack[] recipeItems = new ItemStack[TOTAL_SLOTS + 1];
            int k = index + j;
            if (k < this.recipes.size()) {
                this.recipes.get(k).fillItemGrid(recipeItems);
            }
            items[j] = recipeItems;

            if (items[j][0] == null && this.recipesOnThisPage > j) {
                this.recipesOnThisPage = j;
                this.redrawSlots = true;
                break;
            }

            if (items[j][0] != null && this.recipesOnThisPage == j) {
                this.recipesOnThisPage = j + 1;
                this.redrawSlots = true;
            }
        }

        return items;
    }

    @Override
    public void updateRecipes(ItemStack filter, Boolean getUses) {
        if (filter == null) {
            this.recipes = this.allRecipes;
        } else {
            if (this.recipes == this.allRecipes) {
                this.recipes = new ArrayList<>();
            } else {
                this.recipes.clear();
            }
            if (getUses) {
                for (CarpentryRecipe carpentryRecipe : this.allRecipes) {
                    if (carpentryRecipe.uses(filter)) {
                        this.recipes.add(carpentryRecipe);
                    }
                }
            } else {
                for (CarpentryRecipe carpentryRecipe : this.allRecipes) {
                    if (carpentryRecipe.produce(filter)) {
                        this.recipes.add(carpentryRecipe);
                    }
                }
            }
        }

        this.size = this.recipes.size();
        super.updateRecipes(filter, getUses);
        this.size = this.recipes.size();
    }

    private record CarpentryRecipe(ItemStack ingredient, ItemStack[] results) {
        boolean uses(ItemStack itemStack) {
            return itemStack.getItemID() == this.ingredient.getItemID() &&
                    itemStack.getItemDamage() == this.ingredient.getItemDamage();
        }

        boolean produce(ItemStack itemStack) {
            for (ItemStack result : this.results) {
                if (itemStack.getItemID() == result.getItemID() &&
                        itemStack.getItemDamage() == result.getItemDamage()) {
                    return true;
                }
            }
            return false;
        }

        void fillItemGrid(ItemStack[] items) {
            items[0] = this.ingredient;
            System.arraycopy(this.results, 0, items, 1, this.results.length);
        }
    }
}
