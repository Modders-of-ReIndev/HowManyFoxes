package com.fox2code.howmanyfoxes.hmi.tabs;

import com.fox2code.howmanyfoxes.hmi.Utils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.common.item.ItemStack;

public abstract class TabWithTexture extends Tab {
   public final String TEXTURE_PATH;
   public final int TEXTURE_X;
   public final int TEXTURE_Y;
   public final int BUTTON_POS_X;
   public final int BUTTON_POS_Y;
   public int SLOTS_OFFSET_X = 0;
   public int SLOTS_OFFSET_Y = 0;

   public TabWithTexture(
      String tabCreator, int slotsPerRecipe, String texturePath, int width, int height, int minPaddingX, int minPaddingY, int textureX, int textureY
   ) {
      this(tabCreator, slotsPerRecipe, texturePath, width, height, minPaddingX, minPaddingY, textureX, textureY, 0, 0);
   }

   public TabWithTexture(
      String tabCreator,
      int slotsPerRecipe,
      String texturePath,
      int width,
      int height,
      int minPaddingX,
      int minPaddingY,
      int textureX,
      int textureY,
      int buttonX,
      int buttonY
   ) {
      super(tabCreator, slotsPerRecipe, width, height, minPaddingX, minPaddingY);
      this.slots = new Integer[slotsPerRecipe][];
      this.TEXTURE_PATH = texturePath;
      this.TEXTURE_X = textureX;
      this.TEXTURE_Y = textureY;
      this.BUTTON_POS_X = buttonX;
      this.BUTTON_POS_Y = buttonY;
   }

   @Override
   public void draw(int x, int y, int recipeOnThisPageIndex, int cursorX, int cursorY) {
      Utils.bindTexture(this.TEXTURE_PATH);
      Utils.disableLighting();
      Utils.gui.drawTexturedModalRect(x, y, this.TEXTURE_X, this.TEXTURE_Y, this.WIDTH, this.HEIGHT);
   }

   public Boolean drawSetupRecipeButton(GuiScreen parent, ItemStack[] recipeItems) {
      return false;
   }

   public boolean[] itemsInInventory(GuiScreen parent, ItemStack[] recipeItems) {
      return new boolean[]{true};
   }

   public void setupRecipe(GuiScreen parent, ItemStack[] recipeItems) {
   }
}
