package io.github.lightman314.lightmanscurrency.common.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public abstract class Menu extends ScreenHandler {

    protected Menu(@Nullable ScreenHandlerType<?> type, int windowID) { super(type, windowID); }

    public void closeMenu(PlayerEntity player) {
        if(player instanceof ServerPlayerEntity sp)
            sp.closeHandledScreen();
    }

}
