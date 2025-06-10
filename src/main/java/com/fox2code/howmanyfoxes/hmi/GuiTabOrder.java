package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import com.fox2code.howmanyfoxes.HMIClient;
import com.indigo3d.util.RenderSystem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.renderer.world.Tessellator;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

public class GuiTabOrder extends GuiScreen {
   private final GuiScreen parentScreen;
   private int right;
   private int bottom;
   private final int BUTTON_HEIGHT = 20;
   private final int left;
   private final int top;
   private final int slotHeight;
   private float scrollMultiplier;
   private int selectedButton = -1;
   private float amountScrolled = 0.0F;
   private float initialClickY = -2.0F;
   private long lastClicked = 0L;
   private final ArrayList<Tab> allTabs;
   private final ArrayList<Tab> currentTabs;
   private final Tab[] newOrder;
   private final boolean[] tabEnabled;

   public GuiTabOrder(GuiScreen guiscreen) {
      this.parentScreen = guiscreen;
      this.left = 0;
      this.top = 32;
      this.slotHeight = 21;
      this.currentTabs = HMIClient.getTabs();
      this.allTabs = HMIClient.allTabs;
      this.newOrder = new Tab[this.allTabs.size()];
      this.tabEnabled = new boolean[this.allTabs.size()];
   }

   @Override
   public void initGui() {
      this.amountScrolled = 0.0F;
      this.right = this.width + 80;
      this.bottom = this.height - 51;

      for(int i = 0; i < this.currentTabs.size(); ++i) {
         this.newOrder[i] = this.currentTabs.get(i);
         this.tabEnabled[i] = true;
         this.controlList.add(new GuiButtonHMI(this.controlList.size(), -1, -1, BUTTON_HEIGHT, 3));
         if (i == 0) {
            ((GuiButton)this.controlList.get(this.controlList.size() - 1)).enabled = false;
         }

         this.controlList.add(new GuiButtonHMI(this.controlList.size(), -1, -1, BUTTON_HEIGHT, 4));
         String s = this.currentTabs.get(i).TAB_CREATOR.getClass().getSimpleName().replaceFirst("mod_", "");
         s = s + " - " + this.currentTabs.get(i).name() + ": Enabled";
         this.controlList.add(new GuiSmallButton(this.controlList.size(), -1, -1, 268, BUTTON_HEIGHT, s));
      }

      for(Tab allTab : this.allTabs) {
         if (!this.currentTabs.contains(allTab)) {
            this.newOrder[this.controlList.size() / 3] = allTab;
            this.tabEnabled[this.controlList.size() / 3] = false;
            this.controlList.add(new GuiButtonHMI(this.controlList.size(), -1, -1, BUTTON_HEIGHT, 3));
            ((GuiButton)this.controlList.get(this.controlList.size() - 1)).enabled = false;
            this.controlList.add(new GuiButtonHMI(this.controlList.size(), -1, -1, BUTTON_HEIGHT, 4));
            ((GuiButton)this.controlList.get(this.controlList.size() - 1)).enabled = false;
            String s = allTab.TAB_CREATOR.getClass().getSimpleName().replaceFirst("mod_", "");
            s = s + " - " + allTab.name() + ": Disabled";
            this.controlList.add(new GuiSmallButton(this.controlList.size(), -1, -1, 268, BUTTON_HEIGHT, s));
         }
      }

      if (this.controlList.size() >= 3) {
         ((GuiButton)this.controlList.get(this.controlList.size() - 2)).enabled = false;
      }

      StringTranslate stringtranslate = StringTranslate.getInstance();
      this.controlList
         .add(
            new GuiSmallButton(
               this.controlList.size(), this.width / 2 - 154, this.height - 39, "Gui Size: " + (Config.recipeViewerDraggableGui ? "Draggable" : "Auto")
            )
         );
      this.controlList.add(new GuiSmallButton(this.controlList.size(), this.width / 2 + 4, this.height - 39, stringtranslate.translateKey("gui.done")));
   }

   @Override
   public void onGuiClosed() {
      HMIClient.tabOrderChanged(this.tabEnabled, this.newOrder);
   }

