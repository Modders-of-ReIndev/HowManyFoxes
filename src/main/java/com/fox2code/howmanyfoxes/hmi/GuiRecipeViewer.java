package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.HowManyFoxes;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import com.fox2code.howmanyfoxes.hmi.tabs.TabWithTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.world.RenderHelper;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.logging.Level;

public class GuiRecipeViewer extends GuiContainer {
   private boolean dragging;
   private float cursorPosX;
   private float cursorPosY;
   private int tabPage;
   private boolean tabPageButtons;
   private boolean tabPageButton1;
   private boolean tabPageButton2;
   private final GuiScreen parent;
   public int tabIndex;
   public static ArrayList<Tab> tabs;
   private final InventoryRecipeViewer inv;
   private final ContainerRecipeViewer container;
   protected ScaledResolution scaledResolution = new ScaledResolution();
   private boolean drawingContainer;

   public GuiRecipeViewer(ItemStack itemstack, boolean getUses, GuiScreen parent) {
      this(itemstack, getUses, parent, null, null);
   }

   public GuiRecipeViewer(ItemStack itemstack, GuiScreen parent) {
      this(itemstack, null, parent, null, null);
   }

   private GuiRecipeViewer(ItemStack itemstack, Boolean getUses,
                           GuiScreen parent, ContainerRecipeViewer container,
                           InventoryRecipeViewer inv) {
      super(container = new ContainerRecipeViewer(inv = new InventoryRecipeViewer(),
              parent instanceof GuiContainer ? ((GuiContainer) parent).inventorySlots : null));
      this.inv = inv;
      this.container = container;
      this.dragging = false;
      this.tabPage = 0;
      this.parent = parent;
      this.init();
      if (getUses != null) {
         this.push(itemstack, getUses);
      } else {
         this.pushTabBlock(itemstack);
      }
   }

   private void init() {
      if (Config.recipeViewerDraggableGui) {
         this.xSize = Config.recipeViewerGuiWidth;
         this.ySize = Config.recipeViewerGuiHeight;
      } else {
         this.xSize = Config.recipeViewerGuiWidthDefault;
         if (this.parent instanceof GuiContainer) {
            try {
               this.xSize = Math.max(((GuiContainer) this.parent).getXSize(), this.xSize);
            } catch (Exception var2) {
               HowManyFoxes.logger.log(Level.WARNING, "Failed to get XSize of " + this.parent.getClass().getName(), var2);
            }
         }
         this.ySize = Config.recipeViewerGuiHeightDefault;
      }

      int maxXSize = Math.max(this.width - 48,
              Config.recipeViewerGuiWidthDefault);
      this.xSize = MathHelper.clamp_int(this.xSize,
              Config.recipeViewerGuiWidthDefault / 2, maxXSize);

      int maxYSize = Math.max(this.height - 48,
              Config.recipeViewerGuiHeightDefault);
      this.ySize = MathHelper.clamp_int(this.xSize,
              Config.recipeViewerGuiHeightDefault / 2, maxYSize);

      tabs = HMIClient.getTabs();
      this.newTab(tabs.getFirst());
   }

   public void tickInventory() {
      inv.tick();
   }

   public void pushTabBlock(ItemStack itemstack) {
      if (itemstack != null) {
         inv.filter.push(null);
         inv.newList = true;
         inv.prevTabs.push(inv.currentTab);
         inv.prevPages.push(inv.getPage() * inv.currentTab.recipesPerPage);
         inv.prevGetUses.push(true);

         for(Tab tab : HMIClient.getTabs()) {
            boolean tabMatchesBlock = false;

            for(ItemStack tabBlock : tab.equivalentCraftingStations) {
               if (tabBlock.isItemEqual(itemstack)) {
                  tabMatchesBlock = true;
                  tab.updateRecipes(null, false);
                  break;
               }
            }

            if (!tabMatchesBlock) {
               tab.size = 0;
            }
         }

         this.postPush();
      }
   }

