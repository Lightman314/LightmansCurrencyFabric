package io.github.lightman314.lightmanscurrency.common.menu;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TicketMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menu.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.ticket.TicketMasterSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.ticket.TicketMaterialSlot;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class TicketMachineMenu extends Menu {

    private final Inventory output = new SimpleInventory(1);

    private final TicketMachineBlockEntity ticketMachine;

    public TicketMachineMenu(int windowId, PlayerInventory inventory, BlockPos blockPos)
    {
        super(ModMenus.TICKET_MACHINE, windowId);
        BlockEntity blockEntity = inventory.player.getWorld().getBlockEntity(blockPos);
        if(blockEntity instanceof TicketMachineBlockEntity)
            this.ticketMachine = (TicketMachineBlockEntity)blockEntity;
        else
        {
            this.ticketMachine = null;
            return;
        }

        //Slots
        this.addSlot(new TicketMasterSlot(this.ticketMachine.getStorage(), 0, 20, 21));
        this.addSlot(new TicketMaterialSlot(this.ticketMachine.getStorage(), 1, 56, 21));

        this.addSlot(new OutputSlot(this.output, 0, 116, 21));

        //Player inventory
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 56 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 114));
        }
    }

    @Override
    public boolean canUse(PlayerEntity playerIn) { return this.ticketMachine != null && !this.ticketMachine.isRemoved(); }

    @Override
    public void onClosed(PlayerEntity playerIn)
    {
        super.onClosed(playerIn);
        this.dropInventory(playerIn,  this.output);
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int index)
    {

        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasStack())
        {
            ItemStack slotStack = slot.getStack();
            clickedStack = slotStack.copy();
            int totalSize = this.ticketMachine.getStorage().size() + this.output.size();
            if(index < totalSize)
            {
                if(!this.insertItem(slotStack, totalSize, this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.insertItem(slotStack, 0, this.ticketMachine.getStorage().size(), false))
            {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty())
            {
                slot.setStack(ItemStack.EMPTY);
            }
            else
            {
                slot.markDirty();
            }
        }

        return clickedStack;

    }


    public boolean validInputs() { return !this.ticketMachine.getStorage().getStack(1).isEmpty(); }

    public boolean roomForOutput()
    {
        ItemStack outputStack = this.output.getStack(0);
        if(outputStack.isEmpty())
            return true;
        if(hasMasterTicket() && outputStack.getItem() == ModItems.TICKET)
        {
            //Confirm that the output item has the same ticket id as the master ticket
            UUID ticketID = getTicketID();
            UUID outputTicketID = TicketItem.GetTicketID(outputStack);
            return outputTicketID != null && ticketID.equals(outputTicketID);
        }
        //Not empty, and no master ticket in the slot means that no new master ticket can be placed in the output slot
        return false;
    }

    public boolean hasMasterTicket()
    {
        ItemStack masterTicket = this.ticketMachine.getStorage().getStack(0);
        return !masterTicket.isEmpty() && TicketItem.isMasterTicket(masterTicket);
    }

    public void craftTickets(boolean fullStack)
    {
        if(!validInputs())
        {
            LightmansCurrency.LogWarning("Inputs for the Ticket Machine are not valid. Cannot craft tickets.");
            return;
        }
        else if(!roomForOutput())
        {
            LightmansCurrency.LogWarning("No room for Ticket Machine outputs. Cannot craft tickets.");
            return;
        }
        if(hasMasterTicket())
        {
            int count = 1;
            if(fullStack)
                count = this.ticketMachine.getStorage().getStack(1).getCount();

            //Create a normal ticket
            ItemStack outputStack = this.output.getStack(0);
            if(outputStack.isEmpty())
            {
                //Create a new ticket stack
                ItemStack newTicket = TicketItem.CreateTicket(this.getTicketID(), count);
                this.output.setStack(0, newTicket);
            }
            else
            {
                //Limit the added count by amount of space left in the output
                count = Math.min(count, outputStack.getMaxCount() - outputStack.getCount());
                //Increase the stack size
                outputStack.setCount(outputStack.getCount() + count);
            }
            //Remove the crafting materials
            this.ticketMachine.getStorage().removeStack(1, count);
        }
        else
        {
            //Create a master ticket
            ItemStack newTicket = TicketItem.CreateMasterTicket(UUID.randomUUID());

            this.output.setStack(0, newTicket);

            //Remove the crafting materials
            this.ticketMachine.getStorage().removeStack(1, 1);
        }


    }

    public UUID getTicketID()
    {
        ItemStack masterTicket = this.ticketMachine.getStorage().getStack(0);
        if(TicketItem.isMasterTicket(masterTicket))
            return TicketItem.GetTicketID(masterTicket);
        return null;
    }

}