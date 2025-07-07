package com.fox2code.howmanyfoxes.hmi.overlay;

import com.fox2code.howmanyfoxes.HowManyFoxes;
import com.fox2code.howmanyfoxes.hmi.GuiButtonHMI;
import com.fox2code.howmanyfoxes.hmi.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.creative.ContainerCreative;
import net.minecraft.client.gui.creative.CreativeTabs;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
 * Moved some utility buttons to a new class, so we can work with them separately
 */
public class OverlayUtilityButtons {
    private GuiButtonHMI buttonTimeDay;
    private GuiButtonHMI buttonTimeNight;
    private GuiButtonHMI buttonToggleRain;
    private GuiButtonHMI buttonToggleMode;
    private GuiButtonHMI buttonHeal;

    private boolean isMultiplayerWorld() {
        return Minecraft.getInstance().theWorld.isRemote;
    }

    public void initButtons(List<GuiElement> parentControlList, GuiContainer<?> screen, int beginID) {
        final boolean multiplayer = this.isMultiplayerWorld();
        int begin = 0;

        if (!multiplayer || !HowManyFoxes.CONFIG.mpTimeDayCommand.isEmpty()) {
            parentControlList.add(this.buttonTimeDay = new GuiButtonHMI(beginID++, (begin++) * 20, 0, 20, 12));
        }

        if (!multiplayer || !HowManyFoxes.CONFIG.mpTimeNightCommand.isEmpty()) {
            parentControlList.add(this.buttonTimeNight = new GuiButtonHMI(beginID++, (begin++) * 20, 0, 20, 13));
        }

        if (!multiplayer || !HowManyFoxes.CONFIG.mpRainOFFCommand.isEmpty() || !HowManyFoxes.CONFIG.mpRainONCommand.isEmpty()) {
            parentControlList.add(this.buttonToggleRain = new GuiButtonHMI(beginID++, (begin++) * 20, 0, 20, 14));
        }

        if (!multiplayer && ((screen instanceof GuiContainerInventory) || (screen instanceof GuiContainerCreative))) {
            parentControlList.add(this.buttonToggleMode = new GuiButtonHMI(beginID++, (begin++) * 20, 0, 20, 16));
        }

        if (!multiplayer || !HowManyFoxes.CONFIG.mpHealCommand.isEmpty()) {
            parentControlList.add(this.buttonHeal = new GuiButtonHMI(beginID++, (begin++) * 20, 0, 20, 15));
        }
    }

    public @Nullable String getTooltipFor(Minecraft mc, float mouseX, float mouseY) {
        if (!HowManyFoxes.CONFIG.cheatsEnabled || this.isMultiplayerWorld()) {
            return null;
        }

        if (this.buttonTimeDay != null && this.buttonTimeDay.mousePressed(mc, mouseX, mouseY)) {
            return "Set time to day";
        }

        if (this.buttonTimeNight != null && this.buttonTimeNight.mousePressed(mc, mouseX, mouseY)) {
            return "Set time to night";
        }

        if (this.buttonToggleRain != null && this.buttonToggleRain.mousePressed(mc, mouseX, mouseY)) {
            return "Toggle rain";
        }

        if (this.buttonHeal != null && this.buttonHeal.mousePressed(mc, mouseX, mouseY)) {
            return "Heal";
        }

        if (this.buttonToggleMode != null && this.buttonToggleMode.mousePressed(mc, mouseX, mouseY)) {
            return "Toggle Creative Inventory";
        }

        return null;
    }
    
    public boolean action(Minecraft mc, GuiContainer<?> screen, GuiButton button) {
        if(button == null) {
            return false;
        }

        //toggle inventory
        if(!mc.theWorld.isRemote && button == this.buttonToggleMode) {
            if(screen instanceof GuiContainerInventory) {
                mc.displayGuiScreen(new GuiContainerCreative(mc.thePlayer, new ContainerCreative(CreativeTabs.BUILDING_BLOCKS, mc.thePlayer)));
            } else if(screen instanceof GuiContainerCreative) {
                mc.displayGuiScreen(new GuiContainerInventory(mc.thePlayer));
            }
            return true;
        }

        //heal
        if (!mc.theWorld.isRemote && button == this.buttonHeal) {
            mc.thePlayer.heal(100);
            mc.thePlayer.air = 300;
            if (mc.thePlayer.isBurning()) {
                mc.thePlayer.fire = -mc.thePlayer.fireResistance;
                mc.theWorld.playSoundAtEntity(mc.thePlayer, "random.fizz", 0.7F, 1.6F + (Utils.rand.nextFloat() - Utils.rand.nextFloat()) * 0.4F);
            }
            return true;
        }

        //set day
        if(button == this.buttonTimeDay) {
            if(this.isMultiplayerWorld()) {
                mc.thePlayer.sendChatMessage(HowManyFoxes.CONFIG.mpTimeDayCommand);
            } else {
                long l = mc.theWorld.worldInfo.getWorldTime() + 24000L;
                mc.theWorld.worldInfo.setWorldTime(l - l % 24000L);
            }
            return true;
        }

        //set night
        if(button == this.buttonTimeNight) {
            if(this.isMultiplayerWorld()) {
                mc.thePlayer.sendChatMessage(HowManyFoxes.CONFIG.mpTimeNightCommand);
            } else {
                long l = mc.theWorld.worldInfo.getWorldTime() + 24000L;
                mc.theWorld.worldInfo.setWorldTime(l - l % 24000L + 13000L);
            }
            return true;
        }

        //toggle weather
        if(button == this.buttonToggleRain) {
            if(this.isMultiplayerWorld()) {
                mc.thePlayer.sendChatMessage(mc.theWorld.worldInfo.getRaining() ? HowManyFoxes.CONFIG.mpRainOFFCommand : HowManyFoxes.CONFIG.mpRainONCommand);
            } else {
                mc.theWorld.worldInfo.setThundering(!mc.theWorld.worldInfo.getThundering());
                mc.theWorld.worldInfo.setRaining(!mc.theWorld.worldInfo.getRaining());
            }
            return true;
        }

        return false;
    }
}
