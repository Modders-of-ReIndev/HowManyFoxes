package com.fox2code.howmanyfoxes.hmi;

import net.minecraft.client.gui.GuiTextField;

public class GuiTextFieldHMI extends GuiTextField {
   private final int xPos;
   private final int yPos;
   private final int width;
   private final int height;

   public GuiTextFieldHMI(int i, int j, int k, int l, String s) {
      super(i, j, k, l, s);
      this.xPos = i;
      this.yPos = j;
      this.width = k;
      this.height = l;
   }

   public boolean hovered(int posX, int posY) {
      return this.isEnabled && posX >= this.xPos && posX < this.xPos + this.width && posY >= this.yPos && posY < this.yPos + this.height;
   }
}
