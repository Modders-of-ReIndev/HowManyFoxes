package com.fox2code.howmanyfoxes.event;

import com.fox2code.foxevents.Event;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import net.minecraft.client.gui.GuiContainer;
import net.minecraft.common.item.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public final class RegisterTabsEvent extends Event {
    private final Map<Class<? extends GuiContainer>, ItemStack> guiToBlock;
    private final ArrayList<Tab> tabList;

    public RegisterTabsEvent(Map<Class<? extends GuiContainer>, ItemStack> guiToBlock, ArrayList<Tab> tabList) {
        this.guiToBlock = guiToBlock;
        this.tabList = tabList;
    }

    public Map<Class<? extends GuiContainer>, ItemStack> getGuiToBlock() {
        return this.guiToBlock;
    }

    public void addGuiToBlock(Class<? extends GuiContainer> cls, ItemStack itemStack) {
        this.guiToBlock.put(cls, itemStack);
    }

    public ArrayList<Tab> getTabList() {
        return this.tabList;
    }

    public void addTab(Tab tab) {
        this.tabList.add(tab);
    }

    public <T extends Tab> T getTabFromClass(Class<T> tabClass) {
        for (Tab tab : this.tabList) {
            if (tab.getClass() == tabClass) {
                return tabClass.cast(tab);
            }
        }
        return null;
    }
}
