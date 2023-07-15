package io.github.lightman314.lightmanscurrency.common.menu;

import java.util.List;

import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.menu.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.common.menu.factory.SimpleMenuFactory;
import io.github.lightman314.lightmanscurrency.common.menu.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.network.client.messages.ejectiondata.SMessageChangeSelectedData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class TraderRecoveryMenu extends Menu {

    public static final NamedScreenHandlerFactory PROVIDER = new SimpleMenuFactory((windowID, inventory, player) -> new TraderRecoveryMenu(windowID, inventory));

    public TraderRecoveryMenu(int menuID, PlayerInventory inventory) { this(ModMenus.TRADER_RECOVERY, menuID, inventory); }

    private final PlayerEntity player;

    public boolean isClient() { return this.player.getWorld().isClient; }

    public List<EjectionData> getValidEjectionData() {
        return EjectionSaveData.GetValidEjectionData(this.isClient(), this.player);
    }

    private int selectedIndex = 0;
    public int getSelectedIndex() { return this.selectedIndex; }
    public EjectionData getSelectedData() {
        List<EjectionData> data = this.getValidEjectionData();
        if(data.size() > 0 && this.selectedIndex >= 0 && this.selectedIndex < data.size())
            return data.get(this.selectedIndex);
        return null;
    }

    private final SuppliedContainer ejectionContainer;
    private final Inventory dummyContainer = new SimpleInventory(54);

    private Inventory getSelectedContainer() {
        //Get valid data
        List<EjectionData> data = this.getValidEjectionData();
        //Refresh selection, just in case it's no longer valid.
        this.changeSelection(this.selectedIndex, data.size());
        if(data.size() > 0 && this.selectedIndex >= 0 && this.selectedIndex < data.size())
            return data.get(this.selectedIndex);
        return this.dummyContainer;
    }

    protected TraderRecoveryMenu(ScreenHandlerType<?> type, int menuID, PlayerInventory inventory) {
        super(type, menuID);
        this.player = inventory.player;

        this.ejectionContainer = new SuppliedContainer(this::getSelectedContainer);

        //Menu slots
        for(int y = 0; y < 6; ++y)
        {
            for(int x = 0; x < 9; ++x)
            {
                this.addSlot(new OutputSlot(this.ejectionContainer, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        //Player's Inventory
        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
            }
        }

        //Player's hotbar
        for(int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 198));
        }

    }

    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (slotIndex < 54) {
                if (!this.insertItem(itemstack1, 54, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemstack1, 0, 54, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        //Clear the dummy container for safety.
        this.dropInventory(player, this.dummyContainer);
    }

    public void changeSelection(int newSelection) {
        this.changeSelection(newSelection, this.getValidEjectionData().size());
    }

    private void changeSelection(int newSelection, int dataSize) {
        int oldSelection = this.selectedIndex;
        this.selectedIndex = MathUtil.clamp(newSelection, 0, dataSize - 1);
        if(this.selectedIndex != oldSelection && !this.isClient())
        {
            //Inform the client of the change
            new SMessageChangeSelectedData(this.selectedIndex).sendTo(this.player);
        }
    }

}