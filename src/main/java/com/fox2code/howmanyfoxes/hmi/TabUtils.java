package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.event.AddTabItemsEvent;
import com.fox2code.howmanyfoxes.event.RegisterTabsEvent;
import com.fox2code.howmanyfoxes.hmi.tabs.*;
import net.minecraft.client.gui.*;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TabUtils {
   private static final Map<Class<? extends GuiContainer>, ItemStack> guiToBlock = new HashMap<>();

   public static void loadTabs(ArrayList<Tab> tabList, String mod) {
      TabCrafting workbenchTab = new TabCrafting(mod);
      tabList.add(workbenchTab);
      guiToBlock.put(GuiContainerCrafting.class, new ItemStack(Blocks.CRAFTING_TABLE));
      TabSmelting smeltingTab = new TabSmelting(mod);
      tabList.add(smeltingTab);
      smeltingTab.equivalentCraftingStations.add(new ItemStack(Blocks.FURNACE_ACTIVE));
      guiToBlock.put(GuiContainerFurnace.class, new ItemStack(Blocks.FURNACE_IDLE));
      Tab forgingTab = new TabForging(mod, smeltingTab);
      tabList.add(forgingTab);
      forgingTab.equivalentCraftingStations.add(new ItemStack(Blocks.FORGE_ACTIVE));
      guiToBlock.put(GuiContainerForge.class, new ItemStack(Blocks.FORGE_IDLE));
      Tab freezingTab = new TabFreezing(mod);
      tabList.add(freezingTab);
      freezingTab.equivalentCraftingStations.add(new ItemStack(Blocks.REFRIDGIFREEZER_ACTIVE));
      guiToBlock.put(GuiContainerRefridgifreezer.class, new ItemStack(Blocks.REFRIDGIFREEZER_IDLE));
      Tab carpentryTab = new TabCarpentry(mod);
      tabList.add(carpentryTab);
      guiToBlock.put(GuiContainerCarpentryTable.class, new ItemStack(Blocks.CARPENTRY_TABLE));
      new RegisterTabsEvent(guiToBlock, tabList).callEvent();
   }

   public static ItemStack getItemFromGui(GuiContainer screen) {
      return guiToBlock.get(screen.getClass());
   }

   public static void addHiddenModItems(ArrayList<ItemStack> itemList) {
      new AddTabItemsEvent(itemList).callEvent();
   }
}