   public void push(ItemStack itemstack, boolean getUses) {
      if (inv.filter.isEmpty() || itemstack != null || inv.filter.peek() != null) {
         if (inv.filter.isEmpty()
            || getUses != inv.prevGetUses.peek()
            || itemstack == null && inv.filter.peek() != null
            || itemstack != null && inv.filter.peek() == null
            || itemstack != null && itemstack.getItemID() != inv.filter.peek().getItemID()
            || (itemstack != null && itemstack.getItemDamage() !=
                 inv.filter.peek().getItemDamage() && itemstack.getHasSubtypes())) {
            inv.newList = true;
            if (itemstack == null) {
               inv.filter.push(null);
            } else {
               inv.filter.push(new ItemStack(itemstack.getItem(), 1, Math.max(itemstack.getItemDamage(), 0)));
            }

            inv.prevTabs.push(inv.currentTab);
            inv.prevPages.push(inv.getPage() * inv.currentTab.recipesPerPage);
            inv.prevGetUses.push(getUses);
            inv.newList = true;

            for(Tab tab : HMIClient.getTabs()) {
               tab.updateRecipes(inv.filter.peek(), getUses);
            }

            this.postPush();
         }
      }
   }

   private void postPush() {
      if (inv.currentTab.size == 0) {
         for(Tab tab : HMIClient.getTabs()) {
            if (tab.size > 0) {
               this.newTab(tab);
               break;
            }

            if (HMIClient.getTabs().indexOf(tab) == HMIClient.getTabs().size() - 1) {
               inv.filter.pop();
               inv.prevTabs.pop();
               inv.prevPages.pop();
               inv.prevGetUses.pop();
               if (inv.filter.isEmpty()) {
                  inv.newList = false;
                  return;
               }

               for(Tab tab2 : HMIClient.getTabs()) {
                  tab2.updateRecipes(inv.filter.peek(), inv.prevGetUses.peek());
               }

               inv.index = inv.setIndex(inv.index);
               this.initButtons();
               return;
            }
         }
      }

      inv.index = inv.setIndex(0);
      this.initButtons();
   }

   public void pop() {
      inv.filter.pop();
      inv.prevGetUses.pop();
      if (inv.filter.isEmpty()) {
         inv.newList = false;
         this.displayParent();
      } else {
         for(Tab tab : HMIClient.getTabs()) {
            tab.updateRecipes(inv.filter.peek(), inv.prevGetUses.peek());
         }

         this.newTab(inv.prevTabs.pop());
         inv.newList = true;
         inv.index = inv.setIndex(inv.prevPages.pop());
         this.initButtons();
      }
   }

   @Override
   public void handleMouseInput() {
      int i = Mouse.getEventDWheel();
      if (!Config.scrollInverted) {
         if (i > 0) {
            inv.incIndex();
            this.initButtons();
         }

         if (i < 0) {
            inv.decIndex();
            this.initButtons();
         }
      } else {
         if (i > 0) {
            inv.decIndex();
            this.initButtons();
         }

         if (i < 0) {
            inv.incIndex();
            this.initButtons();
         }
      }

      super.handleMouseInput();
   }

   @Override
   public void mouseMovedOrUp(float clickedX, float clickedY, int mouseButton) {
      if (this.dragging && mouseButton != -1) {
         this.dragging = false;
      }

      if (this.dragging) {
         this.doDragging(clickedX, clickedY);
      }
   }

   private void doDragging(float clickedX, float clickedY) {
      int x = (this.width - this.xSize) / 2;
      int y = (this.height - this.ySize) / 2;
      Tab currentTab = tabs.get(this.tabIndex);
      int newXSize = MathHelper.clamp_int((int) clickedX - x,
              Math.max(Config.recipeViewerGuiWidthDefault / 2, currentTab.WIDTH + 8),
              Math.max(this.width - 48, Config.recipeViewerGuiWidthDefault));
      int newYSize = MathHelper.clamp_int((int) clickedY - y,
              Math.max(Config.recipeViewerGuiHeightDefault / 2, currentTab.HEIGHT + 8),
              Math.max(this.height - 48, Config.recipeViewerGuiHeightDefault));

      if (this.xSize != newXSize || this.ySize != newYSize) {
         this.xSize = newXSize;
         this.ySize = newYSize;

         tabs.get(this.tabIndex).redrawSlots = true;
      }
   }

