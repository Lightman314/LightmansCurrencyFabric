package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.LCConfigCommon;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinData;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class CoinItem extends Item{

	public enum CoinItemTooltipType { DEFAULT, VALUE, NONE }
	
	public CoinItem(Settings properties)
	{
		super(properties);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltip, TooltipContext flagIn)
	{
		super.appendTooltip(stack,  level,  tooltip,  flagIn);
		addCoinTooltips(stack, tooltip);
	}
	
	public static void addCoinTooltips(ItemStack stack, List<Text> tooltip)
	{
		CoinData coinData = MoneyUtil.getData(stack.getItem());
		if(coinData != null)
		{
			switch (LCConfigCommon.INSTANCE.coinTooltipType.get()) {
				case DEFAULT -> {
					if (coinData.convertsDownwards()) {
						tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.down", coinData.getDownwardConversion().getSecond(), MoneyUtil.getPluralName(coinData.getDownwardConversion().getFirst()).getString()).formatted(Formatting.YELLOW));
					}
					Pair<Item, Integer> upwardConversion = MoneyUtil.getUpwardConversion(stack.getItem());
					if (upwardConversion != null) {
						tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.up", upwardConversion.getSecond(), upwardConversion.getFirst().getName(new ItemStack(upwardConversion.getFirst())).getString()).formatted(Formatting.YELLOW));
					}
				}
				case VALUE -> {
					double value = coinData.getDisplayValue();
					tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.value", LCConfigCommon.INSTANCE.formatValueDisplay(value)).formatted(Formatting.YELLOW));
					if (stack.getCount() > 1)
						tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.coinworth.value.stack", LCConfigCommon.INSTANCE.formatValueDisplay(value * stack.getCount())).formatted(Formatting.YELLOW));
				}
				default -> {
				} //Default is NONE
			}
		}
	}
	
}