package io.github.lightman314.lightmanscurrency.common.menu.traderinterface;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;

public abstract class TraderInterfaceTab {

    public static final int TAB_INFO = 0;
    public static final int TAB_STORAGE = 1;
    public static final int TAB_TRADER_SELECT = 2;
    public static final int TAB_TRADE_SELECT = 3;
    public static final int TAB_OWNERSHIP = 100;

    public final TraderInterfaceMenu menu;

    protected TraderInterfaceTab(TraderInterfaceMenu menu) { this.menu = menu; }

    @Environment(EnvType.CLIENT)
    public abstract TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen);

    /**
     * Whether the player has permission to access this tab.
     */
    public abstract boolean canOpen(PlayerEntity player);

    /**
     * Called when the tab is opened. Use this to unhide slots.
     */
    public abstract void onTabOpen();

    /**
     * Called when the tab is closed. Use this to hide slots.
     */
    public abstract void onTabClose();

    public void onMenuClose() { }

    /**
     * Called when the menu is loaded to add any tab-specific slots.
     */
    public abstract void addStorageMenuSlots(Function<Slot,Slot> addSlot);

    public boolean quickMoveStack(ItemStack stack) { return false; }

    /**
     * Sends a message to the server to notify them about an interaction made client-side.
     */
    public abstract void receiveMessage(NbtCompound message);

}