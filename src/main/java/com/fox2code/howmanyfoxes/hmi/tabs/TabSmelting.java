package com.fox2code.howmanyfoxes.hmi.tabs;

import com.fox2code.foxloader.event.client.GuiItemInfoEvent;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.recipe.FurnaceRecipes;
import net.minecraft.common.util.i18n.StringTranslate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TabSmelting extends TabWithTexture {
   protected Map<Integer, ItemStack> recipesComplete;
   protected ArrayList<ItemStack[]> recipes = new ArrayList<>();
   private ArrayList<ItemStack> fuels;
   private final Block tabBlock;
   private final int metadata;
   private boolean damagedFurnaceInput = false;

   protected int getItemBurnTime(ItemStack itemStack) {
      if (itemStack == null) {
         return 0;
      } else {
         int id = itemStack.getItem().itemID;
         return id < 256 && Blocks.BLOCKS_LIST[id].blockMaterial == Materials.WOOD ? 300 :
                 Items.ITEMS_LIST[itemStack.getItem().itemID].getBurnTimeViaType(1);
      }
   }

   public TabSmelting(String tabCreator) {
      this(tabCreator, new HashMap<>(), new ArrayList<>(), "/textures/gui/container/furnace.png", Blocks.FURNACE_IDLE);
      this.recipesComplete = FurnaceRecipes.instance.getSmeltingList();
      // this.fuels.add(new ItemStack(Items.STICK));
      // this.fuels.add(new ItemStack(Items.COAL));
      this.fuels.add(new ItemStack(Items.LAVA_BUCKET));
      // this.fuels.add(new ItemStack(Blocks.sapling));

      for(Block block : Blocks.BLOCKS_LIST) {
         if (block != null
            && (block.blockMaterial == Materials.WOOD ||
                 this.getItemBurnTime(new ItemStack(block)) > 0)
            && block.blockID != 63
            && block.blockID != 64
            && block.blockID != 68
            && block.blockID != 95) {
            this.fuels.add(new ItemStack(block));
         }
      }

      for(Item item : Items.ITEMS_LIST) {
         if (item != null && this.getItemBurnTime(new ItemStack(item)) > 0) {
            this.fuels.add(new ItemStack(item));
         }
      }

      this.damagedFurnaceInput = false;
   }

   public TabSmelting(String tabCreator, Map<Integer, ItemStack> recipes, ArrayList<ItemStack> fuels, String texturePath, Block tabBlock) {
      this(tabCreator, recipes, fuels, texturePath, tabBlock, 0);
   }

   public TabSmelting(String tabCreator, Map<Integer, ItemStack> recipes, ArrayList<ItemStack> fuels, String texturePath, Block tabBlock, int metadata) {
      this(tabCreator, 3, recipes, fuels, texturePath, 84, 56, 54, 15, tabBlock, metadata);
   }

   public TabSmelting(
      String tabCreator,
      int slotsPerRecipe,
      Map<Integer, ItemStack> recipes,
      ArrayList<ItemStack> fuels,
      String texturePath,
      int width,
      int height,
      int textureX,
      int textureY,
      Block tabBlock,
      int metadata
   ) {
      this(tabCreator, slotsPerRecipe, texturePath, width, height, textureX, textureY, tabBlock, metadata);
      this.recipesComplete = recipes;
      this.fuels = fuels;
   }

   public TabSmelting(
      String tabCreator, int slotsPerRecipe, String texturePath, int width, int height, int textureX, int textureY, Block tabBlock, int metadata
   ) {
      super(tabCreator, slotsPerRecipe, texturePath, width, height, 3, 3, textureX, textureY);
      this.tabBlock = tabBlock;
      this.metadata = metadata;
      this.slots[0] = new Integer[]{62, 23};
      this.slots[1] = new Integer[]{2, 5};
      if (slotsPerRecipe > 2) {
         this.slots[2] = new Integer[]{2, 41};
      }

      this.equivalentCraftingStations.add(this.getTabItem());
   }

   @Override
   public ItemStack[][] getItems(int index, ItemStack filter) {
      ItemStack[][] items = new ItemStack[this.recipesPerPage][];

      for(int j = 0; j < this.recipesPerPage; ++j) {
         items[j] = new ItemStack[this.slots.length];
         int k = index + j;
         if (k < this.recipes.size()) {
            ItemStack[] recipe = this.recipes.get(k);

            for(int i = 0; i < recipe.length; ++i) {
               items[j][i] = recipe[i];
               if (recipe[i] != null && recipe[i].getItemDamage() == -1) {
                  if (filter != null && recipe[i].getItemID() == filter.getItemID()) {
                     items[j][i] = new ItemStack(recipe[i].getItem(), 0, filter.getItemDamage());
                  } else {
                     items[j][i] = randM1ItemStack(recipe[i]);
                  }
               }
            }

            if (this.fuels != null) {
               items[j][2] = this.fuels.get(this.rand.nextInt(this.fuels.size()));
            }
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
      this.recipes.clear();
      this.updateRecipesWithoutClear(filter, getUses);
   }

   public void updateRecipesWithoutClear(ItemStack filter, Boolean getUses) {
      this.lastIndex = 0;

      for(Integer obj : this.recipesComplete.keySet()) {
         int dmg = 0;
         if (filter != null) {
            dmg = filter.getItemDamage();
         }

         ItemStack output = this.recipesComplete.get(obj);
         ItemStack input = null;
         if (obj != null) {
            if (obj < Items.ITEMS_LIST.length) {
               input = new ItemStack(Items.ITEMS_LIST[obj], 1, dmg);
            } else {
               if (!this.damagedFurnaceInput
                  || obj - (output.getItemDamage() << 16) >= Items.ITEMS_LIST.length
                  || Blocks.BLOCKS_LIST[obj - (output.getItemDamage() << 16)] == null) {
                  continue;
               }

               input = new ItemStack(Blocks.BLOCKS_LIST[obj - (output.getItemDamage() << 16)], 1, output.getItemDamage());
            }
         }

         if (filter == null || getUses && input != null && input.getItemID() == filter.getItemID()
                 || !getUses && output.getItemID() == filter.getItemID() &&
                 (output.getItemDamage() == filter.getItemDamage() ||
                         output.getItemDamage() < 0 || !output.getHasSubtypes())) {
            this.recipes.add(new ItemStack[]{output, input});
         }
      }

      this.size = this.recipes.size();
      super.updateRecipes(filter, getUses);
      this.size = this.recipes.size();
   }

   @Override
   public @NotNull ItemStack getTabItem() {
      return new ItemStack(this.tabBlock, 1, this.metadata);
   }

   @Override
   public void onAdditionalTooltipInfo(GuiItemInfoEvent event) {
      int burnTime = this.getItemBurnTime(event.getItemStack());
      if (burnTime > 0) {
         event.addDescriptionLine(StringTranslate.getInstance().translateKeyFormat("hmf.smelting.burntime", burnTime));
      }
   }

   public ArrayList<ItemStack> getFuels() {
      return fuels;
   }
}
