package com.fox2code.howmanyfoxes;

import com.fox2code.foxevents.EventHandler;
import com.fox2code.foxloader.client.gui.GuiConfigProvider;
import com.fox2code.foxloader.event.FoxLoaderEvents;
import com.fox2code.foxloader.event.client.GuiItemInfoEvent;
import com.fox2code.foxloader.launcher.FoxLauncher;
import com.fox2code.foxloader.loader.Mod;
import com.fox2code.howmanyfoxes.hmi.Config;
import com.fox2code.howmanyfoxes.hmi.GuiOptionsHMI;
import com.fox2code.howmanyfoxes.hmi.Utils;
import com.fox2code.howmanyfoxes.hmi.tabs.TabLootHints;
import net.minecraft.common.util.ChatColors;
import net.minecraft.common.util.i18n.StringTranslate;

import java.util.logging.Logger;

public class HowManyFoxes extends Mod {
    public static Logger logger;

    public HowManyFoxes() {
        logger = this.getLogger();
    }

    @Override
    public void onPreInit() {
        if (FoxLauncher.isClient()) {
            this.setConfigObject((GuiConfigProvider) GuiOptionsHMI::new);
            Config.init();
        }
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
        if (Config.showItemMod) {
            event.addDescriptionLine(ChatColors.BLUE +
                    event.getItemStack().getItem().getRegisteringMod().getModName());
        }
    }

    @EventHandler
    public void onAdditionalTooltipInfo(GuiItemInfoEvent event) {
        //for loot hints
        final TabLootHints tabLootHints = TabLootHints.tooltipHelper(event.getGuiScreen());
        if(tabLootHints != null) {
            if(tabLootHints.isAimedAtLoot(event.getItemStack())) {
                int[] amount = tabLootHints.getPossibleAmountOf(event.getItemStack());
                if(amount == null) {
                    event.addDescriptionLine(StringTranslate.getInstance().translateKeyFormat("hmf.loothints.amount", 1));
                } else if(amount.length == 1) {
                    event.addDescriptionLine(StringTranslate.getInstance().translateKeyFormat("hmf.loothints.amount", amount[0]));
                } else {
                    event.addDescriptionLine(StringTranslate.getInstance().translateKeyFormat("hmf.loothints.amount.range", amount[0], amount[1]));
                }

                float chance = tabLootHints.getChanceOf(event.getItemStack());
                if(chance != 0.0f) {
                    event.addDescriptionLine(StringTranslate.getInstance().translateKeyFormat("hmf.loothints.chance", chance));
                }
            }
        }

    }
}
