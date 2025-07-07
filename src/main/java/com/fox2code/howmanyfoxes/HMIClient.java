package com.fox2code.howmanyfoxes;

import com.fox2code.foxevents.EventHandler;
import com.fox2code.foxloader.event.GlobalTickEvent;
import com.fox2code.foxloader.event.client.CameraAndRenderUpdatedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.common.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import com.fox2code.howmanyfoxes.hmi.HMFKeyBinds;
import com.fox2code.howmanyfoxes.hmi.GuiOverlay;
import com.fox2code.howmanyfoxes.hmi.GuiRecipeViewer;
import com.fox2code.howmanyfoxes.hmi.TabUtils;
import com.fox2code.howmanyfoxes.hmi.Utils;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;

public class HMIClient {
    private GuiOverlay overlay;
    public static final HMIClient INSTANCE = new HMIClient();
    public static boolean keyHeldLastTick = false;
    private static long focusCooldown = 0L;
    private static ArrayList<Tab> tabs;
    public static ArrayList<Tab> allTabs;
    private static final ArrayList<Tab> modTabs = new ArrayList<>();
    public static ScaledResolution scaledresolution = new ScaledResolution();
    private boolean HMI_LOCAL_BLOCKED_KEY;

    private HMIClient() {}

    @EventHandler
    public void onTick(GlobalTickEvent event) {
        GuiScreen guiscreen = Minecraft.getInstance().currentScreen;
        if (guiscreen instanceof GuiRecipeViewer) {
            ((GuiRecipeViewer) guiscreen).tickInventory();
        }
    }

    public static void addModTab(Tab tab) {
        if (tab != null) {
            modTabs.add(tab);
        }
    }

    public static void onSettingChanged() {
        if (INSTANCE.overlay != null) {
            INSTANCE.overlay.initGui();
        }

        HowManyFoxes.forceSaveConfig();
    }

    public void tickGui(Minecraft mc, GuiScreen guiscreen) {
        final boolean isGuiContainer = guiscreen instanceof GuiContainer;
        if (isGuiContainer || guiscreen instanceof GuiOverlay) {
            GuiContainer screen = isGuiContainer ? (GuiContainer)guiscreen : null;
            if (HowManyFoxes.CONFIG.overlayEnabled && isGuiContainer) {
                if (GuiOverlay.screen != screen
                        || this.overlay == null
                        || screen.width != this.overlay.width
                        || screen.height != this.overlay.height
                        || screen.getXSize() != this.overlay.xSize
                        || screen.getYSize() != this.overlay.ySize) {
                    this.overlay = new GuiOverlay(screen);
                }

                this.overlay.onTick();
            }

            Utils.drawStoredToolTip();
            if (!Keyboard.isKeyDown(HMFKeyBinds.KEY_GET_RECIPES.keyCode) && !Keyboard.isKeyDown(HMFKeyBinds.KEY_GET_USES.keyCode)) {
                if (Keyboard.isKeyDown(HMFKeyBinds.KEY_PREV_RECIPE.keyCode)) {
                    if (!keyHeldLastTick) {
                        if ((guiscreen instanceof GuiRecipeViewer ||
                                guiscreen instanceof GuiOverlay) && !GuiOverlay.searchBoxFocused()) {
                            ((GuiRecipeViewer)guiscreen).pop();
                        } else if (HowManyFoxes.CONFIG.overlayEnabled
                                && guiscreen == GuiOverlay.screen
                                && !GuiOverlay.searchBoxFocused()
                                && HowManyFoxes.CONFIG.fastSearch
                                && !GuiOverlay.emptySearchBox()) {
                            GuiOverlay.focusSearchBox();
                        }
                    }
                } else if (HMFKeyBinds.KEY_CLEAR_SEARCHBOX.keyCode == HMFKeyBinds.KEY_FOCUS_SEARCHBOX.keyCode && Keyboard.isKeyDown(HMFKeyBinds.KEY_CLEAR_SEARCHBOX.keyCode)) {
                    if (System.currentTimeMillis() > focusCooldown) {
                        focusCooldown = System.currentTimeMillis() + 800L;
                        if (!GuiOverlay.searchBoxFocused()) {
                            GuiOverlay.clearSearchBox();
                        }

                        GuiOverlay.focusSearchBox();
                    }
                } else if (Keyboard.isKeyDown(HMFKeyBinds.KEY_CLEAR_SEARCHBOX.keyCode)) {
                    GuiOverlay.clearSearchBox();
                } else if (Keyboard.isKeyDown(HMFKeyBinds.KEY_FOCUS_SEARCHBOX.keyCode)) {
                    if (System.currentTimeMillis() > focusCooldown) {
                        focusCooldown = System.currentTimeMillis() + 800L;
                        GuiOverlay.focusSearchBox();
                    }
                } else if (Keyboard.isKeyDown(HMFKeyBinds.KEY_ALL_RECIPES.keyCode)) {
                    pushRecipe(guiscreen, null, false);
                } else {
                    keyHeldLastTick = false;
                }
            } else if (!keyHeldLastTick) {
                boolean getUses = Keyboard.isKeyDown(HMFKeyBinds.KEY_GET_USES.keyCode);
                scaledresolution.setDimensions(mc.gameSettings, mc.displayWidth, mc.displayHeight);
                int i = scaledresolution.getScaledWidth();
                int j = scaledresolution.getScaledHeight();
                int posX = Mouse.getEventX() * i / mc.displayWidth;
                int posY = j - Mouse.getEventY() * j / mc.displayHeight - 1;
                ItemStack newFilter = isGuiContainer ?
                        Utils.hoveredItem((GuiContainer)guiscreen, posX, posY) : null;
                if (newFilter == null) {
                    newFilter = GuiOverlay.hoverItem;
                }

                if (newFilter == null && guiscreen instanceof GuiRecipeViewer) {
                    newFilter = ((GuiRecipeViewer)guiscreen).getHoverItem();
                }

                if (newFilter != null) {
                    pushRecipe(guiscreen, newFilter, getUses);
                } else if (HowManyFoxes.CONFIG.overlayEnabled && guiscreen == GuiOverlay.screen && !GuiOverlay.searchBoxFocused() && HowManyFoxes.CONFIG.fastSearch) {
                    GuiOverlay.focusSearchBox();
                }
            }

            if (Keyboard.isKeyDown(HMFKeyBinds.KEY_GET_RECIPES.keyCode) ||
                    Keyboard.isKeyDown(HMFKeyBinds.KEY_GET_USES.keyCode) ||
                    Keyboard.isKeyDown(HMFKeyBinds.KEY_PREV_RECIPE.keyCode)) {
                keyHeldLastTick = true;
            }
        }
    }

