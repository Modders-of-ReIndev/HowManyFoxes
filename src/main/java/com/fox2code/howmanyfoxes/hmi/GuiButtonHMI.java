package com.fox2code.howmanyfoxes.hmi;

import com.indigo3d.util.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.common.item.ItemStack;
import org.lwjgl.input.Keyboard;

public class GuiButtonHMI extends GuiButton {
   public int iconIndex;
   private ItemStack item;

   public GuiButtonHMI(int id, int x, int y, int width, int height, String text) {
      super(id, x, y, width, height, text);
      this.iconIndex = -1;
   }

   public GuiButtonHMI(int id, int x, int y, int width, int iconIndex) {
      super(id, x, y, width, width, "");
      this.iconIndex = iconIndex;
   }

   public GuiButtonHMI(int id, int x, int y, int width, int iconIndex, ItemStack item) {
      this(id, x, y, width, iconIndex);
      this.item = item;
   }

   @Override
   public void drawElement(Minecraft minecraft, float x, float y, float deltaTicks) {
      if (this.visible) {
         FontRenderer fontrenderer = minecraft.fontRenderer;
         Utils.bindTexture("/textures/gui/gui.png");
         RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
         boolean isHovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
         int k = this.getHoverState(isHovered);
         this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
         this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height / 2);
         this.drawTexturedModalRect(this.xPosition, this.yPosition + this.height / 2, 0, 46 + k * 20 + 20 - this.height / 2, this.width / 2, this.height / 2);
         this.drawTexturedModalRect(
            this.xPosition + this.width / 2,
            this.yPosition + this.height / 2,
            200 - this.width / 2,
            46 + k * 20 + 20 - this.height / 2,
            this.width / 2,
            this.height / 2
         );
         this.mouseDragged(minecraft, x, y);
         if (this.item == null || !Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54)) {
            if (this.iconIndex < 0) {
               if (!this.enabled) {
                  this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, -6250336);
               } else if (isHovered) {
                  this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 16777120);
               } else {
                  this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 14737632);
               }
            } else {
               Utils.bindTexture();
               this.drawTexturedModalRect(this.xPosition, this.yPosition, this.iconIndex % 12 * 21, this.iconIndex / 12 * 21, this.width, this.width);
            }
         } else {
            Utils.drawItemStack(this.xPosition + 2, this.yPosition + 2, this.item, true);
         }
      }
   }
}
