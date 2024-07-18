package io.github.lightman314.lightmanscurrency.mixin.client;

import io.github.lightman314.lightmanscurrency.client.callbacks.RenderInventoryCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Unique
    private InventoryScreen self() { return (InventoryScreen)(Object) this; }

    @Inject(at = @At("TAIL"), method = "drawBackground")
    public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo info) {
        RenderInventoryCallback.RENDER_BACKGROUND.invoker().renderBG(self(), context, mouseX, mouseY, delta);
    }

}
