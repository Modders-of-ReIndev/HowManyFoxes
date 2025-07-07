package com.fox2code.howmanyfoxes.hmi.tabs;

import com.fox2code.howmanyfoxes.hmi.GuiRecipeViewer;
import com.indigo3d.util.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.tileentity.TileEntityDungeonChest;
import net.minecraft.common.entity.animals.EntityCucurboo;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.loot.LootTable;
import net.minecraft.common.loot.LootTableEntry;
import net.minecraft.common.util.i18n.StringTranslate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

public class TabLootHints extends TabWithTexture {

    private static final Random RANDOM = new Random();

    public static class FancyPackedLoot {
        public final ItemStack resourceItem;
        public final LootTable lootTable;
        public final String customName;
        public final int totalWeight;

        public FancyPackedLoot(ItemStack resourceItem, String customName, LootTable lootTable) {
            this.resourceItem = resourceItem;
            this.lootTable = lootTable;
            this.customName = customName;

            int weight_accumulation = 0;
            for (int i = 0; i < lootTable.getLootTableEntries().size(); i++) {
                weight_accumulation += lootTable.getLootTableEntries().get(i).getItemWeight();
            }

            //I simply make sure it won't divide by 0 at the end lmao
            this.totalWeight = (weight_accumulation == 0) ? 1 : weight_accumulation;
        }

        public FancyPackedLoot(ItemStack resourceItem, LootTable lootTable) {
            this(resourceItem, null, lootTable);
        }

        public ItemStack[] getLootAsArray() {
            ItemStack[] stacks = new ItemStack[lootTable.getLootTableEntries().size()];

            for (int i = 0; i < stacks.length; i++) {
                stacks[i] = fakeGenerateLoot(lootTable.getLootTableEntries().get(i));
            }

            return stacks;
        }

        public ItemStack[][] getLootAsSplitArrays(int chunkSize) {
            final ItemStack[] source = this.getLootAsArray();

            if (source.length < chunkSize) {
                return new ItemStack[][]{source};
            }

            final int numberOfChunks = (int) Math.ceil((double) source.length / chunkSize);
            final ItemStack[][] output = new ItemStack[numberOfChunks][];

            for (int i = 0; i < numberOfChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(source.length, start + chunkSize);
                output[i] = Arrays.copyOfRange(source, start, end);
            }

            return output;
        }

        public int getNumberOfPages(int chunkSize) {
            int source = lootTable.getLootTableEntries().size();
            return (int) Math.ceil((double) source / chunkSize);
        }

        public boolean hasSuchLoot(ItemStack itemStack) {
            if (itemStack == null) {
                return false;
            }

            for (LootTableEntry table : this.lootTable.getLootTableEntries()) {
                if (table.getItemStack().getItemID() == itemStack.getItemID()) {
                    return true;
                }
            }

            return false;
        }

        public ItemStack getDisplayItemStack() {
            ItemStack stack = this.resourceItem.copy();
            if (this.customName != null) {
                stack.setItemName(StringTranslate.getInstance().translateKey(this.customName));
            }
            return stack;
        }

        private ItemStack fakeGenerateLoot(LootTableEntry entry) {
            final ItemStack stack = entry.getItemStack().copy();

            if (RANDOM.nextDouble() * 100.0 < entry.getDropChance() * 100.0) {
                stack.stackSize = entry.getMinimumDropQuantity();
                if (entry.getRandomDropQuantity() > 0) {
                    stack.stackSize = stack.stackSize + RANDOM.nextInt(entry.getRandomDropQuantity() + 1);
                }
            }

            return stack;
        }
    }

    private final FancyPackedLoot currentLoot;
    private final ItemStack[] possibleLoot;
    private final String title;

    public TabLootHints(String tabCreator, FancyPackedLoot currentLoot) {
        super(
                tabCreator,
                29,
                "/assets/hmf/textures/gui/loot_hint.png",
                126, 108,
                5, 5,
                0, 0
        );
        this.currentLoot = currentLoot;
        this.possibleLoot = this.currentLoot.getLootAsArray();
        this.title = StringTranslate.getInstance().translateKeyFormat("hmf.loothints.title", this.currentLoot.getDisplayItemStack().getDisplayName());

        //input slot
        this.slots[0] = new Integer[]{55, 4};

        //loots slot
        for (int i = 0; i < 28; i++) {
            this.slots[i + 1] = new Integer[]{1 + (i % 7) * 18, 40 + (i / 7) * 18};
        }
    }

    @Override
    public String name() {
        return this.title;
    }

    @Override
    public @NotNull ItemStack getTabItem() {
        return this.currentLoot.getDisplayItemStack();
    }

