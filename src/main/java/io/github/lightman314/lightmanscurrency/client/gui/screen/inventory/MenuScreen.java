package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public abstract class MenuScreen<T extends ScreenHandler> extends HandledScreen<T> {

    public MenuScreen(T handler, PlayerInventory inventory) { this(handler, inventory, EasyText.empty()); }
    public MenuScreen(T handler, PlayerInventory inventory, Text title) { super(handler, inventory, title); }

    public final int getGuiLeft() { return this.x; }
    public final int getGuiTop() { return this.y; }

    public final Slot getFocusedSlot() { return this.focusedSlot; }

    public final int getImageWidth() { return this.backgroundWidth; }
    public final int getImageHeight() { return this.backgroundHeight; }

}
