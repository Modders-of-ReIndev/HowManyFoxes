package com.fox2code.howmanyfoxes.hmi;

import com.fox2code.howmanyfoxes.HMIClient;
import com.fox2code.howmanyfoxes.hmi.tabs.Tab;
import net.minecraft.client.gui.inventory.InventoryClientOnly;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;

import java.util.Stack;

public class InventoryRecipeViewer implements IInventory, InventoryClientOnly {
   public final Stack<ItemStack> filter = new Stack<>();
   public final Stack<Tab> prevTabs = new Stack<>();
   public final Stack<Integer> prevPages = new Stack<>();
   public final Stack<Boolean> prevGetUses = new Stack<>();

   public Tab currentTab = HMIClient.getTabs().getFirst();
   public Boolean newList = true;
   public int index;
   public ItemStack[][] items;
   public int timeSinceLastUpdate;

   public void initTab(Tab tab) {
      this.currentTab = tab;
      this.newList = true;
      this.index = this.setIndex(tab.lastIndex);
   }

   public void decIndex() {
      this.index = this.setIndex(this.index - this.currentTab.recipesPerPage);
   }

   public void incIndex() {
      this.index = this.setIndex(this.index + this.currentTab.recipesPerPage);
   }

   public int getPage() {
      return this.index / this.currentTab.recipesPerPage;
   }

   public int setIndex(int i) {
      if (this.index == i && !this.newList) {
         return i;
      } else {
         this.timeSinceLastUpdate = 0;
         if ((double)this.currentTab.size / (double)this.currentTab.recipesPerPage <= 1.0) {
            i = 0;
         }

         this.newList = false;
         if (i < 0) {
            if (this.currentTab.size % this.currentTab.recipesPerPage != 0) {
               i = this.currentTab.size - this.currentTab.size % this.currentTab.recipesPerPage;
            } else {
               i = this.currentTab.size - this.currentTab.recipesPerPage;
            }

            if (i == -1) {
               i = 0;
            }

            if (i == this.currentTab.size) {
               i = this.currentTab.size - 1;
            }
         } else if (i >= this.currentTab.size) {
            i = 0;
         }

         if (!this.filter.isEmpty()) {
            this.items = this.currentTab.getItems(i, this.filter.peek());
         }

         return this.currentTab.lastIndex = i;
      }
   }

   public void tick() {
      if (this.timeSinceLastUpdate++ > 40) {
         this.newList = true;
         this.setIndex(this.index);
      }
   }

   public int getSizeInventory() {
      return this.currentTab.recipesPerPage * this.currentTab.slots.length;
   }

   public ItemStack decrStackSize(int i, int j) {
      return null;
   }

   public void setInventorySlotContents(int i, ItemStack itemstack) {
   }

   @Override
   public boolean canInteractWith(EntityPlayer entityplayer) {
      return true;
   }

   public String getInvName() {
      return String.format("%d / %d", this.getPage() + 1, (this.currentTab.size - 1) / this.currentTab.recipesPerPage + 1);
   }

   public ItemStack getStackInSlot(int i) {
      return !this.filter.isEmpty() && this.items[i / this.currentTab.slots.length] != null
         ? this.items[i / this.currentTab.slots.length][i % this.currentTab.slots.length]
         : null;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void onInventoryChanged() {
   }
}
