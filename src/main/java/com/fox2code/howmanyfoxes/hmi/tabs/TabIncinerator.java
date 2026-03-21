package com.fox2code.howmanyfoxes.hmi.tabs;

import com.fox2code.howmanyfoxes.hmi.Utils;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.tileentity.TileEntityIncinerator;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.item.children.ItemFood;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TabIncinerator extends TabWithTexture {
    private final ArrayList<ItemStack> fuels = new ArrayList<>();
    private final ArrayList<ItemStack[]> recipes = new ArrayList<>();

    public TabIncinerator(String tabCreator) {
        super(
                tabCreator, 19,
                "/textures/gui/container/incinerator.png",
                166, 58,
                5, 5,
                5, 14
        );

        for (int i = 0; i < 9; i++) {
            this.slots[i] = new Integer[]{
                    3 + (i % 3) * 18,
                    6 + (i / 3) * 18
            };
        }

        for (int i = 0; i < 9; i++) {
            this.slots[i + 9] = new Integer[]{
                    111 + (i % 3) * 18,
                    6 + (i / 3) * 18
            };
        }

        this.slots[18] = new Integer[]{75, 42};

        for (ItemStack item : Utils.itemList()) {
            if (item == null) continue;
            if(
                    (item.getItemID() < 256 && Blocks.BLOCKS_LIST[item.getItemID()].blockMaterial == Materials.WOOD) ||
                            (item.getItem().getBurnTimeViaType(1) >> 1 > 0)
            ) {
                this.fuels.add(item);
            }
        }

        this.equivalentCraftingStations.add(this.getTabItem());
    }

    @Override
    public ItemStack getTabItem() {
        return new ItemStack(Blocks.INCINERATOR);
    }

    @Override
    public ItemStack[][] getItems(int index, ItemStack filter) {
        ItemStack[][] items = new ItemStack[this.recipesPerPage][];

        for (int j = 0; j < this.recipesPerPage; ++j) {
            items[j] = new ItemStack[this.slots.length];
            int k = index + j;
            if (k < this.recipes.size()) {
                ItemStack[] recipe = this.recipes.get(k);

                items[j][0] = recipe[0];
                items[j][9] = recipe[1];
                items[j][18] = this.fuels.get(this.rand.nextInt(this.fuels.size()));
            }

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
        this.lastIndex = 0;
        this.recipes.clear();

        if (getUses && (filter != null)) {
            if(TileEntityIncinerator.isItemValidFuel(filter)) {
                this.recipes.add(new ItemStack[]{filter.copy(), this.returnOutput(filter)});
            }
        } else if (filter != null) {
            //TODO: hope that incinerator will have an output chances list in the future
            if (filter.getItemID() == Items.BONE.itemID) this.recipes.addAll(this.collectAllFood(new ItemStack(Items.BONE)));
            if (filter.getItemID() == Items.DYE_POWDER.itemID && filter.getItemDamage() == 15) this.recipes.addAll(this.collectAllFood(new ItemStack(Items.DYE_POWDER, 1, 15)));
            if (filter.getItemID() == Items.COAL.itemID) this.recipes.addAll(this.collectAllItems(new ItemStack(Items.COAL)));
            if (filter.getItemID() == Items.ASH.itemID) this.recipes.addAll(this.collectAllItems(new ItemStack(Items.ASH)));
        }

        this.size = this.recipes.size();
        super.updateRecipes(filter, getUses);
        this.size = this.recipes.size();
    }

    //TODO: Hope that incinerator will have a static method to give an output from input itemstack
    private ItemStack returnOutput(ItemStack input) {
        ItemStack output = new ItemStack(Items.ASH, 1, 0);
        if (input.getItem() instanceof ItemFood) {
            if (this.rand.nextInt(8) == 0) {
                output = new ItemStack(Items.BONE, 1, 0);
            } else if (this.rand.nextInt(4) == 0) {
                output = new ItemStack(Items.DYE_POWDER, 1, 15);
            } else if (this.rand.nextBoolean()) {
                output = new ItemStack(Items.COAL, 1, 0);
            }
        } else if (this.rand.nextInt(8) == 0) {
            output = new ItemStack(Items.COAL, 1, 0);
        }
        return output;
    }

    private Collection<ItemStack[]> collectAllFood(ItemStack output) {
        final List<ItemStack[]> list = new ArrayList<>();
        for (ItemStack item : Utils.itemList()) {
            if (!TileEntityIncinerator.isItemValidFuel(item)) continue;
            if (item.getItem() instanceof ItemFood) {
                list.add(new ItemStack[]{item, output});
            }
        }
        return list;
    }

    private Collection<ItemStack[]> collectAllItems(ItemStack output) {
        final List<ItemStack[]> list = new ArrayList<>();
        for (ItemStack item : Utils.itemList()) {
            if(TileEntityIncinerator.isItemValidFuel(item)) {
                list.add(new ItemStack[] {item, output});
            }
        }
        return list;
    }
}
