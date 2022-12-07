package io.github.lightman314.lightmanscurrency.common.menu.slots.ticket;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class TicketMasterSlot extends Slot {

    public TicketMasterSlot(Inventory inventory, int index, int x, int y) { super(inventory, index, x, y); }

    @Override
    public boolean canInsert(ItemStack stack) { return stack.getItem() == ModItems.TICKET_MASTER; }

    @Override
    public Pair<Identifier,Identifier> getBackgroundSprite() { return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, TicketSlot.EMPTY_TICKET_SLOT); }

}