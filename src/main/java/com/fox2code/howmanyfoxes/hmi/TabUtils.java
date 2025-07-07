package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.HowManyFoxes;
import com.fox2code.howmanyfoxes.event.AddTabItemsEvent;
import com.fox2code.howmanyfoxes.event.RegisterTabsEvent;
import com.fox2code.howmanyfoxes.hmi.tabs.*;
import net.minecraft.client.gui.*;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TabUtils {
   private static final Map<Class<? extends GuiContainer>, ItemStack> guiToBlock = new HashMap<>();

   public static void loadTabs(ArrayList<Tab> tabList, String mod) {
      //workbench tab
      final TabCrafting workbenchTab = new TabCrafting(mod);
      tabList.add(workbenchTab);
      guiToBlock.put(GuiContainerCrafting.class, new ItemStack(Blocks.CRAFTING_TABLE));

      //furnace tab
      final TabSmelting smeltingTab = new TabSmelting(mod);
      tabList.add(smeltingTab);
      smeltingTab.equivalentCraftingStations.add(new ItemStack(Blocks.FURNACE_ACTIVE));
      guiToBlock.put(GuiContainerFurnace.class, new ItemStack(Blocks.FURNACE_IDLE));

      //forge tab
      final Tab forgingTab = new TabForging(mod, smeltingTab);
      tabList.add(forgingTab);
      forgingTab.equivalentCraftingStations.add(new ItemStack(Blocks.FORGE_ACTIVE));
      guiToBlock.put(GuiContainerForge.class, new ItemStack(Blocks.FORGE_IDLE));

      //refridgerizer tab
      final Tab freezingTab = new TabFreezing(mod);
      tabList.add(freezingTab);
      freezingTab.equivalentCraftingStations.add(new ItemStack(Blocks.REFRIDGIFREEZER_ACTIVE));
      guiToBlock.put(GuiContainerRefridgifreezer.class, new ItemStack(Blocks.REFRIDGIFREEZER_IDLE));

      //carpenter table tab
      final Tab carpentryTab = new TabCarpentry(mod);
      tabList.add(carpentryTab);
      guiToBlock.put(GuiContainerCarpentryTable.class, new ItemStack(Blocks.CARPENTRY_TABLE));

      //loot table tab
      for(TabLootHints.FancyPackedLoot packedLoot : TabLootHints.fetchKnownLootDetails()) {
         final Tab lootHintTab = new TabLootHints(mod, packedLoot);
         tabList.add(lootHintTab);
      }

      //custom tabs
      new RegisterTabsEvent(guiToBlock, tabList).callEvent();
   }

   public static ItemStack getItemFromGui(GuiContainer screen) {
      return guiToBlock.get(screen.getClass());
   }

   public static void addHiddenModItems(ArrayList<ItemStack> itemList) {
      new AddTabItemsEvent(itemList).callEvent();
   }

   public static ArrayList<Tab> orderTabs() {
       final ArrayList<Tab> orderedTabs = new ArrayList<>();

       for (Tab tab : HMIClient.allTabs) {
           if (tab.index >= 0) {
               while (orderedTabs.size() <= tab.index) {
                   orderedTabs.add(null);
               }
               orderedTabs.set(tab.index, tab);
           }
       }

       orderedTabs.removeAll(Collections.singleton(null));

       for (int i = 0; i < orderedTabs.size(); i++) {
           orderedTabs.get(i).index = i;
       }

       for (Tab tab : HMIClient.allTabs) {
           if (tab.index == -2) {
               tab.index = orderedTabs.size();
               orderedTabs.add(tab);
           } else if (tab.index < 0) {
               tab.index = -1;
           }
       }

       HowManyFoxes.forceSaveConfig();
       return orderedTabs;
   }
}