    public void tickGame(Minecraft minecraft) {
        if (minecraft.currentScreen == null && Keyboard.isKeyDown(HMFKeyBinds.KEY_ALL_RECIPES.keyCode) && !keyHeldLastTick) {
            keyHeldLastTick = true;
            pushRecipe(null, null, false);
        }
    }

    public void runClickEvent() {
        if (Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof GuiContainer && !GuiOverlay.searchBoxFocused()) {
            HowManyFoxes.CONFIG.overlayEnabled = !HowManyFoxes.CONFIG.overlayEnabled;
            HowManyFoxes.forceSaveConfig();
            if (this.overlay != null) {
                this.overlay.toggle();
            }
        }
    }

    public static void pushRecipe(GuiScreen gui, ItemStack item, boolean getUses) {
        Minecraft mc = Minecraft.theMinecraft;
        if (mc.thePlayer.inventory.getCursorStack() == null) {
            if (gui instanceof GuiRecipeViewer) {
                ((GuiRecipeViewer)gui).push(item, getUses);
            } else if (!GuiOverlay.searchBoxFocused() && !getTabs().isEmpty()) {
                mc.setIngameNotInFocus();
                GuiRecipeViewer newgui = new GuiRecipeViewer(item, getUses, gui);
                mc.currentScreen = newgui;
                scaledresolution.setDimensions(mc.gameSettings, mc.displayWidth, mc.displayHeight);
                int i = scaledresolution.getScaledWidth();
                int j = scaledresolution.getScaledHeight();
                newgui.setWorldAndResolution(mc, i, j);
                mc.skipRenderWorld = false;
            }
        }
    }

    public static void pushTabBlock(GuiScreen gui, ItemStack item) {
        if (gui instanceof GuiRecipeViewer) {
            ((GuiRecipeViewer)gui).pushTabBlock(item);
        } else if (!GuiOverlay.searchBoxFocused() && !getTabs().isEmpty()) {
            Minecraft mc = Minecraft.theMinecraft;
            mc.setIngameNotInFocus();
            GuiRecipeViewer newgui = new GuiRecipeViewer(item, gui);
            mc.currentScreen = newgui;
            scaledresolution.setDimensions(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            newgui.setWorldAndResolution(mc, i, j);
            mc.skipRenderWorld = false;
        }
    }

    public static ArrayList<Tab> getTabs() {
        if (tabs == null) {
            TabUtils.loadTabs(allTabs = new ArrayList<>(), "HMF");
            allTabs.addAll(modTabs);
            //Config.readConfig();
            tabs = TabUtils.orderTabs();
        }

        return tabs;
    }

    public static void tabOrderChanged(boolean[] tabEnabled, Tab[] tabOrder) {
        for (Tab tab : HMIClient.allTabs) {
            for (int i = 0; i < tabOrder.length; ++i) {
                if (tab.equals(tabOrder[i])) {
                    tab.index = i;
                    if (!tabEnabled[i]) {
                        tab.index = -1;
                    }
                }
            }
        }
        HowManyFoxes.forceSaveConfig();
        tabs = TabUtils.orderTabs();
    }

    @EventHandler
    public void onCameraAndRenderUpdated(CameraAndRenderUpdatedEvent event) {
        if (this.HMI_LOCAL_BLOCKED_KEY &&
                !Keyboard.isKeyDown(HMFKeyBinds.KEY_TOGGLE_OVERLAY.keyCode)) {
            this.HMI_LOCAL_BLOCKED_KEY = false;
        }

        if (Minecraft.getInstance().theWorld != null) {
            HMIClient.INSTANCE.tickGame(Minecraft.getInstance());
        }

        if (Minecraft.getInstance().currentScreen != null) {
            HMIClient.INSTANCE.tickGui(Minecraft.getInstance(), Minecraft.getInstance().currentScreen);
        }

        if (!this.HMI_LOCAL_BLOCKED_KEY &&
                Keyboard.isKeyDown(HMFKeyBinds.KEY_TOGGLE_OVERLAY.keyCode)) {
            HMIClient.INSTANCE.runClickEvent();
            this.HMI_LOCAL_BLOCKED_KEY = true;
        }
    }
}
