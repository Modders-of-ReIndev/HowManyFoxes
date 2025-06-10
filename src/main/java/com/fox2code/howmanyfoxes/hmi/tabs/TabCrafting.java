package com.fox2code.howmanyfoxes.hmi.tabs;

import com.fox2code.foxloader.recipe.ReshapeRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.player.EntityPlayerSP;
import net.minecraft.client.player.PlayerController;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.container.Slot;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class TabCrafting extends TabWithTexture {
   protected List<IRecipe> recipesComplete;
   protected List<IRecipe> recipes;
   private final int slotsWidth;
   private final Block tabBlock;
   private boolean isVanillaWorkbench;
   public ArrayList<Class<? extends GuiContainer>> guiCraftingStations;
   protected ScaledResolution scaledResolution = new ScaledResolution();

   public TabCrafting(String tabCreator) {
      this(tabCreator, new ArrayList<>(CraftingManager.getInstance().getRecipeList()), Blocks.CRAFTING_TABLE);

      for(int i = 0; i < this.recipesComplete.size(); ++i) {
         if (this.recipesComplete.get(i).getRecipeSize() > 9) {
            this.recipesComplete.remove(i);
            --i;
         }
      }

      this.isVanillaWorkbench = true;
      this.guiCraftingStations.add(GuiContainerCrafting.class);
   }

   public TabCrafting(String tabCreator, List<IRecipe> recipesComplete, Block tabBlock) {
      this(tabCreator, 10, recipesComplete, tabBlock, "/textures/gui/container/crafting.png", 118, 56, 28, 15, 56, 46, 3);
      this.slots[0] = new Integer[]{96, 23};
   }

   public TabCrafting(
      String tabCreator,
      int slotsPerRecipe,
      List<IRecipe> recipesComplete,
      Block tabBlock,
      String texturePath,
      int width,
      int height,
      int textureX,
      int textureY,
      int buttonX,
      int buttonY,
      int slotsWidth
   ) {
      super(tabCreator, slotsPerRecipe, texturePath, width, height, 3, 4, textureX, textureY, buttonX, buttonY);
      this.isVanillaWorkbench = false;
      this.guiCraftingStations = new ArrayList<>();
      this.slotsWidth = slotsWidth;
      this.recipesComplete = recipesComplete;
      this.tabBlock = tabBlock;
      this.recipes = recipesComplete;
      int i = 1;

      for(int l = 0; l < 3; ++l) {
         for(int i2 = 0; i2 < slotsWidth; ++i2) {
            this.slots[i++] = new Integer[]{2 + i2 * 18, 5 + l * 18};
         }
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
            IRecipe irecipe = this.recipes.get(k);
            if (irecipe instanceof ShapedRecipes shapedRecipe) {
               Ingredient[] aitemstack = shapedRecipe.getIngredients();
               items[j][0] = irecipe.getRecipeOutput();

               int l = shapedRecipe.getWidth();
               for(int k1 = 0; k1 < aitemstack.length; ++k1) {
                  int l1 = k1 % l;
                  int i2 = k1 / l;
                  items[j][l1 + i2 * slotsWidth + 1] =
                          passOrRandM1ItemStack(aitemstack[k1], filter);
               }
            } else if (irecipe instanceof ShapelessRecipes) {
               items[j][0] = irecipe.getRecipeOutput();

               List<Ingredient> list = ((ShapelessRecipes)irecipe).getIngredients();
               for(int j2 = 0; j2 < list.size(); ++j2) {
                  Ingredient ingredient = list.get(j2);
                  items[j][j2 + 1] = passOrRandM1ItemStack(ingredient, filter);
               }
            } else if (irecipe instanceof ReshapeRecipe) {
               items[j][0] = irecipe.getRecipeOutput();

               List<Ingredient> list = ((ReshapeRecipe)irecipe).getIngredients();
               for(int j2 = 0; j2 < list.size(); ++j2) {
                  Ingredient ingredient = list.get(j2);
                  items[j][j2 + 1] = passOrRandM1ItemStack(ingredient, filter);
               }
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
      List<IRecipe> arraylist = new ArrayList<>();
      if (filter == null) {
         this.recipes = this.recipesComplete;
      } else {
         for(IRecipe irecipe : this.recipesComplete) {
            if (!getUses
               && filter.getItemID() == irecipe.getRecipeOutput().getItemID()
               && (
                  irecipe.getRecipeOutput().getItemDamage() == filter.getItemDamage()
                     || irecipe.getRecipeOutput().getItemDamage() < 0
                     || !irecipe.getRecipeOutput().getHasSubtypes()
               )) {
               arraylist.add(irecipe);
            } else if (irecipe instanceof ShapedRecipes && getUses) {
               try {
                  Ingredient[] aitemstack = ((ShapedRecipes)irecipe).getIngredients();

                  for(Ingredient itemstack1 : aitemstack) {
                     if (itemstack1 != null && itemstack1.matchIngredient(filter)) {
                        arraylist.add(irecipe);
                        break;
                     }
                  }
               } catch (Exception var11) {
                  var11.printStackTrace();
               }
            } else if (irecipe instanceof ShapelessRecipes && getUses) {
               try {
                  for(Ingredient obj : ((ShapelessRecipes)irecipe).getIngredients()) {
                     if (obj != null && obj.matchIngredient(filter)) {
                        arraylist.add(irecipe);
                        break;
                     }
                  }
               } catch (Exception var12) {
                  var12.printStackTrace();
               }
            } else if (irecipe instanceof ReshapeRecipe && getUses) {
               try {
                  for(Ingredient obj : ((ReshapeRecipe)irecipe).getIngredients()) {
                     if (obj != null && obj.matchIngredient(filter)) {
                        arraylist.add(irecipe);
                        break;
                     }
                  }
               } catch (Exception var12) {
                  var12.printStackTrace();
               }
            }
         }

         this.recipes = arraylist;
      }

      this.size = this.recipes.size();
      super.updateRecipes(filter, getUses);
      this.size = this.recipes.size();
   }

   @Override
   public @NotNull ItemStack getTabItem() {
      return new ItemStack(this.tabBlock);
   }

   @Override
   public Boolean drawSetupRecipeButton(GuiScreen parent, ItemStack[] recipeItems) {
      for(Class<? extends GuiContainer> gui : this.guiCraftingStations) {
         if (gui.isInstance(parent)) {
            return true;
         }
      }

      if (this.isVanillaWorkbench && (parent instanceof GuiContainerInventory || parent == null)) {
         for(int i = 3; i < 10; ++i) {
            if (i != 4 && i != 5 && recipeItems[i] != null) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean[] itemsInInventory(GuiScreen parent, ItemStack[] recipeItems) {
      boolean[] itemsInInv = new boolean[this.slots.length - 1];
      Minecraft mc = Minecraft.theMinecraft;
      List<Slot> list = parent instanceof GuiContainer ?
              ((GuiContainer)parent).inventorySlots.slots :
              mc.thePlayer.currentContainer != null ?
                      mc.thePlayer.currentContainer.slots :
                      mc.thePlayer.playerContainer.slots;

      if (list.isEmpty()) {
         return itemsInInv;
      }

      ItemStack[] aslot = new ItemStack[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         if (list.get(i).getHasStack()) {
            aslot[i] = list.get(i).getStack().copy();
         }
      }

      aslot[0] = null;

      label54:
      for(int i = 1; i < recipeItems.length; ++i) {
         ItemStack item = recipeItems[i];
         if (item == null) {
            itemsInInv[i - 1] = true;
         } else {
            for(ItemStack slot : aslot) {
               if (slot != null
                  && slot.stackSize > 0
                  && slot.getItemID() == item.getItemID()
                  && (slot.getItemDamage() == item.getItemDamage() || item.getItemDamage() < 0 || !item.getHasSubtypes())) {
                  --slot.stackSize;
                  itemsInInv[i - 1] = true;
                  continue label54;
               }
            }

            itemsInInv[i - 1] = false;
         }
      }

      return itemsInInv;
   }

   private int recipeStackSize(List<Slot> list, ItemStack[] recipeItems) {
      int[] itemStackSize = new int[recipeItems.length - 1];

      for(int i = 1; i < recipeItems.length; ++i) {
         ItemStack[] aslot = new ItemStack[list.size()];

         for(int k = 0; k < list.size(); ++k) {
            if (list.get(k).getHasStack()) {
               aslot[k] = list.get(k).getStack().copy();
            }
         }

         aslot[0] = null;
         ItemStack item = recipeItems[i];
         itemStackSize[i - 1] = 0;
         if (item == null) {
            itemStackSize[i - 1] = -1;
         } else {
            int stackSize = 0;

            for(ItemStack slot : aslot) {
               if (slot != null
                  && slot.stackSize > 0
                  && slot.getItemID() == item.getItemID()
                  && (slot.getItemDamage() == item.getItemDamage() || item.getItemDamage() < 0 || !item.getHasSubtypes())) {
                  stackSize += slot.stackSize;
                  slot.stackSize = 0;
               }
            }

            int prevEqualItemCount = 1;

            for(int j = 1; j < i; ++j) {
               if (recipeItems[j] != null && recipeItems[j].isItemEqual(item)) {
                  ++prevEqualItemCount;
               }
            }

            for(int j = 1; j < recipeItems.length; ++j) {
               if (recipeItems[j] != null && recipeItems[j].isItemEqual(item)) {
                  itemStackSize[j - 1] = stackSize / prevEqualItemCount;
               }
            }
         }
      }

      int finalItemStackSize = -1;

      for(int l = 0; l < itemStackSize.length; ++l) {
         ItemStack item = recipeItems[l + 1];
         if (item == null) continue;
         if (itemStackSize[l] != -1 && item.getMaxStackSize() != 1 && (finalItemStackSize == -1 || itemStackSize[l] < finalItemStackSize)) {
            finalItemStackSize = itemStackSize[l];
         }
      }

      return finalItemStackSize > 0 ? finalItemStackSize : 1;
   }

   @Override
   public void setupRecipe(GuiScreen parent, ItemStack[] recipeItems) {
      if (parent == null) {
         Minecraft mc = Minecraft.theMinecraft;
         mc.setIngameNotInFocus();
         this.scaledResolution.setDimensions(mc.gameSettings, mc.displayWidth, mc.displayHeight);
         int i = this.scaledResolution.getScaledWidth();
         int j = this.scaledResolution.getScaledHeight();
         parent = new GuiContainerInventory(mc.thePlayer);
         (mc.currentScreen = parent).setWorldAndResolution(mc, i, j);
         mc.skipRenderWorld = false;
      }

      List<Slot> list = ((GuiContainer)parent).inventorySlots.slots;
      int recipeStackSize = 1;
      if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
         recipeStackSize = this.recipeStackSize(list, recipeItems);
      }

      EntityPlayerSP player = Minecraft.getInstance().thePlayer;
      PlayerController inv = Minecraft.getInstance().playerController;
      int x = ((GuiContainer)parent).inventorySlots.windowId;

      for(int k = 1; k < recipeItems.length; ++k) {
         ItemStack item = recipeItems[k];
         Slot currentSlot = list.get(k);
         if (parent instanceof GuiContainerInventory && k > 5) {
            break;
         }

         if (currentSlot.getHasStack()) {
            inv.clickSlot(x, k, 0, 1, player);
            if (currentSlot.getHasStack()) {
               inv.clickSlot(x, k, 0, 0, player);
               if (player.inventory.getCursorStack() != null) {
                  for(int l = k + 1; l < list.size(); ++l) {
                     Slot slot = list.get(l);
                     if (!slot.getHasStack()) {
                        inv.clickSlot(x, l, 0, 0, player);
                        break;
                     }
                  }

                  if (player.inventory.getCursorStack() != null) {
                     inv.clickSlot(x, -999, 0, 0, player);
                  }
               }
            }
         }

         if (item != null) {
            while(!currentSlot.getHasStack() || currentSlot.getStack().stackSize < recipeStackSize && currentSlot.getStack().getMaxStackSize() > 1) {
               for(int l = k + 1; l < list.size(); ++l) {
                  Slot slot = list.get(l);
                  if (slot.getHasStack()
                     && slot.getStack().getItemID() == item.getItemID()
                     && (slot.getStack().getItemDamage() == item.getItemDamage() || item.getItemDamage() < 0 || !item.getHasSubtypes())) {
                     inv.clickSlot(x, l, 0, 0, player);
                     if (parent instanceof GuiContainerInventory && k > 3) {
                        inv.clickSlot(x, k - 1, 1, 0, player);
                        currentSlot = list.get(k - 1);
                     } else {
                        inv.clickSlot(x, k, 1, 0, player);
                     }

                     inv.clickSlot(x, l, 0, 0, player);
                     break;
                  }
               }
            }
         }
      }
   }
}
