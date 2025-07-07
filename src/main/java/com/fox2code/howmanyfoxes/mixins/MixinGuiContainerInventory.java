package com.fox2code.howmanyfoxes.mixins;

import com.fox2code.howmanyfoxes.HowManyFoxes;
import com.fox2code.howmanyfoxes.hmi.overlay.IInventoryOverlayUpdate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiContainerInventory;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainerInventory.class)
public class MixinGuiContainerInventory extends GuiScreen implements IInventoryOverlayUpdate {
    @Inject(method = "initGui", at = @At("TAIL"))
    private void hmf$injectToggleButtons(CallbackInfo ci) {
        this.pushTrigger(HowManyFoxes.CONFIG.overlayEnabled);
    }

    @Override
    public void pushTrigger(boolean overlayPresent) {
        for(GuiElement element : this.controlList) {
            if(element instanceof GuiButton button) {
                if(button.id == 2 || button.id == 3) {
                    button.enabled = !overlayPresent && this.mc.playerController.isInCreativeMode();
                    button.visible = !overlayPresent && this.mc.playerController.isInCreativeMode();
                }
            }
        }
    }
}
