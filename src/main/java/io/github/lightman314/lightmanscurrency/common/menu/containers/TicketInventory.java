package io.github.lightman314.lightmanscurrency.common.menu.containers;

import net.minecraft.inventory.SimpleInventory;

public class TicketInventory extends SimpleInventory {

    public TicketInventory(int numSlots) { super(numSlots); }

    @Override
    public int getMaxCountPerStack() { return 1; }

}