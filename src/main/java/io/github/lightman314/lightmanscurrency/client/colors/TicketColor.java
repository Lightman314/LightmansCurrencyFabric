package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class TicketColor implements ItemColorProvider {

    @Override
    public int getColor(ItemStack itemStack, int color) {

        if(color == 0)
            return GetTicketColor(itemStack);

        if(color == 1)
            return 0xFFFFFF - GetTicketColor(itemStack);

        return 0xFFFFFF;


    }

    private static int GetTicketColor(ItemStack item)
    {
        //Get the Ticket's ID
        UUID id = TicketItem.GetTicketID(item);
        if(id == null)
            return 0xFFFFFF;
        //Force color to yellow if creative ticket
        if(id.getLeastSignificantBits() == 0 && id.getMostSignificantBits() == 0)
            return 0xFFFF00;
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