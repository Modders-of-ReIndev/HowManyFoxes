package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.HowManyFoxes;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import net.minecraft.client.util.KeyBinding;

import java.util.ArrayList;

public class Config {
   public static final int recipeViewerGuiWidthDefault = 251;
   public static final int recipeViewerGuiHeightDefault = 134;

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

      HowManyFoxes.forceSaveConfig();
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

      HowManyFoxes.forceSaveConfig();
   }
}
