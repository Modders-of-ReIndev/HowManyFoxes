package com.fox2code.howmanyfoxes.hmi;

import net.minecraft.client.gui.inventory.ContainerWrapped;
import net.minecraft.common.block.container.Container;
import net.minecraft.common.block.container.Slot;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;

public class ContainerRecipeViewer extends Container implements ContainerWrapped {
   private int count;
   private final InventoryRecipeViewer inv;
   private final Container parentContainer;

   public ContainerRecipeViewer(InventoryRecipeViewer iinventory, Container parentContainer) {
      this.inv = iinventory;
      this.parentContainer = parentContainer;
      this.resetSlots();
   }

   public void resetSlots() {
      super.slots.clear();
      this.count = 0;
   }

   public void addSlot(int i, int j) {
      this.addSlot(new Slot(this.inv, this.count++, i, j));
   }

   @Override
   public ItemStack quickMove(int i) {
      return null;
   }

   @Override
   public boolean isUsableByPlayer(EntityPlayer entityplayer) {
      return this.inv.canInteractWith(entityplayer);
   }

   @Override
   public Container getParentContainer() {
      return this.parentContainer;
   }
}