   @Override
   public void mouseClicked(float posX, float posY, int k) {
      int x = (this.width - this.xSize) / 2;
      int y = (this.height - this.ySize) / 2;
      ItemStack item = Utils.hoveredItem(this, posX, posY);
      if (item != null && this.mc.thePlayer.inventory.getCursorStack() == null) {
         this.push(item, k == 1);
      } else if (Config.recipeViewerDraggableGui
         && posX - this.xSize + 10 > x
         && posX - this.xSize - 4 < x
         && posY - this.ySize + 10 > y
         && posY - this.ySize - 4 < y
         && k == 0
         && !this.dragging) {
         this.dragging = true;
      } else if (posX > x && posX < x + this.xSize && posY > y + 4 && posY < y + this.ySize + 4) {
         if (k == 0) {
            for(GuiButton button : this.buttons()) {
               if (button.mousePressed(this.mc, posX, posY)) {
                  this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
                  this.actionPerformed(button);
                  return;
               }
            }
         }

         if (k == 0) {
            inv.incIndex();
            this.initButtons();
         }

         if (k == 1) {
            inv.decIndex();
            this.initButtons();
         }
      } else {
         int tabCount = 0;

         for(int z = this.tabPage; z < tabs.size() && (tabCount + 1) * 27 < this.xSize; ++z) {
            if (tabs.get(z).size > 0) {
               if (posX - tabCount * 27 + 1 > x && posX - (tabCount + 1) * 27 < x && posY + 21 > y && posY - 3 < y && k == 0 && this.tabIndex != z) {
                  this.newTab(tabs.get(z));
                  break;
               }

               ++tabCount;
            }
         }
      }
   }

   public void newTab(Tab tab) {
      this.tabIndex = tabs.indexOf(tab);
      if (Config.recipeViewerDraggableGui) {
         if (this.xSize < tab.WIDTH + 8) {
            this.xSize = Config.recipeViewerGuiWidth = tab.WIDTH + 8;
         }
         if (this.ySize < tab.HEIGHT + 8) {
            this.ySize = Config.recipeViewerGuiHeight = tab.HEIGHT + 8;
         }
      }
      tab.redrawSlots = true;
      inv.initTab(tab);
      this.initButtons();
   }

   @Override
   public void keyTyped(char c, int i) {
      if (i == 205) {
         inv.incIndex();
         this.initButtons();
      }

      if (i == 203) {
         inv.decIndex();
         this.initButtons();
      }

      if (i != 1 && i != this.mc.gameSettings.keyBindInventory.keyCode) {
         super.keyTyped(c, i);
      } else {
         this.displayParent();
         if (i == this.mc.gameSettings.keyBindInventory.keyCode) {
            this.mc.thePlayer.closeScreen();
         }
      }
   }

   @Override
   public void initGui() {
      super.initGui();
      if (inv.filter.isEmpty()) {
         this.displayParent();
      } else {
         this.initButtons();
      }
   }

