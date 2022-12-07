package io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.text.Text;

public class ItemCapacityUpgrade extends CapacityUpgrade {
	
	@Override
	public List<Text> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(Text.translatable("tooltip.lightmanscurrency.upgrade.item_capacity", data.getIntValue(CapacityUpgrade.CAPACITY)));
	}

}