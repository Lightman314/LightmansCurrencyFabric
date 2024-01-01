package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MenuScreen;
import io.github.lightman314.lightmanscurrency.mixin.client.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;

public class ScreenUtil {

    private static Screen pendingScreen = null;

    public static void safelyOpenScreen(Screen screen) { pendingScreen = screen; }

    public static void onRenderTick(WorldRenderContext ignored) {
        if(pendingScreen != null)
        {
            MinecraftClient.getInstance().setScreen(pendingScreen);
            pendingScreen = null;
        }
    }

    public static ScreenPosition getScreenCorner(HandledScreen<?> screen)
    {
        if(screen instanceof MenuScreen<?> s)
            return ScreenPosition.of(s.getGuiLeft(), s.getGuiTop());
        if(screen instanceof HandledScreenAccessor s)
            return ScreenPosition.of(s.getX(), s.getY());
        if(screen instanceof InventoryScreen)
            return calculateScreenCorner(screen.width, screen.height, 176, 166);
        else if(screen instanceof CreativeInventoryScreen)
            return calculateScreenCorner(screen.width, screen.height, 195, 136);
        else
            return ScreenPosition.of(0,0);
    }

    public static ScreenArea getScreenArea(HandledScreen<?> screen)
    {
        if(screen instanceof MenuScreen<?> s)
            return ScreenArea.of(s.getGuiLeft(), s.getGuiTop(), s.getImageWidth(), s.getImageHeight());
        if(screen instanceof HandledScreenAccessor s)
            return ScreenArea.of(s.getX(), s.getY(), s.getImageWidth(), s.getImageHeight());
        else
            return ScreenArea.of(0,0,0,0);
    }

    private static ScreenPosition calculateScreenCorner(int screenWidth, int screenHeight, int backgroundWidth, int backgroundHeight) {
        int x = (screenWidth - backgroundWidth) / 2;
        int y = (screenHeight - backgroundHeight) / 2;
        return ScreenPosition.of(x,y);
    }

    public static Slot getFocusedSlot(HandledScreen<?> screen) {
        if(screen instanceof MenuScreen<?> s)
            return s.getFocusedSlot();
        if(screen instanceof HandledScreenAccessor s)
            return s.getFocusedSlot();
        return null;
    }

}