   @Override
   protected void actionPerformed(GuiButton guibutton) {
      if (guibutton.id == this.controlList.size() - 1) {
         this.mc.displayGuiScreen(this.parentScreen);
      } else if (guibutton.id == this.controlList.size() - 2) {
         Config.recipeViewerDraggableGui = !Config.recipeViewerDraggableGui;
         guibutton.displayString = "Gui Size: " + (Config.recipeViewerDraggableGui ? "Draggable" : "Auto");
         HMIClient.onSettingChanged();
      } else {
         if (guibutton.id % 3 == 2) {
            this.tabEnabled[guibutton.id / 3] = !this.tabEnabled[guibutton.id / 3];
            if (this.tabEnabled[guibutton.id / 3]) {
               int pos = guibutton.displayString.lastIndexOf("Disabled");
               if (pos > -1) {
                  guibutton.displayString = guibutton.displayString.substring(0, pos) + "Enabled";
               }
            } else {
               int pos = guibutton.displayString.lastIndexOf("Enabled");
               if (pos > -1) {
                  guibutton.displayString = guibutton.displayString.substring(0, pos) + "Disabled";
               }
            }
         } else {
            int upOrDown = 0;
            if (guibutton.id % 3 == 0) {
               upOrDown = -1;
            } else if (guibutton.id % 3 == 1) {
               upOrDown = 1;
            }

            int index = guibutton.id / 3;
            boolean tempBool = this.tabEnabled[index];
            this.tabEnabled[index] = this.tabEnabled[index + upOrDown];
            this.tabEnabled[index + upOrDown] = tempBool;
            Tab tempTab = this.newOrder[index];
            this.newOrder[index] = this.newOrder[index + upOrDown];
            this.newOrder[index + upOrDown] = tempTab;
            String tempString = ((GuiButton)this.controlList.get(index * 3 + 2)).displayString;
            ((GuiButton)this.controlList.get(index * 3 + 2)).displayString = ((GuiButton)this.controlList.get((index + upOrDown) * 3 + 2)).displayString;
            ((GuiButton)this.controlList.get((index + upOrDown) * 3 + 2)).displayString = tempString;
         }

         for(int i = 0; i < this.newOrder.length; ++i) {
            ((GuiButton)this.controlList.get(i * 3)).enabled = this.tabEnabled[i];
            ((GuiButton)this.controlList.get(i * 3 + 1)).enabled = this.tabEnabled[i];
            if (i == 0) {
               ((GuiButton)this.controlList.get(i * 3)).enabled = false;
            } else if (i == this.newOrder.length - 1) {
               ((GuiButton)this.controlList.get(i * 3 + 1)).enabled = false;
            }
         }
      }
   }

   public void updateScrolled(float amount) {
      int i = this.getContentHeight() - (this.bottom - this.top - 4);
      if (i < 0) {
         i /= 2;
      }

      this.amountScrolled += amount;
      if (this.amountScrolled < 0.0F) {
         this.amountScrolled = 0.0F;
      } else if (this.amountScrolled > (float)i) {
         this.amountScrolled = (float)i;
      }
   }

   @Override
   public void keyTyped(char key, int keyId) {
      if (this.selectedButton >= 0) {
         this.selectedButton = -1;
      } else {
         super.keyTyped(key, keyId);
      }
   }

   @Override
   public void handleMouseInput() {
      int amount = Mouse.getEventDWheel();
      if (amount != 0 && this.getContentHeight() - (this.bottom - this.top - 4) > 0) {
         byte var2;
         if (amount > 0) {
            var2 = -1;
         } else {
            var2 = 1;
         }

         this.updateScrolled((float)(var2 * this.slotHeight / 2));
      }

      super.handleMouseInput();
   }

   protected int getContentHeight() {
      return this.getSize() * this.slotHeight + 2;
   }

   public int getSize() {
      return this.allTabs.size();
   }

