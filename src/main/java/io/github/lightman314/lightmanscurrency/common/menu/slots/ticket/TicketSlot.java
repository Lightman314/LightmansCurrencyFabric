package io.github.lightman314.lightmanscurrency.common.menu.slots.ticket;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class TicketSlot extends Slot {

    public static final Identifier EMPTY_TICKET_SLOT = new Identifier(LightmansCurrency.MODID, "item/empty_ticket_slot");

    public TicketSlot(Inventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) { return InventoryUtil.ItemHasTag(stack, TicketItem.TICKET_TAG); }

    @Override
    public Pair<Identifier,Identifier> getBackgroundSprite() { return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_TICKET_SLOT); }


}