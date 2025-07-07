package com.fox2code.howmanyfoxes;

import com.fox2code.foxevents.EventHandler;
import com.fox2code.foxloader.config.ConfigIO;
import com.fox2code.foxloader.event.FoxLoaderEvents;
import com.fox2code.foxloader.event.client.GuiItemInfoEvent;
import com.fox2code.foxloader.launcher.FoxLauncher;
import com.fox2code.foxloader.loader.Mod;
import com.fox2code.foxloader.loader.ModContainer;
import com.fox2code.howmanyfoxes.hmi.TabUtils;
import com.fox2code.howmanyfoxes.hmi.config.HMFKeyBinds;
import com.fox2code.howmanyfoxes.hmi.overlay.GuiOverlay;
import com.fox2code.howmanyfoxes.hmi.Utils;
import com.fox2code.howmanyfoxes.hmi.config.HMIFoxedConfig;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import com.fox2code.howmanyfoxes.hmi.tabs.TabLootHints;
import net.minecraft.common.util.ChatColors;
import net.minecraft.common.util.i18n.StringTranslate;

import java.util.logging.Logger;

public class HowManyFoxes extends Mod {
    public static HMIFoxedConfig CONFIG = new HMIFoxedConfig();
    private static ModContainer CONTAINER;
    public static Logger logger;

    public HowManyFoxes() {
        logger = this.getLogger();
    }

    @Override
    public void onPreInit() {
        CONTAINER = this.getModContainer();
        if (FoxLauncher.isClient()) {
            this.setConfigObject(CONFIG);
            //read additional stuff
            GuiOverlay.hiddenItems = HMIFoxedConfig.unpackHiddenItems(CONFIG.hiddenItems);
            HMIFoxedConfig.unpackTabIndexes(CONFIG.tableIndexes);
            HMFKeyBinds.register();
        }
    }

    public static void forceSaveConfig() {
        CONFIG.hiddenItems = HMIFoxedConfig.packHiddenItems(GuiOverlay.hiddenItems);
        CONFIG.tableIndexes = HMIFoxedConfig.packTabIndexes();
        ConfigIO.writeConfiguration(CONTAINER, CONFIG);
    }

    @Override
    public void onPostInit() {
        if (FoxLauncher.isClient()) {
            System.out.println("HowManyFoxes: Post init!");
            FoxLoaderEvents.INSTANCE.registerEvents(HMIClient.INSTANCE);
            new Thread(Utils::itemList, "HowManyFoxes async pre-init thread!").start();
        }
    }

    @EventHandler(priority = -1000)
    public void onGetItemInfo(GuiItemInfoEvent event) {
        if (HowManyFoxes.CONFIG.showItemMod) {
            event.addDescriptionLine(ChatColors.BLUE +
                    event.getItemStack().getItem().getRegisteringMod().getModName());
        }
    }

    @EventHandler
    public void onAdditionalTooltipInfo(GuiItemInfoEvent event) {
        //for loot hints
        final Tab tab = TabUtils.getTabOfGuiScreen(event.getGuiScreen());
        if(tab != null) {
            tab.onAdditionalTooltipInfo(event);
        }
    }
}