   @Override
   public void drawScreen(float mouseX, float mouseY, float deltaTicks) {
      int size = this.getSize();
      int l = this.right / 2 + 124;
      int i1 = l + 6;
      if (Mouse.isButtonDown(0)) {
         if (this.initialClickY == -1.0F) {
            boolean flag = true;
            if (mouseY >= this.top && mouseY <= this.bottom) {
               int j1 = this.width / 2 - 110;
               int k1 = this.width / 2 + 110;
               int i2 =  (int) mouseY - this.top + (int)this.amountScrolled - 4;
               int k2 = i2 / this.slotHeight;
               if (mouseX >= j1 && mouseX <= k1 && k2 >= 0 && i2 >= 0 && k2 < size) {
                  this.lastClicked = System.currentTimeMillis();
               } else if (mouseX >= j1 && mouseX <= k1 && i2 < 0) {
                  flag = false;
               }

               if (mouseX >= l && mouseX <= i1) {
                  this.scrollMultiplier = -1.0F;
                  int i3 = this.getContentHeight() - (this.bottom - this.top - 4);
                  if (i3 < 1) {
                     i3 = 1;
                  }

                  int l2 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());
                  if (l2 < 32) {
                     l2 = 32;
                  }

                  if (l2 > this.bottom - this.top - 8) {
                     l2 = this.bottom - this.top - 8;
                  }

                  this.scrollMultiplier /= (float)(this.bottom - this.top - l2) / (float)i3;
               } else {
                  this.scrollMultiplier = 1.0F;
               }

               if (flag) {
                  this.initialClickY = mouseY;
               } else {
                  this.initialClickY = -2.0F;
               }
            } else {
               this.initialClickY = -2.0F;
            }
         } else if (this.initialClickY >= 0.0F) {
            this.amountScrolled -= (mouseY - this.initialClickY) * this.scrollMultiplier;
            this.initialClickY = mouseY;
         }
      } else {
         this.initialClickY = -1.0F;
      }

      this.bindAmountScrolled();
      RenderSystem.disableLighting();
      RenderSystem.disableFog();
      Tessellator tessellator = Tessellator.instance;
      RenderSystem.bindTexture2D("/textures/gui/background.png");
      RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
      float f2 = 32.0F;
      tessellator.startDrawingQuads();
      tessellator.setColorOpaque_I(2105376);
      tessellator.addVertexWithUV(
              this.left, this.bottom, 0.0, (float)this.left / 32.0F, (float)(this.bottom + (int)this.amountScrolled) / 32.0F
      );
      tessellator.addVertexWithUV(
              this.right, this.bottom, 0.0, (float)this.right / 32.0F, (float)(this.bottom + (int)this.amountScrolled) / 32.0F
      );
      tessellator.addVertexWithUV(
              this.right, this.top, 0.0, (float)this.right / 32.0F, (float)(this.top + (int)this.amountScrolled) / 32.0F
      );
      tessellator.addVertexWithUV(
              this.left, this.top, 0.0, (float)this.left / 32.0F, (float)(this.top + (int)this.amountScrolled) / 32.0F
      );
      tessellator.draw();
      int k3 = this.top + 4 - (int)this.amountScrolled;

      for(int i5 = 0; i5 < size; ++i5) {
         int k4 = k3 + i5 * this.slotHeight;
         int j2 = this.slotHeight - 4;
         if (k4 <= this.bottom && k4 + j2 >= this.top) {
            int left2 = this.width / 2 - 155;

            int m;
            for(int start = m = i5 * 3; m < start + 3; ++m) {
               int offset = m % 3 * 21;
               if (m < this.controlList.size()) {
                  GuiButton button = (GuiButton)this.controlList.get(m);
                  button.xPosition = left2 + offset;
                  button.yPosition = k4;
                  button.drawElement(this.mc, mouseX, mouseY, deltaTicks);
               }
            }
         }
      }

