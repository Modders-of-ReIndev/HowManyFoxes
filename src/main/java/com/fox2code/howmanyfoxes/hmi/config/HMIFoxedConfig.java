package com.fox2code.howmanyfoxes.hmi.config;

import com.fox2code.foxloader.client.gui.GuiConfigProviderConfigObject;
import com.fox2code.foxloader.config.ConfigEntry;
import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.hmi.GuiOptionsHMI;
import com.fox2code.howmanyfoxes.hmi.GuiRecipeViewer;
import com.fox2code.howmanyfoxes.hmi.Utils;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.common.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class HMIFoxedConfig implements GuiConfigProviderConfigObject {

    /* ===== GENERAL CONFIG ENTRIES ===== */

    @ConfigEntry
    public boolean overlayEnabled = true;

    @ConfigEntry
    public boolean cheatsEnabled = false;

    @ConfigEntry
    public boolean showItemMod = false;

    @ConfigEntry
    public String hiddenItems = HMIFoxedConfig.packHiddenItems(DefaultHiddenItems.DEFAULT_HIDDEN_ITEMS);

    @ConfigEntry
    public String tableIndexes = HMIFoxedConfig.packTabIndexes();

    /* ===== OVERLAY GUI SPECIFIC ENTRIES ===== */

    @ConfigEntry
    public boolean centredSearchBar = false;

    @ConfigEntry
    public boolean fastSearch = false;

    @ConfigEntry
    public boolean scrollInverted = false;


    /* ===== AUTO-PUSH COMMANDS ENTRIES ===== */

    @ConfigEntry
    public String mpGiveCommand = "/give {0} {1} {2}";

    @ConfigEntry
    public String mpHealCommand = "";

    @ConfigEntry
    public String mpTimeDayCommand = "/time set 0";

    @ConfigEntry
    public String mpTimeNightCommand = "/time set 13000";

    @ConfigEntry
    public String mpRainONCommand = "/weather rain";

    @ConfigEntry
    public String mpRainOFFCommand = "/weather clear";


    /* ===== RECIPE VIEWER GUI ===== */

    @ConfigEntry
    public boolean recipeViewerDraggableGui = false;

    @ConfigEntry
    public int recipeViewerGuiWidth = GuiRecipeViewer.RECIPE_VIEWER_DEFAULT_WIDTH;

    @ConfigEntry
    public int recipeViewerGuiHeight = GuiRecipeViewer.RECIPE_VIEWER_DEFAULT_HEIGHT;


    @Override
    public GuiScreen provideConfigScreen(GuiScreen parent) {
        return new GuiOptionsHMI(parent);
    }

    public static @NotNull String packHiddenItems(@Nullable ArrayList<ItemStack> hiddenItems) {
        if (hiddenItems == null || hiddenItems.isEmpty()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hiddenItems.size(); ++i) {
            if (i > 0) {
                builder.append(",");
            }

            final ItemStack item = hiddenItems.get(i);
            builder.append(item.getItemID());

            if (item.getHasSubtypes()) {
                builder.append(":").append(item.getItemDamage());
                int meta = item.getItemDamage();

                for (int q = i + 1; q < hiddenItems.size() &&
                        hiddenItems.get(q).getItemID() == item.getItemID(); i = q++) {
                    if (++meta != hiddenItems.get(q).getItemDamage()) {
                        --meta;
                        break;
                    }
                }

                if (meta > item.getItemDamage()) {
                    builder.append("-").append(meta);
                }
            }
        }

        return builder.toString();
    }

    public static @NotNull ArrayList<ItemStack> unpackHiddenItems(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }

        final String[] codes = input.split(",");
        final ArrayList<ItemStack> collector = new ArrayList<>();

        for (String fullItemStack : codes) {
            if (fullItemStack.contains(":")) {
                final String[] splitIDMeta = fullItemStack.split(":");

                if (splitIDMeta[1].contains("-")) {
                    final String[] meta = splitIDMeta[1].split("-");
                    final int minMeta = Integer.parseInt(meta[0]);
                    final int maxMeta = Integer.parseInt(meta[1]);
                    for (int q = minMeta; q <= maxMeta; ++q) {
                        collector.add(new ItemStack(Integer.parseInt(splitIDMeta[0]), 1, q));
                    }
                    continue;
                }

                collector.add(new ItemStack(Integer.parseInt(splitIDMeta[0]), 1, Integer.parseInt(splitIDMeta[1])));
                continue;
            }

            if (!fullItemStack.isEmpty()) {
                collector.add(new ItemStack(Integer.parseInt(fullItemStack), 1, 0));
            }
        }

        return collector;
    }

    public static @NotNull String packTabIndexes() {
        if (HMIClient.allTabs == null) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < Utils.visibleTabSize(); ++i) {
            for (Tab tab : HMIClient.allTabs) {
                if (tab.index == i) {
                    builder.append(tab.TAB_CREATOR).append(":").append(tab.name()).append(":").append(tab.index).append(";");
                }
            }
        }

        for (Tab tab2 : HMIClient.allTabs) {
            if (tab2.index == -1) {
                builder.append(tab2.TAB_CREATOR).append(":").append(tab2.name()).append(":").append(tab2.index).append(";");
            }
        }

        return builder.toString();
    }

    public static void unpackTabIndexes(@Nullable String input) {
        if(input == null || input.isEmpty() || HMIClient.allTabs == null) {
            return;
        }

        final String[] tabsInfo = input.split(";");

        for(String description : tabsInfo) {
            if(description == null || description.isEmpty()) {
                continue;
            }

            final String[] everything = description.split(":");

            for (Tab tab : HMIClient.allTabs) {
                if (tab.TAB_CREATOR.equalsIgnoreCase(everything[0]) && tab.name().equalsIgnoreCase(everything[1])) {
                    tab.index = Integer.parseInt(everything[2]);
                }
            }
        }
    }
}
