package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.foxloader.registry.missing.MissingItem;
import com.fox2code.foxloader.registry.missing.MissingItemBlock;
import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import com.indigo3d.util.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.creative.CreativeTabAllItems;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.world.RenderHelper;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.children.BlockGearConveyorBelt;
import net.minecraft.common.block.container.Slot;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.recipe.CraftingManager;
import net.minecraft.common.recipe.IRecipe;
import net.minecraft.common.recipe.RecipesArmorDyes;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Utils {
   private static final Object initializationLock = new Object();
   private static ArrayList<ItemStack> allItems;
   public static RenderItem itemRenderer = new RenderItem();
   public static Random rand = new Random();
   public static final Gui gui = new Gui();
   public static ArrayList<ItemStack> hiddenItems = new ArrayList<>();
   private static boolean itemLighting;
   static boolean localTextureBound;
   public static Boolean lighting;
   private static String tooltipText;
   private static ItemStack tooltipItem;
   private static GuiScreen tooltipItemGui;
   private static float tooltipX;
   private static float tooltipY;

   public static String getNiceItemName(ItemStack item, boolean withID) {
      String s = StringTranslate.getInstance().translateNamedKey(item.getItemName());
      if (s == null || s.isEmpty()) {
         s = item.getItemName();
         if (s == null) {
            s = "null";
         }
      }

      if (Config.showItemIDs && withID) {
         s = s + " " + item.getItemID();
         if (item.getHasSubtypes()) {
            s = s + ":" + item.getItemDamage();
         }
      }

      return s;
   }

   public static String getNiceItemName(ItemStack item) {
      return getNiceItemName(item, true);
   }

   public static ItemStack hoveredItem(GuiContainer gui, float posX, float posY) {
      try {
         Slot slotAtPosition = gui.getSlotAtPosition(posX, posY);
         if (slotAtPosition != null) {
            return slotAtPosition.getStack();
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return null;
   }

   public static ArrayList<ItemStack> itemList() {
      if (allItems == null) {
         synchronized (initializationLock) {
            makeItemList();
         }
      }
      return allItems;
   }

   private static void makeItemList() {
      if (allItems == null) {
         ArrayList<ItemStack> allItemsTmp = new ArrayList<>();
         Item[] mcItemsList = Items.ITEMS_LIST;
         ArrayList<ItemStack> hiddenItems = GuiOverlay.hiddenItems;
         if (hiddenItems == null) hiddenItems = Utils.hiddenItems;

         for(Item item : mcItemsList) {
            if (item != null && item.itemID != 0 &&
                    !(item instanceof MissingItem || item instanceof MissingItemBlock)) {
               HashSet<String> currentItemNames = new HashSet<>();
               int dmg = 0;

               item_loop:
               while(true) {
                  ItemStack itemstack = new ItemStack(item, 1, dmg);

                  for(ItemStack hiddenItem : hiddenItems) {
                     if (hiddenItem.matchIngredient(itemstack)) {
                        if (dmg == 0) {
                           break item_loop;
                        }
                        ++dmg;
                        continue item_loop;
                     }
                  }

                  try {
                     int l = item.getIconIndex(itemstack).getOriginX();
                     String s = StringTranslate.getInstance().translateNamedKey(itemstack.getItemName());
                     if (s.isEmpty()) {
                        s = itemstack.getItemName() + "@" + l;
                     }

                     if (dmg >= 4 && (s.contains(String.valueOf(dmg)) || s.contains(String.valueOf(dmg + 1)) ||
                             s.contains(String.valueOf(dmg - 1)) || s.contains("(INVALID METADATA)"))) {
                        break;
                     }

                     s = s + "@" + l;
                     if (currentItemNames.contains(s)) {
                        break;
                     }

                     allItemsTmp.add(itemstack);
                     currentItemNames.add(s);
                  } catch (IndexOutOfBoundsException | NullPointerException var10) {
                     break;
                  }

                  ++dmg;
               }
            }
         }

         item_loop:
         for(IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            if (!(recipe instanceof RecipesArmorDyes)) {
               ItemStack itemstack = new ItemStack(recipe.getRecipeOutput().getItem(), 1, recipe.getRecipeOutput().getItemDamage());

               for(ItemStack hiddenItem : hiddenItems) {
                  if (hiddenItem.matchIngredient(itemstack)) {
                     continue item_loop;
                  }
               }

               if (itemstack.getHasSubtypes()) {
                  addItemInOrder(allItemsTmp, itemstack);
               }
            }
         }

         item_loop:
         for(ItemStack itemstack : CreativeTabAllItems.getGlobalItemList()) {
            for(ItemStack hiddenItem : hiddenItems) {
               if (hiddenItem.matchIngredient(itemstack)) {
                  System.out.println(itemstack.getItemID() + ":" + itemstack.itemDamage);
                  continue item_loop;
               }
            }

            addItemInOrder(allItemsTmp, itemstack);
         }

         TabUtils.addHiddenModItems(allItemsTmp);
         allItems = allItemsTmp;
      }
   }

   public static void addItemInOrder(ArrayList<ItemStack> itemList, ItemStack itemstack) {
      for(ItemStack item : itemList) {
         if (item.isItemEqual(itemstack)) {
            return;
         }

         if (item.getItemID() > itemstack.getItemID() ||
                 item.getItemID() == itemstack.getItemID() &&
                         item.getItemDamage() > itemstack.getItemDamage()) {
            itemList.add(itemList.indexOf(item), itemstack);
            break;
         }
      }
   }

   public static int visibleTabSize() {
      int largestIndex = -1;

      for(Tab tab : HMIClient.allTabs) {
         if (tab.index > largestIndex) {
            largestIndex = tab.index;
         }
      }

      return largestIndex + 1;
   }

   public static void drawRect(int i, int j, int k, int l, int colour) {
      disableLighting();
      Gui.drawRect(i, j, k, l, colour);
   }

   public static void drawSlot(int x, int y, int colour) {
      drawRect(x, y, x + 18, y + 18, colour);
   }

   public static void drawTooltip(String s, float x, float y) {
      tooltipText = s;
      tooltipItemGui = null;
      tooltipItem = null;
      tooltipX = x;
      tooltipY = y;
   }

   public static void drawTooltip(GuiScreen gui, ItemStack s, float x, float y) {
      tooltipText = null;
      tooltipItemGui = gui;
      tooltipItem = s;
      tooltipX = x;
      tooltipY = y;
   }

   public static void drawStoredToolTip() {
      if (tooltipItem != null && tooltipItemGui != null) {
         disableLighting();
         tooltipItemGui.drawItemTooltip(tooltipItem, tooltipX, tooltipY);
         tooltipItem = null;
         tooltipItemGui = null;
         tooltipText = null;
         postRender();
         return;
      }
      if (tooltipItem != null) {
         tooltipText = Utils.getNiceItemName(tooltipItem);
         tooltipItem = null;
      }
      if (tooltipText != null) {
         // FontRenderer font, float x, float y, int width, int textColor, int color, String title
         disableLighting();
         gui.drawTooltip(Minecraft.theMinecraft.fontRenderer, tooltipX + 12, tooltipY - 12,
                 Minecraft.theMinecraft.fontRenderer.getStringWidth(tooltipText),
                 0xffffffff, 0xc0000000, tooltipText);
         tooltipText = null;
         postRender();
         /* GL11.glPushMatrix();
         disableLighting();
         int k1 = tooltipX + 12;
         int i2 = tooltipY - 12;
         int j2 = mc.fontRenderer.getStringWidth(tooltipText);
         drawRect(k1 - 3, i2 - 3, k1 + j2 + 3, i2 + 8 + 3, -1073741824);
         mc.fontRenderer.drawStringWithShadow(tooltipText, k1, i2, -1);
         tooltipText = null;
         postRender();
         GL11.glPopMatrix(); */
      }
   }

   public static void disableLighting() {
      if (lighting != Boolean.FALSE) {
         RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableLighting();
         RenderSystem.disableDepthTest();
         lighting = false;
      }
   }

   private static void enableLighting() {
      if (lighting != Boolean.TRUE) {
         RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableLighting();
         RenderSystem.enableDepthTest();
         lighting = true;
      }
   }

   public static void bindTexture(String texturePath) {
      RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.bindTexture2D(texturePath);
      localTextureBound = false;
   }

   public static void bindTexture() {
      if (!localTextureBound) {
         RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.bindTexture2D("/assets/hmf/textures/gui/icons.png");
         localTextureBound = true;
      }
   }

   public static void drawItemStack(float x, float y, ItemStack item, boolean drawOverlay) {
      drawItemStack((int) x,(int) y, item, drawOverlay);
   }

   public static void drawItemStack(int x, int y, ItemStack item, boolean drawOverlay) {
      localTextureBound = false;
      enableItemLighting();
      final Minecraft mc = Minecraft.theMinecraft;
      itemRenderer.renderItemIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);
      if (drawOverlay) {
         itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);
      }
   }

   private static void enableItemLighting() {
      enableLighting();
      if (!itemLighting) {
         RenderSystem.enableRescaleNormal();
         GL11.glPushMatrix();
         GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
         RenderHelper.enableStandardItemLighting();
         GL11.glPopMatrix();
         itemLighting = true;
      }
   }

   public static void preRender() {
      RenderSystem.color(1.0F, 1.0F, 1.0F, 1.0F);
      lighting = null;
      itemLighting = false;
      localTextureBound = false;
   }

   public static void postRender() {
      lighting = null;
      RenderHelper.disableStandardItemLighting();
      enableLighting();
   }

   static {
      short[] a = new short[]{
              26, 34, 59, 63, 64, 68, 71, 75, 10, 8, 28,
              23, 22, 36, 51, 69, 76, 119, 120, 121,
              147, 148, 162, 163, 165, 164, 185, 181,
              182, 198, 356, 357, 331, 381, 360, 1019,
              1002, 1001, 183, 184, 109, 112,
              1697, 1698, 1699, 1700, 1701, 1702, 1703,
              1704, 1705, 1706, 1707, 1708, 1709, 1710,
              1711, 1712, 1713, 1714, 1715, 1716, 1729,
              1738, 1748, 1756, 1757, 1758, 1759, 1760,
              1761, 1771,
      };
      short[][] b = new short[][]{
              {360, 0}, {100, 2}, {116, 3}, {130, 4}, {178, 2}, {130, 3},
              {135, 0}, {135, 1}, {135, 2}, {98, 2}, {44, 0}, {44, 1}, {44, 2}
      };

      for(short n : a) {
         hiddenItems.add(n < 256 ?
                 new ItemStack(Blocks.BLOCKS_LIST[n], 1, -1) :
                 new ItemStack(Items.ITEMS_LIST[n], 1, -1));
      }

      for(short[] n2 : b) {
         hiddenItems.add(n2[0] < 256 ?
                 new ItemStack(Blocks.BLOCKS_LIST[n2[0]], 1, n2[1]) :
                 new ItemStack(Items.ITEMS_LIST[n2[0]], 1, n2[1]));
      }
   }
}
