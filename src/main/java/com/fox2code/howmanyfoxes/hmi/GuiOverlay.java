package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.HowManyFoxes;
import com.fox2code.howmanyfoxes.hmi.config.DefaultHiddenItems;
import com.fox2code.howmanyfoxes.hmi.config.HMFKeyBinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.common.block.container.Slot;
import net.minecraft.common.entity.player.InventoryPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.ChatAllowedCharacters;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Stack;

public class GuiOverlay extends GuiScreen {
   private final int BUTTON_HEIGHT = 20;
   public static GuiContainer screen;
   private static ArrayList<ItemStack> currentItems;
   public static ItemStack hoverItem;
   private static GuiTextFieldHMI searchBox;
   public static boolean stateReset = false;
   private static int index = 0;
   private int itemsPerPage;
   private GuiButtonHMI buttonNextPage;
   private GuiButtonHMI buttonPrevPage;
   private GuiButtonHMI buttonOptions;
   private GuiButtonHMI buttonTimeDay;
   private GuiButtonHMI buttonTimeNight;
   private GuiButtonHMI buttonToggleRain;
   private GuiButtonHMI buttonHeal;
   private GuiButtonHMI buttonTrash;
   private ItemStack guiBlock;
   public static boolean hiddenItemsModified = false;
   public static ArrayList<ItemStack> hiddenItems;
   public static boolean showHiddenItems = false;
   public int xSize;
   public int ySize;
   public static long guiClosedCooldown = 0L;
   private ItemStack draggingFrom;
   private static long deleteAllWaitUntil = 0L;
   private static int lastKey = -1;
   private static long lastKeyTimeout = 0L;
   private static final Stack<ArrayList<ItemStack>> prevSearches = new Stack<>();
   private static String lastSearch = "";
   public boolean modTickKeyPress;
   protected ScaledResolution scaledresolution = new ScaledResolution();

   public GuiOverlay(GuiContainer gui) {
      this.xSize = 0;
      this.ySize = 0;
      this.draggingFrom = null;
      this.modTickKeyPress = false;
      if (hiddenItems == null) {
         hiddenItems = new ArrayList<>(DefaultHiddenItems.DEFAULT_HIDDEN_ITEMS);
      }

      if (currentItems == null) {
         currentItems = getCurrentList(Utils.itemList());
      }

      screen = gui;
      lastKeyTimeout = System.currentTimeMillis() + 200L;
      lastKey = Keyboard.getEventKey();
      if (!HMIClient.getTabs().isEmpty()) {
         this.guiBlock = TabUtils.getItemFromGui(screen);
      }

      this.setWorldAndResolution(Minecraft.theMinecraft, screen.width, screen.height);
   }