      RenderSystem.disableDepthTest();
      this.overlayBackground(0, this.top, 255, 255);
      this.overlayBackground(this.bottom, this.height, 255, 255);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      RenderSystem.disableAlphaTest();
      RenderSystem.useSmoothShadeModel();
      RenderSystem.disableTexture2D();
      tessellator.startDrawingQuads();
      tessellator.setColorRGBA_I(0, 0);
      tessellator.addVertexWithUV(this.left, this.top + 4, 0.0, 0.0, 1.0);
      tessellator.addVertexWithUV(this.right, this.top + 4, 0.0, 1.0, 1.0);
      tessellator.setColorRGBA_I(0, 255);
      tessellator.addVertexWithUV(this.right, this.top, 0.0, 1.0, 0.0);
      tessellator.addVertexWithUV(this.left, this.top, 0.0, 0.0, 0.0);
      tessellator.draw();
      tessellator.startDrawingQuads();
      tessellator.setColorRGBA_I(0, 255);
      tessellator.addVertexWithUV(this.left, this.bottom, 0.0, 0.0, 1.0);
      tessellator.addVertexWithUV(this.right, this.bottom, 0.0, 1.0, 1.0);
      tessellator.setColorRGBA_I(0, 0);
      tessellator.addVertexWithUV(this.right, this.bottom - 4, 0.0, 1.0, 0.0);
      tessellator.addVertexWithUV(this.left, this.bottom - 4, 0.0, 0.0, 0.0);
      tessellator.draw();
      int contentHeight = this.getContentHeight() - (this.bottom - this.top - 4);
      if (contentHeight > 0) {
         int k5 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
         if (k5 < 32) {
            k5 = 32;
         }

         if (k5 > this.bottom - this.top - 8) {
            k5 = this.bottom - this.top - 8;
         }

         int i6 = (int)this.amountScrolled * (this.bottom - this.top - k5) / contentHeight + this.top;
         if (i6 < this.top) {
            i6 = this.top;
         }

         tessellator.startDrawingQuads();
         tessellator.setColorRGBA_I(0, 255);
         tessellator.addVertexWithUV(l, this.bottom, 0.0, 0.0, 1.0);
         tessellator.addVertexWithUV(i1, this.bottom, 0.0, 1.0, 1.0);
         tessellator.addVertexWithUV(i1, this.top, 0.0, 1.0, 0.0);
         tessellator.addVertexWithUV(l, this.top, 0.0, 0.0, 0.0);
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setColorRGBA_I(8421504, 255);
         tessellator.addVertexWithUV(l, i6 + k5, 0.0, 0.0, 1.0);
         tessellator.addVertexWithUV(i1, i6 + k5, 0.0, 1.0, 1.0);
         tessellator.addVertexWithUV(i1, i6, 0.0, 1.0, 0.0);
         tessellator.addVertexWithUV(l, i6, 0.0, 0.0, 0.0);
         tessellator.draw();
         tessellator.startDrawingQuads();
         tessellator.setColorRGBA_I(12632256, 255);
         tessellator.addVertexWithUV(l, i6 + k5 - 1, 0.0, 0.0, 1.0);
         tessellator.addVertexWithUV(i1 - 1, i6 + k5 - 1, 0.0, 1.0, 1.0);
         tessellator.addVertexWithUV(i1 - 1, i6, 0.0, 1.0, 0.0);
         tessellator.addVertexWithUV(l, i6, 0.0, 0.0, 0.0);
         tessellator.draw();
      }

      RenderSystem.enableTexture2D();
      RenderSystem.useFlatShadeModel();
      RenderSystem.enableAlphaTest();
      RenderSystem.disableBlend();
      this.drawCenteredString(this.fontRenderer, "Recipe Viewer Tab Order", this.width / 2, BUTTON_HEIGHT, 16777215);
      this.controlList.get(this.controlList.size() - 2).drawElement(this.mc, mouseX, mouseY, deltaTicks);
      this.controlList.get(this.controlList.size() - 1).drawElement(this.mc, mouseX, mouseY, deltaTicks);
   }

   private void bindAmountScrolled() {
      int i = this.getContentHeight() - (this.bottom - this.top - 4);
      if (i < 0) {
         i /= 2;
      }

      if (this.amountScrolled < 0.0F) {
         this.amountScrolled = 0.0F;
      }

      if (this.amountScrolled > (float)i) {
         this.amountScrolled = (float)i;
      }
   }

   void overlayBackground(int top, int bottom, int k, int l) {
      Tessellator tessellator = Tessellator.instance;
      RenderSystem.bindTexture2D("/textures/gui/background.png");
      RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
      float f = 32.0F;
      tessellator.startDrawingQuads();
      tessellator.setColorRGBA_I(4210752, l);
      tessellator.addVertexWithUV(0.0, bottom, 0.0, 0.0, (float)bottom / 32.0F);
      tessellator.addVertexWithUV(this.right, bottom, 0.0, (float)this.right / 32.0F, (float)bottom / 32.0F);
      tessellator.setColorRGBA_I(4210752, k);
      tessellator.addVertexWithUV(this.right, top, 0.0, (float)this.right / 32.0F, (float)top / 32.0F);
      tessellator.addVertexWithUV(0.0, top, 0.0, 0.0, (float)top / 32.0F);
      tessellator.draw();
   }
}
