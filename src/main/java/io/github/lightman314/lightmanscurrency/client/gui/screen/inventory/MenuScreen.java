package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.widget.util.IScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class MenuScreen<T extends ScreenHandler> extends HandledScreen<T> implements IScreen {

    public MenuScreen(T handler, PlayerInventory inventory) { this(handler, inventory, Text.empty()); }
    public MenuScreen(T handler, PlayerInventory inventory, Text title) { super(handler, inventory, title); }

    private final List<Runnable> tickListeners = new ArrayList<>();

    public final int getGuiLeft() { return this.x; }
    public final int getGuiTop() { return this.y; }

    public final Slot getFocusedSlot() { return this.focusedSlot; }

    public final int getImageWidth() { return this.backgroundWidth; }
    public final int getImageHeight() { return this.backgroundHeight; }

    @Override
    protected void init() {
        super.init();
        this.tickListeners.clear();
    }

    @Override
    public void addTickListener(Runnable r) {
        if(!this.tickListeners.contains(r))
            this.tickListeners.add(r);
    }

    @Override
    protected void handledScreenTick() {
        for(Runnable r : new ArrayList<>(this.tickListeners))
            r.run();
    }

}