   public void initButtons() {
      this.controlList.clear();
      int x = (this.width - this.xSize) / 2;
      int y = (this.height - this.ySize) / 2;
      this.controlList.add(new GuiSmallButton(-1, x + this.xSize - 20, y - 43, 20, 20, ">"));
      this.controlList.add(new GuiSmallButton(-2, x, y - 43, 20, 20, "<"));
      ((GuiButton)this.controlList.get(0)).visible = this.tabPageButtons;
      ((GuiButton)this.controlList.get(0)).enabled = this.tabPageButton1;
      ((GuiButton)this.controlList.get(1)).visible = this.tabPageButtons;
      ((GuiButton)this.controlList.get(1)).enabled = this.tabPageButton2;

      if (tabs.get(this.tabIndex) instanceof TabWithTexture tab) {
          int gapX = (this.xSize - 8) % (tab.WIDTH + tab.MIN_PADDING_X);
         int noX = (this.xSize - 8) / (tab.WIDTH + tab.MIN_PADDING_X);
         if (noX == 0) {
            ++noX;
         }

         int gapY = (this.ySize - 8) % (tab.HEIGHT + tab.MIN_PADDING_Y);
         int noY = (this.ySize - 8) / (tab.HEIGHT + tab.MIN_PADDING_Y);
         if (noY == 0) {
            ++noY;
         }

         if (tab.size == 1) {
            noX = 1;
            noY = 1;
         }

         int i = 0;

         for(int l1 = 0; l1 < noX; ++l1) {
            for(int i2 = 0; i2 < noY; ++i2) {
               if (tab.size > 0
                  && inv.items != null
                  && i++ < tab.recipesOnThisPage
                  && inv.items.length > i - 1
                  && tab.drawSetupRecipeButton(this.parent, inv.items[i - 1])) {
                  int posX = 4 + gapX / 4 + l1 * (this.xSize - gapX / 2) / noX;
                  int posY = 4 + gapY / 4 + i2 * (this.ySize - gapY / 2) / noY;
                  if (noX == 1) {
                     posX = (this.xSize - tab.WIDTH) / 2;
                  }

                  if (noY == 1) {
                     posY = (this.ySize - tab.HEIGHT) / 2;
                  }

                  GuiButtonHMI button = new GuiButtonHMI(i, x + posX + tab.BUTTON_POS_X, y + posY + tab.BUTTON_POS_Y, 12, 12, "+");
                  boolean[] itemsInInv = tab.itemsInInventory(this.parent, inv.items[i - 1]);

                  for (boolean b : itemsInInv) {
                     if (!b) {
                        button.enabled = false;
                        break;
                     }
                  }

                  this.controlList.add(button);
               }
            }
         }
      }
   }

   @Override
   protected void actionPerformed(GuiButton guibutton) {
      if (guibutton.id - 1 < inv.items.length && tabs.get(this.tabIndex) instanceof TabWithTexture tabWithTexture) {
         this.displayParent();
         tabWithTexture.setupRecipe(this.parent, inv.items[guibutton.id - 1]);
      } else if (guibutton.id == -1) {
         this.tabPage += this.xSize / 27;
         if (this.tabPage >= tabs.size()) {
            this.tabPage -= this.xSize / 27;
         }
      } else if (guibutton.id == -2) {
         this.tabPage -= this.xSize / 27;
         if (this.tabPage < 0) {
            this.tabPage = 0;
         }
      }
   }

