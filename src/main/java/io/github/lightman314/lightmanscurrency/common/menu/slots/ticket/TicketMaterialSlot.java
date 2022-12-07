package io.github.lightman314.lightmanscurrency.common.menu.slots.ticket;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class TicketMaterialSlot extends Slot {

    public TicketMaterialSlot(Inventory inventory, int index, int x, int y) { super(inventory, index, x, y); }

    @Override
    public boolean canInsert(ItemStack stack) { return InventoryUtil.ItemHasTag(stack, TicketItem.TICKET_MATERIAL_TAG); }

}