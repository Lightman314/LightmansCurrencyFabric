package io.github.lightman314.lightmanscurrency.mixin.client;

import io.github.lightman314.lightmanscurrency.client.callbacks.RenderInventoryCallback;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin {

    @Unique
    private CreativeInventoryScreen self() { return (CreativeInventoryScreen)(Object) this; }


    @Inject(at = @At("TAIL"), method = "drawBackground")
    public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo info) {
        RenderInventoryCallback.RENDER_BACKGROUND.invoker().renderBG(self(), context, mouseX, mouseY, delta);
    }

}
