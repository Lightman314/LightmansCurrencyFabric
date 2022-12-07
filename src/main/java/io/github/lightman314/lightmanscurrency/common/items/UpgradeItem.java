package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.IUpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.UpgradeData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public abstract class UpgradeItem extends Item implements IUpgradeItem{

	protected final UpgradeType upgradeType;
	private boolean addTooltips = true;
	Function<UpgradeData,List<Text>> customTooltips = null;
	
	public UpgradeItem(UpgradeType upgradeType, Settings properties)
	{
		super(properties);
		this.upgradeType = upgradeType;
	}
	
	public final boolean addsTooltips() { return this.addTooltips; }
	protected final void ignoreTooltips() { this.addTooltips = false; }
	protected final void setCustomTooltips(Function<UpgradeData,List<Text>> customTooltips) { this.customTooltips = customTooltips; }
	
	@Override
	public UpgradeType getUpgradeType() { return this.upgradeType; }
	
	@Override
	public UpgradeData getDefaultUpgradeData()
	{
		UpgradeData data = this.upgradeType.getDefaultData();
		this.fillUpgradeData(data);
		return data;
	}
	
	protected abstract void fillUpgradeData(UpgradeData data);
	
	public static UpgradeData getUpgradeData(ItemStack stack)
	{
		if(stack.getItem() instanceof UpgradeItem)
		{
			UpgradeData data = ((UpgradeItem)stack.getItem()).getDefaultUpgradeData();
			if(stack.hasNbt())
			{
				NbtCompound tag = stack.getNbt();
				if(tag.contains("UpgradeData", NbtElement.COMPOUND_TYPE))
					data.read(tag.getCompound("UpgradeData"));
			}
			return data;
		}
		return null;
	}
	
	public static void setUpgradeData(ItemStack stack, UpgradeData data)
	{
		if(stack.getItem() instanceof UpgradeItem)
		{
			UpgradeType source = ((UpgradeItem)stack.getItem()).upgradeType;
			NbtCompound tag = stack.getOrCreateNbt();
			tag.put("UpgradeData",  data.writeToNBT(source));
		}
		else
		{
			NbtCompound tag = stack.getOrCreateNbt();
			tag.put("UpgradeData", data.writeToNBT());
		}
	}
	
	public static List<Text> getUpgradeTooltip(ItemStack stack) { return getUpgradeTooltip(stack, false); }
	
	public static List<Text> getUpgradeTooltip(ItemStack stack, boolean forceCollection)
	{
		if(stack.getItem() instanceof UpgradeItem)
		{
			UpgradeItem item = (UpgradeItem)stack.getItem();
			if(!item.addTooltips && !forceCollection) //Block if tooltips have been blocked
				return Lists.newArrayList();
			UpgradeType type = item.getUpgradeType();
			UpgradeData data = getUpgradeData(stack);
			if(item.customTooltips != null)
				return item.customTooltips.apply(data);
			return type.getTooltip(data);
		}
		return Lists.newArrayList();
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltip, TooltipContext flagIn)
	{
		//Add upgrade tooltips
		List<Text> upgradeTooltips = getUpgradeTooltip(stack);
		if(upgradeTooltips != null)
			upgradeTooltips.forEach(upgradeTooltip -> tooltip.add(upgradeTooltip));
		
		super.appendTooltip(stack,  level,  tooltip,  flagIn);
		
	}
	
	public static class Simple extends UpgradeItem
	{
		public Simple(UpgradeType upgradeType, Settings properties) { super(upgradeType, properties); }
		@Override
		protected void fillUpgradeData(UpgradeData data) { }
	}
	
}