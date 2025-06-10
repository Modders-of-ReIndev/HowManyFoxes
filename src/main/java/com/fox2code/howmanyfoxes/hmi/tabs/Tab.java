package com.fox2code.howmanyfoxes.hmi.tabs;

import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.Ingredient;
import net.minecraft.common.util.i18n.StringTranslate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

public abstract class Tab {
   public int size;
   public ArrayList<ItemStack> equivalentCraftingStations = new ArrayList<>();
   public int index = -2;
   public int recipesPerPage = 1;
   public boolean redrawSlots = false;
   public int recipesOnThisPage = 1;
   public int lastIndex = 0;
   public int autoX = 1;
   public int autoY = 2;
   public final String TAB_CREATOR;
   public Integer[][] slots;
   public final int WIDTH;
   public final int HEIGHT;
   public final int MIN_PADDING_X;
   public final int MIN_PADDING_Y;
   protected Random rand = new Random();

   public Tab(String tabCreator, int slotsPerRecipe, int width, int height, int minPaddingX, int minPaddingY) {
      this.slots = new Integer[slotsPerRecipe][];
      this.WIDTH = width;
      this.HEIGHT = height;
      this.MIN_PADDING_X = minPaddingX;
      this.MIN_PADDING_Y = minPaddingY;
      this.TAB_CREATOR = tabCreator;
   }

   @NotNull
   public abstract ItemStack getTabItem();

   public abstract ItemStack[][] getItems(int var1, ItemStack var2);

   public void updateRecipes(ItemStack filter, Boolean getUses) {
      if (this.size == 0 && getUses == Boolean.TRUE && filter != null) {
         for(ItemStack craftingStation : this.equivalentCraftingStations) {
            if (filter.getItemID() == craftingStation.getItemID() && filter.getItemDamage() == craftingStation.getItemDamage()) {
               this.updateRecipes(null, true);
               break;
            }
         }
      }
   }

   public abstract void draw(int var1, int var2, int var3, int var4, int var5);

   public String name() {
      return StringTranslate.getInstance().translateNamedKey(this.getTabItem().getItemName()).trim();
   }

   public ItemStack passOrRandM1ItemStack(Ingredient ingredient, ItemStack filter) {
      if (ingredient != null && filter != null && ingredient.matchIngredient(filter)) {
         return new ItemStack(filter.getItemID(), 1, filter.itemDamage);
      }
      return randM1ItemStack(ingredient);
   }

   public ItemStack randM1ItemStack(Ingredient ingredient) {
      return ingredient == null ? null : ingredient.getRandomItemStack(rand);
   }
}
