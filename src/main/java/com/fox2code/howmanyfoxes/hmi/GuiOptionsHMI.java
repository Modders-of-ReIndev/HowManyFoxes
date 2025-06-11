package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.HMIClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;

public class GuiOptionsHMI extends GuiScreen {
   private GuiButton buttonCheats;
   private GuiButton buttonIDs;
   private GuiButton buttonCentredSearchBar;
   private GuiButton buttonFastSearch;
   private GuiButton buttonHiding;
   private GuiButton buttonInvertedScroll;
   private GuiButton buttonKeybinds;
   private GuiButton buttonTabOrder;
   private GuiButton buttonDone;
   private int lastMouseX;
   private int lastMouseY;
   private long mouseStillTime;
   private final GuiScreen parentScreen;

   public GuiOptionsHMI(GuiScreen guiscreen) {
      this.parentScreen = guiscreen;
   }

   @Override
   public void initGui() {
      int i = -1;
      this.controlList
         .add(
            this.buttonCheats = new GuiSmallButton(
               ++i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), "Mode: " + (Config.cheatsEnabled ? "Cheat Mode" : "Recipe Mode")
            )
         );
      this.controlList
         .add(
            this.buttonIDs = new GuiSmallButton(
               ++i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), "Item IDs: " + (Config.showItemIDs ? "ON" : "OFF")
            )
         );
      this.controlList
         .add(
            this.buttonCentredSearchBar = new GuiSmallButton(
               ++i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), "Centred Search Bar: " + (Config.centredSearchBar ? "ON" : "OFF")
            )
         );
      this.controlList
         .add(
            this.buttonFastSearch = new GuiSmallButton(
               ++i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), "Fast Search: " + (Config.fastSearch ? "ON" : "OFF")
            )
         );
      this.controlList
         .add(
            this.buttonHiding = new GuiSmallButton(
               ++i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), "Hide Items Mode: " + (GuiOverlay.showHiddenItems ? "ON" : "OFF")
            )
         );
      this.controlList
         .add(
            this.buttonInvertedScroll = new GuiSmallButton(
               ++i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), "Flip Scroll Direction: " + (Config.scrollInverted ? "ON" : "OFF")
            )
         );
      this.controlList.add(this.buttonKeybinds = new GuiButton(++i, this.width / 2 - 100, this.height / 6 + 96 + 12, "Keybinds..."));
      this.controlList.add(this.buttonTabOrder = new GuiButton(++i, this.width / 2 - 100, this.height / 6 + 120 + 12, "Recipe Viewer Settings..."));
      this.controlList.add(this.buttonDone = new GuiButton(++i, this.width / 2 - 100, this.height / 6 + 168, "Done"));
   }

   @Override
   protected void actionPerformed(GuiButton guibutton) {
      if (guibutton == this.buttonCheats) {
         Config.cheatsEnabled = !Config.cheatsEnabled;
         this.buttonCheats.displayString = "Mode: " + (Config.cheatsEnabled ? "Cheat Mode" : "Recipe Mode");
      } else if (guibutton == this.buttonIDs) {
         Config.showItemIDs = !Config.showItemIDs;
         this.buttonIDs.displayString = "Item IDs: " + (Config.showItemIDs ? "ON" : "OFF");
      } else if (guibutton == this.buttonCentredSearchBar) {
         Config.centredSearchBar = !Config.centredSearchBar;
         this.buttonCentredSearchBar.displayString = "Centred Search Bar: " + (Config.centredSearchBar ? "ON" : "OFF");
      } else if (guibutton == this.buttonFastSearch) {
         Config.fastSearch = !Config.fastSearch;
         this.buttonFastSearch.displayString = "Fast Search: " + (Config.fastSearch ? "ON" : "OFF");
      } else if (guibutton == this.buttonHiding) {
         GuiOverlay.showHiddenItems = !GuiOverlay.showHiddenItems;
         GuiOverlay.resetItems(true);
         this.buttonHiding.displayString = "Hide Items Mode: " + (GuiOverlay.showHiddenItems ? "ON" : "OFF");
      } else if (guibutton == this.buttonInvertedScroll) {
         Config.scrollInverted = !Config.scrollInverted;
         this.buttonInvertedScroll.displayString = "Flip Scroll Direction: " + (Config.scrollInverted ? "ON" : "OFF");
      } else {
         if (guibutton == this.buttonDone) {
            GuiOverlay.guiClosedCooldown = System.currentTimeMillis() + 100L;
            this.mc.displayGuiScreen(this.parentScreen);
            return;
         }

         if (guibutton == this.buttonKeybinds) {
            this.mc.displayGuiScreen(new GuiControlsHMI(this));
            return;
         }

         if (guibutton == this.buttonTabOrder) {
            this.mc.displayGuiScreen(new GuiTabOrder(this));
            return;
         }
      }

      HMIClient.onSettingChanged();
   }

   @Override
   public void drawScreen(float posX, float posY, float f) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRenderer, "HMI Options", this.width / 2, 20, 16777215);
      super.drawScreen(posX, posY, f);
      if (Math.abs(posX - this.lastMouseX) <= 5 && Math.abs(posY - this.lastMouseY) <= 5) {
         int k = 700;
         if (System.currentTimeMillis() >= this.mouseStillTime + 700L) {
            GuiButton hoveredButton = null;

            for(GuiButton obj : this.buttons()) {
               if (obj.mousePressed(this.mc, posX, posY)) {
                  hoveredButton = obj;
               }
            }

            if (hoveredButton != null && this.getTooltipContent(hoveredButton) != null) {
               String[] tooltip = this.getTooltipContent(hoveredButton);
               if (tooltip != null) {
                  int i = this.width / 2 - 150;
                  int j = this.height / 6 - 5;
                  if (posY <= j + 98) {
                     j += 182 - 11 * tooltip.length;
                  }

                  int j2 = i + 150 + 150;
                  int k2 = j + 11 * tooltip.length + 6;
                  this.drawGradientRect(i, j, j2, k2, -536870912, -536870912);

                  for (int l1 = 0; l1 < tooltip.length; ++l1) {
                     String line = tooltip[l1];
                     this.fontRenderer.drawStringWithShadow(line, i + 5, j + 5 + l1 * 11, 14540253);
                  }
               }
            }
         }
      } else {
         this.lastMouseX = (int) posX;
         this.lastMouseY = (int) posY;
         this.mouseStillTime = System.currentTimeMillis();
      }
   }

   private String[] getTooltipContent(GuiButton guibutton) {
      if (guibutton == this.buttonCheats) {
         return new String[]{
            "Recipe Mode",
            "  LMB on items to see recipes and RMB to see uses",
            "Cheat Mode",
            "  LMB on items to spawn a stack and RMB to spawn 1",
            "  Also enables utility buttons"
         };
      } else if (guibutton == this.buttonIDs) {
         return new String[]{"Show item IDs in HowManyItems overlay"};
      } else if (guibutton == this.buttonFastSearch) {
         return new String[]{"Automatically focus the searchbar when you press a key"};
      } else if (guibutton == this.buttonHiding) {
         return new String[]{
            "View and configure hidden items",
            "  Click to toggle an item being hidden",
            "  Shift click to toggle items with the same ID and higher dmg",
            "  Click and drag to toggle all selected items",
            "",
            "Turn off to save config"
         };
      } else if (guibutton == this.buttonInvertedScroll) {
         return new String[]{"Invert page scroll direction when using mouse wheel"};
      } else {
         return guibutton == this.buttonTabOrder
            ? new String[]{"Enable/disable recipe viewer tabs", "Change recipe viewer tab order", "Change recipe viewer gui size option"}
            : null;
      }
   }
}
