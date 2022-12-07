package io.github.lightman314.lightmanscurrency.common.menu.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.function.Function;

public class SimpleSlot extends Slot {

    public boolean active = true;
    public boolean locked = false;

    public SimpleSlot(Inventory container, int index, int x, int y) { super(container, index, x, y); }

    @Override
    public boolean isEnabled() { return this.active; }

    @Override
    public boolean canInsert(ItemStack stack) {
        if(this.locked)
            return false;
        return super.canInsert(stack);
    }

    @Override
    public ItemStack takeStack(int amount) {
        if(this.locked)
            return ItemStack.EMPTY;
        return super.takeStack(amount);
    }

    @Override
    public boolean canTakeItems(PlayerEntity player) {
        if(this.locked)
            return false;
        return super.canTakeItems(player);
    }

    public static void SetActive(ScreenHandler menu) {
        SetActive(menu, (slot) -> true);
    }

    public static void SetActive(ScreenHandler menu, Function<SimpleSlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof SimpleSlot) {
                SimpleSlot simpleSlot = (SimpleSlot)slot;
                if(filter.apply(simpleSlot))
                    simpleSlot.active = true;
            }
        });
    }

    public static void SetInactive(ScreenHandler menu) {
        SetInactive(menu, (slot) -> true);
    }

    public static void SetInactive(ScreenHandler menu, Function<SimpleSlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof SimpleSlot) {
                SimpleSlot simpleSlot = (SimpleSlot)slot;
                if(filter.apply(simpleSlot))
                    simpleSlot.active = false;
            }
        });
    }

    public static void SetActive(List<? extends SimpleSlot> slots) { SetActive(slots, true); }
    public static void SetInactive(List<? extends SimpleSlot> slots) { SetActive(slots, false); }

    public static void SetActive(List<? extends SimpleSlot> slots, boolean active) {
        for(SimpleSlot slot: slots) {
            slot.active = active;
        }
    }

}