   public void displayParent() {
      this.mc = Minecraft.theMinecraft;
      this.mc.thePlayer.currentContainer = this.mc.thePlayer.playerContainer;
      this.onGuiClosed();
      if (this.parent != null) {
         this.mc.currentScreen = this.parent;
         this.scaledResolution.setDimensions(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
         int i = this.scaledResolution.getScaledWidth();
         int j = this.scaledResolution.getScaledHeight();
         this.mc.setIngameNotInFocus();
         this.parent.setWorldAndResolution(this.mc, i, j);
      } else {
         this.mc.displayGuiScreen(null);
      }
   }

   @Override
   public void drawScreen(float mouseX, float mouseY, float deltaTicks) {
      this.drawingContainer = true;
      super.drawScreen(this.cursorPosX = mouseX, this.cursorPosY = mouseY, deltaTicks);
      this.drawingContainer = false;
      if (this.dragging) {
         this.doDragging(mouseX, mouseY);
      }
   }

   @Override
   public void drawItemTooltip(ItemStack item, float mouseX, float mouseY) {
      if (this.drawingContainer) {
         Utils.drawTooltip(this, item, mouseX, mouseY);
         return;
      }
      super.drawItemTooltip(item, mouseX, mouseY);
   }

   protected void drawGuiContainerForegroundLayer() {
      GL11.glPushMatrix();
      if ((double)inv.currentTab.size / (double)inv.currentTab.recipesPerPage > 1.0) {
         String s = inv.getInvName();
         int containerXDiff = this.width - 176 >> 1;
         this.fontRenderer.drawString(s, ((this.width + this.xSize) / 2) - 6 -
                 this.fontRenderer.getStringWidth(s) - containerXDiff, 10, 0x404040);
      }

      int tabCount = 0;
      int x = (this.width - this.xSize) / 2;
      int y = (this.height - this.ySize) / 2;

      for(int z = this.tabPage; z < tabs.size(); ++z) {
         if (tabs.get(z).size > 0 && (tabCount + 1) * 27 < this.xSize) {
            if (this.cursorPosX > x + tabCount * 27 + 6 && this.cursorPosX < x + (tabCount + 1) * 27 - 6 && this.cursorPosY > y - 16 && this.cursorPosY < y) {
               String s2 = tabs.get(z).name();
               if (!s2.isEmpty()) {
                  Utils.drawTooltip(s2, (int) this.cursorPosX, (int) this.cursorPosY);
               }
               break;
            }

            ++tabCount;
         }
      }

      if (tabs.get(this.tabIndex) instanceof TabWithTexture tab) {
          y += 3;
         int gapX = (this.xSize - 8) % (tab.WIDTH + tab.MIN_PADDING_X);
         int noX = (this.xSize - 8) / (tab.WIDTH + tab.MIN_PADDING_X);
         if (noX == 0) {
            ++noX;
         }

         int gapY = (this.ySize - 8) % (tab.HEIGHT + tab.MIN_PADDING_Y);
         int noY = (this.ySize - 8) / (tab.HEIGHT + tab.MIN_PADDING_Y);
         if (noY == 0) {
            ++noY;
         }

         if (tab.size == 1) {
            noX = 1;
            noY = 1;
         }

         int i = 0;

         for(int l1 = 0; l1 < noX; ++l1) {
            for(int i2 = 0; i2 < noY; ++i2) {
               if (tab.size > 0 && i++ < tab.recipesOnThisPage) {
                  int posX = 4 + gapX / 4 + l1 * (this.xSize - gapX / 2) / noX;
                  int posY = 4 + gapY / 4 + i2 * (this.ySize - gapY / 2) / noY;
                  if (noX == 1) {
                     posX = (this.xSize - tab.WIDTH) / 2;
                  }

                  if (noY == 1) {
                     posY = (this.ySize - tab.HEIGHT) / 2;
                  }

                  if (tab.drawSetupRecipeButton(this.parent, inv.items[i - 1])
                     && this.cursorPosX > x + posX + tab.BUTTON_POS_X - 1
                     && this.cursorPosX < x + posX + tab.BUTTON_POS_X + 12
                     && this.cursorPosY > y + posY + tab.BUTTON_POS_Y - 3 - 1
                     && this.cursorPosY < y + posY + tab.BUTTON_POS_Y - 3 + 12) {
                     boolean[] itemsInInv = tab.itemsInInventory(this.parent, inv.items[i - 1]);

                     for(int qq = 0; qq < itemsInInv.length; ++qq) {
                        if (!itemsInInv[qq]) {
                           this.drawGradientRect(
                              posX + tab.slots[qq + 1][0],
                              posY + tab.slots[qq + 1][1],
                              posX + tab.slots[qq + 1][0] + 16,
                              posY + tab.slots[qq + 1][1] + 16,
                              -2132009966,
                              -2132009966
                           );
                        }
                     }
                  }
               }
            }
         }
      }

      Utils.postRender();
      Utils.disableLighting();
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GL11.glPopMatrix();
   }

   public ItemStack getHoverItem() {
      int tabCount = 0;
      int x = (this.width - this.xSize) / 2;
      int y = (this.height - this.ySize) / 2;

      for(int z = this.tabPage; z < tabs.size(); ++z) {
         if (tabs.get(z).size > 0) {
            if (this.cursorPosX > x + tabCount * 27 + 6 && this.cursorPosX < x + (tabCount + 1) * 27 - 6 && this.cursorPosY > y - 16 && this.cursorPosY < y) {
               return tabs.get(z).getTabItem();
            }

            ++tabCount;
         }
      }

      return null;
   }

   protected void drawGuiContainerBackgroundLayer(float f) {
      GL11.glPushMatrix();
      Utils.preRender();
      int x = (this.width - this.xSize) / 2;
      int y = (this.height - this.ySize) / 2 + 3;
      int tabCount = 0;

      for(int z = this.tabPage; z < tabs.size(); ++z) {
         if (tabs.get(z).size > 0) {
            if (z != this.tabIndex && (tabCount + 1) * 27 < this.xSize) {
               Utils.bindTexture();
               Utils.disableLighting();
               this.drawTexturedModalRect(x + tabCount * 27, y - 25, 28, 113, 28, 28);
               Utils.drawItemStack(x + 6 + tabCount * 27, y - 18, tabs.get(z).getTabItem(), true);
            }

            ++tabCount;
         }
      }

      Utils.bindTexture();
      Utils.disableLighting();

      for(int l1 = 0; l1 < this.xSize - 4; l1 += 48) {
         for(int i2 = 0; i2 < this.ySize - 4; i2 += 48) {
            this.drawTexturedModalRect(x + 4 + l1, y + 4 + i2, 4, 145, this.xSize - l1 - 8, this.ySize - i2 - 8);
         }
      }

      if (this.controlList.size() >= 2) {
         if ((tabCount + this.tabPage) * 27 < this.xSize) {
            this.tabPage = 0;
            this.tabPageButtons = false;
         } else {
            this.tabPageButtons = true;
         }

         ((GuiButton)this.controlList.get(0)).visible = this.tabPageButtons;
         ((GuiButton)this.controlList.get(1)).visible = this.tabPageButtons;
         this.tabPageButton1 = this.xSize / 27 < tabCount;
         this.tabPageButton2 = this.tabPage != 0;
         ((GuiButton)this.controlList.get(0)).enabled = this.tabPageButton1;
         ((GuiButton)this.controlList.get(1)).enabled = this.tabPageButton2;
      }

      for(int l1 = 0; l1 < this.xSize - 4; l1 += 48) {
         this.drawTexturedModalRect(x + 4 + l1, y, 4, 141, this.xSize - l1 - 8, 4);
         this.drawTexturedModalRect(x + 4 + l1, y + this.ySize - 4, 4, 193, this.xSize - l1 - 8, 4);
      }

      for(int i3 = 0; i3 < this.ySize - 4; i3 += 48) {
         this.drawTexturedModalRect(x, y + 4 + i3, 0, 145, 4, this.ySize - i3 - 8);
         this.drawTexturedModalRect(x + this.xSize - 4, y + 4 + i3, 52, 145, 4, this.ySize - i3 - 8);
      }

      this.drawTexturedModalRect(x, y, 0, 141, 4, 4);
      this.drawTexturedModalRect(x + this.xSize - 4, y, 52, 141, 4, 4);
      this.drawTexturedModalRect(x, y + this.ySize - 4, 0, 193, 4, 4);
      this.drawTexturedModalRect(x + this.xSize - 4, y + this.ySize - 4, 52, 193, 4, 4);
      tabCount = 0;

      for(int z = this.tabPage; z < tabs.size(); ++z) {
         if (tabs.get(z).size > 0) {
            if (z == this.tabIndex && (tabCount + 1) * 27 < this.xSize) {
               Utils.bindTexture();
               Utils.disableLighting();
               this.drawTexturedModalRect(x + tabCount * 27, y - 25, 0, 113, 28, 28);
               if (tabCount == 0) {
                  this.drawTexturedModalRect(x, y, 0, 145, 4, 5);
               }

               Utils.drawItemStack(x + 6 + tabCount * 27, y - 18, tabs.get(z).getTabItem(), true);
            }

            ++tabCount;
         }
      }

      Utils.bindTexture();
      Utils.disableLighting();
      if (Config.recipeViewerDraggableGui && !this.dragging) {
         this.drawTexturedModalRect(x + this.xSize - 29, y + this.ySize - 29, 56, 169, 28, 28);
      }

      Tab tab = tabs.get(this.tabIndex);
      int gapX = (this.xSize - 8) % (tab.WIDTH + tab.MIN_PADDING_X);
      int noX = (this.xSize - 8) / (tab.WIDTH + tab.MIN_PADDING_X);
      if (noX == 0) {
         ++noX;
      }

      int gapY = (this.ySize - 8) % (tab.HEIGHT + tab.MIN_PADDING_Y);
      int noY = (this.ySize - 8) / (tab.HEIGHT + tab.MIN_PADDING_Y);
      if (noY == 0) {
         ++noY;
      }

      if (this.xSize < tab.WIDTH + 8) {
         this.xSize = tab.WIDTH + 8;
      }

      if (this.ySize < tab.HEIGHT + 8) {
         this.ySize = tab.HEIGHT + 8;
      }

      if (!tab.redrawSlots && tab.slots.length > 0 && container.slots.size() / tab.slots.length > tab.recipesOnThisPage) {
         tab.redrawSlots = true;
      }

      boolean redrawItems = false;
      if (tab.recipesPerPage != noX * noY) {
         tab.recipesPerPage = noX * noY;
         tab.recipesOnThisPage = tab.recipesPerPage;
         tab.redrawSlots = true;
         redrawItems = true;
      }

      if (tab.size == 1) {
         noX = 1;
         noY = 1;
      }

      if (tab.redrawSlots) {
         container.resetSlots();
      }

      int j = 0;

      // offset add slot
      final int margin = 4;
      int tabWidth = tab.WIDTH + (margin * 2);
      int tabHeight = tab.HEIGHT + (margin * 2);
      int tabWidthHalf = tabWidth / 2;
      int tabHeightHalf = tabHeight / 2;
      int startX = (this.xSize / 2) - (tabWidthHalf * noX);
      int startY = (this.ySize / 2) - (tabHeightHalf * noY);
      int slotXDiff = this.width - 176 >> 1;
      int slotYDiff = (this.height - this.ySize >> 1) + 3;
      for(int l2 = 0; l2 < noX; ++l2) {
         for(int i4 = 0; i4 < noY; ++i4) {
            if (tab.size > 0 && j++ < tab.recipesOnThisPage) {
               int posX = startX + (tabWidth * l2) + margin;
               int posY = startY + (tabHeight * i4) + margin;

               tab.draw(x + posX, y + posY, j - 1, (int) this.cursorPosX, (int) this.cursorPosY);
               if (tab.redrawSlots) {
                  for(int q = 0; q < tab.slots.length; ++q) {
                     container.addSlot(
                             x + posX + tab.slots[q][0] - slotXDiff,
                             y + posY + tab.slots[q][1] - slotYDiff);
                  }
               }
            }
         }
      }

      if (tab.redrawSlots) {
         inv.newList = true;
         if (redrawItems) {
            inv.setIndex(inv.index);
         }

         this.initButtons();
         tab.redrawSlots = false;
      }

      GL11.glPopMatrix();
   }

   public void onGuiClosed() {
      if (Config.recipeViewerGuiWidth != this.xSize || Config.recipeViewerGuiHeight != this.ySize) {
         Config.recipeViewerGuiWidth = this.xSize;
         Config.recipeViewerGuiHeight = this.ySize;
         Config.writeConfig();
      }
   }
}
