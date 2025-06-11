package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.foxloader.client.KeyBindingAPI;
import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import net.minecraft.client.util.KeyBinding;
import net.minecraft.common.CoreConstants;
import net.minecraft.common.item.ItemStack;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class Config {
   private static final String LEGACY_DEFAULT_CONFIG = "hiddenItems=26,34,59,63,64,68,71,75,10,8,28,21,23,22,36,51,69,76,119,120,121,147,148,162,163,165,164,185,181,182,356,357,331,381,360,1019,1002,1001,183,184,109,112,360,5:3,100:2,116:3,130:4,249:0,178:2,135:0-2,98:2,44:0-2";
   private static final File configFile = new File(CoreConstants.CORE.getMinecraftDir(), "/config/HowManyItems.cfg");
   public static boolean overlayEnabled = true;
   public static boolean cheatsEnabled = false;
   public static boolean showItemIDs = false;
   public static boolean centredSearchBar = false;
   public static boolean fastSearch = false;
   public static boolean scrollInverted = false;
   public static String mpGiveCommand = "/give {0} {1} {2}";
   public static String mpHealCommand = "";
   public static String mpTimeDayCommand = "/time set 0";
   public static String mpTimeNightCommand = "/time set 13000";
   public static String mpRainONCommand = "";
   public static String mpRainOFFCommand = "";
   public static boolean recipeViewerDraggableGui = false;
   public static final int recipeViewerGuiWidthDefault = 251;
   public static final int recipeViewerGuiHeightDefault = 134;
   public static int recipeViewerGuiWidth = recipeViewerGuiWidthDefault;
   public static int recipeViewerGuiHeight = recipeViewerGuiHeightDefault;
   public static final KeyBinding pushRecipe = new KeyBinding("key.hmf.get-recipes", 19);
   public static final KeyBinding pushUses = new KeyBinding("key.hmf.get-uses", 22);
   public static final KeyBinding prevRecipe = new KeyBinding("key.hmf.previous-Recipe", 14);
   public static final KeyBinding allRecipes = new KeyBinding("key.hmf.show-all-recipes", 0);
   public static final KeyBinding clearSearchBox = new KeyBinding("key.hmf.clear-search", 211);
   public static final KeyBinding focusSearchBox = new KeyBinding("key.hmf.focus-search", 28);
   public static final KeyBinding toggleOverlay = new KeyBinding("key.hmf.toggle-hmf", 24);
   public static final KeyBinding[] keyBinds = new KeyBinding[]{
           Config.pushRecipe, Config.pushUses, Config.prevRecipe, Config.allRecipes,
           Config.clearSearchBox, Config.focusSearchBox, Config.toggleOverlay
   };

   public static void init() {
      for (KeyBinding keyBinding : keyBinds) {
         KeyBindingAPI.registerKeyBinding(keyBinding);
      }

      if (!configFile.exists()) {
         writeConfig();
      }

      readConfig();
   }

   public static void writeConfig() {
      try {
         configFile.createNewFile();
         BufferedWriter configWriter = new BufferedWriter(new FileWriter(configFile));
         configWriter.write("// Config file for HowManyItems");

         for(Field field : Config.class.getFields()) {
            if ((field.getType() == Boolean.TYPE || field.getType() == String.class ||
                    field.getType() == Integer.TYPE) && !Modifier.isFinal(field.getModifiers())) {
               try {
                  configWriter.write(System.lineSeparator() + field.getName() + "=" + field.get(null).toString());
               } catch (Exception var6) {
                  var6.printStackTrace();
               }
            }
         }

         KeyBinding[] array = new KeyBinding[]{pushRecipe, pushUses, prevRecipe, allRecipes, clearSearchBox, focusSearchBox};

         for(KeyBinding keybind : array) {
            configWriter.write(System.lineSeparator() + "key_" + keybind.keyDescription + ":" + keybind.keyCode);
         }

         configWriter.write(System.lineSeparator() + "hiddenItems=");

         ArrayList<ItemStack> hiddenItems = GuiOverlay.hiddenItems;
         if (hiddenItems == null || !GuiOverlay.hiddenItemsModified) {
            hiddenItems = new ArrayList<>(); // Empty list is interpreted as use-defaults.
         }
         for(int i = 0; i < hiddenItems.size(); ++i) {
            if (i > 0) {
               configWriter.write(",");
            }

            ItemStack item = hiddenItems.get(i);
            configWriter.write(String.valueOf(item.getItemID()));
            if (item.getHasSubtypes()) {
               configWriter.write(":" + item.getItemDamage());
               int meta = item.getItemDamage();

               for(int q = i + 1; q < hiddenItems.size() &&
                       hiddenItems.get(q).getItemID() == item.getItemID(); i = q++) {
                  if (++meta != hiddenItems.get(q).getItemDamage()) {
                     --meta;
                     break;
                  }
               }

               if (meta > item.getItemDamage()) {
                  configWriter.write("-" + meta);
               }
            }
         }

         if (HMIClient.allTabs != null) {
            configWriter.write(System.lineSeparator() + "// Below are the index values for each tab");
            configWriter.write(System.lineSeparator() + "// Use -1 to disable the tab");

            for(int i = 0; i < Utils.visibleTabSize(); ++i) {
               for(Tab tab : HMIClient.allTabs) {
                  if (tab.index == i) {
                     configWriter.write(System.lineSeparator() + tab.TAB_CREATOR.getClass().getSimpleName() + ":" + tab.name() + ":" + tab.index);
                  }
               }
            }

            for(Tab tab2 : HMIClient.allTabs) {
               if (tab2.index == -1) {
                  configWriter.write(System.lineSeparator() + tab2.TAB_CREATOR.getClass().getSimpleName() + ":" + tab2.name() + ":" + tab2.index);
               }
            }
         }

         configWriter.close();
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   public static void readConfig() {
      try {
         BufferedReader configReader = new BufferedReader(new FileReader(configFile));

         String s;
         while ((s = configReader.readLine()) != null) {
            if (s.charAt(0) != '/' || s.charAt(1) != '/') {
               if (s.startsWith("key_")) {
                  continue;
               }
               if (s.equals("hiddenItems=") || s.equals(LEGACY_DEFAULT_CONFIG)) {
                  if (GuiOverlay.hiddenItems == null) {
                     GuiOverlay.hiddenItems = new ArrayList<>(Utils.hiddenItems);
                  }
               } else if (s.startsWith("hiddenItems=")) {
                  if (GuiOverlay.hiddenItems == null) {
                     GuiOverlay.hiddenItems = new ArrayList<>();
                     String[] as = s.replaceFirst("hiddenItems=", "").split(",");

                     for (String a : as) {
                        if (a.contains(":")) {
                           String[] as2 = a.split(":");
                           if (as2[1].contains("-")) {
                              String[] meta = as2[1].split("-");
                              int minMeta = Integer.parseInt(meta[0]);
                              int maxMeta = Integer.parseInt(meta[1]);

                              for (int q = minMeta; q <= maxMeta; ++q) {
                                 GuiOverlay.hiddenItems.add(new ItemStack(Integer.parseInt(as2[0]), 1, q));
                              }
                           } else {
                              GuiOverlay.hiddenItems.add(new ItemStack(Integer.parseInt(as2[0]), 1, Integer.parseInt(as2[1])));
                           }
                        } else if (!a.isEmpty()) {
                           GuiOverlay.hiddenItems.add(new ItemStack(Integer.parseInt(a), 1, 0));
                        }
                     }
                  }
               } else {
                  if (s.contains("=")) {
                     String[] as = s.split("=");

                     for (Field field : Config.class.getDeclaredFields()) {
                        if (field.getName().equalsIgnoreCase(as[0]) &&
                                !Modifier.isFinal(field.getModifiers())) {
                           if (field.getType() == Integer.TYPE) {
                              field.set(null, Integer.parseInt(as[1]));
                           } else if (field.getType() == Boolean.TYPE) {
                              field.set(null, Boolean.parseBoolean(as[1]));
                           } else if (field.getType() == String.class) {
                              if (as.length == 1) {
                                 field.set(null, "");
                              } else {
                                 field.set(null, String.valueOf(as[1]));
                              }
                           }
                        }
                     }
                  } else if (s.contains(":") && HMIClient.allTabs != null) {
                     String[] as = s.split(":");

                     for (Tab tab : HMIClient.allTabs) {
                        if (tab.TAB_CREATOR.getClass().getSimpleName().equalsIgnoreCase(as[0]) && tab.name().equalsIgnoreCase(as[1])) {
                           tab.index = Integer.parseInt(as[2]);
                        }
                     }
                  }
               }
            }
         }

         configReader.close();
      } catch (Exception var12) {
         var12.printStackTrace();
      }
   }

   public static ArrayList<Tab> orderTabs() {
      ArrayList<Tab> orderedTabs = new ArrayList<>();

      for(int i = 0; i < HMIClient.allTabs.size(); ++i) {
         Tab tab = HMIClient.allTabs.get(i);

         while(orderedTabs.size() < tab.index + 1) {
            orderedTabs.add(null);
         }

         if (tab.index >= 0) {
            orderedTabs.set(tab.index, tab);
         }
      }

      while(orderedTabs.remove(null)) {
      }

      int i = 0;

      while(i < orderedTabs.size()) {
         orderedTabs.get(i).index = i++;
      }

      for(int ix = 0; ix < HMIClient.allTabs.size(); ++ix) {
         Tab tab = HMIClient.allTabs.get(ix);
         if (tab.index == -2) {
            tab.index = orderedTabs.size();
            orderedTabs.add(tab);
         } else if (tab.index < 0) {
            tab.index = -1;
         }
      }

      writeConfig();
      return orderedTabs;
   }

   public static void tabOrderChanged(boolean[] tabEnabled, Tab[] tabOrder) {
      for(int i = 0; i < HMIClient.allTabs.size(); ++i) {
         Tab tab = HMIClient.allTabs.get(i);

         for(int j = 0; j < tabOrder.length; ++j) {
            if (tab.equals(tabOrder[j])) {
               tab.index = j;
               if (!tabEnabled[j]) {
                  tab.index = -1;
               }
            }
         }
      }

      writeConfig();
   }
}