   @Override
   public void initGui() {
      if (this.mc.currentScreen == this) {
         screen.setWorldAndResolution(this.mc, this.width, this.height);
      }
      try {
         this.xSize = screen.getXSize();
         this.ySize = screen.getYSize();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      this.controlList.clear();
      int k = (screen.width - this.xSize) / 2 + 1;
      String search = "";
      if (searchBox != null) {
         search = searchBox.getText();
      }

      int searchBoxX = k + this.xSize + 1;
      int searchBoxWidth = screen.width - k - this.xSize - 20 - 2;
      if (HowManyFoxes.CONFIG.centredSearchBar) {
         searchBoxX -= this.xSize;
         searchBoxWidth = this.xSize - 20 - 3;
      }

      int id = 0;
      (searchBox = new GuiTextFieldHMI(searchBoxX, screen.height - 20 + 1, searchBoxWidth, 16, search))
              .setMaxStringLength((searchBoxWidth - 10) / 6);
      this.controlList.add(this.buttonOptions = new GuiButtonHMI(
              id++, searchBoxX + searchBoxWidth + 1, screen.height - 20 - 1, 20,
              HowManyFoxes.CONFIG.cheatsEnabled ? 1 : 0, this.guiBlock));
      this.controlList.add(this.buttonNextPage = new GuiButtonHMI(
              id++, screen.width - (screen.width - k - this.xSize) / 3, 0,
              (screen.width - k - this.xSize) / 3, 20, "Next"));
      this.controlList.add(this.buttonPrevPage = new GuiButtonHMI(
              id++, k + this.xSize, 0,
              (screen.width - k - this.xSize) / 3, 20, "Prev"));
      if (HowManyFoxes.CONFIG.cheatsEnabled) {
         boolean mp = this.mc.theWorld.isRemote;
         if (!mp || !HowManyFoxes.CONFIG.mpTimeDayCommand.isEmpty()) {
            this.controlList.add(this.buttonTimeDay = new GuiButtonHMI(id++, 0, 0, 20, 12));
         }

         if (!mp || !HowManyFoxes.CONFIG.mpTimeNightCommand.isEmpty()) {
            this.controlList.add(this.buttonTimeNight = new GuiButtonHMI(id++, 20, 0, 20, 13));
         }

         if (!mp || !HowManyFoxes.CONFIG.mpRainOFFCommand.isEmpty() || !HowManyFoxes.CONFIG.mpRainONCommand.isEmpty()) {
            this.controlList.add(this.buttonToggleRain = new GuiButtonHMI(id++, 40, 0, 20, 14));
         }

         if (!mp || !HowManyFoxes.CONFIG.mpHealCommand.isEmpty()) {
            this.controlList.add(this.buttonHeal = new GuiButtonHMI(id++, 60, 0, 20, 15));
         }

         if (!mp) {
            this.controlList.add(this.buttonTrash = new GuiButtonHMI(id++, 0, screen.height - 20 - 1, 60, 20, "Trash"));
         }
      }
   }

   @Override
   public void drawScreen(float mouseX, float mouseY, float deltaTicks) {
      boolean shiftHeld = Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
      if (shiftHeld && !HMIClient.getTabs().isEmpty()) {
         this.buttonOptions.iconIndex = 2;
         if (this.buttonTrash != null) {
            this.buttonTrash.displayString = "Delete ALL";
         }
      } else {
         this.buttonOptions.iconIndex = HowManyFoxes.CONFIG.cheatsEnabled ? 1 : 0;
         if (this.buttonTrash != null) {
            this.buttonTrash.displayString = "Trash";
         }
      }

      int k = (screen.width - this.xSize) / 2 + this.xSize + 1;
      int w = screen.width - (screen.width - this.xSize) / 2 - this.xSize - 1;
      Utils.disableLighting();

      for(GuiElement guiButton : this.controlList) {
         guiButton.drawElement(this.mc, mouseX, mouseY, deltaTicks);
      }

      searchBox.drawTextBox();
      int x = 0;
      int y = 0;
      boolean itemHovered = false;
      InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
      int canvasHeight = screen.height - 40;
      if (HowManyFoxes.CONFIG.centredSearchBar) {
         canvasHeight += 20;
      }

      for(int i = index; i < currentItems.size(); ++i) {
         if ((x + 1) * 18 > w) {
            ++y;
            x = 0;
         }

         if ((y + 1) * 18 > canvasHeight) {
            if (index + this.itemsPerPage <= currentItems.size()) {
            }
            break;
         }

         int white = 0x40ffffff;
         int green = 0xAA66CD00;
         int lightRed = 0xAAE50000;
         int darkRed = 0x80E50000;
         int slotX = (w % 18)/2 + k + x * 18 - 1;
         int slotY = y * 18 - 1 + (canvasHeight % 18) /2  + BUTTON_HEIGHT;
         if(!itemHovered &&
                 (int) mouseX + 2> k + (w % 18)/2 &&
                 ((int) mouseX - (w % 18)/2 - k + 1) / 18 >= x &&
                 ((int) mouseX - (w % 18)/2 - k + 1) / 18 < x + 1 &&
                 ((int) mouseY - (canvasHeight % 18) /2  - BUTTON_HEIGHT) / 18 >= y &&
                 ((int) mouseY - (canvasHeight % 18) /2  - BUTTON_HEIGHT) / 18 < y + 1 &&
                 (int) mouseY >= BUTTON_HEIGHT + (canvasHeight % 18) /2 - 1) {
            itemHovered = true;
            hoverItem = currentItems.get(i);
            if(!hiddenItems.contains(currentItems.get(i))) {
               if(!showHiddenItems) {
                  Utils.drawSlot(slotX, slotY, white);
               }
               else if(draggingFrom == null || !hiddenItems.contains(draggingFrom)){
                  Utils.drawSlot(slotX, slotY, lightRed);
               }
               else Utils.drawSlot(slotX, slotY, green);
            }
            else {
               if(draggingFrom == null || hiddenItems.contains(draggingFrom))
                  Utils.drawSlot(slotX, slotY, green);
               else Utils.drawSlot(slotX, slotY, lightRed);
            }
         }
         else if(showHiddenItems && hoverItem != null && currentItems.indexOf(hoverItem) < i &&
                 hoverItem.getItemID() == currentItems.get(i).getItemID() && shiftHeld && !Mouse.isButtonDown(0)) {
            if(!hiddenItems.contains(hoverItem))
               Utils.drawSlot(slotX, slotY, lightRed);
            else
               Utils.drawSlot(slotX, slotY, green);
         }
         else if(hiddenItems.contains(currentItems.get(i)) && draggingFrom == null) {
            Utils.drawSlot(slotX, slotY, darkRed);
         }
         else if (showHiddenItems && draggingFrom != null && hoverItem != null){
            if((currentItems.indexOf(draggingFrom) <= i && i < currentItems.indexOf(hoverItem) || (currentItems.indexOf(draggingFrom) >= i && i > currentItems.indexOf(hoverItem)))){
               if(!hiddenItems.contains(draggingFrom))
                  Utils.drawSlot(slotX, slotY, lightRed);
               else
                  Utils.drawSlot(slotX, slotY, green);

            }
            else {
               if(hiddenItems.contains(currentItems.get(i)))
                  Utils.drawSlot(slotX, slotY, darkRed);
            }
         }

         Utils.drawItemStack(slotX + 1, slotY + 1, currentItems.get(i), true);
         ++x;
         if (i == currentItems.size() - 1 && canvasHeight / 18 * (w / 18) > currentItems.size()) {
            index = 0;
         }
      }

      if (this.draggingFrom != null && !Mouse.isButtonDown(0)) {
         int lowerIndex;
         int higherIndex;
         if ((lowerIndex = currentItems.indexOf(this.draggingFrom)) >= (higherIndex = currentItems.indexOf(hoverItem))) {
            int temp = lowerIndex;
            lowerIndex = higherIndex;
            higherIndex = temp;
         }

         boolean hideItems = !hiddenItems.contains(this.draggingFrom);

         for(int j = lowerIndex; j <= higherIndex; ++j) {
            ItemStack currentItem = currentItems.get(j);
            if (hideItems) {
               if (!hiddenItems.contains(currentItem)) {
                  hiddenItems.add(currentItem);
                  hiddenItemsModified = true;
               }
            } else {
               hiddenItems.remove(currentItem);
               hiddenItemsModified = true;
            }
         }

         this.draggingFrom = null;
      }

      this.itemsPerPage = canvasHeight / 18 * ((w - w % 18) / 18);
      if (this.itemsPerPage == 0) {
         this.itemsPerPage = currentItems.size();
      }

      int pageIndex = index / this.itemsPerPage;
      if (index + this.itemsPerPage > currentItems.size()) {
         pageIndex = 0;
      }

      if (this.itemsPerPage < currentItems.size()) {
         pageIndex = index / this.itemsPerPage;
      }

      Utils.disableLighting();
      String page = pageIndex + 1 + "/" + (currentItems.size() / this.itemsPerPage + 1);
      this.fontRenderer.drawStringWithShadow(page, screen.width - w / 2 - this.fontRenderer.getStringWidth(page) / 2, 6, 16777215);
      GuiButtonHMI buttonNextPage = this.buttonNextPage;
      GuiButtonHMI buttonPrevPage = this.buttonPrevPage;
      boolean b = this.itemsPerPage < currentItems.size();
      buttonPrevPage.enabled = b;
      buttonNextPage.enabled = b;
      /*if (inventoryplayer.getCursorStack() != null) {
         Utils.drawItemStack(mouseX - 8, mouseY - 8, inventoryplayer.getCursorStack(), true);
      }*/

      if (!itemHovered) {
         hoverItem = null;
      }

      String s = "";
      ItemStack displayItem = null;
      if (inventoryplayer.getCursorStack() == null && hoverItem != null) {
         if (!showHiddenItems) {
            // s = Utils.getNiceItemName(hoverItem);
            displayItem = hoverItem;
         } else if (this.draggingFrom != null && this.draggingFrom != hoverItem) {
            if (hiddenItems.contains(hoverItem)) {
               s = "Unhide selected items";
            } else {
               s = "Hide selected items";
            }
         } else if (hiddenItems.contains(hoverItem)) {
            if (shiftHeld && hoverItem.getHasSubtypes()) {
               s = "Unhide all items with same ID and higher dmg";
            } else {
               s = "Unhide " + Utils.getNiceItemName(hoverItem);
            }
         } else if (shiftHeld && hoverItem.getHasSubtypes()) {
            s = "Hide all items with same ID and higher dmg";
         } else {
            s = "Hide " + Utils.getNiceItemName(hoverItem);
         }
      } else if (!HowManyFoxes.CONFIG.cheatsEnabled
         || inventoryplayer.getCursorStack() == null
         || hoverItem == null
            && (
               mouseY <= k + w % 18 / 2
                  || mouseY <= screen.height - 20 + canvasHeight % 18 / 2 - canvasHeight
                  || mouseX >= screen.width - w % 18 / 2
                  || mouseY <= 20 + canvasHeight % 18 / 2
                  || mouseY >= 20 + canvasHeight
            )) {
         if (this.buttonOptions.mousePressed(this.mc, mouseX, mouseY)) {
            if (!shiftHeld || HMIClient.getTabs().isEmpty()) {
               s = "Settings";
            } else if (this.guiBlock != null) {
               s = "View " + Utils.getNiceItemName(this.guiBlock, false) + " Recipes";
            } else {
               s = "View All Recipes";
            }
         } else if (HowManyFoxes.CONFIG.cheatsEnabled && !this.mc.theWorld.isRemote && this.buttonTimeDay.mousePressed(this.mc, mouseX, mouseY)) {
            s = "Set time to day";
         } else if (HowManyFoxes.CONFIG.cheatsEnabled && !this.mc.theWorld.isRemote && this.buttonTimeNight.mousePressed(this.mc, mouseX, mouseY)) {
            s = "Set time to night";
         } else if (HowManyFoxes.CONFIG.cheatsEnabled && !this.mc.theWorld.isRemote && this.buttonToggleRain.mousePressed(this.mc, mouseX, mouseY)) {
            s = "Toggle rain";
         } else if (HowManyFoxes.CONFIG.cheatsEnabled && !this.mc.theWorld.isRemote && this.buttonHeal.mousePressed(this.mc, mouseX, mouseY)) {
            s = "Heal";
         } else if (HowManyFoxes.CONFIG.cheatsEnabled && !this.mc.theWorld.isRemote && this.buttonTrash.mousePressed(this.mc, mouseX, mouseY)) {
            if (inventoryplayer.getCursorStack() == null) {
               if (shiftHeld) {
                  s = "Delete ALL Items";
               } else {
                  s = "Drag item here to delete";
               }
            } else if (shiftHeld) {
               s = "Delete ALL " + Utils.getNiceItemName(inventoryplayer.getCursorStack());
            } else {
               s = "Delete " + Utils.getNiceItemName(inventoryplayer.getCursorStack());
            }
         }
      } else {
         s = "Delete " + Utils.getNiceItemName(inventoryplayer.getCursorStack());
      }

      ItemStack hoveredItem;
      if (displayItem != null || !s.isEmpty()) {
         float k2 = mouseX;
         float i2 = mouseY;
         int j2 = this.fontRenderer.getStringWidth(s);
         if (mouseX + j2 + 12 > screen.width - 3) {
            k2 = mouseX - (mouseX + j2 + 12 - screen.width + 2);
         }

         if (mouseY - 15 < 0) {
            i2 = mouseY - (mouseY - 15);
         }

         if (displayItem != null) {
            Utils.drawTooltip(this, displayItem, k2, i2);
         } else {
            Utils.drawTooltip(s, k2, i2);
         }
      } /* else if (inventoryplayer.getCursorStack() == null &&
              (hoveredItem = Utils.hoveredItem(screen, mouseX, mouseY)) != null) {
         s = StringTranslate.getInstance().translateNamedKey(hoveredItem.getItemName());
         int j3 = this.fontRenderer.getStringWidth(s);
         if (mouseX + 9 <= k && mouseX + j3 + 15 > k) {
            Utils.drawRect(k, (int) (mouseY - 15), (int) (mouseX + j3 + 15), (int) (mouseY - 1), -1073741824);
            this.fontRenderer.drawStringWithShadow(s, mouseX + 12, mouseY - 12, -1);
         }

         if (s.isEmpty()) {
            Utils.drawTooltip(Utils.getNiceItemName(hoveredItem), (int) mouseX, (int) mouseY);
         } else if (Config.showItemIDs) {
            s = " " + hoveredItem.getItemID();
            if (hoveredItem.getHasSubtypes()) {
               s = s + ":" + hoveredItem.getItemDamage();
            }

            int j4 = this.fontRenderer.getStringWidth(s);
            Utils.drawRect((int) (mouseX + j3 + 15), (int) (mouseY - 15),
                    (int) (mouseX + j3 + j4 + 15), (int) (mouseY + 8 - 9), -1073741824);
            this.fontRenderer.drawStringWithShadow(s, mouseX + j3 + 12, mouseY - 12, -1);
         }
      } */
   }

   @Override
   public void mouseClicked(float posX, float posY, int eventButton) {
      boolean shiftHeld = Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
      if (System.currentTimeMillis() > guiClosedCooldown) {
         int k = (screen.width - this.xSize) / 2 + this.xSize + 1;
         int w = screen.width - (screen.width - this.xSize) / 2 - this.xSize - 1;
         int canvasHeight = screen.height - 40;
         if (HowManyFoxes.CONFIG.centredSearchBar) {
            canvasHeight += 20;
         }

         searchBox.mouseClicked(posX, posY, eventButton);
         if (!showHiddenItems) {
            if (hoverItem != null && this.mc.thePlayer.inventory.getCursorStack() == null) {
               if (this.mc.thePlayer.inventory.getCursorStack() == null && HowManyFoxes.CONFIG.cheatsEnabled) {
                  if (eventButton == 0 || eventButton == 1) {
                     if (!this.mc.theWorld.isRemote) {
                        ItemStack spawnedItem = hoverItem.copy();
                        if (eventButton == 0) {
                           spawnedItem.stackSize = hoverItem.getMaxStackSize();
                        } else {
                           spawnedItem.stackSize = 1;
                        }

                        this.mc.thePlayer.inventory.addItemStackToInventory(spawnedItem);
                     } else if (!HowManyFoxes.CONFIG.mpGiveCommand.isEmpty()) {
                        NumberFormat numberformat = NumberFormat.getIntegerInstance();
                        numberformat.setGroupingUsed(false);
                        MessageFormat messageformat = new MessageFormat(HowManyFoxes.CONFIG.mpGiveCommand);
                        messageformat.setFormatByArgumentIndex(1, numberformat);
                        messageformat.setFormatByArgumentIndex(2, numberformat);
                        messageformat.setFormatByArgumentIndex(3, numberformat);
                        Object[] aobj = new Object[]{
                           this.mc.thePlayer.username, hoverItem.getItemID(), eventButton == 0 ? hoverItem.getMaxStackSize() : 1, hoverItem.getItemDamage()
                        };
                        this.mc.thePlayer.sendChatMessage(messageformat.format(aobj));
                     }
                  }
               } else if (this.mc.thePlayer.inventory.getCursorStack() == null) {
                  HMIClient.pushRecipe(screen, hoverItem, eventButton == 1);
               }
            }
         } else if (hoverItem != null && this.mc.thePlayer.inventory.getCursorStack() == null) {
            if (hiddenItems.contains(hoverItem)) {
               if (shiftHeld) {
                  for(int i = currentItems.indexOf(hoverItem); currentItems.get(i).getItemID() == hoverItem.getItemID() && i < currentItems.size(); ++i) {
                     hiddenItems.remove(currentItems.get(i));
                     hiddenItemsModified = true;
                  }
               } else {
                  this.draggingFrom = hoverItem;
               }
            } else if (shiftHeld) {
               for(int i = currentItems.indexOf(hoverItem); currentItems.get(i).getItemID() == hoverItem.getItemID() && i < currentItems.size(); ++i) {
                  if (!hiddenItems.contains(currentItems.get(i))) {
                     hiddenItems.add(currentItems.get(i));
                     hiddenItemsModified = true;
                  }
               }
            } else {
               this.draggingFrom = hoverItem;
            }
         }

         if (this.mc.thePlayer.inventory.getCursorStack() != null
            && !this.mc.theWorld.isRemote
            && (
               hoverItem != null
                  || posX > k + w % 18 / 2
                     && posY > screen.height - 20 + canvasHeight % 18 / 2 - canvasHeight
                     && posX < screen.width - w % 18 / 2
                     && posY > 20 + canvasHeight % 18 / 2
                     && posY < 20 + canvasHeight
            )
            && HowManyFoxes.CONFIG.cheatsEnabled) {
            if (eventButton == 0) {
               this.mc.thePlayer.inventory.setCursorStack(null);
            } else if (eventButton == 1) {
               this.mc
                  .thePlayer
                  .inventory
                  .setCursorStack(this.mc.thePlayer.inventory.getCursorStack().splitStack(
                          this.mc.thePlayer.inventory.getCursorStack().stackSize - 1));
            }
         } else if (HowManyFoxes.CONFIG.cheatsEnabled
            && !this.mc.theWorld.isRemote
            && this.buttonTrash.mousePressed(this.mc, posX, posY)
            && this.mc.thePlayer.inventory.getCursorStack() != null
            && eventButton == 1) {
            this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
            if (this.mc.thePlayer.inventory.getCursorStack().stackSize > 1) {
               this.mc
                  .thePlayer
                  .inventory
                  .setCursorStack(this.mc.thePlayer.inventory.getCursorStack().splitStack(
                          this.mc.thePlayer.inventory.getCursorStack().stackSize - 1));
            } else {
               this.mc.thePlayer.inventory.setCursorStack(null);
            }
         } else {
            super.mouseClicked(posX, posY, eventButton);

            for(GuiButton guiButton : this.buttons()) {
               if (guiButton.mousePressed(this.mc, posX, posY)) {
                  return;
               }
            }

            if (!searchBox.hovered((int) posX, (int) posY)) {
               try {
                  screen.mouseClicked(posX, posY, eventButton);
               } catch (Exception var11) {
                  var11.printStackTrace();
               }
            }
         }
      }
   }

   @Override
   protected void actionPerformed(GuiButton guibutton) {
      boolean shiftHeld = Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
      if (guibutton == this.buttonNextPage) {
         this.incIndex();
      } else if (guibutton == this.buttonPrevPage) {
         this.decIndex();
      } else if (guibutton == this.buttonOptions) {
         if (!shiftHeld || HMIClient.getTabs().size() <= 0) {
            this.mc.displayGuiScreen(new GuiOptionsHMI(screen));
         } else if (this.guiBlock == null) {
            HMIClient.pushRecipe(screen, null, true);
         } else {
            HMIClient.pushTabBlock(screen, this.guiBlock);
         }
      } else if (guibutton != this.buttonTimeDay && guibutton != this.buttonTimeNight && guibutton != this.buttonToggleRain) {
         if (!this.mc.theWorld.isRemote && guibutton == this.buttonHeal) {
            this.mc.thePlayer.heal(100);
            this.mc.thePlayer.air = 300;
            if (this.mc.thePlayer.isBurning()) {
               this.mc.thePlayer.fire = -this.mc.thePlayer.fireResistance;
               this.mc.theWorld.playSoundAtEntity(this.mc.thePlayer, "random.fizz", 0.7F, 1.6F + (Utils.rand.nextFloat() - Utils.rand.nextFloat()) * 0.4F);
            }
         } else if (!this.mc.theWorld.isRemote && guibutton == this.buttonTrash) {
            if (this.mc.thePlayer.inventory.getCursorStack() == null) {
               if (shiftHeld && !(screen instanceof GuiRecipeViewer) && System.currentTimeMillis() > deleteAllWaitUntil) {
                  for(int i = 0; i < screen.inventorySlots.slots.size(); ++i) {
                     Slot slot = screen.inventorySlots.slots.get(i);
                     slot.putStack(null);
                  }
               }
            } else {
               if (shiftHeld) {
                  for(int i = 0; i < screen.inventorySlots.slots.size(); ++i) {
                     Slot slot = screen.inventorySlots.slots.get(i);
                     if (slot.getHasStack() && slot.getStack().isItemEqual(
                             this.mc.thePlayer.inventory.getCursorStack())) {
                        slot.putStack(null);
                     }
                  }

                  deleteAllWaitUntil = System.currentTimeMillis() + 1000L;
               }

               this.mc.thePlayer.inventory.setCursorStack(null);
            }
         }
      } else if (!this.mc.theWorld.isRemote) {
         try {
            if (guibutton == this.buttonTimeDay) {
               long l = this.mc.theWorld.worldInfo.getWorldTime() + 24000L;
               this.mc.theWorld.worldInfo.setWorldTime(l - l % 24000L);
            } else if (guibutton == this.buttonTimeNight) {
               long l = this.mc.theWorld.worldInfo.getWorldTime() + 24000L;
               this.mc.theWorld.worldInfo.setWorldTime(l - l % 24000L + 13000L);
            } else {
               this.mc.theWorld.worldInfo.setThundering(!this.mc.theWorld.worldInfo.getThundering());
               this.mc.theWorld.worldInfo.setRaining(!this.mc.theWorld.worldInfo.getRaining());
            }
         } catch (IllegalArgumentException var6) {
            var6.printStackTrace();
         }
      } else if (guibutton == this.buttonTimeDay) {
         this.mc.thePlayer.sendChatMessage(HowManyFoxes.CONFIG.mpTimeDayCommand);
      } else if (guibutton == this.buttonTimeNight) {
         this.mc.thePlayer.sendChatMessage(HowManyFoxes.CONFIG.mpTimeNightCommand);
      } else if (guibutton == this.buttonToggleRain) {
         try {
            if (this.mc.theWorld.worldInfo.getRaining()) {
               this.mc.thePlayer.sendChatMessage(HowManyFoxes.CONFIG.mpRainOFFCommand);
            } else {
               this.mc.thePlayer.sendChatMessage(HowManyFoxes.CONFIG.mpRainONCommand);
            }
         } catch (IllegalArgumentException var5) {
            var5.printStackTrace();
         }
      }
   }

   @Override
   public void keyTyped(char c, int i) {
      if (!searchBoxFocused()
         && HowManyFoxes.CONFIG.fastSearch
         && !HMIClient.keyHeldLastTick
         && i != this.mc.gameSettings.keyBindInventory.keyCode
         && i != HMFKeyBinds.KEY_ALL_RECIPES.keyCode
         && i != HMFKeyBinds.KEY_TOGGLE_OVERLAY.keyCode
         && (ChatAllowedCharacters.isAllowedCharacter(c) || i == 14 &&
              searchBox != null && !searchBox.getText().isEmpty())) {
         this.scaledresolution.setDimensions(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
         int i2 = this.scaledresolution.getScaledWidth();
         int j2 = this.scaledresolution.getScaledHeight();
         int posX = Mouse.getEventX() * i2 / this.mc.displayWidth;
         int posY = j2 - Mouse.getEventY() * j2 / this.mc.displayHeight - 1;
         if ((Utils.hoveredItem(screen, posX, posY) == null && hoverItem == null ||
                 i != HMFKeyBinds.KEY_GET_RECIPES.keyCode && i != HMFKeyBinds.KEY_GET_USES.keyCode)
            && (!(screen instanceof GuiRecipeViewer) || i != HMFKeyBinds.KEY_PREV_RECIPE.keyCode)
            && System.currentTimeMillis() > lastKeyTimeout) {
            searchBox.isFocused = true;
         }
      }

      if (searchBoxFocused()) {
         Keyboard.enableRepeatEvents(true);
         if (i == 1) {
            Keyboard.enableRepeatEvents(false);
            searchBox.setFocused(false);
         } else {
            searchBox.textboxKeyTyped(c, i);
         }

         if (searchBox.getText().length() > lastSearch.length()) {
            prevSearches.push(currentItems);
            currentItems = getCurrentList(currentItems);
         } else if (searchBox.getText().isEmpty()) {
            resetItems(false);
         } else if (searchBox.getText().length() < lastSearch.length()) {
            if (prevSearches.isEmpty()) {
               currentItems = getCurrentList(Utils.itemList());
            } else {
               currentItems = prevSearches.pop();
            }
         }

         lastSearch = searchBox.getText();
      } else {
         Keyboard.enableRepeatEvents(false);
         if (this.modTickKeyPress) {
            if (i != lastKey || System.currentTimeMillis() > lastKeyTimeout) {
               lastKey = i;
               lastKeyTimeout = System.currentTimeMillis() + 200L;
               if (this.mc.currentScreen == this) {
                  if (i == HMFKeyBinds.KEY_ALL_RECIPES.keyCode && this.mc.thePlayer.inventory.getCursorStack() == null) {
                     if (screen instanceof GuiRecipeViewer) {
                        ((GuiRecipeViewer)screen).push(null, false);
                     } else if (!HMIClient.getTabs().isEmpty()) {
                        GuiRecipeViewer newgui = new GuiRecipeViewer(null, false, screen);
                        this.mc.currentScreen = newgui;
                        this.scaledresolution.setDimensions(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
                        int i3 = this.scaledresolution.getScaledWidth();
                        int j3 = this.scaledresolution.getScaledHeight();
                        newgui.setWorldAndResolution(this.mc, i3, j3);
                     }
                  } else if (i == 1 && screen instanceof GuiRecipeViewer) {
                  }
               }
            }
         } else {
            try {
               screen.keyTyped(c, i);
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      }
   }

   public static void resetItems(boolean force) {
      if (force || !(stateReset && (searchBox == null || searchBox.getText().isEmpty()))) {
         currentItems = getCurrentList(Utils.itemList());
         prevSearches.clear();
      }
   }

   public boolean mouseOverUI(Minecraft minecraft, int posX, int posY) {
      for(GuiButton button : this.buttons()) {
         if (button.mousePressed(minecraft, posX, posY)) {
            return true;
         }
      }

      return searchBox.hovered(posX, posY) || posX > (this.xSize + screen.width) / 2;
   }

   @Override
   public void mouseMovedOrUp(float x, float y, int click) {
      super.mouseMovedOrUp(x, y, click);

      try {
         screen.mouseMovedOrUp(x, y, click);
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static boolean searchBoxFocused() {
      return searchBox != null && searchBox.isFocused;
   }

   @Override
   public void handleKeyboardInput() {
      if (searchBoxFocused()) {
         while(Keyboard.next()) {
            this.modTickKeyPress = false;
            if (Keyboard.getEventKeyState()) {
               this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
            }
         }
      } else if (Keyboard.getEventKeyState()) {
         this.modTickKeyPress = true;
         this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
      }
   }

   @Override
   public void handleMouseInput() {
      int posX = Mouse.getEventX() * screen.width / this.mc.displayWidth;
      int k = (screen.width - this.xSize) / 2 + this.xSize + 1;
      if (posX > k) {
         int i = Mouse.getEventDWheel();
         if (!HowManyFoxes.CONFIG.scrollInverted) {
            if (i > 0) {
               this.incIndex();
            }

            if (i < 0) {
               this.decIndex();
            }
         } else {
            if (i > 0) {
               this.decIndex();
            }

            if (i < 0) {
               this.incIndex();
            }
         }
      }

      super.handleMouseInput();
   }

   public void incIndex() {
      index += this.itemsPerPage;
      if (index > currentItems.size()) {
         index = 0;
      }
   }

   public void decIndex() {
      if (index > 0) {
         index -= this.itemsPerPage;
         if (index < 0) {
            index = 0;
         }
      } else {
         index = currentItems.size() - currentItems.size() % this.itemsPerPage;
      }
   }

   public static void clearSearchBox() {
      if (searchBox != null) {
         boolean wasFocused = searchBox.isFocused;
         searchBox.isFocused = true;
         searchBox.setText("");
         searchBox.isFocused = wasFocused;
         currentItems = getCurrentList(Utils.itemList());
         prevSearches.clear();
      }
   }

   private static ArrayList<ItemStack> getCurrentList(ArrayList<ItemStack> listToSearch) {
      index = 0;
      ArrayList<ItemStack> newList = new ArrayList<>();
      if (searchBox != null && !searchBox.getText().isEmpty()) {
         stateReset = false;
         for(ItemStack currentItem : listToSearch) {
            String s = (StringTranslate.getInstance().translateNamedKey(currentItem.getItemName())).trim();
            if (s.toLowerCase().contains(searchBox.getText().toLowerCase()) && (showHiddenItems || !hiddenItems.contains(currentItem))) {
               newList.add(currentItem);
            }
         }
      } else {
         stateReset = true;
         if (showHiddenItems) {
            return new ArrayList<>(Utils.itemList());
         }

         for(ItemStack currentItem : Utils.itemList()) {
            if (!hiddenItems.contains(currentItem)) {
               newList.add(currentItem);
            }
         }
      }

      return newList;
   }

   public void toggle() {
      if (this.buttonNextPage != null) {
         for(GuiButton obj : this.buttons()) {
            if (HowManyFoxes.CONFIG.overlayEnabled) {
               if (HowManyFoxes.CONFIG.cheatsEnabled || obj == this.buttonNextPage || obj == this.buttonPrevPage || obj == this.buttonOptions) {
                  obj.visible = true;
               }
            } else {
               obj.visible = false;
            }
         }

         searchBox.isEnabled = HowManyFoxes.CONFIG.overlayEnabled;
      }

      if (!HowManyFoxes.CONFIG.overlayEnabled) {
         Minecraft.theMinecraft.currentScreen = screen;
         hoverItem = null;
      }

      HowManyFoxes.forceSaveConfig();
   }

   public static void focusSearchBox() {
      if (searchBox != null && (searchBox.isFocused = !searchBox.isFocused)) {
         Keyboard.enableRepeatEvents(false);
      }
   }

   public static boolean emptySearchBox() {
      return searchBox != null && searchBox.getText().isEmpty();
   }

   public void onTick() {
      this.scaledresolution.setDimensions(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
      int posX = Mouse.getX() * this.scaledresolution.getScaledWidth() / this.mc.displayWidth;
      int posY = this.scaledresolution.getScaledHeight() - Mouse.getY() * this.scaledresolution.getScaledHeight() / this.mc.displayHeight - 1;
      Utils.preRender();
      this.drawScreen(posX, posY, 0F);
      if (this.mouseOverUI(this.mc, posX, posY)) {
         while(Mouse.next()) {
            this.handleMouseInput();
         }
      } else if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
         searchBox.mouseClicked(posX, posY, Mouse.getEventButton());
      }

      this.handleKeyboardInput();
      Utils.postRender();
   }
}
