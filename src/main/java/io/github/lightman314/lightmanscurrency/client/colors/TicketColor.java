package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class TicketColor implements ItemColorProvider {

    @Override
    public int getColor(ItemStack itemStack, int color) {

        if(color > 0)
            return 0xFFFFFF;

        //Get the Ticket's ID
        UUID id = TicketItem.GetTicketID(itemStack);
        if(id == null)
            return 0xFFFFFF;
        //Convert the ID into a color
        int hash = id.hashCode();
        //Ensure that the hash code is a positive number
        if(hash < 0)
            hash *= -1;
        //Limit to a valid color id
        int output = hash % 0xFFFFFF;
        return output;
    }

}