    @Override
    public ItemStack[][] getItems(int index, ItemStack filter) {
        ItemStack[][] items = new ItemStack[this.recipesPerPage][];

        for (int j = 0; j < this.recipesPerPage; ++j) {
            items[j] = new ItemStack[this.slots.length];
            int k = index + j;

            final ItemStack[][] sets = currentLoot.getLootAsSplitArrays(28);

            if (k < sets.length) {
                final ItemStack[] data = sets[k];
                items[j][0] = currentLoot.resourceItem;
                for (ItemStack itemStack : data) {
                    if (itemStack.stackSize == 0) {
                        itemStack.stackSize = 1;
                    }
                }
                System.arraycopy(data, 0, items[j], 1, data.length);
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
        updateRecipesWithoutClear(filter, getUses);
    }

    public void updateRecipesWithoutClear(ItemStack filter, Boolean getUses) {
        lastIndex = 0;
        boolean has = false;
        final int countPage = this.currentLoot.getNumberOfPages(28);

        final ItemStack input = this.currentLoot.resourceItem;

        if (filter == null || (getUses && input != null && input.getItemID() == filter.getItemID()) || (!getUses && currentLoot.hasSuchLoot(filter))) {
            has = true;
        }

        this.size = has ? countPage : 0;
        super.updateRecipes(filter, getUses);
        this.size = has ? countPage : 0;
    }

    public boolean isAimedAtLoot(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        for (ItemStack stack : possibleLoot) {
            if (itemStack.getItemID() == stack.getItemID() && itemStack.getItemDamage() == stack.getItemDamage()) {
                return true;
            }
        }

        return false;
    }

    public float getChanceOf(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }

        for (LootTableEntry entry : currentLoot.lootTable.getLootTableEntries()) {
            if (entry.getItemStack().getItemDamage() == stack.getItemDamage() &&
                    entry.getItemStack().getItemID() == stack.getItemID()) {
                double weightReducer = 1D;
                if (entry.getMinimumDropQuantity() == 0) {
                    weightReducer = 1D - (1D / entry.getRandomDropQuantity());
                }

                return Math.round(10000F * weightReducer *
                        (entry.getItemWeight() / (float) currentLoot.totalWeight)) / 100F;
            }
        }

        return 0.0f;
    }

    public int[] getPossibleAmountOf(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        for (LootTableEntry entry : currentLoot.lootTable.getLootTableEntries()) {
            if (entry.getItemStack().getItemDamage() == stack.getItemDamage() && entry.getItemStack().getItemID() == stack.getItemID()) {
                if (entry.getRandomDropQuantity() == 0) {
                    return new int[]{
                            entry.getMinimumDropQuantity()
                    };
                }

                int offset = 0;
                if (entry.getMinimumDropQuantity() == 0) {
                    offset = 1;
                }

                return new int[]{
                        entry.getMinimumDropQuantity() + offset,
                        entry.getMinimumDropQuantity() + entry.getRandomDropQuantity() - offset
                };
            }
        }

        return null;
    }

    /**
     * Get a complete list of possible loots of each item/block
     */
    public static FancyPackedLoot[] fetchKnownLootDetails() {
        final FancyPackedLoot[] data = new FancyPackedLoot[3];

        //CUCURBOO
        data[0] = new FancyPackedLoot(
                new ItemStack(Blocks.CUCURBOO_TOMBSTONE),
                yoinkLootTable(EntityCucurboo.class, "CUCURBOO_LOOT_TABLE")
        );

        //NETHER DUNGEON CHEST
        data[1] = new FancyPackedLoot(
                new ItemStack(Blocks.DUNGEON_CHEST_ACTIVE),
                yoinkLootTable(TileEntityDungeonChest.class, "DUNGEON_LOOT_TABLE")
        );

        //CLOVER
        final float fixed1p13chance = (float) 1 / 13;
        //clover doesn't have LootTable, so I created a copy based on chances from pickLootItem
        data[2] = new FancyPackedLoot(
                new ItemStack(Blocks.CLOVER),
                new LootTable(
                        new LootTableEntry(1, new ItemStack(Items.STICKY_TORCH), fixed1p13chance, 1, 7),
                        new LootTableEntry(1, new ItemStack(Items.GOLD_INGOT), fixed1p13chance, 1, 4),
                        new LootTableEntry(1, new ItemStack(Items.DYNAMITE), fixed1p13chance, 1, 4),
                        new LootTableEntry(1, new ItemStack(Items.EMPTY_BUCKET), fixed1p13chance),
                        new LootTableEntry(1, new ItemStack(Items.POTION), fixed1p13chance),
                        new LootTableEntry(1, new ItemStack(Items.CHAINMAIL_HELMET), fixed1p13chance * 1 / 4),
                        new LootTableEntry(1, new ItemStack(Items.CHAINMAIL_CHESTPLATE), fixed1p13chance * 1 / 4),
                        new LootTableEntry(1, new ItemStack(Items.CHAINMAIL_LEGGINGS), fixed1p13chance * 1 / 4),
                        new LootTableEntry(1, new ItemStack(Items.CHAINMAIL_BOOTS), fixed1p13chance * 1 / 4),
                        new LootTableEntry(1, new ItemStack(Items.THIRTEEN_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.CAT_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.BLOCKS_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.CHIRP_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.FAR_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.MALL_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.MELLOHI_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.STAL_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.STRAD_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.WARD_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.ELEVEN_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.WAIT_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.DOG_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.FAE_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.FROZEN_RECORD), fixed1p13chance * 1 / 15),
                        new LootTableEntry(1, new ItemStack(Items.IRON_INGOT), fixed1p13chance, 1, 4),
                        new LootTableEntry(1, new ItemStack(Items.WHEAT), fixed1p13chance, 1, 5),
                        new LootTableEntry(1, new ItemStack(Items.GUNPOWDER), fixed1p13chance, 1, 5),
                        new LootTableEntry(1, new ItemStack(Items.BREAD), fixed1p13chance, 1, 4),
                        new LootTableEntry(1, new ItemStack(Blocks.SPONGE), fixed1p13chance, 1, 7),
                        new LootTableEntry(1, new ItemStack(Items.SADDLE), fixed1p13chance)
                )
        );

        return data;
    }

    /* as most of them are "private static final", we have to "reflect" through them */
    private static LootTable yoinkLootTable(Class<?> target, String name) {
        try {
            Field field = target.getDeclaredField(name);
            field.setAccessible(true);
            return (LootTable) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * basic helper code for showing tooltip details
     * based on basically checking if the current GUI points to this tab,
     * returns null otherwise
     */
    public static TabLootHints tooltipHelper(GuiScreen screen) {
        if (screen instanceof GuiRecipeViewer viewer) {
            if (GuiRecipeViewer.tabs.get(viewer.tabIndex) instanceof TabLootHints tab) {
                return tab;
            }
        }
        return null;
    }
}
