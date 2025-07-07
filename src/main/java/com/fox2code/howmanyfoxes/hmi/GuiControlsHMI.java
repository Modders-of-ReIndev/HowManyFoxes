package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.HMIClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.input.Keyboard;

public class GuiControlsHMI extends GuiScreen {
   private GuiButton buttonDone;
   private int buttonId = -1;

   public GuiControlsHMI(GuiScreen guiscreen) {
      this.parentScreen = guiscreen;
   }

   private int func_20080_j() {
      return this.width / 2 - 155;
   }

   @Override
   public void initGui() {
      int i = this.func_20080_j();

      for(int j = 0; j < Config.keyBinds.length; ++j) {
         this.controlList.add(new GuiSmallButton(j, i + j % 2 * 160,
                 this.height / 6 + 24 * (j >> 1), 70, 20,
                 Keyboard.getKeyName(Config.keyBinds[j].keyCode)));
      }

      this.controlList.add(this.buttonDone = new GuiButton(-1,
              this.width / 2 - 100, this.height / 6 + 168, "Done"));
   }

   @Override
   public void mouseClicked(float x, float y, int click) {
      if (this.buttonId > -1 && click == 0) {
         for(int l = 0; l < this.controlList.size(); ++l) {
            GuiButton guibutton = (GuiButton)this.controlList.get(l);
            if (guibutton.id == this.buttonId && !guibutton.mousePressed(this.mc, x, y)) {
               guibutton.displayString = Keyboard.getKeyName(Config.keyBinds[l].keyCode);
               this.buttonId = -1;
               break;
            }
         }
      }

      super.mouseClicked(x, y, click);
   }

   @Override
   public void keyTyped(char c, int i) {
      if (this.buttonId >= 0) {
         if (i == 1) {
            i = 0;
         }

         if (Config.keyBinds[this.buttonId] == Config.toggleOverlay) {
            for(int j = 0; j < this.mc.gameSettings.keyBindings.length; ++j) {
               if (this.mc.gameSettings.keyBindings[j] == Config.toggleOverlay) {
                  this.mc.gameSettings.setKeyBinding(j, i);
               }
            }
         }

         Config.keyBinds[this.buttonId].keyCode = i;
         ((GuiButton)this.controlList.get(this.buttonId)).displayString = Keyboard.getKeyName(i);
         this.buttonId = -1;
         HMIClient.onSettingChanged();
      } else {
         super.keyTyped(c, i);
      }
   }

   @Override
   protected void actionPerformed(GuiButton guibutton) {
      if (guibutton == this.buttonDone) {
         this.mc.displayGuiScreen(this.parentScreen);
      } else {
         this.buttonId = guibutton.id;
         guibutton.displayString = "> " + Keyboard.getKeyName(Config.keyBinds[guibutton.id].keyCode) + " <";
         HMIClient.onSettingChanged();
      }
   }

   @Override
   public void drawScreen(float mouseX, float mouseY, float deltaTicks) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRenderer, "HMI Keybinds", this.width / 2, 20, 16777215);
      int k = this.func_20080_j();

      StringTranslate st = StringTranslate.getInstance();
      for(int l = 0; l < Config.keyBinds.length; ++l) {
         this.drawString(this.fontRenderer, st.translateKey(Config.keyBinds[l].keyDescription),
                 k + l % 2 * 160 + 70 + 6, this.height / 6 + 24 * (l >> 1) + 7, -1);
      }

      super.drawScreen(mouseX, mouseY, deltaTicks);
   }
}
