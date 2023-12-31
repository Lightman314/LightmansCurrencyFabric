package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;
import java.util.UUID;

import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TicketItem extends Item{

	public static final Identifier TICKET_TAG = new Identifier(LightmansCurrency.MODID,"ticket");
	public static final Identifier TICKET_MATERIAL_TAG = new Identifier(LightmansCurrency.MODID,"ticket_material");
	public static final TagKey<Item> TICKET_MATERIAL_KEY = TagKey.of(Registries.ITEM.getKey(), TICKET_MATERIAL_TAG);
	
	public TicketItem(Settings properties) { super(properties); }
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltip, TooltipContext flagIn)
	{
		if(Screen.hasShiftDown())
		{
			UUID ticketID = GetTicketID(stack);
			if(ticketID != null)
				tooltip.add(Text.translatable("tooltip.lightmanscurrency.ticket.id", ticketID));
		}
	}
	
	public static boolean isMasterTicket(ItemStack ticket)
	{
		if(ticket.isEmpty() || !ticket.hasNbt())
			return false;
		return ticket.getItem() == ModItems.TICKET_MASTER;
	}
	
	public static UUID GetTicketID(ItemStack ticket)
	{
		//Get the ticket item
		if(ticket.isEmpty() || !(ticket.getItem() instanceof TicketItem) || !ticket.hasNbt())
			return null;
		NbtCompound ticketTag = ticket.getNbt();
		if(ticketTag == null || !ticketTag.contains("TicketID"))
			return null;
		return ticketTag.getUuid("TicketID");
	}
	
	public static ItemStack CreateMasterTicket(UUID ticketID)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET_MASTER);
		if(ticketID != null)
			ticket.getOrCreateNbt().putUuid("TicketID", ticketID);
		return ticket;
	}
	
	public static ItemStack CreateTicket(UUID ticketID)
	{
		return CreateTicket(ticketID, 1);
	}
	
	public static ItemStack CreateTicket(UUID ticketID, int count)
	{
		ItemStack ticket = new ItemStack(ModItems.TICKET, count);
		if(ticketID != null)
			ticket.getOrCreateNbt().putUuid("TicketID", ticketID);
		return ticket;
	}
	
	public static Text getTicketMaterialsList() {
		MutableText list = Text.empty();
		
		try {
			Registries.ITEM.getEntryList(TICKET_MATERIAL_KEY).ifPresent(itemList -> {
				for(RegistryEntry<Item> item : itemList)
					list.append(Text.literal("\n")).append(new ItemStack(item.value()).getName());
			});
		} catch(Throwable t) { t.printStackTrace(); }
		
		return list;
	}
	
}