package io.github.lightman314.lightmanscurrency.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("focusedSlot")
    Slot getFocusedSlot();

    @Accessor("backgroundWidth")
    int getImageWidth();

    @Accessor("backgroundHeight")
    int getImageHeight();

    @Accessor("x")
    int getX();

    @Accessor("y")
    int getY();

}
