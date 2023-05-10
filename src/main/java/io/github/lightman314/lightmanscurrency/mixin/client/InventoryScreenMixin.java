package io.github.lightman314.lightmanscurrency.mixin.client;

import io.github.lightman314.lightmanscurrency.client.callbacks.RenderInventoryCallback;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Unique
    protected InventoryScreen self() { return (InventoryScreen)(Object) this; }


    @Inject(at = @At("TAIL"), method = "drawBackground")
    public void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo info) {
        RenderInventoryCallback.RENDER_BACKGROUND.invoker().renderBG(self(), matrices, mouseX, mouseY, delta);
    }

}