package io.github.lightman314.lightmanscurrency.client.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;

public class RenderInventoryCallback {

    private RenderInventoryCallback() {}

    public static final Event<RenderInventoryBackground> RENDER_BACKGROUND = EventFactory.createArrayBacked(RenderInventoryBackground.class, (listeners) -> (screen, matrix, mouseX, mouseY, delta) -> {
        for(RenderInventoryBackground listener : listeners)
            listener.renderBG(screen, matrix, mouseX, mouseY, delta);
    });


    public interface RenderInventoryBackground
    {
        void renderBG(HandledScreen<?> screen, DrawContext gui, int mouseX, int mouseY, float deltaTick);
    }

}
