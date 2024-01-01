package io.github.lightman314.lightmanscurrency.common.items.tooltips;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import net.minecraft.text.Text;

public class LCTooltips {

	
	
	public static final Supplier<List<Text>> ATM = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.atm");
	public static final Supplier<List<Text>> TERMINAL = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.terminal");
	public static final Supplier<List<Text>> TICKET_MACHINE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.ticketmachine");
	public static final Supplier<List<Text>> CASH_REGISTER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.cashregister");
	
	public static final Supplier<List<Text>> COIN_MINT = () -> {
		List<Text> result = new ArrayList<>();
		//if(Config.SERVER.allowCoinMinting.get())
			result.add(Text.translatable("tooltip.lightmanscurrency.coinmint.mintable").fillStyle(TooltipItem.DEFAULT_STYLE));
		//if(Config.SERVER.allowCoinMelting.get())
			result.add(Text.translatable("tooltip.lightmanscurrency.coinmint.meltable").fillStyle(TooltipItem.DEFAULT_STYLE));
		return result;
	};
	
	public static final Supplier<List<Text>> ITEM_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item");
	public static final Supplier<List<Text>> SLOT_MACHINE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.slot_machine");
	public static final Supplier<List<Text>> ITEM_TRADER_ARMOR = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.armor");
	public static final Supplier<List<Text>> ITEM_TRADER_TICKET = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.ticket");
	public static final Supplier<List<Text>> ITEM_NETWORK_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.network.item");
	public static final Supplier<List<Text>> ITEM_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.interface.item");
	public static final Supplier<List<Text>> PAYGATE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.paygate");
	
	
}